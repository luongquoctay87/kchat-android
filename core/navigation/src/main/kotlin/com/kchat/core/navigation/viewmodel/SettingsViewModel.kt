package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.design.KChatAppearance
import com.kchat.core.model.UserProfile
import com.kchat.core.model.UserSettings
import com.kchat.core.model.PasswordRules
import com.kchat.core.navigation.settings.toAppearance
import com.kchat.core.navigation.settings.toMessageStorage
import com.kchat.core.navigation.settings.toPrivacyDmValue
import com.kchat.core.navigation.settings.toSettingsPatch
import com.kchat.core.ui.screens.settings.DmPrivacyPolicy
import com.kchat.core.ui.screens.settings.MessageStorageSettings
import com.kchat.core.ui.screens.settings.QuietHours
import com.kchat.data.repository.AuthRepository
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.SettingsRepository
import com.kchat.data.repository.UserSettingsPatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    pinLockStore: PinLockStore,
) : ViewModel() {
    val profile: StateFlow<UserProfile?> = settingsRepository.profile
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val settings: StateFlow<UserSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pinEnabled: StateFlow<Boolean> = pinLockStore.isEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _profileSaving = MutableStateFlow(false)
    val profileSaving: StateFlow<Boolean> = _profileSaving.asStateFlow()

    private val _avatarUploading = MutableStateFlow(false)
    val avatarUploading: StateFlow<Boolean> = _avatarUploading.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    private val _passwordSaving = MutableStateFlow(false)
    val passwordSaving: StateFlow<Boolean> = _passwordSaving.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _passwordSuccess = MutableStateFlow<String?>(null)
    val passwordSuccess: StateFlow<String?> = _passwordSuccess.asStateFlow()

    private val _settingsError = MutableStateFlow<String?>(null)
    val settingsError: StateFlow<String?> = _settingsError.asStateFlow()

    init {
        refresh()
    }

    fun clearSettingsError() {
        _settingsError.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                settingsRepository.refreshProfile()
                settingsRepository.refreshSettings()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // Profile/settings refresh is best-effort at startup.
            }
        }
    }

    fun updateProfile(displayName: String, phone: String, onComplete: (Boolean) -> Unit = {}) {
        if (displayName.isBlank()) {
            _profileError.value = "Tên hiển thị không được để trống"
            onComplete(false)
            return
        }
        val normalizedPhone = phone.trim()
        if (normalizedPhone.isNotEmpty() && !PHONE_PATTERN.matches(normalizedPhone)) {
            _profileError.value = "Số điện thoại không hợp lệ"
            onComplete(false)
            return
        }
        viewModelScope.launch {
            _profileSaving.value = true
            _profileError.value = null
            settingsRepository.updateProfile(displayName.trim(), normalizedPhone)
                .onSuccess {
                    _profileSaving.value = false
                    onComplete(true)
                }
                .onFailure { error ->
                    _profileSaving.value = false
                    _profileError.value = error.message ?: "Lưu hồ sơ thất bại"
                    onComplete(false)
                }
        }
    }

    fun updateAvatar(uri: android.net.Uri, mimeType: String?, displayName: String?) {
        viewModelScope.launch {
            _avatarUploading.value = true
            _profileError.value = null
            settingsRepository.updateAvatar(uri, mimeType, displayName)
                .onSuccess { _avatarUploading.value = false }
                .onFailure { error ->
                    _avatarUploading.value = false
                    _profileError.value = error.message ?: "Cập nhật ảnh thất bại"
                }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onComplete: (Boolean) -> Unit = {},
    ) {
        val strengthError = PasswordRules.validate(newPassword)
        when {
            currentPassword.isBlank() -> {
                _passwordError.value = "Vui lòng nhập mật khẩu hiện tại"
                _passwordSuccess.value = null
                onComplete(false)
                return
            }
            strengthError != null -> {
                _passwordError.value = strengthError
                _passwordSuccess.value = null
                onComplete(false)
                return
            }
            newPassword != confirmPassword -> {
                _passwordError.value = "Mật khẩu xác nhận không khớp"
                _passwordSuccess.value = null
                onComplete(false)
                return
            }
        }
        viewModelScope.launch {
            _passwordSaving.value = true
            _passwordError.value = null
            _passwordSuccess.value = null
            authRepository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _passwordSaving.value = false
                    _passwordSuccess.value = "Đổi mật khẩu thành công. Vui lòng đăng nhập lại."
                    onComplete(true)
                }
                .onFailure { error ->
                    _passwordSaving.value = false
                    _passwordError.value = error.message ?: "Đổi mật khẩu thất bại"
                    onComplete(false)
                }
        }
    }

    fun updateAppearance(appearance: KChatAppearance) {
        patchSettings(appearance.toSettingsPatch())
    }

    fun setPushEnabled(enabled: Boolean) {
        patchSettings(UserSettingsPatch(pushEnabled = enabled))
    }

    fun setShowOnline(showOnline: Boolean) {
        patchSettings(UserSettingsPatch(showOnline = showOnline))
    }

    fun setDmPolicy(policy: DmPrivacyPolicy) {
        patchSettings(UserSettingsPatch(privacyDm = policy.toPrivacyDmValue()))
    }

    fun setQuietHours(quietHours: QuietHours) {
        if (quietHours.enabled && quietHours.startHour == quietHours.endHour) {
            _settingsError.value = "Giờ bắt đầu và kết thúc phải khác nhau"
            return
        }
        patchSettings(quietHours.toSettingsPatch())
    }

    fun setMessageStorage(storage: MessageStorageSettings) {
        patchSettings(storage.toSettingsPatch())
    }

    private fun patchSettings(patch: UserSettingsPatch) {
        viewModelScope.launch {
            settingsRepository.updateSettings(patch)
                .onSuccess { _settingsError.value = null }
                .onFailure { error ->
                    _settingsError.value = error.message ?: "Lưu cài đặt thất bại"
                }
        }
    }

    private companion object {
        val PHONE_PATTERN = Regex("^\\+?[0-9][0-9\\s\\-()]{6,30}$")
    }
}
