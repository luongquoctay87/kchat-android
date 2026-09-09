package com.kchat.data.network.repository

import com.kchat.core.model.AuthTokens
import com.kchat.core.model.CallRealtimeEvent
import com.kchat.core.model.ChatMessage
import com.kchat.core.model.MessageType
import com.kchat.core.model.GroupMember
import com.kchat.core.model.MentionUser
import com.kchat.core.model.ReadReceipt
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.mergeWith
import com.kchat.core.model.withEnrichedReply
import com.kchat.core.model.RoomMeta
import com.kchat.core.model.RoomSummary
import com.kchat.core.model.RoomTitles
import com.kchat.core.model.SearchResult
import com.kchat.data.local.ChatLocalDataSource
import com.kchat.data.network.api.KChatApi
import com.kchat.data.network.apiMessage
import com.kchat.data.network.apiResult
import com.kchat.data.network.auth.JwtPayload
import com.kchat.data.network.dto.AddMembersRequest
import com.kchat.data.network.dto.AuthResponse
import com.kchat.data.network.dto.CallEventPayload
import com.kchat.data.network.dto.CreateGroupRequest
import com.kchat.data.network.dto.DeviceSessionRevokedPayload
import com.kchat.data.network.dto.EditMessageRequest
import com.kchat.data.network.dto.EmailRequest
import com.kchat.data.network.dto.IceAnswerRequest
import com.kchat.data.network.dto.IceCandidateRequest
import com.kchat.data.network.dto.IceOfferRequest
import com.kchat.data.network.dto.IceSignalPayload
import com.kchat.data.network.dto.LoginRequest
import com.kchat.data.network.dto.LogoutRequest
import com.kchat.data.network.dto.MessageDeletedPayload
import com.kchat.data.network.dto.MessageNewPayload
import com.kchat.data.network.dto.MessagesReadPayload
import com.kchat.data.network.dto.PinMessageRequest
import com.kchat.data.network.dto.ReactMessageRequest
import com.kchat.data.network.dto.RefreshRequest
import com.kchat.data.network.dto.RegisterDeviceRequest
import com.kchat.data.network.util.currentUtcOffsetMinutes
import com.kchat.data.network.dto.RegisterRequest
import com.kchat.data.network.dto.ResetPasswordRequest
import com.kchat.data.network.dto.VerifyOtpRequest
import com.kchat.data.network.dto.SendMessageRequest
import com.kchat.data.network.dto.TypingPayload
import com.kchat.data.network.dto.TypingRequest
import com.kchat.data.network.dto.MuteRoomRequest
import com.kchat.data.network.dto.UpdateRoomRequest
import com.kchat.data.network.dto.WsEnvelope
import com.kchat.data.network.mapper.toModel
import com.kchat.data.network.media.MediaUploadHelper
import com.kchat.data.network.ws.KChatWebSocketClient
import com.kchat.data.network.ws.WsEvent
import com.kchat.data.network.ws.WsEventType
import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.ActiveRoomTracker
import com.kchat.data.repository.IncomingMessageNotifier
import com.kchat.data.repository.AuthRepository
import com.kchat.data.repository.CallRepository
import com.kchat.data.repository.CallSignalBus
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.ContactsRepository
import com.kchat.data.repository.DeviceTokenStore
import com.kchat.data.repository.EmergencyWipeStore
import com.kchat.data.repository.PushTokenRegistrar
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.RealtimeCoordinator
import com.kchat.data.repository.SettingsRepository
import com.kchat.data.repository.TokenStore
import com.kchat.data.repository.TypingStateStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkAuthRepository @Inject constructor(
    private val api: KChatApi,
    private val tokenStore: TokenStore,
    private val localDataSource: ChatLocalDataSource,
    private val realtimeCoordinator: RealtimeCoordinator,
    private val settingsRepository: SettingsRepository,
    private val pinLockStore: PinLockStore,
    private val contactsRepository: ContactsRepository,
    private val emergencyWipeStore: EmergencyWipeStore,
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = tokenStore.tokens.map { it != null }

    override suspend fun login(identifier: String, password: String): Result<Unit> = apiResult(
        "Email/tên đăng nhập hoặc mật khẩu không đúng",
    ) {
        val response = api.login(LoginRequest(identifier, password))
        tokenStore.saveTokens(AuthTokens(response.accessToken, response.refreshToken))
    }

    override suspend fun sendRegistrationOtp(email: String): Result<Unit> = runCatching {
        api.sendRegistrationOtp(EmailRequest(email.trim()))
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(Exception(it.apiMessage("Không gửi được mã OTP"))) },
    )

    override suspend fun verifyRegistrationOtp(email: String, otp: String): Result<String> = runCatching {
        api.verifyRegistrationOtp(VerifyOtpRequest(email.trim(), otp.trim())).registrationToken
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception(it.apiMessage("Mã OTP không đúng hoặc đã hết hạn"))) },
    )

    override suspend fun register(
        registrationToken: String,
        displayName: String,
        email: String,
        username: String,
        password: String,
    ): Result<Unit> = apiResult("Đăng ký thất bại") {
        val response = api.register(
            RegisterRequest(
                registrationToken = registrationToken,
                email = email,
                username = username,
                password = password,
                displayName = displayName,
            ),
        )
        tokenStore.saveTokens(AuthTokens(response.accessToken, response.refreshToken))
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        api.forgotPassword(EmailRequest(email.trim()))
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(Exception(it.apiMessage("Không gửi được mã OTP"))) },
    )

    override suspend fun verifyResetOtp(email: String, otp: String): Result<String> = runCatching {
        api.verifyResetOtp(VerifyOtpRequest(email.trim(), otp.trim())).resetToken
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception(it.apiMessage("Mã OTP không đúng hoặc đã hết hạn"))) },
    )

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> = runCatching {
        api.resetPassword(ResetPasswordRequest(token.trim(), newPassword))
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(Exception(it.apiMessage("Đặt lại mật khẩu thất bại"))) },
    )

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        runCatching {
            api.changePassword(
                com.kchat.data.network.dto.ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                ),
            )
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(IllegalArgumentException(it.apiMessage("Đổi mật khẩu thất bại"))) },
        )

    override suspend fun logout() {
        realtimeCoordinator.disconnect()
        val refresh = tokenStore.currentTokens()?.refreshToken
        if (!refresh.isNullOrBlank()) {
            runCatching { api.logout(LogoutRequest(refresh)) }
        }
        tokenStore.clear()
        localDataSource.clearAll()
        runCatching { settingsRepository.clearLocal() }
        runCatching { pinLockStore.clear() }
        emergencyWipeStore.reset()
        contactsRepository.restoreAfterLogout()
    }
}

