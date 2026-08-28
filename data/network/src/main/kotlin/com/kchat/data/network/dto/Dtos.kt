package com.kchat.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String,
)

@Serializable
data class EmailRequest(
    val email: String,
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class RegisterRequest(
    @SerialName("registration_token") val registrationToken: String,
    val email: String,
    val username: String,
    val password: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
data class VerifyRegistrationOtpResponse(
    @SerialName("registration_token") val registrationToken: String,
)

@Serializable
data class VerifyResetOtpResponse(
    @SerialName("reset_token") val resetToken: String,
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    @SerialName("new_password") val newPassword: String,
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class SendMessageRequest(
    val text: String,
    @SerialName("reply_to_id") val replyToId: String? = null,
)

@Serializable
data class TypingRequest(
    val typing: Boolean = true,
)

@Serializable
data class EditMessageRequest(
    val text: String,
)

@Serializable
data class RoomDto(
    val id: String,
    val title: String,
    val preview: String,
    val time: String,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("is_channel") val isChannel: Boolean = false,
    @SerialName("is_group") val isGroup: Boolean = false,
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("disappearing_after_seconds") val disappearingAfterSeconds: Int? = null,
    @SerialName("my_role") val myRole: String? = null,
    @SerialName("is_muted") val isMuted: Boolean = false,
    @SerialName("muted_until_epoch_millis") val mutedUntilEpochMillis: Long? = null,
)

@Serializable
data class ReactMessageRequest(
    val emoji: String,
)

@Serializable
data class ReactionDto(
    val emoji: String,
    val count: Int = 0,
    @SerialName("reacted_by_me") val reactedByMe: Boolean = false,
)

@Serializable
data class MessageSearchResultDto(
    val id: String,
    val author: String,
    val time: String,
    val snippet: String,
    @SerialName("created_at") val createdAt: Long? = null,
)

@Serializable
data class MessageDto(
    val id: String,
    val type: String = "text",
    val text: String = "",
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("file_size") val fileSize: String? = null,
    @SerialName("image_label") val imageLabel: String? = null,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("sender_name") val senderName: String? = null,
    @SerialName("is_mine") val isMine: Boolean = false,
    val time: String,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("reply_author") val replyAuthor: String? = null,
    @SerialName("reply_text") val replyText: String? = null,
    @SerialName("reply_to_id") val replyToId: String? = null,
    @SerialName("reply_media_url") val replyMediaUrl: String? = null,
    @SerialName("reply_type") val replyType: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    val edited: Boolean = false,
    @SerialName("bot_title") val botTitle: String? = null,
    @SerialName("bot_service") val botService: String? = null,
    val reactions: List<ReactionDto> = emptyList(),
)

@Serializable
data class RegisterDeviceRequest(
    @SerialName("fcm_token") val fcmToken: String,
    @SerialName("device_name") val deviceName: String,
    val platform: String = "android",
    @SerialName("utc_offset_minutes") val utcOffsetMinutes: Int? = null,
)

@Serializable
data class ContactDto(
    val id: String,
    val name: String,
    val subtitle: String = "",
    @SerialName("is_online") val isOnline: Boolean = false,
    val email: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class CreateDirectRoomRequest(
    @SerialName("user_id") val userId: String,
)

@Serializable
data class DirectRoomDto(
    val id: String,
)

@Serializable
data class PinMessageRequest(
    @SerialName("message_id") val messageId: String,
)

@Serializable
data class PinnedMessageDto(
    @SerialName("message_id") val messageId: String,
    val text: String,
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    @SerialName("member_ids") val memberIds: List<String>,
)

@Serializable
data class AddMembersRequest(
    @SerialName("user_ids") val userIds: List<String>,
)

@Serializable
data class RenameRoomRequest(
    val name: String,
)

@Serializable
data class MuteRoomRequest(
    @SerialName("duration_seconds") val durationSeconds: Int,
)

@Serializable
data class UpdateRoomRequest(
    val name: String? = null,
    @SerialName("disappearing_after_seconds") val disappearingAfterSeconds: Int? = null,
)

@Serializable
data class RoomMemberDto(
    val id: String,
    val username: String = "",
    val name: String,
    val role: String = "member",
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("is_me") val isMe: Boolean = false,
)

@Serializable
data class WsEnvelope(
    val type: String,
    val payload: kotlinx.serialization.json.JsonObject? = null,
)

@Serializable
data class MessageNewPayload(
    @SerialName("room_id") val roomId: String,
    val message: MessageDto,
)

@Serializable
data class MessageDeletedPayload(
    @SerialName("room_id") val roomId: String,
    @SerialName("message_id") val messageId: String,
)

@Serializable
data class PresencePayload(
    @SerialName("user_id") val userId: String,
    val online: Boolean = false,
)

@Serializable
data class TypingPayload(
    @SerialName("room_id") val roomId: String,
    @SerialName("user_id") val userId: String = "",
    val typing: Boolean = true,
)

@Serializable
data class DeviceSessionRevokedPayload(
    @SerialName("device_token") val deviceToken: String,
)

@Serializable
data class MessagesReadPayload(
    @SerialName("room_id") val roomId: String,
    @SerialName("reader_id") val readerId: String = "",
    @SerialName("up_to_message_id") val upToMessageId: String = "",
    @SerialName("up_to_created_at") val upToCreatedAt: Long = 0L,
)

@Serializable
data class ReadReceiptDto(
    val name: String,
    val time: String,
)

@Serializable
data class UserProfileDto(
    val id: String,
    val username: String,
    val email: String = "",
    @SerialName("display_name") val displayName: String,
    val phone: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("display_name") val displayName: String,
    val phone: String = "",
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String,
)

@Serializable
data class UserSettingsDto(
    @SerialName("push_enabled") val pushEnabled: Boolean = true,
    val theme: String = "system",
    @SerialName("font_size") val fontSize: String = "medium",
    @SerialName("show_online") val showOnline: Boolean = true,
    @SerialName("enter_to_send") val enterToSend: Boolean = true,
    @SerialName("privacy_dm") val privacyDm: String = "everyone",
    @SerialName("quiet_hours_enabled") val quietHoursEnabled: Boolean = false,
    @SerialName("quiet_hours_start_hour") val quietHoursStartHour: Int? = null,
    @SerialName("quiet_hours_end_hour") val quietHoursEndHour: Int? = null,
    @SerialName("local_cache_retention_days") val localCacheRetentionDays: Int? = null,
    @SerialName("default_disappearing_seconds") val defaultDisappearingSeconds: Int? = null,
)

@Serializable
data class UpdateUserSettingsRequest(
    @SerialName("push_enabled") val pushEnabled: Boolean? = null,
    val theme: String? = null,
    @SerialName("font_size") val fontSize: String? = null,
    @SerialName("show_online") val showOnline: Boolean? = null,
    @SerialName("enter_to_send") val enterToSend: Boolean? = null,
    @SerialName("privacy_dm") val privacyDm: String? = null,
    @SerialName("quiet_hours_enabled") val quietHoursEnabled: Boolean? = null,
    @SerialName("quiet_hours_start_hour") val quietHoursStartHour: Int? = null,
    @SerialName("quiet_hours_end_hour") val quietHoursEndHour: Int? = null,
    @SerialName("local_cache_retention_days") val localCacheRetentionDays: Int? = null,
    @SerialName("default_disappearing_seconds") val defaultDisappearingSeconds: Int? = null,
)

@Serializable
data class WipeMessagesDto(
    @SerialName("deleted_count") val deletedCount: Int = 0,
    @SerialName("rooms_left") val roomsLeft: Int = 0,
)

@Serializable
data class DeviceDto(
    val id: String,
    val name: String,
    val subtitle: String,
    @SerialName("is_current") val isCurrent: Boolean = false,
)

@Serializable
data class CallDto(
    val id: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("initiator_id") val initiatorId: String,
    @SerialName("initiator_name") val initiatorName: String = "",
    @SerialName("callee_id") val calleeId: String = "",
    @SerialName("callee_name") val calleeName: String = "",
    @SerialName("call_type") val callType: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null,
)

@Serializable
data class CreateCallRequest(
    @SerialName("call_type") val callType: String,
)

@Serializable
data class IceServerDto(
    val urls: List<String> = emptyList(),
    val username: String? = null,
    val credential: String? = null,
)

@Serializable
data class IceServersResponse(
    @SerialName("ice_servers") val iceServers: List<IceServerDto> = emptyList(),
    @SerialName("ttl_seconds") val ttlSeconds: Long? = null,
)

@Serializable
data class CallEventPayload(
    val call: CallDto,
)

@Serializable
data class IceSignalPayload(
    @SerialName("call_id") val callId: String,
    @SerialName("room_id") val roomId: String = "",
    @SerialName("from_user_id") val fromUserId: String = "",
    val sdp: String? = null,
    val candidate: String? = null,
    @SerialName("sdp_mid") val sdpMid: String? = null,
    @SerialName("sdp_m_line_index") val sdpMLineIndex: Int? = null,
)

@Serializable
data class IceOfferRequest(val sdp: String)

@Serializable
data class IceAnswerRequest(val sdp: String)

@Serializable
data class IceCandidateRequest(
    val candidate: String,
    @SerialName("sdp_mid") val sdpMid: String? = null,
    @SerialName("sdp_m_line_index") val sdpMLineIndex: Int? = null,
)
