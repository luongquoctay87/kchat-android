package com.kchat.data.network.mapper

import com.kchat.core.model.ChatMessage
import com.kchat.core.model.ContactSummary
import com.kchat.core.model.DeviceSession
import com.kchat.core.model.GroupMember
import com.kchat.core.model.MessageType
import com.kchat.core.model.ReactionCount
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.RoomSummary
import com.kchat.core.model.SearchResult
import com.kchat.core.model.UserProfile
import com.kchat.core.model.UserSettings
import com.kchat.core.model.CallInfo
import com.kchat.core.model.RtcIceServer
import com.kchat.data.network.dto.ContactDto
import com.kchat.data.network.dto.CallDto
import com.kchat.data.network.dto.DeviceDto
import com.kchat.data.network.dto.IceServerDto
import com.kchat.data.network.dto.MessageDto
import com.kchat.data.network.dto.MessageSearchResultDto
import com.kchat.data.network.dto.RoomDto
import com.kchat.data.network.dto.RoomMemberDto
import com.kchat.data.network.dto.UserProfileDto
import com.kchat.data.network.dto.UserSettingsDto
import com.kchat.data.network.dto.UpdateUserSettingsRequest
import com.kchat.data.repository.UserSettingsPatch

internal fun RoomDto.toModel() = RoomSummary(
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
    mutedUntilEpochMs = mutedUntilEpochMillis,
)

internal fun ContactDto.toModel() = ContactSummary(
    id = id,
    name = name,
    subtitle = subtitle,
    isOnline = isOnline,
    email = email,
    avatarUrl = avatarUrl,
)

internal fun RoomMemberDto.toModel() = GroupMember(
    id = id,
    name = name,
    username = username,
    role = role,
    isOnline = isOnline,
    isMe = isMe,
)

internal fun MessageSearchResultDto.toModel() = SearchResult(
    id = id,
    author = author,
    time = time,
    snippet = snippet,
    createdAtMillis = createdAt,
)

internal fun MessageDto.toModel() = ChatMessage(
    id = id,
    type = when (type.lowercase()) {
        "image" -> MessageType.Image
        "file" -> MessageType.File
        "call_event" -> MessageType.CallEvent
        "system", "bot" -> MessageType.Bot
        else -> MessageType.Text
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
    replyTo = replyAuthor?.let { author ->
        ReplyQuote(
            author = author,
            text = replyText.orEmpty(),
            messageId = replyToId,
            mediaUrl = replyMediaUrl,
            previewType = when (replyType?.lowercase()) {
                "image" -> MessageType.Image
                "file" -> MessageType.File
                else -> null
            },
        )
    },
    reactions = reactions.map { ReactionCount(it.emoji, it.count, it.reactedByMe) },
    isRead = isRead,
    isEdited = edited,
    botTitle = botTitle,
    botService = botService,
)

internal fun UserProfileDto.toModel() = UserProfile(
    id = id,
    username = username,
    email = email,
    displayName = displayName,
    phone = phone,
    avatarUrl = avatarUrl,
)

internal fun UserSettingsDto.toModel() = UserSettings(
    pushEnabled = pushEnabled,
    theme = theme,
    fontSize = fontSize,
    showOnline = showOnline,
    enterToSend = enterToSend,
    privacyDm = privacyDm,
    quietHoursEnabled = quietHoursEnabled,
    quietHoursStartHour = quietHoursStartHour,
    quietHoursEndHour = quietHoursEndHour,
    localCacheRetentionDays = localCacheRetentionDays,
    defaultDisappearingSeconds = defaultDisappearingSeconds,
)

internal fun DeviceDto.toModel() = DeviceSession(
    id = id,
    name = name,
    subtitle = subtitle,
    isCurrent = isCurrent,
)

internal fun CallDto.toModel() = CallInfo(
    id = id,
    roomId = roomId,
    initiatorId = initiatorId,
    initiatorName = initiatorName,
    calleeId = calleeId,
    calleeName = calleeName,
    callType = callType,
    status = status,
    createdAt = createdAt,
    startedAt = startedAt,
    endedAt = endedAt,
)

internal fun IceServerDto.toModel() = RtcIceServer(
    urls = urls,
    username = username,
    credential = credential,
)

internal fun UserSettingsPatch.toRequest() = UpdateUserSettingsRequest(
    pushEnabled = pushEnabled,
    theme = theme,
    fontSize = fontSize,
    showOnline = showOnline,
    enterToSend = enterToSend,
    privacyDm = privacyDm,
    quietHoursEnabled = quietHoursEnabled,
    quietHoursStartHour = quietHoursStartHour,
    quietHoursEndHour = quietHoursEndHour,
    localCacheRetentionDays = localCacheRetentionDays,
    defaultDisappearingSeconds = defaultDisappearingSeconds,
)
