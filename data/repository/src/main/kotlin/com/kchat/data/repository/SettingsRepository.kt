package com.kchat.data.repository

import com.kchat.core.model.DeviceSession
import com.kchat.core.model.UserProfile
import com.kchat.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

data class UserSettingsPatch(
    val pushEnabled: Boolean? = null,
    val theme: String? = null,
    val fontSize: String? = null,
    val showOnline: Boolean? = null,
    val enterToSend: Boolean? = null,
    val privacyDm: String? = null,
    val quietHoursEnabled: Boolean? = null,
    val quietHoursStartHour: Int? = null,
    val quietHoursEndHour: Int? = null,
    val localCacheRetentionDays: Int? = null,
    /** Use [CLEAR_DEFAULT_DISAPPEARING] to clear server-side default. */
    val defaultDisappearingSeconds: Int? = null,
) {
    companion object {
        const val CLEAR_DEFAULT_DISAPPEARING = 0
    }
}

interface SettingsRepository {
    val profile: Flow<UserProfile?>

    val settings: Flow<UserSettings?>

    suspend fun refreshProfile()

    suspend fun refreshSettings()

    suspend fun updateProfile(displayName: String, phone: String): Result<Unit>

    suspend fun updateAvatar(uri: android.net.Uri, mimeType: String?, displayName: String?): Result<Unit>

    suspend fun updateSettings(patch: UserSettingsPatch): Result<Unit>

    fun observeDevices(): Flow<List<DeviceSession>>

    suspend fun refreshDevices()

    suspend fun registerCurrentDevice(): Result<Unit>

    suspend fun revokeDevice(deviceId: String): Result<Unit>

    /** Clears in-memory + cached profile/settings (logout). */
    suspend fun clearLocal()
}
