package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.model.PinRules
import com.kchat.core.ui.screens.settings.PinSettingsMode
import com.kchat.data.repository.PinLockStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PinSettingsUiState(
    val mode: PinSettingsMode = PinSettingsMode.Overview,
    val currentPin: String = "",
    val newPin: String = "",
    val confirmPin: String = "",
    val error: String? = null,
    val isSaving: Boolean = false,
)

@HiltViewModel
class PinSettingsViewModel @Inject constructor(
    private val pinLockStore: PinLockStore,
) : ViewModel() {
    val isEnabled = pinLockStore.isEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isEmergencyEnabled = pinLockStore.isEmergencyEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _uiState = MutableStateFlow(PinSettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun setMode(mode: PinSettingsMode) {
        _uiState.value = PinSettingsUiState(mode = mode)
    }

    fun onCurrentPinChange(value: String) {
        _uiState.update { it.copy(currentPin = digits(value), error = null) }
    }

    fun onNewPinChange(value: String) {
        _uiState.update { it.copy(newPin = digits(value), error = null) }
    }

    fun onConfirmPinChange(value: String) {
        _uiState.update { it.copy(confirmPin = digits(value), error = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSaving) return
        val action: suspend () -> Result<Unit> = when (state.mode) {
            PinSettingsMode.Overview -> return
            PinSettingsMode.Create -> {
                {
                    when {
                        state.newPin != state.confirmPin ->
                            Result.failure(IllegalArgumentException("Hai lần nhập PIN không khớp"))
                        else -> pinLockStore.setPin(state.newPin)
                    }
                }
            }
            PinSettingsMode.Change -> {
                {
                    when {
                        state.newPin != state.confirmPin ->
                            Result.failure(IllegalArgumentException("Hai lần nhập PIN không khớp"))
                        else -> pinLockStore.changePin(state.currentPin, state.newPin)
                    }
                }
            }
            PinSettingsMode.Disable -> {
                { pinLockStore.disable(state.currentPin) }
            }
            PinSettingsMode.CreateEmergency -> {
                {
                    when {
                        state.newPin != state.confirmPin ->
                            Result.failure(IllegalArgumentException("Hai lần nhập mã khẩn cấp không khớp"))
                        else -> pinLockStore.setEmergency(state.currentPin, state.newPin)
                    }
                }
            }
            PinSettingsMode.ChangeEmergency -> {
                {
                    when {
                        state.newPin != state.confirmPin ->
                            Result.failure(IllegalArgumentException("Hai lần nhập mã khẩn cấp không khớp"))
                        else -> pinLockStore.changeEmergency(state.currentPin, state.newPin)
                    }
                }
            }
            PinSettingsMode.DisableEmergency -> {
                { pinLockStore.disableEmergency(state.currentPin) }
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            action()
                .onSuccess {
                    _uiState.value = PinSettingsUiState(mode = PinSettingsMode.Overview)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSaving = false, error = e.message ?: "Không lưu được")
                    }
                }
        }
    }

    private fun digits(value: String): String = value.filter { it.isDigit() }.take(PinRules.LENGTH)
}
