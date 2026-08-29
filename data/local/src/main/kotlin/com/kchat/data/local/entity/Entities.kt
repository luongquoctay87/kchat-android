package com.kchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String,
    val title: String,
    val preview: String,
    val time: String,
    val unreadCount: Int,
    val isOnline: Boolean,
    val isChannel: Boolean,
    val isGroup: Boolean,
    val memberCount: Int,
    val disappearingAfterSeconds: Int? = null,
    val myRole: String? = null,
    val isMuted: Boolean = false,
    val mutedUntilEpochMs: Long? = null,
    val avatarUrl: String? = null,
    val updatedAt: Long,
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val type: String,
    val text: String,
    val fileName: String?,
    val fileSize: String?,
    val imageLabel: String?,
    val mediaUrl: String?,
    val senderName: String?,
    val isMine: Boolean,
    val time: String,
    val replyAuthor: String?,
    val replyText: String?,
    val replyMessageId: String?,
    val replyMediaUrl: String?,
    val replyPreviewType: String?,
    val reactionsJson: String?,
    val isRead: Boolean,
    val isEdited: Boolean = false,
    val botTitle: String?,
    val botService: String?,
    val createdAt: Long,
)
