package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.model.DeviceSession
import com.kchat.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val devices: StateFlow<List<DeviceSession>> = settingsRepository.observeDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshDevices()
    }

    fun clearError() {
        _error.value = null
    }

    fun refreshDevices() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { settingsRepository.refreshDevices() }
                .onSuccess { _error.value = null }
                .onFailure { error ->
                    _error.value = error.message ?: "Không tải được danh sách thiết bị"
                }
            _isLoading.value = false
        }
    }

    fun revokeDevice(deviceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.revokeDevice(deviceId)
                .onSuccess {
                    _error.value = null
                    runCatching { settingsRepository.refreshDevices() }
                        .onFailure { error ->
                            _error.value = error.message ?: "Không tải được danh sách thiết bị"
                        }
                }
                .onFailure { error ->
                    _error.value = error.message ?: "Không đăng xuất được thiết bị"
                }
            _isLoading.value = false
        }
    }
}
