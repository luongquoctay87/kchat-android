package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.model.EmailRules
import com.kchat.core.model.PasswordRules
import com.kchat.core.model.RegisterValidation
import com.kchat.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val usernameEdited: Boolean = false,
    val isSendingOtp: Boolean = false,
    val showOtpDialog: Boolean = false,
    val otp: String = "",
    val isVerifyingOtp: Boolean = false,
    val formError: String? = null,
    val displayNameError: String? = null,
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val otpError: String? = null,
    val isRegistered: Boolean = false,
) {
    val canSubmitForm: Boolean
        get() = RegisterValidation.canSubmit(displayName, email, username, password, confirm) &&
            displayNameError == null &&
            emailError == null &&
            usernameError == null &&
            passwordError == null &&
            confirmError == null
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private var passwordValidationJob: Job? = null
    private var usernameAvailabilityJob: Job? = null
    private var emailAvailabilityJob: Job? = null

    fun onDisplayNameChange(value: String) =
        _uiState.update {
            it.copy(
                displayName = value,
                formError = null,
                displayNameError = liveDisplayNameError(value),
            )
        }

    fun onUsernameChange(value: String) {
        val sanitized = RegisterValidation.sanitizeUsernameInput(value)
        _uiState.update {
            it.copy(
                username = sanitized,
                usernameEdited = true,
                formError = null,
                usernameError = liveUsernameError(sanitized),
            )
        }
        scheduleUsernameAvailabilityCheck(sanitized)
    }

    fun onEmailChange(value: String) {
        val sanitized = EmailRules.sanitizeEmailInput(value)
        var nextUsername = ""
        var shouldCheckSuggestedUsername = false
        _uiState.update { state ->
            val suggested = RegisterValidation.suggestUsernameFromEmail(sanitized)
            nextUsername = if (state.usernameEdited) state.username else suggested
            shouldCheckSuggestedUsername = !state.usernameEdited
            state.copy(
                email = sanitized,
                formError = null,
                emailError = liveEmailError(sanitized),
                username = nextUsername,
                usernameError = if (state.usernameEdited) {
                    state.usernameError
                } else {
                    liveUsernameError(nextUsername)
                },
            )
        }
        scheduleEmailAvailabilityCheck(sanitized)
        if (shouldCheckSuggestedUsername) {
            scheduleUsernameAvailabilityCheck(nextUsername)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, formError = null, passwordError = null)
        }
        schedulePasswordValidation()
    }

    fun onConfirmChange(value: String) {
        _uiState.update {
            it.copy(confirm = value, formError = null, confirmError = null)
        }
        schedulePasswordValidation()
    }

    fun onOtpChange(value: String) =
        _uiState.update { it.copy(otp = value.filter(Char::isDigit).take(6), otpError = null) }

    fun dismissOtpDialog() {
        if (_uiState.value.isVerifyingOtp) return
        _uiState.update {
            it.copy(showOtpDialog = false, otp = "", otpError = null)
        }
    }

    /** Step 1: validate form + re-check availability + send OTP → show dialog. */
    fun submit() {
        val state = _uiState.value
        if (state.isSendingOtp) return

        passwordValidationJob?.cancel()
        usernameAvailabilityJob?.cancel()
        emailAvailabilityJob?.cancel()

        val fieldErrors = RegisterValidation.validateFields(
            displayName = state.displayName,
            email = state.email,
            username = state.username,
            password = state.password,
            confirm = state.confirm,
        )
        if (fieldErrors.hasAny) {
            _uiState.update {
                it.copy(
                    formError = null,
                    displayNameError = fieldErrors.displayName,
                    emailError = fieldErrors.email,
                    usernameError = fieldErrors.username,
                    passwordError = fieldErrors.password,
                    confirmError = fieldErrors.confirm,
                )
            }
            return
        }
        if (state.usernameError != null || state.emailError != null) return

        val parsed = RegisterValidation.parse(state.displayName, state.email, state.username)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingOtp = true,
                    formError = null,
                    displayNameError = null,
                    passwordError = null,
                    confirmError = null,
                )
            }

            val usernameAvailable = authRepository.checkUsernameAvailable(parsed.username)
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            formError = error.message ?: "Không kiểm tra được username",
                        )
                    }
                    return@launch
                }
            if (!usernameAvailable) {
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        usernameError = USERNAME_TAKEN_MESSAGE,
                    )
                }
                return@launch
            }

            val emailAvailable = authRepository.checkEmailAvailable(parsed.email)
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            formError = error.message ?: "Không kiểm tra được email",
                        )
                    }
                    return@launch
                }
            if (!emailAvailable) {
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        emailError = EMAIL_TAKEN_MESSAGE,
                    )
                }
                return@launch
            }

            authRepository.sendRegistrationOtp(parsed.email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            showOtpDialog = true,
                            otp = "",
                            otpError = null,
                            usernameError = null,
                            emailError = null,
                        )
                    }
                }
                .onFailure { e ->
                    val message = e.message ?: "Không gửi được OTP. Vui lòng thử lại."
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            emailError = if (isEmailConflict(message)) message else null,
                            formError = if (isEmailConflict(message)) null else message,
                        )
                    }
                }
        }
    }

    /** Step 2: verify OTP → create account → enter session. */
    fun verifyOtp() {
        val state = _uiState.value
        if (state.isVerifyingOtp || state.otp.length != 6) return

        val parsed = RegisterValidation.parse(state.displayName, state.email, state.username)
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingOtp = true, otpError = null) }
            authRepository.verifyRegistrationOtp(parsed.email, state.otp)
                .onSuccess { registrationToken ->
                    authRepository.register(
                        registrationToken = registrationToken,
                        displayName = parsed.displayName,
                        email = parsed.email,
                        username = parsed.username,
                        password = state.password,
                    ).onSuccess {
                        _uiState.update {
                            it.copy(
                                isVerifyingOtp = false,
                                showOtpDialog = false,
                                isRegistered = true,
                            )
                        }
                    }.onFailure { e ->
                        applyRegisterFailure(e.message ?: "Đăng ký thất bại. Vui lòng thử lại.")
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isVerifyingOtp = false,
                            otpError = e.message ?: "Mã OTP không đúng",
                        )
                    }
                }
        }
    }

    fun resendOtp() {
        val state = _uiState.value
        if (state.isSendingOtp || state.isVerifyingOtp) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingOtp = true, otpError = null) }
            authRepository.sendRegistrationOtp(state.email.trim().lowercase())
                .onSuccess {
                    _uiState.update {
                        it.copy(isSendingOtp = false, otp = "", otpError = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            otpError = e.message ?: "Gửi lại OTP thất bại",
                        )
                    }
                }
        }
    }

    private fun scheduleUsernameAvailabilityCheck(username: String) {
        usernameAvailabilityJob?.cancel()
        if (RegisterValidation.validateUsername(username) != null) return
        usernameAvailabilityJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            if (_uiState.value.username != username) return@launch
            authRepository.checkUsernameAvailable(username)
                .onSuccess { available ->
                    if (_uiState.value.username != username) return@onSuccess
                    if (!available) {
                        _uiState.update { it.copy(usernameError = USERNAME_TAKEN_MESSAGE) }
                    }
                }
        }
    }

    private fun scheduleEmailAvailabilityCheck(email: String) {
        emailAvailabilityJob?.cancel()
        if (EmailRules.validateForRegistration(email) != null) return
        emailAvailabilityJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            if (_uiState.value.email != email) return@launch
            authRepository.checkEmailAvailable(email)
                .onSuccess { available ->
                    if (_uiState.value.email != email) return@onSuccess
                    if (!available) {
                        _uiState.update { it.copy(emailError = EMAIL_TAKEN_MESSAGE) }
                    }
                }
        }
    }

    private fun applyRegisterFailure(message: String) {
        when {
            isUsernameConflict(message) -> {
                _uiState.update {
                    it.copy(
                        isVerifyingOtp = false,
                        showOtpDialog = false,
                        otp = "",
                        otpError = null,
                        usernameError = message,
                        formError = null,
                    )
                }
            }
            isEmailConflict(message) -> {
                _uiState.update {
                    it.copy(
                        isVerifyingOtp = false,
                        showOtpDialog = false,
                        otp = "",
                        otpError = null,
                        emailError = message,
                        formError = null,
                    )
                }
            }
            isRegistrationTokenError(message) -> {
                _uiState.update {
                    it.copy(
                        isVerifyingOtp = false,
                        otpError = message,
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        isVerifyingOtp = false,
                        showOtpDialog = false,
                        otp = "",
                        otpError = null,
                        formError = message,
                    )
                }
            }
        }
    }

    private fun schedulePasswordValidation() {
        passwordValidationJob?.cancel()
        passwordValidationJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    passwordError = livePasswordError(state.password),
                    confirmError = liveConfirmError(state.password, state.confirm),
                )
            }
        }
    }

    private fun livePasswordError(password: String): String? =
        if (password.isEmpty()) null else PasswordRules.validate(password)

    private fun liveConfirmError(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> null
        password != confirm -> "Mật khẩu xác nhận không khớp"
        else -> null
    }

    private fun liveDisplayNameError(displayName: String): String? =
        if (displayName.isBlank()) null else RegisterValidation.validateDisplayName(displayName)

    private fun liveEmailError(email: String): String? =
        if (email.isBlank()) null else EmailRules.validateForRegistration(email)

    private fun liveUsernameError(username: String): String? =
        if (username.isBlank()) null else RegisterValidation.validateUsername(username)

    private companion object {
        private const val DEBOUNCE_MS = 1_500L
        private const val USERNAME_TAKEN_MESSAGE = "Tên đăng nhập đã được sử dụng"
        private const val EMAIL_TAKEN_MESSAGE = "Email đã được đăng ký"

        fun isUsernameConflict(message: String): Boolean =
            message.contains(USERNAME_TAKEN_MESSAGE, ignoreCase = true) ||
                message.contains("Username already taken", ignoreCase = true)

        fun isEmailConflict(message: String): Boolean =
            message.contains(EMAIL_TAKEN_MESSAGE, ignoreCase = true) ||
                message.contains("Email already registered", ignoreCase = true) ||
                message.contains("Email hoặc tên đăng nhập đã được đăng ký", ignoreCase = true)

        fun isRegistrationTokenError(message: String): Boolean =
            message.contains("invalid_registration_token", ignoreCase = true) ||
                message.contains("Invalid or expired registration token", ignoreCase = true) ||
                message.contains("Phiên đăng ký không hợp lệ", ignoreCase = true) ||
                message.contains("Vui lòng xác minh OTP", ignoreCase = true)
    }
}
