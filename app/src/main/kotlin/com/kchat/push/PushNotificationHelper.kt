package com.kchat.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null && existing.importance < NotificationManager.IMPORTANCE_HIGH) {
            manager.deleteNotificationChannel(CHANNEL_ID)
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tin nhắn",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Thông báo tin nhắn mới"
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showMessageNotification(
        roomId: String,
        roomTitle: String,
        senderName: String,
        body: String,
    ) {
        if (!canPostNotifications()) return

        ensureChannel()
        val title = roomTitle.ifBlank { "k-chat" }
        val line = if (senderName.isBlank()) body else "$senderName: $body"
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
        const val EXTRA_ROOM_ID = "room_id"
        const val EXTRA_ROOM_TITLE = "room_title"
    }
}
