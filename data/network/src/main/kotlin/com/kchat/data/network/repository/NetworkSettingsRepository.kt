package com.kchat.data.network.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kchat.core.model.DeviceSession
import com.kchat.core.model.UserProfile
import com.kchat.core.model.UserSettings
import com.kchat.data.network.api.KChatApi
import com.kchat.data.network.apiMessage
import com.kchat.data.network.apiResult
import com.kchat.data.network.dto.UpdateProfileRequest
import com.kchat.data.network.dto.RegisterDeviceRequest
import com.kchat.data.network.util.currentUtcOffsetMinutes
import com.kchat.data.repository.DeviceTokenStore
import com.kchat.data.network.mapper.toModel
import com.kchat.data.network.mapper.toRequest
import com.kchat.data.network.media.MediaUploadHelper
import com.kchat.data.repository.LocalCacheCleanupScheduler
import com.kchat.data.repository.SettingsRepository
import com.kchat.data.repository.TokenStore
import com.kchat.data.repository.UserSettingsPatch
import com.kchat.data.repository.UserSettingsPatch.Companion.CLEAR_DEFAULT_DISAPPEARING
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody

private val Context.profileDataStore by preferencesDataStore(name = "kchat_profile")

@Singleton
class NetworkSettingsRepository @Inject constructor(
    private val api: KChatApi,
    private val tokenStore: TokenStore,
    private val deviceTokenStore: DeviceTokenStore,
    private val localCacheCleanupScheduler: LocalCacheCleanupScheduler,
    @Named("apiBaseUrl") private val apiBaseUrl: String,
    @ApplicationContext private val context: Context,
) : SettingsRepository {
    private val dataStore = context.profileDataStore
    private val profileKey = stringPreferencesKey("cached_profile_json")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val profileState = MutableStateFlow<UserProfile?>(null)
    private val settingsState = MutableStateFlow<UserSettings?>(null)
    private val devices = MutableStateFlow<List<DeviceSession>>(emptyList())
    private val settingsMutex = Mutex()

    override val profile: Flow<UserProfile?> = profileState.asStateFlow()

    override val settings: Flow<UserSettings?> = settingsState.asStateFlow()

    init {
        scope.launch { loadCachedProfile() }
    }

    override suspend fun refreshProfile() {
        // Show cache immediately so Settings/Profile never flash empty "@" / "?"
        if (profileState.value == null) {
            loadCachedProfile()
        }
        // Never throw — callers often run on Main; uncaught HttpException kills the process.
        try {
            ensureToken()
            val remote = api.getProfile().toModel().withAbsoluteAvatarUrl(apiBaseUrl)
            profileState.value = remote
            cacheProfile(remote)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            // Keep cached profile; session clear happens via TokenAuthenticator on 401 refresh failure.
        }
    }

    override suspend fun refreshSettings() {
        settingsMutex.withLock {
            try {
                ensureToken()
                settingsState.value = api.getSettings().toModel()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep last-known settings.
            }
        }
    }

    override suspend fun updateProfile(displayName: String, phone: String): Result<Unit> = apiResult("Lưu hồ sơ thất bại") {
        ensureToken()
        val remote = api.updateProfile(UpdateProfileRequest(displayName, phone.trim()))
            .toModel()
            .withAbsoluteAvatarUrl(apiBaseUrl)
        profileState.value = remote
        cacheProfile(remote)
    }

    override suspend fun updateAvatar(
        uri: android.net.Uri,
        mimeType: String?,
        displayName: String?,
    ): Result<Unit> {
        return runCatching {
            ensureToken()
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
            val part = MultipartBody.Part.createFormData("file", name, body)
            val remote = api.updateAvatar(part).toModel().withAbsoluteAvatarUrl(apiBaseUrl)
            profileState.value = remote
            cacheProfile(remote)
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(Exception(it.apiMessage("Cập nhật ảnh thất bại"))) },
        )
    }

    override suspend fun updateSettings(patch: UserSettingsPatch): Result<Unit> = settingsMutex.withLock {
        ensureToken()
        val previous = settingsState.value
        settingsState.update { current ->
            val base = current ?: UserSettings()
            val quietOff = patch.quietHoursEnabled == false
            base.copy(
                pushEnabled = patch.pushEnabled ?: base.pushEnabled,
                theme = patch.theme ?: base.theme,
                fontSize = patch.fontSize ?: base.fontSize,
                showOnline = patch.showOnline ?: base.showOnline,
                enterToSend = patch.enterToSend ?: base.enterToSend,
                privacyDm = patch.privacyDm ?: base.privacyDm,
                quietHoursEnabled = patch.quietHoursEnabled ?: base.quietHoursEnabled,
                quietHoursStartHour = when {
                    quietOff -> null
                    patch.quietHoursStartHour != null -> patch.quietHoursStartHour
                    else -> base.quietHoursStartHour
                },
                quietHoursEndHour = when {
                    quietOff -> null
                    patch.quietHoursEndHour != null -> patch.quietHoursEndHour
                    else -> base.quietHoursEndHour
                },
                localCacheRetentionDays = patch.localCacheRetentionDays ?: base.localCacheRetentionDays,
                defaultDisappearingSeconds = when {
                    patch.defaultDisappearingSeconds == CLEAR_DEFAULT_DISAPPEARING -> null
                    patch.defaultDisappearingSeconds != null -> patch.defaultDisappearingSeconds
                    else -> base.defaultDisappearingSeconds
                },
            )
        }
        val result = runCatching {
            settingsState.value = api.updateSettings(patch.toRequest()).toModel()
        }
        if (result.isFailure) {
            settingsState.value = previous
            Result.failure(Exception(result.exceptionOrNull()?.apiMessage("Lưu cài đặt thất bại") ?: "Lưu cài đặt thất bại"))
        } else {
            if (patch.localCacheRetentionDays != null) {
                localCacheCleanupScheduler.scheduleForSettings(settingsState.value?.localCacheRetentionDays)
            }
            Result.success(Unit)
        }
    }

    override fun observeDevices(): Flow<List<DeviceSession>> = devices.asStateFlow()

    override suspend fun refreshDevices() {
        try {
            ensureToken()
            var loaded = runCatching { api.getDevices().map { it.toModel() } }
            if (loaded.isSuccess && loaded.getOrThrow().isEmpty()) {
                registerCurrentDevice().getOrNull()
                loaded = runCatching { api.getDevices().map { it.toModel() } }
            }
            if (loaded.isSuccess) {
                devices.value = loaded.getOrThrow()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort — device list UI can retry; do not block login bootstrap.
        }
    }

    override suspend fun registerCurrentDevice(): Result<Unit> = runCatching {
        ensureToken()
        api.registerDevice(
            RegisterDeviceRequest(
                fcmToken = deviceTokenStore.getOrCreate(),
                deviceName = android.os.Build.MODEL.ifBlank { "Android" },
                platform = "android",
                utcOffsetMinutes = currentUtcOffsetMinutes(),
            ),
        )
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(it) },
    )

    override suspend fun revokeDevice(deviceId: String): Result<Unit> {
        ensureToken()
        return runCatching {
            api.revokeDevice(deviceId)
            devices.update { list -> list.filterNot { it.id == deviceId } }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = {
                Result.failure(Exception(it.apiMessage("Không đăng xuất được thiết bị")))
            },
        )
    }

    override suspend fun clearLocal() {
        profileState.value = null
        settingsState.value = null
        devices.value = emptyList()
        dataStore.edit { it.remove(profileKey) }
    }

    private suspend fun ensureToken() {
        tokenStore.getAccessToken()
    }

    private suspend fun loadCachedProfile() {
        val raw = dataStore.data.first()[profileKey] ?: return
        val cached = runCatching {
            json.decodeFromString(CachedUserProfile.serializer(), raw)
        }.getOrNull() ?: return
        profileState.value = cached.toModel()
    }

    private suspend fun cacheProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[profileKey] = json.encodeToString(CachedUserProfile.serializer(), CachedUserProfile.from(profile))
        }
    }
}

@kotlinx.serialization.Serializable
private data class CachedUserProfile(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val phone: String = "",
    val avatarUrl: String? = null,
) {
    fun toModel() = UserProfile(id, username, email, displayName, phone, avatarUrl)

    companion object {
        fun from(profile: UserProfile) = CachedUserProfile(
            id = profile.id,
            username = profile.username,
            email = profile.email,
            displayName = profile.displayName,
            phone = profile.phone,
            avatarUrl = profile.avatarUrl,
        )
    }
}

private fun UserProfile.withAbsoluteAvatarUrl(baseUrl: String): UserProfile {
    val relative = avatarUrl ?: return this
    if (relative.startsWith("http://") || relative.startsWith("https://")) return this
    return copy(avatarUrl = baseUrl.trimEnd('/') + relative)
}
