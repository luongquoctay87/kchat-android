package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kchat.core.navigation.KChatRoute
import com.kchat.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyResetOtpUiState(
    val email: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val error: String? = null,
    val resendMessage: String? = null,
    val resendError: String? = null,
    val verifiedResetToken: String? = null,
)

@HiltViewModel
class VerifyResetOtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<KChatRoute.VerifyResetOtp>()

    private val _uiState = MutableStateFlow(VerifyResetOtpUiState(email = route.email))
    val uiState = _uiState.asStateFlow()

    fun onOtpChange(value: String) = _uiState.update { it.copy(otp = value, error = null) }

    fun verify() {
        val state = _uiState.value
        if (state.isLoading || state.otp.length != 6) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.verifyResetOtp(state.email, state.otp)
                .onSuccess { token ->
                    _uiState.update { it.copy(isLoading = false, verifiedResetToken = token) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "Mã OTP không đúng")
                    }
                }
        }
    }

    fun resend() {
        if (_uiState.value.isResending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, resendMessage = null, resendError = null) }
            authRepository.forgotPassword(route.email)
                .onSuccess {
                    _uiState.update {
                        it.copy(isResending = false, resendMessage = "Đã gửi lại mã OTP")
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isResending = false,
                            resendError = error.message ?: "Gửi lại thất bại",
                        )
                    }
                }
        }
    }
}
