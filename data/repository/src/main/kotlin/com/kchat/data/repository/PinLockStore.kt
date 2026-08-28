package com.kchat.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PinLockStore {
    val isEnabled: Flow<Boolean>

    val isEmergencyEnabled: Flow<Boolean>

    /** In-memory only — false again after process death. */
    val isUnlocked: StateFlow<Boolean>

    fun lock()

    suspend fun setPin(pin: String): Result<Unit>

    suspend fun verify(pin: String): PinVerifyResult

    suspend fun changePin(currentPin: String, newPin: String): Result<Unit>

    suspend fun disable(currentPin: String): Result<Unit>

    suspend fun setEmergency(currentPin: String, code: String): Result<Unit>

    suspend fun changeEmergency(currentPin: String, newCode: String): Result<Unit>

    suspend fun disableEmergency(currentPin: String): Result<Unit>

    suspend fun clear()
}
