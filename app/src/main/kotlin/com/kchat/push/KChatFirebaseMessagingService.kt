package com.kchat.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kchat.BuildConfig
import com.kchat.data.repository.ActiveRoomTracker
import com.kchat.data.repository.AppForegroundTracker
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.TokenStore
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
    @Inject lateinit var chatRepository: ChatRepository
    @Inject lateinit var tokenStore: TokenStore
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
        if (!BuildConfig.FCM_ENABLED) return
        try {
            handleMessageReceived(message)
        } catch (ex: UninitializedPropertyAccessException) {
            android.util.Log.w("KChatPush", "FCM before Hilt inject", ex)
        }
    }

    private fun handleMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"]

        if (type == "call_incoming") {
            val callId = data["call_id"] ?: return
            val roomId = data["room_id"] ?: ""
            val callerName = data["caller_name"]?.takeIf { it.isNotBlank() } ?: "k-chat"
            val isVideo = "video".equals(data["call_type"], ignoreCase = true)
            pushNotificationHelper.showIncomingCallNotification(
                callId = callId,
                roomId = roomId,
                callerName = callerName,
                isVideo = isVideo,
            )
            return
        }

        if (type == "call_ended") {
            val callId = data["call_id"] ?: return
            pushNotificationHelper.cancelCallNotification(callId)
            return
        }

        if (!type.isNullOrBlank() && type != "message_new") return

        val roomId = data["room_id"] ?: return
        if (activeRoomTracker.shouldSuppressTray(roomId, appForegroundTracker.isForeground)) {
            pushNotificationHelper.playInAppAlert()
            return
        }

        val senderName = data["sender_name"].orEmpty()
        val roomTitle = data["room_title"].orEmpty().ifBlank {
            message.notification?.title.orEmpty().ifBlank {
                senderName.ifBlank { "k-chat" }
            }
        }
        val body = data["body"]
            ?: message.notification?.body
            ?: "Tin nhắn mới"

        pushNotificationHelper.showMessageNotification(
            roomId = roomId,
            roomTitle = roomTitle,
            senderName = senderName,
            body = body,
        )

        scope.launch {
            if (tokenStore.getAccessToken().isNullOrBlank()) return@launch
            runCatching {
                chatRepository.ingestPushMessage(
                    roomId = roomId,
                    roomTitle = roomTitle,
                    senderName = senderName,
                    body = body,
                    messageId = data["message_id"],
                    createdAtMillis = data["created_at"]?.toLongOrNull(),
                    messageType = data["msg_type"] ?: data["message_type"],
                )
            }
        }
    }
}
