package com.kchat.data.local

import com.kchat.core.model.ChatMessage
import com.kchat.core.model.MessageType
import com.kchat.core.model.ReactionCount
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.RoomSummary
import com.kchat.data.local.entity.MessageEntity
import com.kchat.data.local.entity.RoomEntity

internal object EntityMappers {
    fun RoomSummary.toEntity(updatedAt: Long = System.currentTimeMillis()) = RoomEntity(
        id = id,
        title = title,
        preview = preview,
        time = time,
        unreadCount = unreadCount,
        isOnline = isOnline,
        isChannel = isChannel,
        isGroup = isGroup,
        memberCount = memberCount,
        disappearingAfterSeconds = disappearingAfterSeconds,
        myRole = myRole,
        isMuted = isMuted,
        mutedUntilEpochMs = mutedUntilEpochMs,
        updatedAt = updatedAt,
    )

    fun RoomEntity.toModel() = RoomSummary(
        id = id,
        title = title,
        preview = preview,
        time = time,
        unreadCount = unreadCount,
        isOnline = isOnline,
        isChannel = isChannel,
        isGroup = isGroup,
        memberCount = memberCount,
        disappearingAfterSeconds = disappearingAfterSeconds,
        myRole = myRole,
        isMuted = isMuted,
        mutedUntilEpochMs = mutedUntilEpochMs,
    )

    fun ChatMessage.toEntity(roomId: String, createdAt: Long = createdAtMillis ?: System.currentTimeMillis()) = MessageEntity(
        id = id,
        roomId = roomId,
        type = type.name,
        text = text,
        fileName = fileName,
        fileSize = fileSize,
        imageLabel = imageLabel,
        mediaUrl = mediaUrl,
        senderName = senderName,
        isMine = isMine,
        time = time,
        replyAuthor = replyTo?.author,
        replyText = replyTo?.text,
        replyMessageId = replyTo?.messageId,
        replyMediaUrl = replyTo?.mediaUrl,
        replyPreviewType = replyTo?.previewType?.name,
        reactionsJson = encodeReactions(reactions),
        isRead = isRead,
        isEdited = isEdited,
        botTitle = botTitle,
        botService = botService,
        createdAt = createdAt,
    )

    fun MessageEntity.toModel(): ChatMessage {
        val replyTo = replyAuthor?.let { author ->
            ReplyQuote(
                author = author,
                text = replyText.orEmpty(),
                messageId = replyMessageId,
                mediaUrl = replyMediaUrl,
                previewType = replyPreviewType?.let { type ->
                    when (type) {
                        "Image", "image" -> MessageType.Image
                        "File", "file" -> MessageType.File
                        else -> runCatching { MessageType.valueOf(type) }.getOrNull()
                    }
                },
            )
        }
        return ChatMessage(
            id = id,
            type = when (type) {
                "CallEvent", "call_event" -> MessageType.CallEvent
                "Bot", "bot", "system" -> MessageType.Bot
                "Image", "image" -> MessageType.Image
                "File", "file" -> MessageType.File
                else -> runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.Text)
            },
            text = text,
            fileName = fileName,
            fileSize = fileSize,
            imageLabel = imageLabel,
            mediaUrl = mediaUrl,
            senderName = senderName,
            isMine = isMine,
            time = time,
            createdAtMillis = createdAt,
            replyTo = replyTo,
            reactions = decodeReactions(reactionsJson),
            isRead = isRead,
            isEdited = isEdited,
            botTitle = botTitle,
            botService = botService,
        )
    }

    /** count\\temoji[\\tmine] per line — emoji may contain any unicode except newline/tab. */
    fun encodeReactions(reactions: List<ReactionCount>): String? {
        if (reactions.isEmpty()) return null
        return reactions.joinToString("\n") { r ->
            buildString {
                append(r.count)
                append('\t')
                append(r.emoji)
                if (r.reactedByMe) {
                    append('\t')
                    append('1')
                }
            }
        }
    }

    fun decodeReactions(raw: String?): List<ReactionCount> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.lineSequence().mapNotNull { line ->
            val parts = line.split('\t')
            if (parts.size < 2) return@mapNotNull null
            val count = parts[0].toIntOrNull() ?: return@mapNotNull null
            val emoji = parts[1]
            if (emoji.isBlank()) return@mapNotNull null
            val mine = parts.getOrNull(2) == "1"
            ReactionCount(emoji, count, mine)
        }.toList()
    }

    fun mergeReactedByMe(
        remote: List<ReactionCount>,
        local: List<ReactionCount>?,
    ): List<ReactionCount> {
        if (local.isNullOrEmpty()) return remote
        val mine = local.filter { it.reactedByMe }.map { it.emoji }.toSet()
        if (mine.isEmpty()) return remote
        return remote.map { it.copy(reactedByMe = it.emoji in mine) }
    }
}