@Singleton
class NetworkChatRepository @Inject constructor(
    private val api: KChatApi,
    private val localDataSource: ChatLocalDataSource,
    private val emergencyWipeStore: EmergencyWipeStore,
    private val typingStateStore: TypingStateStore,
    private val okHttpClient: okhttp3.OkHttpClient,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    @javax.inject.Named("apiBaseUrl") private val apiBaseUrl: String,
) : ChatRepository {
    override fun observeRooms(): Flow<List<RoomSummary>> = localDataSource.observeRooms()

    override fun observeMessages(roomId: String): Flow<List<ChatMessage>> =
        localDataSource.observeMessages(roomId)

    override fun observeRoomMeta(roomId: String): Flow<RoomMeta> {
        val key = roomId.trim().lowercase()
        return combine(
            localDataSource.observeRooms().map { rooms ->
                rooms.find { it.id.trim().equals(key, ignoreCase = true) }
            },
            typingStateStore.observeTyping(key),
        ) { room, showTyping ->
            RoomMeta(
                isGroup = room?.isGroup == true,
                isChannel = room?.isChannel == true,
                memberCount = room?.memberCount ?: 0,
                onlineCount = 0,
                isOnline = room?.isOnline == true,
                showTyping = showTyping,
                disappearingAfterSeconds = room?.disappearingAfterSeconds,
                myRole = room?.myRole,
                isMuted = room?.isMuted == true,
            )
        }
    }

    override fun roomMeta(roomId: String): RoomMeta = RoomMeta()

    override suspend fun sendMessage(
        roomId: String,
        text: String,
        replyTo: ReplyQuote?,
    ): Result<ChatMessage> = apiResult("Không gửi được tin nhắn") {
        val dto = api.sendMessage(
            roomId,
            SendMessageRequest(
                text = text.trim(),
                replyToId = replyTo?.messageId,
            ),
        )
        val existing = localDataSource.getMessages(roomId)
        val base = dto.toModel().withAbsoluteMediaUrl(apiBaseUrl)
        val merged = base.copy(replyTo = base.replyTo?.mergeWith(replyTo) ?: replyTo)
        val message = merged.withEnrichedReply(existing)
        persistLocalMessage(roomId, message)
        message
    }

    override suspend fun editMessage(
        roomId: String,
        messageId: String,
        text: String,
    ): Result<ChatMessage> = apiResult("Không sửa được tin nhắn") {
        val dto = api.editMessage(roomId, messageId, EditMessageRequest(text.trim()))
        val message = dto.toModel().withAbsoluteMediaUrl(apiBaseUrl)
        persistLocalMessage(roomId, message, incrementUnread = false)
        message
    }

    override suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit> = apiResult("Không xóa được tin nhắn") {
        api.deleteMessage(roomId, messageId)
        localDataSource.deleteMessage(messageId)
    }

    override suspend fun toggleReaction(
        roomId: String,
        messageId: String,
        emoji: String,
    ): Result<ChatMessage> = apiResult("Không gửi được phản ứng") {
        val dto = api.toggleReaction(roomId, messageId, ReactMessageRequest(emoji))
        val message = dto.toModel().withAbsoluteMediaUrl(apiBaseUrl)
        localDataSource.appendMessage(roomId, message)
        message
    }

    override suspend fun sendMedia(
        roomId: String,
        uri: android.net.Uri,
        mimeType: String?,
        displayName: String?,
    ): Result<ChatMessage> = apiResult("Không gửi được file") {
        val resolver = context.contentResolver
        val type = MediaUploadHelper.resolveMime(resolver, uri, mimeType)
        val name = MediaUploadHelper.resolveDisplayName(resolver, uri, displayName)
        val length = MediaUploadHelper.contentLength(resolver, uri)
        if (length > MediaUploadHelper.MAX_BYTES) {
            error("File vượt quá 25MB")
        }
        val body = MediaUploadHelper.requestBody(context, uri, type, length.coerceAtLeast(-1L))
        val part = MultipartBody.Part.createFormData("file", name, body)
        val dto = api.sendMedia(roomId, part)
        val message = dto.toModel().withAbsoluteMediaUrl(apiBaseUrl)
        persistLocalMessage(roomId, message)
        message
    }

    override suspend fun searchInRoom(roomId: String, query: String): List<SearchResult> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        return api.searchMessages(roomId, q).map { it.toModel() }
    }

    override fun pinnedMessage(roomId: String): String? = null

    override suspend fun getPinnedMessages(roomId: String): Result<List<com.kchat.core.model.PinnedMessage>> =
        apiResult("Không tải được tin ghim") {
            api.getPinned(roomId).map { com.kchat.core.model.PinnedMessage(it.messageId, it.text) }
        }

    override suspend fun pinMessage(roomId: String, messageId: String): Result<com.kchat.core.model.PinnedMessage> =
        apiResult("Không ghim được tin nhắn") {
            val dto = api.pinMessage(roomId, PinMessageRequest(messageId))
            com.kchat.core.model.PinnedMessage(dto.messageId, dto.text)
        }

    override suspend fun unpinMessage(roomId: String, messageId: String): Result<Unit> =
        apiResult("Không bỏ ghim được") {
            api.unpinMessage(roomId, messageId)
        }

    override fun mentionUsers(query: String): List<MentionUser> = emptyList()

    override suspend fun getReadReceipts(roomId: String, messageId: String): List<ReadReceipt> =
        api.getMessageReceipts(roomId, messageId).map { ReadReceipt(it.name, it.time) }

    override suspend fun emergencyWipeAllMessages(): Result<Unit> {
        localDataSource.clearAll()
        runCatching { api.wipeMyMessages() }
        // Drop anything an in-flight GET rooms/messages wrote while the wipe ran.
        localDataSource.clearAll()
        return Result.success(Unit)
    }

    override suspend fun ensureLocalRoom(roomId: String, title: String) {
        localDataSource.ensureRoomStub(roomId.trim().lowercase(), title)
    }

    override suspend fun ingestPushMessage(
        roomId: String,
        roomTitle: String,
        senderName: String,
        body: String,
        messageId: String?,
        createdAtMillis: Long?,
        messageType: String?,
    ) {
        val id = roomId.trim().lowercase()
        if (id.isBlank()) return
        localDataSource.ensureRoomStub(id, RoomTitles.resolve(roomTitle, senderName))
        val remoteId = messageId?.trim().orEmpty()
        if (remoteId.isNotEmpty()) {
            val createdAt = createdAtMillis?.takeIf { it > 0L } ?: System.currentTimeMillis()
            persistLocalMessage(
                roomId = id,
                message = ChatMessage(
                    id = remoteId,
                    type = messageType.toPushMessageType(),
                    text = body,
                    senderName = senderName.takeIf { it.isNotBlank() },
                    isMine = false,
                    time = pushTimeLabel(createdAt),
                    createdAtMillis = createdAt,
                ),
            )
        }
        refreshMessages(id)
        refreshRooms()
    }

    override suspend fun refreshRooms() {
        try {
            val rooms = api.getRooms().map {
                it.toModel().withAbsoluteAvatarUrl(apiBaseUrl).let { room ->
                    room.copy(id = room.id.trim().lowercase())
                }
            }
            localDataSource.cacheRooms(rooms)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            // Keep local Room cache.
        }
    }

    override suspend fun refreshMessages(roomId: String, limit: Int) {
        val id = roomId.trim().lowercase()
        try {
            val mapped = api.getMessages(id, limit = limit)
                .map { it.toModel().withAbsoluteMediaUrl(apiBaseUrl) }
            val enriched = mapped.map { it.withEnrichedReply(mapped) }
            localDataSource.cacheMessages(id, enriched)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            // Keep local messages.
        }
    }

    override suspend fun markRoomRead(roomId: String) {
        localDataSource.markRoomRead(roomId)
        try {
            api.markRoomRead(roomId)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            // Local read state already updated.
        }
    }

    override suspend fun downloadMedia(mediaUrl: String, fileName: String): Result<java.io.File> = apiResult("Tải file thất bại") {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = if (mediaUrl.startsWith("http")) mediaUrl else apiBaseUrl.trimEnd('/') + mediaUrl
            val request = okhttp3.Request.Builder().url(url).get().build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Tải file thất bại")
                val body = response.body ?: error("Tải file thất bại")
                val safe = fileName.replace(Regex("[^a-zA-Z0-9._\\-]"), "_").ifBlank { "file" }
                val dir = java.io.File(context.cacheDir, "media").also { it.mkdirs() }
                val out = java.io.File(dir, "${System.currentTimeMillis()}_$safe")
                body.byteStream().use { input ->
                    out.outputStream().use { input.copyTo(it) }
                }
                out
            }
        }
    }

    override suspend fun createGroup(name: String, memberIds: List<String>): Result<RoomSummary> = apiResult("Tạo nhóm thất bại") {
        val dto = api.createGroup(CreateGroupRequest(name = name.trim(), memberIds = memberIds))
        val room = dto.toModel().withAbsoluteAvatarUrl(apiBaseUrl)
        refreshRooms()
        room
    }

    override suspend fun listMembers(roomId: String): Result<List<GroupMember>> = apiResult("Không tải được thành viên") {
        api.getRoomMembers(roomId).map { it.toModel() }
    }

    override suspend fun addMembers(roomId: String, userIds: List<String>): Result<Unit> = apiResult("Không thêm được thành viên") {
        api.addRoomMembers(roomId, AddMembersRequest(userIds = userIds))
        refreshRooms()
    }

    override suspend fun removeMember(roomId: String, userId: String): Result<Unit> = apiResult("Không xóa được thành viên") {
        api.removeRoomMember(roomId, userId)
        refreshRooms()
    }

    override suspend fun leaveRoom(roomId: String): Result<Unit> = apiResult("Rời nhóm thất bại") {
        api.leaveRoom(roomId)
        refreshRooms()
    }

    override suspend fun updateGroupAvatar(
        roomId: String,
        uri: android.net.Uri,
        mimeType: String?,
        displayName: String?,
    ): Result<RoomSummary> = apiResult("Cập nhật ảnh nhóm thất bại") {
        val resolver = context.contentResolver
        val type = MediaUploadHelper.resolveMime(resolver, uri, mimeType)
        if (!MediaUploadHelper.isAllowedAvatarMime(type)) {
            error("Chỉ hỗ trợ ảnh JPEG, PNG, WebP hoặc GIF")
        }
        val name = MediaUploadHelper.resolveDisplayName(resolver, uri, displayName)
        val length = MediaUploadHelper.contentLength(resolver, uri)
        if (length > MediaUploadHelper.AVATAR_MAX_BYTES) {
            error("Ảnh đại diện tối đa 5MB")
        }
        val body = MediaUploadHelper.requestBody(context, uri, type, length.coerceAtLeast(-1L))
        val part = okhttp3.MultipartBody.Part.createFormData("file", name, body)
        val room = api.updateGroupAvatar(roomId, part).toModel().withAbsoluteAvatarUrl(apiBaseUrl)
        localDataSource.upsertRoom(room)
        room
    }

    override suspend fun updateRoomDisappearing(
        roomId: String,
        disappearingAfterSeconds: Int?,
    ): Result<Unit> = apiResult("Không lưu được tin biến mất") {
        val seconds = disappearingAfterSeconds ?: 0
        val updated = api.updateRoom(
            roomId,
            UpdateRoomRequest(disappearingAfterSeconds = seconds),
        ).toModel().withAbsoluteAvatarUrl(apiBaseUrl)
        localDataSource.upsertRoom(updated)
    }

    override suspend fun muteRoom(roomId: String, durationSeconds: Int): Result<Unit> =
        apiResult("Không lưu được tắt thông báo") {
            val updated = api.muteRoom(roomId, MuteRoomRequest(durationSeconds = durationSeconds))
                .toModel()
                .withAbsoluteAvatarUrl(apiBaseUrl)
            localDataSource.upsertRoom(updated)
        }

    /** Persist inbound/outbound locally. After wipe this recreates only this room, not server history. */
    private suspend fun persistLocalMessage(
        roomId: String,
        message: ChatMessage,
        incrementUnread: Boolean = true,
        preserveReactedByMe: Boolean = false,
    ) {
        localDataSource.appendMessageAndTouchRoom(
            roomId = roomId,
            message = message,
            incrementUnread = incrementUnread,
            preserveReactedByMe = preserveReactedByMe,
        )
    }
}

