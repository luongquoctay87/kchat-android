package com.kchat.core.model

enum class MessageType {
    Text,
    Image,
    File,
    Bot,
    CallEvent,
}

data class ReactionCount(
    val emoji: String,
    val count: Int,
    val reactedByMe: Boolean = false,
)

data class ReplyQuote(
    val author: String,
    val text: String,
    val messageId: String? = null,
    val mediaUrl: String? = null,
    val previewType: MessageType? = null,
)

fun replyQuoteTextFor(message: ChatMessage): String = when (message.type) {
    MessageType.Image -> message.text.ifBlank { "Ảnh" }
    MessageType.File -> message.fileName ?: "Tệp tin"
    else -> message.text
}

fun ReplyQuote.mergeWith(fallback: ReplyQuote?): ReplyQuote {
    if (fallback == null) return this
    return copy(
        messageId = messageId ?: fallback.messageId,
        mediaUrl = mediaUrl ?: fallback.mediaUrl,
        previewType = previewType ?: fallback.previewType,
        text = text.ifBlank { fallback.text },
    )
}

fun ReplyQuote.enrichFromMessage(original: ChatMessage): ReplyQuote = when (original.type) {
    MessageType.Image -> copy(
        mediaUrl = mediaUrl ?: original.mediaUrl,
        previewType = previewType ?: MessageType.Image,
        text = text.ifBlank { original.imageLabel ?: "Ảnh" },
    )
    MessageType.File -> copy(
        previewType = previewType ?: MessageType.File,
        text = text.ifBlank { original.fileName ?: "Tệp tin" },
    )
    else -> this
}

fun ReplyQuote.enrichFromMessages(
    messages: List<ChatMessage>,
    beforeMillis: Long? = null,
): ReplyQuote {
    if (!mediaUrl.isNullOrBlank() && previewType == MessageType.Image) return this

    messageId?.let { id ->
        messages.find { it.id == id }?.let { return enrichFromMessage(it) }
    }

    val looksLikeImageReply = previewType == MessageType.Image ||
        text.isBlank() ||
        text == "Ảnh"

    if (looksLikeImageReply) {
        val image = messages
            .asSequence()
            .filter { msg ->
                val ts = msg.createdAtMillis
                beforeMillis == null || ts == null || ts <= beforeMillis
            }
            .filter { matchesReplyAuthor(it, author) }
            .filter { it.type == MessageType.Image && !it.mediaUrl.isNullOrBlank() }
            .lastOrNull()
        if (image != null) return enrichFromMessage(image)
    }

    return this
}

private fun matchesReplyAuthor(message: ChatMessage, replyAuthor: String): Boolean = when {
    replyAuthor == "Bạn" -> message.isMine
    message.isMine -> false
    else -> message.senderName == replyAuthor
}

fun ChatMessage.withEnrichedReply(messages: List<ChatMessage>): ChatMessage {
    val quote = replyTo ?: return this
    return copy(replyTo = quote.enrichFromMessages(messages, beforeMillis = createdAtMillis))
}

data class ReadReceipt(
    val name: String,
    val time: String,
)

data class MentionUser(
    val username: String,
    val displayName: String,
)

data class SearchResult(
    val id: String,
    val author: String,
    val time: String,
    val snippet: String,
    val createdAtMillis: Long? = null,
)

data class UserProfile(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val phone: String = "",
    val avatarUrl: String? = null,
)

data class UserSettings(
    val pushEnabled: Boolean = true,
    val theme: String = "system",
    val fontSize: String = "medium",
    val showOnline: Boolean = true,
    val enterToSend: Boolean = true,
    val privacyDm: String = "everyone",
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int? = null,
    val quietHoursEndHour: Int? = null,
    val localCacheRetentionDays: Int? = null,
    val defaultDisappearingSeconds: Int? = null,
)

data class DeviceSession(
    val id: String,
    val name: String,
    val subtitle: String,
    val isCurrent: Boolean = false,
)

data class ContactSummary(
    val id: String,
    val name: String,
    val subtitle: String,
    val isOnline: Boolean,
    val email: String = "",
    val avatarUrl: String? = null,
    val isContact: Boolean = false,
    val username: String = "",
    val phone: String = "",
)

data class ChatMessage(
    val id: String,
    val type: MessageType = MessageType.Text,
    val text: String = "",
    val fileName: String? = null,
    val fileSize: String? = null,
    val imageLabel: String? = null,
    val mediaUrl: String? = null,
    val senderName: String? = null,
    val isMine: Boolean,
    val time: String,
    val createdAtMillis: Long? = null,
    val replyTo: ReplyQuote? = null,
    val reactions: List<ReactionCount> = emptyList(),
    val isRead: Boolean = false,
    val readByCount: Int = 0,
    val isEdited: Boolean = false,
    val botTitle: String? = null,
    val botService: String? = null,
)

data class GroupMember(
    val id: String,
    val name: String,
    val username: String = "",
    val role: String? = null,
    val isOnline: Boolean = false,
    val isMe: Boolean = false,
)

data class RoomSummary(
    val id: String,
    val title: String,
    val preview: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isChannel: Boolean = false,
    val isGroup: Boolean = false,
    val memberCount: Int = 0,
    val disappearingAfterSeconds: Int? = null,
    val myRole: String? = null,
    val isMuted: Boolean = false,
    val mutedUntilEpochMs: Long? = null,
    val avatarUrl: String? = null,
)

/** Mute durations for [ChatRepository.muteRoom] — must match backend [RoomMuteOptions]. */
object RoomMuteDurations {
    const val UNMUTE = 0
    const val EIGHT_HOURS = 28_800
    const val ONE_WEEK = 604_800
    const val FOREVER = -1
}

data class RoomMeta(
    val isGroup: Boolean = false,
    val isChannel: Boolean = false,
    val memberCount: Int = 0,
    val onlineCount: Int = 0,
    val isOnline: Boolean = false,
    val showTyping: Boolean = false,
    val disappearingAfterSeconds: Int? = null,
    val myRole: String? = null,
    val isMuted: Boolean = false,
) {
    /** Direct: any member. Group: owner/admin. Channel: never. */
    val canChangeDisappearing: Boolean
        get() = when {
            isChannel -> false
            isGroup -> isManagerRole(myRole)
            else -> true
        }

    /** Channel: only owner/admin can post. Others: always. */
    val canPost: Boolean
        get() = !isChannel || isManagerRole(myRole)

    private companion object {
        fun isManagerRole(role: String?): Boolean =
            role.equals("owner", ignoreCase = true) || role.equals("admin", ignoreCase = true)
    }
}

data class PinnedMessage(
    val messageId: String,
    val text: String,
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

