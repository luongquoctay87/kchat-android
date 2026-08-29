package com.kchat.data.fake

import com.kchat.core.model.ChatMessage
import com.kchat.core.model.GroupMember
import com.kchat.core.model.MentionUser
import com.kchat.core.model.ReactionCount
import com.kchat.core.model.ReadReceipt
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.RoomMeta
import com.kchat.core.model.RoomSummary
import com.kchat.core.model.SearchResult
import com.kchat.core.model.PinnedMessage
import com.kchat.core.model.PinMessageRules
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.EmergencyWipeStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeChatRepository @Inject constructor(
    private val emergencyWipeStore: EmergencyWipeStore,
) : ChatRepository {
    private val rooms = MutableStateFlow(FakeSampleData.rooms)
    private val messagesByRoom = FakeSampleData.rooms.associate { room ->
        room.id to MutableStateFlow(FakeSampleData.messagesForRoom(room.id))
    }.toMutableMap()
    private val pinsByRoom = mutableMapOf<String, MutableList<PinnedMessage>>().apply {
        put(
            "room-2",
            mutableListOf(
                PinnedMessage(messageId = "pinned-room-2", text = FakeSampleData.pinnedMessage),
            ),
        )
    }

    override fun observeRooms(): Flow<List<RoomSummary>> = rooms

    override fun observeMessages(roomId: String): Flow<List<ChatMessage>> =
        messagesByRoom.getOrPut(roomId) {
            MutableStateFlow(FakeSampleData.messagesForRoom(roomId))
        }

    override fun observeRoomMeta(roomId: String): Flow<RoomMeta> =
        rooms.map { list ->
            val base = FakeSampleData.roomMeta(roomId)
            val room = list.find { it.id == roomId }
            base.copy(
                disappearingAfterSeconds = room?.disappearingAfterSeconds ?: base.disappearingAfterSeconds,
                myRole = room?.myRole ?: base.myRole,
                isGroup = room?.isGroup ?: base.isGroup,
                isChannel = room?.isChannel ?: base.isChannel,
                memberCount = room?.memberCount ?: base.memberCount,
                isOnline = room?.isOnline ?: base.isOnline,
                isMuted = room?.isMuted ?: base.isMuted,
            )
        }

    override fun roomMeta(roomId: String): RoomMeta = FakeSampleData.roomMeta(roomId)

    override suspend fun sendMessage(
        roomId: String,
        text: String,
        replyTo: ReplyQuote?,
    ): Result<ChatMessage> {
        val flow = messagesByRoom.getOrPut(roomId) {
            MutableStateFlow(FakeSampleData.messagesForRoom(roomId))
        }
        val message = ChatMessage(
            id = "local-${System.currentTimeMillis()}",
            text = text.trim(),
            isMine = true,
            time = "vừa xong",
            replyTo = replyTo,
            isRead = false,
        )
        flow.update { it + message }
        return Result.success(message)
    }

    override suspend fun editMessage(
        roomId: String,
        messageId: String,
        text: String,
    ): Result<ChatMessage> {
        val flow = messagesByRoom[roomId] ?: return Result.failure(IllegalStateException("No room"))
        var updated: ChatMessage? = null
        flow.update { list ->
            list.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(text = text.trim(), isEdited = true).also { updated = it }
                } else {
                    msg
                }
            }
        }
        return updated?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Message not found"))
    }

    override suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit> {
        messagesByRoom[roomId]?.update { list -> list.filterNot { it.id == messageId } }
        return Result.success(Unit)
    }

    override suspend fun toggleReaction(
        roomId: String,
        messageId: String,
        emoji: String,
    ): Result<ChatMessage> {
        val flow = messagesByRoom[roomId] ?: return Result.failure(IllegalStateException("No room"))
        var updated: ChatMessage? = null
        flow.update { list ->
            list.map { msg ->
                if (msg.id != messageId) {
                    msg
                } else {
                    applyFakeReactionToggle(msg, emoji).also { updated = it }
                }
            }
        }
        return updated?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Message not found"))
    }

    override suspend fun sendMedia(
        roomId: String,
        uri: Uri,
        mimeType: String?,
        displayName: String?,
    ): Result<ChatMessage> {
        val isImage = mimeType?.startsWith("image/") == true
        val message = ChatMessage(
            id = "local-media-${System.currentTimeMillis()}",
            type = if (isImage) com.kchat.core.model.MessageType.Image else com.kchat.core.model.MessageType.File,
            text = "",
            fileName = displayName ?: "file",
            fileSize = "—",
            imageLabel = displayName,
            mediaUrl = uri.toString(),
            isMine = true,
            time = "vừa xong",
        )
        messagesByRoom.getOrPut(roomId) {
            MutableStateFlow(FakeSampleData.messagesForRoom(roomId))
        }.update { it + message }
        return Result.success(message)
    }

    override suspend fun searchInRoom(roomId: String, query: String): List<SearchResult> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        val messages = messagesByRoom[roomId]?.value ?: FakeSampleData.messagesForRoom(roomId)
        return messages.filter { msg ->
            msg.text.contains(q, ignoreCase = true) ||
                msg.fileName?.contains(q, ignoreCase = true) == true ||
                msg.imageLabel?.contains(q, ignoreCase = true) == true
        }
            .map { msg ->
                val snippetSource = msg.text.ifBlank { msg.fileName ?: msg.imageLabel ?: "" }
                SearchResult(
                    id = msg.id,
                    author = if (msg.isMine) "Bạn" else (msg.senderName ?: "User"),
                    time = msg.time,
                    snippet = buildFakeSearchSnippet(snippetSource, q),
                    createdAtMillis = msg.createdAtMillis,
                )
            }
    }

    override fun pinnedMessage(roomId: String): String? =
        pinsByRoom[roomId]?.firstOrNull()?.text

    override suspend fun getPinnedMessages(roomId: String): Result<List<PinnedMessage>> =
        Result.success(pinsByRoom[roomId]?.toList().orEmpty())

    override suspend fun pinMessage(roomId: String, messageId: String): Result<PinnedMessage> {
        val pins = pinsByRoom.getOrPut(roomId) { mutableListOf() }
        pins.find { it.messageId == messageId }?.let { return Result.success(it) }
        if (pins.size >= PinMessageRules.MAX_PER_ROOM) {
            return Result.failure(IllegalStateException("Mỗi phòng chỉ ghim tối đa ${PinMessageRules.MAX_PER_ROOM} tin nhắn"))
        }
        val text = messagesByRoom[roomId]?.value?.find { it.id == messageId }?.let {
            it.text.ifBlank { it.fileName ?: "Tin nhắn" }
        } ?: "Tin nhắn"
        val pin = PinnedMessage(messageId, text)
        pins.add(0, pin)
        return Result.success(pin)
    }

    override suspend fun unpinMessage(roomId: String, messageId: String): Result<Unit> {
        pinsByRoom[roomId]?.removeAll { it.messageId == messageId }
        return Result.success(Unit)
    }

    override fun mentionUsers(query: String): List<MentionUser> =
        FakeSampleData.mentionUsers.filter {
            query.isBlank() || it.username.startsWith(query, ignoreCase = true)
        }

    override suspend fun getReadReceipts(roomId: String, messageId: String): List<ReadReceipt> =
        FakeSampleData.readReceipts

    override suspend fun emergencyWipeAllMessages(): Result<Unit> {
        messagesByRoom.clear()
        rooms.value = emptyList()
        return Result.success(Unit)
    }

    override suspend fun refreshRooms() {
        if (emergencyWipeStore.isActiveNow()) return
        rooms.value = FakeSampleData.rooms
    }

    override suspend fun refreshMessages(roomId: String, limit: Int) {
        if (emergencyWipeStore.isActiveNow()) return
        messagesByRoom.getOrPut(roomId) {
            MutableStateFlow(FakeSampleData.messagesForRoom(roomId))
        }.value = FakeSampleData.messagesForRoom(roomId)
    }

    override suspend fun markRoomRead(roomId: String) {
        rooms.update { list ->
            list.map { if (it.id == roomId) it.copy(unreadCount = 0) else it }
        }
    }

    override suspend fun downloadMedia(mediaUrl: String, fileName: String): Result<java.io.File> =
        Result.failure(UnsupportedOperationException("Fake: no download"))

    override suspend fun createGroup(name: String, memberIds: List<String>): Result<RoomSummary> {
        val room = RoomSummary(
            id = "group-${System.currentTimeMillis()}",
            title = name.trim(),
            preview = "Nhóm mới",
            time = "vừa xong",
            isGroup = true,
            memberCount = memberIds.size + 1,
        )
        rooms.update { listOf(room) + it }
        messagesByRoom[room.id] = MutableStateFlow(emptyList())
        return Result.success(room)
    }

    override suspend fun listMembers(roomId: String): Result<List<GroupMember>> {
        val room = rooms.value.find { it.id.equals(roomId, ignoreCase = true) }
        if (room != null && !room.isGroup && !room.isChannel) {
            val peerId = when {
                roomId.equals("room-1", ignoreCase = true) -> "u-1"
                roomId.startsWith("room-", ignoreCase = true) ->
                    roomId.removePrefix("room-").removePrefix("ROOM-")
                else -> "u-1"
            }
            val peer = FakeSampleData.contacts.find { it.id.equals(peerId, ignoreCase = true) }
            return Result.success(
                listOf(
                    GroupMember(id = "me", name = "Bạn", username = "me", isMe = true),
                    GroupMember(
                        id = peerId,
                        name = peer?.name ?: room.title,
                        username = peer?.username.orEmpty(),
                        isOnline = peer?.isOnline == true,
                    ),
                ),
            )
        }
        return Result.success(FakeSampleData.groupMembers)
    }

    override suspend fun addMembers(roomId: String, userIds: List<String>): Result<Unit> =
        Result.success(Unit)

    override suspend fun removeMember(roomId: String, userId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun leaveRoom(roomId: String): Result<Unit> {
        rooms.update { list -> list.filterNot { it.id == roomId } }
        return Result.success(Unit)
    }

    override suspend fun updateGroupAvatar(
        roomId: String,
        uri: android.net.Uri,
        mimeType: String?,
        displayName: String?,
    ): Result<RoomSummary> {
        var updated: RoomSummary? = null
        rooms.update { list ->
            list.map { room ->
                if (room.id != roomId) {
                    room
                } else {
                    room.copy(avatarUrl = uri.toString()).also { updated = it }
                }
            }
        }
        return Result.success(updated ?: error("Room not found"))
    }

    override suspend fun updateRoomDisappearing(
        roomId: String,
        disappearingAfterSeconds: Int?,
    ): Result<Unit> {
        rooms.update { list ->
            list.map { room ->
                if (room.id == roomId) room.copy(disappearingAfterSeconds = disappearingAfterSeconds) else room
            }
        }
        return Result.success(Unit)
    }

    override suspend fun muteRoom(roomId: String, durationSeconds: Int): Result<Unit> {
        val muted = durationSeconds != com.kchat.core.model.RoomMuteDurations.UNMUTE
        rooms.update { list ->
            list.map { room ->
                if (room.id == roomId) {
                    room.copy(
                        isMuted = muted,
                        mutedUntilEpochMs = if (muted) System.currentTimeMillis() + 3_600_000 else null,
                    )
                } else {
                    room
                }
            }
        }
        return Result.success(Unit)
    }
}

private fun applyFakeReactionToggle(message: ChatMessage, emoji: String): ChatMessage {
    val existing = message.reactions.toMutableList()
    val idx = existing.indexOfFirst { it.emoji == emoji }
    if (idx >= 0 && existing[idx].reactedByMe) {
        val next = existing[idx].count - 1
        if (next <= 0) existing.removeAt(idx)
        else existing[idx] = existing[idx].copy(count = next, reactedByMe = false)
    } else if (idx >= 0) {
        existing[idx] = existing[idx].copy(count = existing[idx].count + 1, reactedByMe = true)
    } else {
        existing.add(ReactionCount(emoji, 1, reactedByMe = true))
    }
    return message.copy(reactions = existing)
}

private fun buildFakeSearchSnippet(content: String, query: String): String {
    val idx = content.indexOf(query, ignoreCase = true)
    if (idx < 0) return content.take(80)
    val start = maxOf(0, idx - 30)
    val end = minOf(content.length, idx + query.length + 30)
    var excerpt = content.substring(start, end)
    if (start > 0) excerpt = "...$excerpt"
    if (end < content.length) excerpt = "$excerpt..."
    return excerpt
}
