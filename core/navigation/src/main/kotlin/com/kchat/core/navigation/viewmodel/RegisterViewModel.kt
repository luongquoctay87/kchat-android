package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.model.PasswordRules
import com.kchat.core.model.RegisterValidation
import com.kchat.data.repository.AuthRepository
import com.kchat.data.repository.SessionCoordinator
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
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val otpError: String? = null,
    val isRegistered: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionCoordinator: SessionCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private var passwordValidationJob: Job? = null

    fun onDisplayNameChange(value: String) =
        _uiState.update { it.copy(displayName = value, formError = null) }

    fun onUsernameChange(value: String) {
        val sanitized = RegisterValidation.sanitizeUsernameInput(value)
        _uiState.update {
            it.copy(
                username = sanitized,
                usernameEdited = true,
                formError = null,
                usernameError = null,
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { state ->
            val suggested = RegisterValidation.suggestUsernameFromEmail(value)
            state.copy(
                email = value,
                formError = null,
                username = if (state.usernameEdited) state.username else suggested,
            )
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
        _uiState.update { it.copy(otp = value.filter { c -> c.isDigit() }.take(6), otpError = null) }

    fun dismissOtpDialog() {
        if (_uiState.value.isVerifyingOtp) return
        _uiState.update {
            it.copy(showOtpDialog = false, otp = "", otpError = null)
        }
    }

    /** Step 1: validate form + send OTP → show dialog. */
    fun submit() {
        val state = _uiState.value
        if (state.isSendingOtp) return

        passwordValidationJob?.cancel()
        val usernameError = RegisterValidation.validateUsername(state.username)
        val passwordError = PasswordRules.validate(state.password)
        val confirmError = when {
            state.confirm.isEmpty() -> "Vui lòng xác nhận mật khẩu"
            state.password != state.confirm -> "Mật khẩu xác nhận không khớp"
            else -> null
        }

        val formError = RegisterValidation.validate(
            displayName = state.displayName,
            email = state.email,
            username = state.username,
            password = state.password,
            confirm = state.confirm,
        )

        if (formError != null) {
            val fieldRelated =
                formError == usernameError ||
                    formError == passwordError ||
                    formError == confirmError ||
                    formError == "Mật khẩu xác nhận không khớp"
            _uiState.update {
                it.copy(
                    formError = if (fieldRelated) null else formError,
                    usernameError = usernameError,
                    passwordError = passwordError,
                    confirmError = confirmError,
                )
            }
            return
        }

        val email = state.email.trim()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingOtp = true,
                    formError = null,
                    usernameError = null,
                    passwordError = null,
                    confirmError = null,
                )
            }
            authRepository.sendRegistrationOtp(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            showOtpDialog = true,
                            otp = "",
                            otpError = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            formError = e.message ?: "Không gửi được OTP. Vui lòng thử lại.",
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
                        try {
                            sessionCoordinator.onAuthenticated()
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // Tokens already saved — bootstrap is best-effort.
                        }
                        _uiState.update {
                            it.copy(
                                isVerifyingOtp = false,
                                showOtpDialog = false,
                                isRegistered = true,
                            )
                        }
                    }.onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isVerifyingOtp = false,
                                otpError = e.message ?: "Đăng ký thất bại. Vui lòng thử lại.",
                            )
                        }
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
            authRepository.sendRegistrationOtp(state.email.trim())
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

    private fun schedulePasswordValidation() {
        passwordValidationJob?.cancel()
        passwordValidationJob = viewModelScope.launch {
            delay(PASSWORD_VALIDATION_DEBOUNCE_MS)
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

    companion object {
        private const val PASSWORD_VALIDATION_DEBOUNCE_MS = 1_500L
    }
}
