package com.kchat.push

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
        if (appForegroundTracker.isForeground) return

        val settings = runCatching { settingsRepository.settings.first() }.getOrNull()
        if (!PushNotificationPolicy.shouldNotify(settings, room)) return

        val title = room?.title?.takeIf { it.isNotBlank() } ?: "k-chat"
        val senderName = message.senderName.orEmpty()
        val body = formatBody(message)

        pushNotificationHelper.showMessageNotification(
            roomId = roomId,
            roomTitle = title,
            senderName = senderName,
            body = body,
        )
    }

    private fun formatBody(message: ChatMessage): String = when (message.type) {
        MessageType.Image -> "[Ảnh]"
        MessageType.File -> message.fileName?.takeIf { it.isNotBlank() }?.let { "[File] $it" } ?: "[File]"
        MessageType.CallEvent -> "[Cuộc gọi]"
        MessageType.Bot -> message.text.ifBlank { "[Bot]" }
        else -> message.text.takeIf { it.isNotBlank() } ?: "Tin nhắn mới"
    }
}
