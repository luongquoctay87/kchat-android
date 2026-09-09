package com.kchat.push

import android.util.Log
import com.kchat.core.model.ChatMessage
import com.kchat.core.model.MessageType
import com.kchat.core.model.RoomSummary
import com.kchat.data.repository.ActiveRoomTracker
import com.kchat.data.repository.AppForegroundTracker
import com.kchat.data.repository.IncomingMessageNotifier
import com.kchat.data.repository.PushNotificationPolicy
import com.kchat.data.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class IncomingMessageNotifierImpl @Inject constructor(
    private val pushNotificationHelper: PushNotificationHelper,
    private val appForegroundTracker: AppForegroundTracker,
    private val activeRoomTracker: ActiveRoomTracker,
    private val settingsRepository: SettingsRepository,
) : IncomingMessageNotifier {
    override suspend fun onIncomingMessage(
        roomId: String,
        message: ChatMessage,
        room: RoomSummary?,
    ) {
        if (message.isMine) return
        if (activeRoomTracker.shouldSuppressTray(roomId, appForegroundTracker.isForeground)) {
            Log.d(TAG, "Suppress notify: viewing active room $roomId — playing in-app alert")
            pushNotificationHelper.playInAppAlert()
            return
        }

        val settings = runCatching { settingsRepository.settings.first() }.getOrNull()
        if (!PushNotificationPolicy.shouldNotify(settings, room)) {
            Log.w(TAG, "Suppress notify: policy blocked (pushEnabled=${settings?.pushEnabled}, quietHours=${settings?.quietHoursEnabled}, muted=${room?.isMuted})")
            return
        }

        val senderName = message.senderName.orEmpty()
        val title = if (senderName.isNotBlank()) {
            if (room != null && room.isGroup && room.title.isNotBlank()) {
                room.title
            } else {
                senderName
            }
        } else {
            room?.title?.takeIf { it.isNotBlank() } ?: "k-chat"
        }
        val body = formatBody(message)

        Log.i(TAG, "Posting incoming message notification for room $roomId from $senderName (title=$title)")
        pushNotificationHelper.showMessageNotification(
            roomId = roomId,
            roomTitle = title,
            senderName = senderName,
            body = body,
        )
    }

    override fun dismissCallNotification(callId: String) {
        pushNotificationHelper.cancelCallNotification(callId)
    }

    private fun formatBody(message: ChatMessage): String = when (message.type) {
        MessageType.Image -> "[Ảnh]"
        MessageType.File -> message.fileName?.takeIf { it.isNotBlank() }?.let { "[File] $it" } ?: "[File]"
        MessageType.CallEvent -> message.text.ifBlank { "[Cuộc gọi]" }
        MessageType.Bot -> message.text.ifBlank { "[Bot]" }
        else -> message.text.takeIf { it.isNotBlank() } ?: "Tin nhắn mới"
    }

    companion object {
        private const val TAG = "KChatIncomingNotifier"
    }
}