private fun RoomSummary.withAbsoluteAvatarUrl(baseUrl: String): RoomSummary {
    val relative = avatarUrl ?: return this
    if (relative.startsWith("http://") || relative.startsWith("https://")) return this
    return copy(avatarUrl = baseUrl.trimEnd('/') + relative)
}

private fun ChatMessage.withAbsoluteMediaUrl(baseUrl: String): ChatMessage {
    val base = baseUrl.trimEnd('/')
    val mainUrl = mediaUrl?.let { url ->
        if (url.startsWith("http://") || url.startsWith("https://")) url else base + url
    } ?: mediaUrl
    val reply = replyTo?.let { quote ->
        val replyUrl = quote.mediaUrl?.let { url ->
            if (url.startsWith("http://") || url.startsWith("https://")) url else base + url
        }
        if (replyUrl == quote.mediaUrl) quote else quote.copy(mediaUrl = replyUrl)
    }
    return when {
        mainUrl == mediaUrl && reply == replyTo -> this
        else -> copy(mediaUrl = mainUrl, replyTo = reply)
    }
}

private fun String?.toPushMessageType(): MessageType = when (this?.trim()?.lowercase()) {
    "image" -> MessageType.Image
    "file" -> MessageType.File
    "call_event" -> MessageType.CallEvent
    "system", "bot" -> MessageType.Bot
    else -> MessageType.Text
}

