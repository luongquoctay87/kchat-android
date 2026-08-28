package com.kchat.data.network.api

import com.kchat.data.network.dto.AuthResponse
import com.kchat.data.network.dto.AddMembersRequest
import com.kchat.data.network.dto.CallDto
import com.kchat.data.network.dto.ChangePasswordRequest
import com.kchat.data.network.dto.ContactDto
import com.kchat.data.network.dto.CreateCallRequest
import com.kchat.data.network.dto.CreateDirectRoomRequest
import com.kchat.data.network.dto.CreateGroupRequest
import com.kchat.data.network.dto.DirectRoomDto
import com.kchat.data.network.dto.EditMessageRequest
import com.kchat.data.network.dto.EmailRequest
import com.kchat.data.network.dto.IceAnswerRequest
import com.kchat.data.network.dto.IceCandidateRequest
import com.kchat.data.network.dto.IceOfferRequest
import com.kchat.data.network.dto.IceServersResponse
import com.kchat.data.network.dto.LoginRequest
import com.kchat.data.network.dto.LogoutRequest
import com.kchat.data.network.dto.MessageDto
import com.kchat.data.network.dto.MessageSearchResultDto
import com.kchat.data.network.dto.MuteRoomRequest
import com.kchat.data.network.dto.PinMessageRequest
import com.kchat.data.network.dto.PinnedMessageDto
import com.kchat.data.network.dto.ReactMessageRequest
import com.kchat.data.network.dto.ReadReceiptDto
import com.kchat.data.network.dto.RefreshRequest
import com.kchat.data.network.dto.RegisterDeviceRequest
import com.kchat.data.network.dto.RegisterRequest
import com.kchat.data.network.dto.ResetPasswordRequest
import com.kchat.data.network.dto.VerifyOtpRequest
import com.kchat.data.network.dto.VerifyRegistrationOtpResponse
import com.kchat.data.network.dto.VerifyResetOtpResponse
import com.kchat.data.network.dto.RoomDto
import com.kchat.data.network.dto.RoomMemberDto
import com.kchat.data.network.dto.SendMessageRequest
import com.kchat.data.network.dto.TypingRequest
import com.kchat.data.network.dto.UpdateProfileRequest
import com.kchat.data.network.dto.UpdateRoomRequest
import com.kchat.data.network.dto.UpdateUserSettingsRequest
import com.kchat.data.network.dto.UserProfileDto
import com.kchat.data.network.dto.UserSettingsDto
import com.kchat.data.network.dto.WipeMessagesDto
import com.kchat.data.network.dto.DeviceDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface KChatApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/send-registration-otp")
    suspend fun sendRegistrationOtp(@Body body: EmailRequest)

    @POST("auth/verify-registration-otp")
    suspend fun verifyRegistrationOtp(@Body body: VerifyOtpRequest): VerifyRegistrationOtpResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest)

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: EmailRequest)

    @POST("auth/verify-reset-otp")
    suspend fun verifyResetOtp(@Body body: VerifyOtpRequest): VerifyResetOtpResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest)

    @POST("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest)

    @GET("users/me")
    suspend fun getProfile(): UserProfileDto

    @PATCH("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserProfileDto

    @Multipart
    @POST("users/me/avatar")
    suspend fun updateAvatar(@Part file: MultipartBody.Part): UserProfileDto

    @GET("users/me/settings")
    suspend fun getSettings(): UserSettingsDto

    @PATCH("users/me/settings")
    suspend fun updateSettings(@Body body: UpdateUserSettingsRequest): UserSettingsDto

    @DELETE("users/me/messages")
    suspend fun wipeMyMessages(): WipeMessagesDto

    @GET("rooms")
    suspend fun getRooms(): List<RoomDto>

    @POST("rooms/direct")
    suspend fun createDirectRoom(@Body body: CreateDirectRoomRequest): DirectRoomDto

    @POST("rooms/group")
    suspend fun createGroup(@Body body: CreateGroupRequest): RoomDto

    @GET("rooms/{roomId}/members")
    suspend fun getRoomMembers(@Path("roomId") roomId: String): List<RoomMemberDto>

    @POST("rooms/{roomId}/members")
    suspend fun addRoomMembers(
        @Path("roomId") roomId: String,
        @Body body: AddMembersRequest,
    )

    @DELETE("rooms/{roomId}/members/{userId}")
    suspend fun removeRoomMember(
        @Path("roomId") roomId: String,
        @Path("userId") userId: String,
    )

    @POST("rooms/{roomId}/leave")
    suspend fun leaveRoom(@Path("roomId") roomId: String)

    @PATCH("rooms/{roomId}")
    suspend fun updateRoom(
        @Path("roomId") roomId: String,
        @Body body: UpdateRoomRequest,
    ): RoomDto

    @PATCH("rooms/{roomId}/members/me/mute")
    suspend fun muteRoom(
        @Path("roomId") roomId: String,
        @Body body: MuteRoomRequest,
    ): RoomDto

    @GET("rooms/{roomId}/messages")
    suspend fun getMessages(
        @Path("roomId") roomId: String,
        @Query("limit") limit: Int = 50,
    ): List<MessageDto>

    @GET("rooms/{roomId}/search")
    suspend fun searchMessages(
        @Path("roomId") roomId: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 50,
    ): List<MessageSearchResultDto>

    @POST("rooms/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: String,
        @Body body: SendMessageRequest,
    ): MessageDto

    @PATCH("rooms/{roomId}/messages/{messageId}")
    suspend fun editMessage(
        @Path("roomId") roomId: String,
        @Path("messageId") messageId: String,
        @Body body: EditMessageRequest,
    ): MessageDto

    @DELETE("rooms/{roomId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("roomId") roomId: String,
        @Path("messageId") messageId: String,
    )

    @PUT("rooms/{roomId}/messages/{messageId}/reactions")
    suspend fun toggleReaction(
        @Path("roomId") roomId: String,
        @Path("messageId") messageId: String,
        @Body body: ReactMessageRequest,
    ): MessageDto

    @Multipart
    @POST("rooms/{roomId}/media")
    suspend fun sendMedia(
        @Path("roomId") roomId: String,
        @Part file: MultipartBody.Part,
        @Part("caption") caption: RequestBody? = null,
    ): MessageDto

    @POST("rooms/{roomId}/read")
    suspend fun markRoomRead(@Path("roomId") roomId: String)

    @POST("rooms/{roomId}/typing")
    suspend fun sendTyping(
        @Path("roomId") roomId: String,
        @Body body: TypingRequest = TypingRequest(),
    )

    @GET("rooms/{roomId}/messages/{messageId}/receipts")
    suspend fun getMessageReceipts(
        @Path("roomId") roomId: String,
        @Path("messageId") messageId: String,
    ): List<ReadReceiptDto>

    @GET("rooms/{roomId}/pin")
    suspend fun getPinned(@Path("roomId") roomId: String): retrofit2.Response<PinnedMessageDto>

    @PUT("rooms/{roomId}/pin")
    suspend fun pinMessage(
        @Path("roomId") roomId: String,
        @Body body: PinMessageRequest,
    ): PinnedMessageDto

    @DELETE("rooms/{roomId}/pin")
    suspend fun unpinMessage(@Path("roomId") roomId: String)

    @GET("contacts")
    suspend fun getContacts(): List<ContactDto>

    @GET("devices")
    suspend fun getDevices(): List<DeviceDto>

    @DELETE("devices/{deviceId}")
    suspend fun revokeDevice(@Path("deviceId") deviceId: String)

    @POST("devices")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest)

    @POST("rooms/{roomId}/calls")
    suspend fun initiateCall(
        @Path("roomId") roomId: String,
        @Body body: CreateCallRequest,
    ): CallDto

    @GET("calls/ice-servers")
    suspend fun getIceServers(): IceServersResponse

    @GET("calls/incoming")
    suspend fun getIncomingCalls(): List<CallDto>

    @POST("calls/{callId}/accept")
    suspend fun acceptCall(@Path("callId") callId: String): CallDto

    @POST("calls/{callId}/decline")
    suspend fun declineCall(@Path("callId") callId: String): CallDto

    @POST("calls/{callId}/end")
    suspend fun endCall(@Path("callId") callId: String): CallDto

    @POST("calls/{callId}/ice/offer")
    suspend fun relayIceOffer(
        @Path("callId") callId: String,
        @Body body: IceOfferRequest,
    )

    @POST("calls/{callId}/ice/answer")
    suspend fun relayIceAnswer(
        @Path("callId") callId: String,
        @Body body: IceAnswerRequest,
    )

    @POST("calls/{callId}/ice/candidate")
    suspend fun relayIceCandidate(
        @Path("callId") callId: String,
        @Body body: IceCandidateRequest,
    )
}
