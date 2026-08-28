package com.kchat.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kchat.BuildConfig
import com.kchat.data.repository.ActiveRoomTracker
import com.kchat.data.repository.AppForegroundTracker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class KChatFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var fcmTokenHandler: FcmTokenHandler
    @Inject lateinit var pushNotificationHelper: PushNotificationHelper
    @Inject lateinit var activeRoomTracker: ActiveRoomTracker
    @Inject lateinit var appForegroundTracker: AppForegroundTracker

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch {
            fcmTokenHandler.onNewToken(token)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (BuildConfig.USE_FAKE_DATA || !BuildConfig.FCM_ENABLED) return
        val data = message.data
        if (data["type"] != "message_new") return

        val roomId = data["room_id"] ?: return
        // Suppress only when user is viewing this room in foreground (ViewModel may survive on Home).
        if (appForegroundTracker.isForeground && activeRoomTracker.isActive(roomId)) return

        val roomTitle = data["room_title"].orEmpty()
        val senderName = data["sender_name"].orEmpty()
        val body = data["body"] ?: message.notification?.body ?: "Tin nhắn mới"

        pushNotificationHelper.showMessageNotification(
            roomId = roomId,
            roomTitle = roomTitle,
            senderName = senderName,
            body = body,
        )
    }
}
