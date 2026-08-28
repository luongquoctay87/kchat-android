package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kchat.core.model.PasswordRules
import com.kchat.core.navigation.KChatRoute
import com.kchat.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordUiState(
    val password: String = "",
    val confirm: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val token = savedStateHandle.toRoute<KChatRoute.ResetPassword>().token

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onConfirmChange(value: String) = _uiState.update { it.copy(confirm = value, error = null) }

    fun submit() {
        val state = _uiState.value
        PasswordRules.validate(state.password)?.let { message ->
            _uiState.update { it.copy(error = message) }
            return
        }
        if (state.password != state.confirm) {
            _uiState.update { it.copy(error = "Mật khẩu xác nhận không khớp") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.resetPassword(token, state.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
