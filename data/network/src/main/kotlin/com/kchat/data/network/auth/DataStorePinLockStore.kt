package com.kchat.data.network.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kchat.core.model.PinRules
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.PinVerifyResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.pinLockDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "kchat_pin_lock",
    corruptionHandler = ReplaceEmptyPreferences,
)

@Singleton
class DataStorePinLockStore @Inject constructor(
    @ApplicationContext context: Context,
) : PinLockStore {
    private val dataStore = context.pinLockDataStore
    private val saltKey = stringPreferencesKey("pin_salt")
    private val hashKey = stringPreferencesKey("pin_hash")
    private val emergencySaltKey = stringPreferencesKey("emergency_salt")
    private val emergencyHashKey = stringPreferencesKey("emergency_hash")

    private val _isUnlocked = MutableStateFlow(false)
    override val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    override val isEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[hashKey].isNullOrBlank() && !prefs[saltKey].isNullOrBlank()
    }

    override val isEmergencyEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[emergencyHashKey].isNullOrBlank() && !prefs[emergencySaltKey].isNullOrBlank()
    }

    override fun lock() {
        _isUnlocked.value = false
    }

    override suspend fun setPin(pin: String): Result<Unit> {
        PinRules.validate(pin)?.let { return Result.failure(IllegalArgumentException(it)) }
        val prefs = dataStore.data.first()
        if (!prefs[hashKey].isNullOrBlank()) {
            return Result.failure(IllegalStateException("PIN đã được đặt. Hãy đổi PIN hoặc tắt trước."))
        }
        persistPin(pin)
        _isUnlocked.value = true
        return Result.success(Unit)
    }

    override suspend fun verify(pin: String): PinVerifyResult {
        if (matchesPin(pin)) {
            _isUnlocked.value = true
            return PinVerifyResult.Unlocked
        }
        if (matchesEmergency(pin)) {
            return PinVerifyResult.EmergencyWipeMessages
        }
        return PinVerifyResult.Wrong
    }

    override suspend fun changePin(currentPin: String, newPin: String): Result<Unit> {
        if (!matchesPin(currentPin)) {
            return Result.failure(IllegalArgumentException("PIN hiện tại không đúng"))
        }
        PinRules.validate(newPin)?.let { return Result.failure(IllegalArgumentException(it)) }
        if (currentPin == newPin) {
            return Result.failure(IllegalArgumentException("PIN mới phải khác PIN hiện tại"))
        }
        if (matchesEmergency(newPin)) {
            return Result.failure(IllegalArgumentException("PIN mới phải khác mã khẩn cấp"))
        }
        persistPin(newPin)
        _isUnlocked.value = true
        return Result.success(Unit)
    }

    override suspend fun disable(currentPin: String): Result<Unit> {
        if (!matchesPin(currentPin)) {
            return Result.failure(IllegalArgumentException("PIN không đúng"))
        }
        clear()
        return Result.success(Unit)
    }

    override suspend fun setEmergency(currentPin: String, code: String): Result<Unit> {
        if (!matchesPin(currentPin)) {
            return Result.failure(IllegalArgumentException("PIN hiện tại không đúng"))
        }
        PinRules.validate(code)?.let { return Result.failure(IllegalArgumentException(it)) }
        PinRules.validateDistinct(currentPin, code, "Mã khẩn cấp")
            ?.let { return Result.failure(IllegalArgumentException(it)) }
        val prefs = dataStore.data.first()
        if (!prefs[emergencyHashKey].isNullOrBlank()) {
            return Result.failure(IllegalStateException("Mã khẩn cấp đã được đặt. Hãy đổi hoặc tắt trước."))
        }
        persistEmergency(code)
        return Result.success(Unit)
    }

    override suspend fun changeEmergency(currentPin: String, newCode: String): Result<Unit> {
        if (!matchesPin(currentPin)) {
            return Result.failure(IllegalArgumentException("PIN hiện tại không đúng"))
        }
        if (!matchesEmergencyStored()) {
            return Result.failure(IllegalStateException("Chưa đặt mã khẩn cấp"))
        }
        PinRules.validate(newCode)?.let { return Result.failure(IllegalArgumentException(it)) }
        if (matchesPin(newCode)) {
            return Result.failure(IllegalArgumentException("Mã khẩn cấp phải khác mã PIN"))
        }
        persistEmergency(newCode)
        return Result.success(Unit)
    }

    override suspend fun disableEmergency(currentPin: String): Result<Unit> {
        if (!matchesPin(currentPin)) {
            return Result.failure(IllegalArgumentException("PIN hiện tại không đúng"))
        }
        dataStore.edit { prefs ->
            prefs.remove(emergencySaltKey)
            prefs.remove(emergencyHashKey)
        }
        return Result.success(Unit)
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
        lock()
    }

    private suspend fun matchesPin(pin: String): Boolean {
        val prefs = dataStore.data.first()
        val salt = prefs[saltKey] ?: return false
        val storedHex = prefs[hashKey] ?: return false
        val expected = storedHex.hexToBytes() ?: return false
        return MessageDigest.isEqual(hash(pin, salt), expected)
    }

    private suspend fun matchesEmergency(pin: String): Boolean {
        if (!matchesEmergencyStored()) return false
        val prefs = dataStore.data.first()
        val salt = prefs[emergencySaltKey] ?: return false
        val storedHex = prefs[emergencyHashKey] ?: return false
        val expected = storedHex.hexToBytes() ?: return false
        return MessageDigest.isEqual(hash(pin, salt), expected)
    }

    private suspend fun matchesEmergencyStored(): Boolean {
        val prefs = dataStore.data.first()
        return !prefs[emergencyHashKey].isNullOrBlank() && !prefs[emergencySaltKey].isNullOrBlank()
    }

    private suspend fun persistPin(pin: String) {
        val salt = newSalt()
        dataStore.edit { prefs ->
            prefs[saltKey] = salt
            prefs[hashKey] = hash(pin, salt).toHex()
        }
    }

    private suspend fun persistEmergency(code: String) {
        val salt = newSalt()
        dataStore.edit { prefs ->
            prefs[emergencySaltKey] = salt
            prefs[emergencyHashKey] = hash(code, salt).toHex()
        }
    }

    companion object {
        private fun newSalt(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.toHex()
        }

        private fun hash(pin: String, salt: String): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
        }

        private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

        private fun String.hexToBytes(): ByteArray? {
            if (length % 2 != 0) return null
            return runCatching {
                ByteArray(length / 2) { i ->
                    substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
            }.getOrNull()
        }
    }
}
