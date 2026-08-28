package com.kchat.data.repository

import com.kchat.core.model.ChatMessage
import com.kchat.core.model.RoomSummary

/** Shows a local OS notification for an inbound WS message when appropriate. */
interface IncomingMessageNotifier {
    suspend fun onIncomingMessage(
        roomId: String,
        message: ChatMessage,
        room: RoomSummary?,
    )
}
