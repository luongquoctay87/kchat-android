package com.kchat.data.fake

import com.kchat.core.model.DeviceSession
import com.kchat.core.model.UserProfile
import com.kchat.core.model.UserSettings
import com.kchat.data.repository.LocalCacheCleanupScheduler
import com.kchat.data.repository.SettingsRepository
import com.kchat.data.repository.UserSettingsPatch
import com.kchat.data.repository.UserSettingsPatch.Companion.CLEAR_DEFAULT_DISAPPEARING
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSettingsRepository @Inject constructor(
    private val localCacheCleanupScheduler: LocalCacheCleanupScheduler,
) : SettingsRepository {
    private val profileState = MutableStateFlow(
        UserProfile(
            id = "u1",
            username = "nguyenva",
            email = FakeSampleData.currentUserEmail,
            displayName = "Nguyễn Văn Demo",
        ),
    )
    private val settingsState = MutableStateFlow(UserSettings())
    private val devices = MutableStateFlow(FakeSampleData.devices)

    override val profile: Flow<UserProfile?> = profileState.asStateFlow()

    override val settings: Flow<UserSettings?> = settingsState.asStateFlow()

    override suspend fun refreshProfile() {
        // no-op — seed data already loaded
    }

    override suspend fun refreshSettings() {
        // no-op
    }

    override suspend fun updateProfile(displayName: String, phone: String): Result<Unit> {
        profileState.update { current ->
            current?.copy(displayName = displayName, phone = phone)
                ?: UserProfile("u1", "nguyenva", FakeSampleData.currentUserEmail, displayName, phone)
        }
        return Result.success(Unit)
    }

    override suspend fun updateAvatar(
        uri: android.net.Uri,
        mimeType: String?,
        displayName: String?,
    ): Result<Unit> {
        profileState.update { current ->
            current?.copy(avatarUrl = uri.toString())
                ?: UserProfile(
                    id = "u1",
                    username = "nguyenva",
                    email = FakeSampleData.currentUserEmail,
                    displayName = "Nguyễn Văn Demo",
                    avatarUrl = uri.toString(),
                )
        }
        return Result.success(Unit)
    }

    override suspend fun updateSettings(patch: UserSettingsPatch): Result<Unit> {
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
        if (patch.localCacheRetentionDays != null) {
            localCacheCleanupScheduler.scheduleForSettings(settingsState.value?.localCacheRetentionDays)
        }
        return Result.success(Unit)
    }

    override fun observeDevices(): Flow<List<DeviceSession>> = devices.asStateFlow()

    override suspend fun refreshDevices() {
        devices.value = FakeSampleData.devices
    }

    override suspend fun registerCurrentDevice(): Result<Unit> = Result.success(Unit)

    override suspend fun revokeDevice(deviceId: String): Result<Unit> {
        devices.update { list -> list.filterNot { it.id == deviceId } }
        return Result.success(Unit)
    }

    override suspend fun clearLocal() {
        profileState.value = UserProfile(
            id = "u1",
            username = "nguyenva",
            email = FakeSampleData.currentUserEmail,
            displayName = "Nguyễn Văn Demo",
        )
        settingsState.value = UserSettings()
        devices.value = FakeSampleData.devices
    }
}
