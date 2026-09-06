package com.kchat.data.local

import com.kchat.core.model.ChatMessage
import com.kchat.core.model.MessageStorageOptions
import com.kchat.core.model.RoomSummary
import com.kchat.data.local.EntityMappers.toEntity
import com.kchat.data.local.EntityMappers.toModel
import com.kchat.data.local.dao.MessageDao
import com.kchat.data.local.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatLocalDataSource @Inject constructor(
    private val roomDao: RoomDao,
    private val messageDao: MessageDao,
) {
    fun observeRooms(): Flow<List<RoomSummary>> =
        roomDao.observeRooms().map { list -> list.map { it.toModel() } }

    fun observeMessages(roomId: String): Flow<List<ChatMessage>> =
        messageDao.observeMessages(roomId).map { list -> list.map { it.toModel() } }

    suspend fun getMessages(roomId: String): List<ChatMessage> =
        messageDao.getByRoomId(roomId).map { it.toModel() }

    suspend fun getRoom(roomId: String): RoomSummary? =
        roomDao.getById(roomId)?.toModel()

    suspend fun cacheRooms(rooms: List<RoomSummary>) {
        // Preserve API order (newest activity first) for RoomDao ORDER BY updatedAt DESC
        val now = System.currentTimeMillis()
        roomDao.upsertAll(
            rooms.mapIndexed { index, room ->
                room.toEntity(updatedAt = now - index)
            },
        )
    }

    suspend fun upsertRoom(room: RoomSummary) {
        val existing = roomDao.getById(room.id)
        roomDao.upsertAll(
            listOf(
                room.toEntity(updatedAt = existing?.updatedAt ?: System.currentTimeMillis()),
            ),
        )
    }

    suspend fun ensureRoomStub(roomId: String, title: String) {
        if (roomDao.getById(roomId) != null) return
        roomDao.upsertAll(
            listOf(
                RoomSummary(
                    id = roomId,
                    title = title.ifBlank { "Chat" },
                    preview = "",
                    time = "",
                ).toEntity(),
            ),
        )
    }

    suspend fun cacheMessages(roomId: String, messages: List<ChatMessage>) {
        messageDao.clearRoom(roomId)
        if (messages.isNotEmpty()) {
            messageDao.upsertAll(messages.map { it.toEntity(roomId) })
        }
    }

    suspend fun appendMessage(
        roomId: String,
        message: ChatMessage,
        preserveReactedByMe: Boolean = false,
    ) {
        val existing = messageDao.getById(message.id)
        val createdAt = existing?.createdAt ?: message.createdAtMillis ?: System.currentTimeMillis()
        val reactions = if (preserveReactedByMe) {
            EntityMappers.mergeReactedByMe(
                message.reactions,
                existing?.reactionsJson?.let { EntityMappers.decodeReactions(it) },
            )
        } else {
            message.reactions
        }
        val entity = message.copy(reactions = reactions).toEntity(roomId, createdAt).copy(
            isRead = existing?.isRead == true || message.isRead,
            isEdited = message.isEdited || existing?.isEdited == true,
            replyAuthor = message.replyTo?.author ?: existing?.replyAuthor,
            replyText = message.replyTo?.text ?: existing?.replyText,
            replyMessageId = message.replyTo?.messageId ?: existing?.replyMessageId,
            replyMediaUrl = message.replyTo?.mediaUrl ?: existing?.replyMediaUrl,
            replyPreviewType = message.replyTo?.previewType?.name ?: existing?.replyPreviewType,
        )
        messageDao.upsertAll(listOf(entity))
    }

    /**
     * Persist inbound/outbound message and bump room list preview (no network).
     * Increments unread only for **new** messages from others when [incrementUnread] is true.
     * Skips room list bump when only reactions (or other non-preview fields) changed.
     */
    suspend fun appendMessageAndTouchRoom(
        roomId: String,
        message: ChatMessage,
        incrementUnread: Boolean = true,
        preserveReactedByMe: Boolean = false,
    ) {
        val existingMsg = messageDao.getById(message.id)
        val isNew = existingMsg == null
        val contentChanged = existingMsg == null ||
            existingMsg.text != message.text ||
            existingMsg.isEdited != message.isEdited ||
            existingMsg.type != message.type.name ||
            existingMsg.mediaUrl != message.mediaUrl
        appendMessage(roomId, message, preserveReactedByMe = preserveReactedByMe)
        if (!isNew && !contentChanged) return
        if (roomDao.getById(roomId) == null) {
            ensureRoomStub(roomId, "")
        }
        val room = roomDao.getById(roomId) ?: return
        val preview = when {
            message.type == com.kchat.core.model.MessageType.CallEvent ->
                message.text.ifBlank { "[Cuộc gọi]" }
            message.text.isNotBlank() -> message.text
            message.type == com.kchat.core.model.MessageType.Image -> "[Ảnh]"
            message.type == com.kchat.core.model.MessageType.File ->
                message.fileName?.takeIf { it.isNotBlank() }?.let { "[File] $it" } ?: "[File]"
            else -> message.fileName ?: message.imageLabel ?: room.preview
        }
        val nextUnread = when {
            isNew && !message.isMine && incrementUnread -> room.unreadCount + 1
            else -> room.unreadCount
        }
        roomDao.upsertAll(
            listOf(
                room.copy(
                    preview = preview,
                    time = message.time.ifBlank { room.time },
                    unreadCount = nextUnread,
                    updatedAt = System.currentTimeMillis(),
                ),
            ),
        )
    }

    suspend fun markRoomRead(roomId: String) {
        val room = roomDao.getById(roomId) ?: return
        if (room.unreadCount == 0) return
        roomDao.upsertAll(listOf(room.copy(unreadCount = 0)))
    }

    suspend fun markMineMessagesReadUpTo(roomId: String, upToCreatedAtMillis: Long) {
        messageDao.markMineReadUpTo(roomId, upToCreatedAtMillis)
    }

    suspend fun clearAll() {
        messageDao.clearAll()
        roomDao.clearAll()
    }

    /** Emergency wipe: delete cached messages; keep room list but clear previews. */
    suspend fun clearAllLocalMessages() {
        messageDao.clearAll()
        roomDao.resetPreviewsAfterMessageWipe()
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteById(messageId)
    }

    /**
     * Removes expired cached messages:
     * - rooms with disappearing TTL → purge by room setting
     * - other rooms → purge by global [retentionDays]
     */
    suspend fun purgeExpiredLocalMessages(
        retentionDays: Int,
        nowMillis: Long = System.currentTimeMillis(),
    ): Int {
        val days = MessageStorageOptions.resolveRetentionDays(retentionDays)
        val globalCutoff = nowMillis - days * MessageStorageOptions.DAY_MILLIS
        val deleted = messageDao.deleteExpiredLocalMessages(nowMillis, globalCutoff)
        if (deleted > 0) {
            roomDao.clearPreviewsForEmptyRooms()
        }
        return deleted
    }
}
