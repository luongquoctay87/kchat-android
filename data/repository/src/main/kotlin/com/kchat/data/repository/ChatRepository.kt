package com.kchat.data.repository

import com.kchat.core.model.ChatMessage
import com.kchat.core.model.GroupMember
import com.kchat.core.model.MentionUser
import com.kchat.core.model.PinnedMessage
import com.kchat.core.model.ReadReceipt
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.RoomMeta
import com.kchat.core.model.RoomSummary
import com.kchat.core.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeRooms(): Flow<List<RoomSummary>>

    fun observeMessages(roomId: String): Flow<List<ChatMessage>>

    fun observeRoomMeta(roomId: String): Flow<RoomMeta>

    /** Snapshot for callers that do not need live updates (fake/demo). */
    fun roomMeta(roomId: String): RoomMeta

    suspend fun sendMessage(roomId: String, text: String, replyTo: ReplyQuote? = null): Result<ChatMessage>

    suspend fun editMessage(roomId: String, messageId: String, text: String): Result<ChatMessage>

    suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit>

    suspend fun toggleReaction(roomId: String, messageId: String, emoji: String): Result<ChatMessage>

    suspend fun sendMedia(roomId: String, uri: android.net.Uri, mimeType: String?, displayName: String?): Result<ChatMessage>

    suspend fun searchInRoom(roomId: String, query: String): List<SearchResult>

    fun pinnedMessage(roomId: String): String?

    suspend fun getPinnedMessage(roomId: String): Result<PinnedMessage?>

    suspend fun pinMessage(roomId: String, messageId: String): Result<PinnedMessage>

    suspend fun unpinMessage(roomId: String): Result<Unit>

    fun mentionUsers(query: String): List<MentionUser>

    suspend fun getReadReceipts(roomId: String, messageId: String): List<ReadReceipt>

    /** Emergency wipe: clear local chat cache, best-effort server wipe. */
    suspend fun emergencyWipeAllMessages(): Result<Unit>

    suspend fun refreshRooms()

    suspend fun refreshMessages(roomId: String, limit: Int = 50)

    /** Clear local unread and notify backend the room was viewed. */
    suspend fun markRoomRead(roomId: String)

    /** Download media to app cache (authenticated). */
    suspend fun downloadMedia(mediaUrl: String, fileName: String): Result<java.io.File>

    suspend fun createGroup(name: String, memberIds: List<String>): Result<RoomSummary>

    suspend fun listMembers(roomId: String): Result<List<GroupMember>>

    suspend fun addMembers(roomId: String, userIds: List<String>): Result<Unit>

    suspend fun removeMember(roomId: String, userId: String): Result<Unit>

    suspend fun leaveRoom(roomId: String): Result<Unit>

    /**
     * Per-room disappearing messages. Pass null or 0 to turn off.
     * Seconds must be one of: 86400, 604800, 2592000, 7776000.
     */
    suspend fun updateRoomDisappearing(roomId: String, disappearingAfterSeconds: Int?): Result<Unit>

    /** Mute room notifications. [com.kchat.core.model.RoomMuteDurations] for values. */
    suspend fun muteRoom(roomId: String, durationSeconds: Int): Result<Unit>
}
