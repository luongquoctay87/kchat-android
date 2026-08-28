package com.kchat.data.fake

import com.kchat.core.model.PinRules
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.PinVerifyResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class FakePinLockStore @Inject constructor() : PinLockStore {
    private val storedPin = MutableStateFlow<String?>(null)
    private val storedEmergency = MutableStateFlow<String?>(null)
    private val _isUnlocked = MutableStateFlow(false)

    override val isEnabled: Flow<Boolean> = storedPin.map { it != null }
    override val isEmergencyEnabled: Flow<Boolean> = storedEmergency.map { it != null }
    override val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    override fun lock() {
        _isUnlocked.value = false
    }

    override suspend fun setPin(pin: String): Result<Unit> {
        PinRules.validate(pin)?.let {
            return Result.failure(IllegalArgumentException(it))
        }
        if (storedPin.value != null) {
            return Result.failure(IllegalStateException("PIN đã được đặt. Hãy đổi PIN hoặc tắt trước."))
        }
        storedPin.value = pin
        _isUnlocked.value = true
        return Result.success(Unit)
    }

    override suspend fun verify(pin: String): PinVerifyResult = when {
        storedPin.value == pin -> {
            _isUnlocked.value = true
            PinVerifyResult.Unlocked
        }
        storedEmergency.value == pin -> PinVerifyResult.EmergencyWipeMessages
        else -> PinVerifyResult.Wrong
    }

    override suspend fun changePin(currentPin: String, newPin: String): Result<Unit> {
        if (storedPin.value != currentPin) {
            return Result.failure(IllegalArgumentException("PIN hiện tại không đúng"))
        }
        PinRules.validate(newPin)?.let {
            return Result.failure(IllegalArgumentException(it))
        }
        if (currentPin == newPin) {
            return Result.failure(IllegalArgumentException("PIN mới phải khác PIN hiện tại"))
        }
        if (storedEmergency.value != null && newPin == storedEmergency.value) {
            return Result.failure(IllegalArgumentException("PIN mới phải khác mã khẩn cấp"))
        }
        storedPin.value = newPin
        _isUnlocked.value = true
        return Result.success(Unit)
    }

    override suspend fun disable(currentPin: String): Result<Unit> {
        if (storedPin.value != currentPin) {
            return Result.failure(IllegalArgumentException("PIN không đúng"))
        }
        clear()
        return Result.success(Unit)
    }

    override suspend fun setEmergency(currentPin: String, code: String): Result<Unit> {
        if (storedPin.value != currentPin) {
            return Result.failure(IllegalArgumentException("PIN hiện tại không đúng"))
        }
        PinRules.validate(code)?.let {
            return Result.failure(IllegalArgumentException(it))
        }
        PinRules.validateDistinct(currentPin, code, "Mã khẩn cấp")
            ?.let { return Result.failure(IllegalArgumentException(it)) }
        if (storedEmergency.value != null) {
            return Result.failure(IllegalStateException("Mã khẩn cấp đã được đặt. Hãy đổi hoặc tắt trước."))
        }
        storedEmergency.value = code
        return Result.success(Unit)
    }

    override suspend fun changeEmergency(currentPin: String, newCode: String): Result<Unit> {
        if (storedPin.value != currentPin) {
            return Result.failure(IllegalArgumentException("PIN hiện tại không đúng"))
        }
        if (storedEmergency.value == null) {
            return Result.failure(IllegalStateException("Chưa đặt mã khẩn cấp"))
        }
        PinRules.validate(newCode)?.let {
            return Result.failure(IllegalArgumentException(it))
        }
        PinRules.validateDistinct(storedPin.value.orEmpty(), newCode, "Mã khẩn cấp")
            ?.let { return Result.failure(IllegalArgumentException(it)) }
        storedEmergency.value = newCode
        return Result.success(Unit)
    }

    override suspend fun disableEmergency(currentPin: String): Result<Unit> {
        if (storedPin.value != currentPin) {
            return Result.failure(IllegalArgumentException("PIN hiện tại không đúng"))
        }
        storedEmergency.value = null
        return Result.success(Unit)
    }

    override suspend fun clear() {
        storedPin.value = null
        storedEmergency.value = null
        lock()
    }
}
