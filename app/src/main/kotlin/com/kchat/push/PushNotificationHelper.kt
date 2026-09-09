package com.kchat.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kchat.MainActivity
import com.kchat.R
import com.kchat.data.repository.PushNavigationStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pushNavigationStore: PushNavigationStore,
) {
    private val lastShownAtMs = java.util.concurrent.ConcurrentHashMap<String, Long>()
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null && existing.importance < NotificationManager.IMPORTANCE_HIGH) {
            manager.deleteNotificationChannel(CHANNEL_ID)
        }
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tin nhắn",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Thông báo tin nhắn mới"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 150, 250)
            setSound(soundUri, audioAttributes)
            enableLights(true)
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    fun playInAppAlert() {
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, soundUri)
            ringtone?.play()
        } catch (ex: Exception) {
            Log.d(TAG, "Failed to play in-app alert", ex)
        }
    }

    fun ensureCallChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        try {
            manager.deleteNotificationChannel("kchat_calls")
        } catch (_: Exception) {}
        val existing = manager.getNotificationChannel(CALL_CHANNEL_ID)
        if (existing == null) {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
            val channel = NotificationChannel(
                CALL_CHANNEL_ID,
                "Cuộc gọi đến",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Thông báo cuộc gọi thoại và video"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 1000, 1000)
                enableLights(true)
                setSound(ringtoneUri, audioAttributes)
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showIncomingCallNotification(
        callId: String,
        roomId: String,
        callerName: String,
        isVideo: Boolean,
    ) {
        if (!canPostNotifications()) {
            Log.w(TAG, "Skip call notify: POST_NOTIFICATIONS not granted")
            return
        }
        ensureCallChannel()
        val callNotificationId = (callId.hashCode() and 0x7FFFFFFF)
        val title = if (isVideo) "Cuộc gọi video đến" else "Cuộc gọi thoại đến"
        val line = "Cuộc gọi từ $callerName"
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_CALL_TYPE, if (isVideo) "IncomingVideo" else "IncomingVoice")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            callNotificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(line)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSound(ringtoneUri)
            .setVibrate(longArrayOf(0, 1000, 1000, 1000))
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(callNotificationId, notification)
            Log.i(TAG, "Showing incoming call notification for callId=$callId caller=$callerName")
        } catch (ex: SecurityException) {
            Log.w(TAG, "Call notification blocked (permission denied)", ex)
        }
    }

    fun cancelCallNotification(callId: String) {
        val callNotificationId = (callId.hashCode() and 0x7FFFFFFF)
        try {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.cancel(callNotificationId)
            Log.i(TAG, "Cancelled incoming call notification for callId=$callId")
        } catch (ex: Exception) {
            Log.w(TAG, "Failed to cancel call notification", ex)
        }
    }

    fun showMessageNotification(
        roomId: String,
        roomTitle: String,
        senderName: String,
        body: String,
    ) {
        if (!canPostNotifications()) {
            Log.w(TAG, "Skip notify: POST_NOTIFICATIONS not granted or notifications disabled")
            return
        }

        val dedupeKey = "$roomId|$body"
        val now = SystemClock.elapsedRealtime()
        val previous = lastShownAtMs[dedupeKey]
        if (previous != null && now - previous < DEDUPE_WINDOW_MS) return
        lastShownAtMs[dedupeKey] = now
        ensureChannel()
        val title = roomTitle.ifBlank { "k-chat" }
        val line = if (senderName.isBlank() || title == senderName) body else "$senderName: $body"
        val notificationId = (roomId.hashCode() and 0x7FFFFFFF)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_ROOM_TITLE, title)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(line)
            .setStyle(NotificationCompat.BigTextStyle().bigText(line))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (ex: SecurityException) {
            Log.w(TAG, "Notification blocked (permission denied)", ex)
        }
    }

    fun handleNotificationTap(roomId: String?, roomTitle: String?) {
        if (roomId.isNullOrBlank()) return
        pushNavigationStore.requestNavigation(roomId, roomTitle.orEmpty())
    }

    private fun canPostNotifications(): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    companion object {
        private const val TAG = "KChatPush"
        const val CHANNEL_ID = "kchat_messages"
        const val CALL_CHANNEL_ID = "kchat_calls_v2"
        const val EXTRA_ROOM_ID = "room_id"
        const val EXTRA_ROOM_TITLE = "room_title"
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_CALL_TYPE = "call_type"
        const val EXTRA_CALLER_NAME = "caller_name"
        private const val DEDUPE_WINDOW_MS = 1_500L
    }
}
