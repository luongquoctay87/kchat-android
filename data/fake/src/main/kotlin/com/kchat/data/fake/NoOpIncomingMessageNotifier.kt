package com.kchat.data.fake

import com.kchat.core.model.ChatMessage
import com.kchat.core.model.RoomSummary
import com.kchat.data.repository.IncomingMessageNotifier
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpIncomingMessageNotifier @Inject constructor() : IncomingMessageNotifier {
    override suspend fun onIncomingMessage(
        roomId: String,
        message: ChatMessage,
        room: RoomSummary?,
    ) = Unit
}
