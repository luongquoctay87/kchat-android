package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.model.PinRules
import com.kchat.data.repository.AuthRepository
import com.kchat.data.repository.EmergencyWipeCoordinator
import com.kchat.data.repository.EmergencyWipeStore
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.PinVerifyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PinLockUiState(
    val pin: String = "",
    val isVerifying: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PinLockViewModel @Inject constructor(
    private val pinLockStore: PinLockStore,
    private val emergencyWipeCoordinator: EmergencyWipeCoordinator,
    private val authRepository: AuthRepository,
    private val emergencyWipeStore: EmergencyWipeStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PinLockUiState())
    val uiState = _uiState.asStateFlow()

    fun onVisible() {
        _uiState.value = PinLockUiState()
    }

    fun onPinChange(value: String) {
        val pin = value.filter { it.isDigit() }.take(PinRules.LENGTH)
        _uiState.update { it.copy(pin = pin, error = null) }
        if (pin.length == PinRules.LENGTH) submit()
    }

    private fun submit() {
        val pin = _uiState.value.pin
        if (pin.length != PinRules.LENGTH || _uiState.value.isVerifying) return
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, error = null) }
            when (pinLockStore.verify(pin)) {
                PinVerifyResult.Unlocked -> {
                    emergencyWipeStore.reset()
                    _uiState.value = PinLockUiState()
                }
                PinVerifyResult.EmergencyWipeMessages -> {
                    emergencyWipeCoordinator.execute()
                    _uiState.value = PinLockUiState()
                }
                PinVerifyResult.Wrong -> _uiState.update {
                    it.copy(isVerifying = false, pin = "", error = "PIN không đúng")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