private fun pushTimeLabel(createdAtMillis: Long): String =
    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(createdAtMillis))

@Singleton
class NetworkPushTokenRegistrar @Inject constructor(
    private val api: KChatApi,
    private val tokenStore: TokenStore,
) : PushTokenRegistrar {
    override suspend fun registerFcmToken(token: String): Result<Unit> = runCatching {
        tokenStore.getAccessToken()
        api.registerDevice(
            RegisterDeviceRequest(
                fcmToken = token,
                deviceName = android.os.Build.MODEL.ifBlank { "Android" },
                platform = "android",
                utcOffsetMinutes = currentUtcOffsetMinutes(),
            ),
        )
    }
}

@Singleton
class NetworkRealtimeCoordinator @Inject constructor(
    private val webSocketClient: KChatWebSocketClient,
    private val api: KChatApi,
    private val accessTokenHolder: AccessTokenHolder,
    private val tokenStore: TokenStore,
    private val deviceTokenStore: DeviceTokenStore,
    private val localDataSource: ChatLocalDataSource,
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
    private val activeRoomTracker: ActiveRoomTracker,
    private val typingStateStore: TypingStateStore,
    private val callSignalBus: CallSignalBus,
    private val callRepository: CallRepository,
    private val incomingMessageNotifier: IncomingMessageNotifier,
    @javax.inject.Named("apiBaseUrl") private val apiBaseUrl: String,
) : RealtimeCoordinator {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO,
    )
    private var connectionJob: Job? = null
    private var presenceRefreshJob: Job? = null
    private var markReadJob: Job? = null
    private var pendingMarkReadRoomId: String? = null
    /** roomId → last sent (typing, epochMs) for per-room debounce / de-dupe. */
    private val lastTypingSent = java.util.concurrent.ConcurrentHashMap<String, Pair<Boolean, Long>>()
    /** Typing frames that failed while the socket was down; flushed on Connected. */
    private val pendingTyping = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
    /** ICE frames that failed while the socket was down; flushed on Connected. */
    private val pendingIce = java.util.concurrent.ConcurrentLinkedQueue<PendingIceFrame>()

    private sealed class PendingIceFrame {
        data class Offer(val callId: String, val sdp: String) : PendingIceFrame()
        data class Answer(val callId: String, val sdp: String) : PendingIceFrame()
        data class Candidate(
            val callId: String,
            val candidate: String,
            val sdpMid: String?,
            val sdpMLineIndex: Int?,
        ) : PendingIceFrame()
    }

    override fun connect() {
        if (webSocketClient.isConnected && connectionJob?.isActive == true) {
            scope.launch {
                runCatching { chatRepository.refreshRooms() }
                runCatching { contactsRepository.refreshContacts() }
                runCatching { refreshIncomingCalls() }
            }
            return
        }
        connectionJob?.cancel()
        connectionJob = scope.launch {
            var backoffMs = WS_RECONNECT_MIN_MS
            while (isActive) {
                runCatching {
                    ensureValidAccessToken()
                    webSocketClient.connect().collect { event ->
                        when (event) {
                            is WsEvent.Message -> handleMessage(event.raw)
                            is WsEvent.Connected -> {
                                backoffMs = WS_RECONNECT_MIN_MS
                                onSocketConnected()
                            }
                            is WsEvent.Error -> {
                                if (event.message.contains("401") ||
                                    event.message.contains("Unauthorized", ignoreCase = true)
                                ) {
                                    refreshTokenInternal()
                                }
                            }
                            else -> Unit
                        }
                    }
                }
                if (!isActive) break
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(WS_RECONNECT_MAX_MS)
            }
        }
    }

    private suspend fun ensureValidAccessToken(): Boolean {
        var current = accessTokenHolder.accessToken
        if (current.isNullOrBlank()) {
            current = tokenStore.getAccessToken()
        }
        if (!current.isNullOrBlank() && !JwtPayload.isExpiredOrExpiring(current)) {
            return true
        }
        return refreshTokenInternal()
    }

    private suspend fun refreshTokenInternal(): Boolean {
        val tokens = tokenStore.currentTokens() ?: return false
        val refreshUrl = apiBaseUrl.trimEnd('/') + "/auth/refresh"
        val bodyJson = json.encodeToString(RefreshRequest(tokens.refreshToken))
        val refreshRequestBuilder = Request.Builder()
            .url(refreshUrl)
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
        val deviceToken = runCatching { deviceTokenStore.getOrCreate() }.getOrNull()
        if (!deviceToken.isNullOrBlank()) {
            refreshRequestBuilder.header("X-Device-Token", deviceToken)
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return runCatching {
            client.newCall(refreshRequestBuilder.build()).execute().use { res ->
                if (!res.isSuccessful) {
                    if (res.code == 401 || res.code == 403) {
                        tokenStore.clear()
                    }
                    false
                } else {
                    val body = res.body?.string().orEmpty()
                    val auth = json.decodeFromString<AuthResponse>(body)
                    tokenStore.saveTokens(AuthTokens(auth.accessToken, auth.refreshToken))
                    true
                }
            }
        }.getOrDefault(false)
    }

    private suspend fun onSocketConnected() {
        flushPendingTyping()
        flushPendingIce()
        refreshIncomingCalls()
        // Re-fetch online flags after WE are present on the WS (and peers may already be online).
        runCatching { chatRepository.refreshRooms() }
        runCatching { contactsRepository.refreshContacts() }
    }

    private fun flushPendingTyping() {
        if (pendingTyping.isEmpty()) return
        val snapshot = pendingTyping.toMap()
        pendingTyping.clear()
        snapshot.forEach { (roomId, typing) ->
            lastTypingSent.remove(roomId)
            sendTyping(roomId, typing)
        }
    }

    private suspend fun refreshIncomingCalls() {
        callRepository.listIncoming().getOrNull().orEmpty().forEach { call ->
            if (call.status.equals("ringing", ignoreCase = true)) {
                callSignalBus.publish(CallRealtimeEvent.Incoming(call))
            }
        }
    }

    override fun sendTyping(roomId: String, typing: Boolean) {
        val normalizedRoomId = roomId.trim().lowercase()
        if (normalizedRoomId.isBlank()) return
        val now = System.currentTimeMillis()
        val prev = lastTypingSent[normalizedRoomId]
        if (typing) {
            if (prev != null && prev.first && now - prev.second < TYPING_HEARTBEAT_MS) return
        } else {
            if (prev?.first != true && !pendingTyping.containsKey(normalizedRoomId)) return
        }
        val body = buildJsonObject {
            put("type", WsEventType.TYPING)
            put(
                "payload",
                buildJsonObject {
                    put("room_id", normalizedRoomId)
                    put("typing", typing)
                },
            )
        }
        val wsSent = webSocketClient.send(body.toString())
        if (!wsSent) {
            relayTypingViaRest(normalizedRoomId, typing)
            if (typing) {
                pendingTyping[normalizedRoomId] = true
            } else {
                pendingTyping.remove(normalizedRoomId)
            }
        } else {
            pendingTyping.remove(normalizedRoomId)
        }
        if (typing) {
            lastTypingSent[normalizedRoomId] = true to now
        } else {
            lastTypingSent.remove(normalizedRoomId)
        }
    }

    private fun relayTypingViaRest(roomId: String, typing: Boolean) {
        scope.launch {
            val delivered = runCatching { api.sendTyping(roomId, TypingRequest(typing = typing)) }.isSuccess
            if (!delivered) {
                if (typing) {
                    pendingTyping[roomId] = true
                }
                android.util.Log.w("KChatTyping", "REST typing not delivered room=$roomId typing=$typing")
            }
        }
    }

    override fun sendIceOffer(callId: String, sdp: String) {
        relayIceOffer(callId, sdp)
    }

    override fun sendIceAnswer(callId: String, sdp: String) {
        relayIceAnswer(callId, sdp)
    }

    override fun sendIceCandidate(
        callId: String,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int?,
    ) {
        relayIceCandidate(callId, candidate, sdpMid, sdpMLineIndex)
    }

    private fun relayIceOffer(callId: String, sdp: String) {
        if (sendIceWsFrame(WsEventType.ICE_OFFER) {
            put("call_id", callId)
            put("sdp", sdp)
        }) return
        scope.launch {
            val delivered = runCatching { api.relayIceOffer(callId, IceOfferRequest(sdp)) }.isSuccess
            if (!delivered) {
                pendingIce.add(PendingIceFrame.Offer(callId, sdp))
                android.util.Log.w("KChatCall", "ICE offer not delivered (WS down, REST failed)")
            }
        }
    }

    private fun relayIceAnswer(callId: String, sdp: String) {
        if (sendIceWsFrame(WsEventType.ICE_ANSWER) {
            put("call_id", callId)
            put("sdp", sdp)
        }) return
        scope.launch {
            val delivered = runCatching { api.relayIceAnswer(callId, IceAnswerRequest(sdp)) }.isSuccess
            if (!delivered) {
                pendingIce.add(PendingIceFrame.Answer(callId, sdp))
                android.util.Log.w("KChatCall", "ICE answer not delivered (WS down, REST failed)")
            }
        }
    }

    private fun relayIceCandidate(
        callId: String,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int?,
    ) {
        if (sendIceWsFrame(WsEventType.ICE_CANDIDATE) {
            put("call_id", callId)
            put("candidate", candidate)
            if (sdpMid != null) put("sdp_mid", sdpMid)
            if (sdpMLineIndex != null) put("sdp_m_line_index", sdpMLineIndex)
        }) return
        scope.launch {
            val delivered = runCatching {
                api.relayIceCandidate(
                    callId,
                    IceCandidateRequest(
                        candidate = candidate,
                        sdpMid = sdpMid,
                        sdpMLineIndex = sdpMLineIndex,
                    ),
                )
            }.isSuccess
            if (!delivered) {
                pendingIce.add(PendingIceFrame.Candidate(callId, candidate, sdpMid, sdpMLineIndex))
                android.util.Log.w("KChatCall", "ICE candidate not delivered (WS down, REST failed)")
            }
        }
    }

    private fun flushPendingIce() {
        val snapshot = pendingIce.toList()
        pendingIce.clear()
        snapshot.forEach { frame ->
            when (frame) {
                is PendingIceFrame.Offer -> relayIceOffer(frame.callId, frame.sdp)
                is PendingIceFrame.Answer -> relayIceAnswer(frame.callId, frame.sdp)
                is PendingIceFrame.Candidate -> relayIceCandidate(
                    frame.callId,
                    frame.candidate,
                    frame.sdpMid,
                    frame.sdpMLineIndex,
                )
            }
        }
    }

    private fun sendIceWsFrame(
        type: String,
        payloadBuilder: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit,
    ): Boolean {
        val body = buildJsonObject {
            put("type", type)
            put("payload", buildJsonObject(payloadBuilder))
        }
        return webSocketClient.send(body.toString())
    }

    private suspend fun handleMessage(raw: String) {
        val envelope = runCatching { json.decodeFromString<WsEnvelope>(raw) }.getOrNull() ?: return
        when (envelope.type) {
            WsEventType.MESSAGE_NEW -> {
                val payload = envelope.payload?.let {
                    runCatching { json.decodeFromJsonElement<MessageNewPayload>(it) }.getOrNull()
                } ?: return
                val roomId = payload.roomId.trim().lowercase()
                val existing = localDataSource.getMessages(roomId)
                val message = payload.message.toModel()
                    .withAbsoluteMediaUrl(apiBaseUrl)
                    .withEnrichedReply(existing)
                val viewing = activeRoomTracker.isActive(roomId)
                localDataSource.appendMessageAndTouchRoom(
                    roomId = roomId,
                    message = message,
                    incrementUnread = !viewing && !message.isMine,
                )
                typingStateStore.clear(roomId)
                if (viewing && !message.isMine) {
                    scheduleMarkRead(roomId)
                }
                if (!message.isMine) {
                    val room = localDataSource.getRoom(roomId)
                    incomingMessageNotifier.onIncomingMessage(roomId, message, room)
                }
            }
            WsEventType.MESSAGE_UPDATED -> {
                val payload = envelope.payload?.let {
                    runCatching { json.decodeFromJsonElement<MessageNewPayload>(it) }.getOrNull()
                } ?: return
                val roomId = payload.roomId.trim().lowercase()
                val existing = localDataSource.getMessages(roomId)
                val message = payload.message.toModel()
                    .withAbsoluteMediaUrl(apiBaseUrl)
                    .withEnrichedReply(existing)
                localDataSource.appendMessageAndTouchRoom(
                    roomId = roomId,
                    message = message,
                    incrementUnread = false,
                    preserveReactedByMe = true,
                )
            }
            WsEventType.MESSAGE_DELETED -> {
                val payload = envelope.payload?.let {
                    runCatching { json.decodeFromJsonElement<MessageDeletedPayload>(it) }.getOrNull()
                } ?: return
                localDataSource.deleteMessage(payload.messageId)
            }
            WsEventType.MESSAGES_READ -> {
                val payload = envelope.payload?.let {
                    runCatching { json.decodeFromJsonElement<MessagesReadPayload>(it) }.getOrNull()
                } ?: return
                if (payload.upToCreatedAt > 0L) {
                    localDataSource.markMineMessagesReadUpTo(payload.roomId, payload.upToCreatedAt)
                }
            }
            WsEventType.PRESENCE -> schedulePresenceRoomRefresh()
            WsEventType.TYPING -> {
                val node = envelope.payload ?: return
                val parsed = runCatching { json.decodeFromJsonElement<TypingPayload>(node) }.getOrNull()
                val roomId = (parsed?.roomId ?: node.stringField("room_id", "roomId"))
                    ?.trim()?.lowercase()
                    .orEmpty()
                if (roomId.isBlank()) return
                val selfId = accessTokenHolder.userId
                val senderId = (parsed?.userId ?: node.stringField("user_id", "userId"))
                    ?.trim()?.lowercase()
                    .orEmpty()
                if (selfId != null && senderId.isNotBlank() && senderId == selfId) return
                val typing = parsed?.typing ?: node["typing"]?.jsonPrimitive?.booleanOrNull ?: true
                typingStateStore.setTyping(roomId, typing)
            }
            WsEventType.CALL_INCOMING,
            WsEventType.CALL_ACCEPTED,
            WsEventType.CALL_REJECTED,
            WsEventType.CALL_ENDED,
            -> handleCallEvent(envelope.type, envelope.payload)
            WsEventType.ICE_OFFER,
            WsEventType.ICE_ANSWER,
            WsEventType.ICE_CANDIDATE,
            -> handleIceEvent(envelope.type, envelope.payload)
            WsEventType.DEVICE_SESSION_REVOKED -> handleDeviceSessionRevoked(envelope.payload)
        }
    }

    private suspend fun handleDeviceSessionRevoked(payloadNode: kotlinx.serialization.json.JsonObject?) {
        val payload = payloadNode?.let {
            runCatching { json.decodeFromJsonElement<DeviceSessionRevokedPayload>(it) }.getOrNull()
        } ?: return
        val localToken = runCatching { deviceTokenStore.getOrCreate() }.getOrNull() ?: return
        if (payload.deviceToken != localToken) return
        webSocketClient.disconnect()
        tokenStore.clear()
    }

    private suspend fun handleCallEvent(type: String, payloadNode: kotlinx.serialization.json.JsonObject?) {
        if (payloadNode == null) {
            android.util.Log.w("KChatCall", "WS $type missing payload")
            return
        }
        val payload = runCatching { json.decodeFromJsonElement<CallEventPayload>(payloadNode) }
            .onFailure { android.util.Log.e("KChatCall", "WS $type decode failed: ${it.message} raw=$payloadNode") }
            .getOrNull()
            ?: return
        val call = payload.call.toModel()
        android.util.Log.i("KChatCall", "WS $type callId=${call.id} status=${call.status}")
        val event = when (type) {
            WsEventType.CALL_INCOMING -> CallRealtimeEvent.Incoming(call)
            WsEventType.CALL_ACCEPTED -> CallRealtimeEvent.Accepted(call)
            WsEventType.CALL_REJECTED -> CallRealtimeEvent.Rejected(call)
            WsEventType.CALL_ENDED -> CallRealtimeEvent.Ended(call)
            else -> return
        }
        // Publish call signal immediately to bus so UI and WebRTC react with zero latency
        callSignalBus.publish(event)
        if (type == WsEventType.CALL_ENDED || type == WsEventType.CALL_REJECTED) {
            incomingMessageNotifier.dismissCallNotification(call.id)
        }
        val selfId = accessTokenHolder.userId?.trim()?.lowercase()
        val peerName = if (!selfId.isNullOrBlank() && call.initiatorId.trim().lowercase() == selfId) {
            call.calleeName
        } else {
            call.initiatorName
        }
        scope.launch {
            runCatching {
                localDataSource.ensureRoomStub(call.roomId.trim().lowercase(), peerName)
            }
        }
    }

    private fun handleIceEvent(type: String, payloadNode: kotlinx.serialization.json.JsonObject?) {
        val payload = payloadNode?.let {
            runCatching { json.decodeFromJsonElement<IceSignalPayload>(it) }.getOrNull()
        } ?: return
        val event = when (type) {
            WsEventType.ICE_OFFER -> {
                val sdp = payload.sdp ?: return
                CallRealtimeEvent.IceOffer(payload.callId, payload.fromUserId, sdp)
            }
            WsEventType.ICE_ANSWER -> {
                val sdp = payload.sdp ?: return
                CallRealtimeEvent.IceAnswer(payload.callId, payload.fromUserId, sdp)
            }
            WsEventType.ICE_CANDIDATE -> {
                val candidate = payload.candidate ?: return
                CallRealtimeEvent.IceCandidate(
                    callId = payload.callId,
                    fromUserId = payload.fromUserId,
                    candidate = candidate,
                    sdpMid = payload.sdpMid,
                    sdpMLineIndex = payload.sdpMLineIndex,
                )
            }
            else -> return
        }
        callSignalBus.publish(event)
    }

    /** Debounce mark-read while actively viewing (BE still increments on each send). */
    private fun scheduleMarkRead(roomId: String) {
        pendingMarkReadRoomId = roomId
        markReadJob?.cancel()
        markReadJob = scope.launch {
            delay(350)
            val id = pendingMarkReadRoomId ?: return@launch
            if (activeRoomTracker.isActive(id)) {
                runCatching { chatRepository.markRoomRead(id) }
            }
        }
    }

    /** Debounce presence storms — refresh online flags from REST. */
    private fun schedulePresenceRoomRefresh() {
        presenceRefreshJob?.cancel()
        presenceRefreshJob = scope.launch {
            delay(400)
            runCatching { chatRepository.refreshRooms() }
            runCatching { contactsRepository.refreshContacts() }
        }
    }

    override fun disconnect() {
        lastTypingSent.clear()
        pendingTyping.clear()
        markReadJob?.cancel()
        markReadJob = null
        pendingMarkReadRoomId = null
        presenceRefreshJob?.cancel()
        presenceRefreshJob = null
        connectionJob?.cancel()
        connectionJob = null
        webSocketClient.disconnect()
    }

    companion object {
        private const val WS_RECONNECT_MIN_MS = 1_000L
        private const val WS_RECONNECT_MAX_MS = 30_000L
        private const val TYPING_HEARTBEAT_MS = 2_000L
    }
}

private fun JsonObject.stringField(vararg keys: String): String? {
    for (key in keys) {
        val value = this[key]?.jsonPrimitive?.content?.trim().orEmpty()
        if (value.isNotEmpty()) return value
    }
    return null
}
