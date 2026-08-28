package com.kchat.data.fake

import com.kchat.core.model.AuthTokens
import com.kchat.core.model.PasswordRules
import com.kchat.data.repository.AuthRepository
import com.kchat.data.repository.ContactsRepository
import com.kchat.data.repository.EmergencyWipeStore
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.SettingsRepository
import com.kchat.data.repository.TokenStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor(
    private val tokenStore: TokenStore,
    private val settingsRepository: SettingsRepository,
    private val pinLockStore: PinLockStore,
    private val contactsRepository: ContactsRepository,
    private val emergencyWipeStore: EmergencyWipeStore,
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = tokenStore.tokens.map { it != null }

    override suspend fun login(identifier: String, password: String): Result<Unit> {
        delay(400)
        if (identifier.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập email và mật khẩu"))
        }
        tokenStore.saveTokens(AuthTokens("fake-access", "fake-refresh"))
        return Result.success(Unit)
    }

    override suspend fun sendRegistrationOtp(email: String): Result<Unit> {
        delay(300)
        return if (email.isBlank()) {
            Result.failure(IllegalArgumentException("Vui lòng nhập email"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun verifyRegistrationOtp(email: String, otp: String): Result<String> {
        delay(300)
        return when {
            email.isBlank() -> Result.failure(IllegalArgumentException("Thiếu email"))
            otp.length != 6 -> Result.failure(IllegalArgumentException("Mã OTP phải có 6 số"))
            otp == "000000" -> Result.failure(IllegalArgumentException("Mã OTP không đúng hoặc đã hết hạn"))
            else -> Result.success("fake-registration-token")
        }
    }

    override suspend fun register(
        registrationToken: String,
        displayName: String,
        email: String,
        username: String,
        password: String,
    ): Result<Unit> {
        delay(400)
        if (registrationToken.isBlank() || displayName.isBlank() || email.isBlank() || username.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng điền đầy đủ thông tin"))
        }
        if (!PasswordRules.isStrong(password)) {
            return Result.failure(IllegalArgumentException(PasswordRules.HINT))
        }
        tokenStore.saveTokens(AuthTokens("fake-access", "fake-refresh"))
        return Result.success(Unit)
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        delay(300)
        return if (email.isBlank()) {
            Result.failure(IllegalArgumentException("Vui lòng nhập email"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun verifyResetOtp(email: String, otp: String): Result<String> {
        delay(300)
        return when {
            email.isBlank() -> Result.failure(IllegalArgumentException("Thiếu email"))
            otp.length != 6 -> Result.failure(IllegalArgumentException("Mã OTP phải có 6 số"))
            otp == "000000" -> Result.failure(IllegalArgumentException("Mã OTP không đúng hoặc đã hết hạn"))
            else -> Result.success("fake-reset-token")
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        delay(300)
        return PasswordRules.validate(newPassword)
            ?.let { Result.failure(IllegalArgumentException(it)) }
            ?: Result.success(Unit)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        delay(300)
        val strengthError = PasswordRules.validate(newPassword)
        return when {
            currentPassword.isBlank() -> Result.failure(IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại"))
            strengthError != null -> Result.failure(IllegalArgumentException(strengthError))
            currentPassword == "wrong" ->
                Result.failure(IllegalArgumentException("Mật khẩu hiện tại không đúng"))
            else -> Result.success(Unit)
        }
    }

    override suspend fun logout() {
        tokenStore.clear()
        settingsRepository.clearLocal()
        pinLockStore.clear()
        emergencyWipeStore.reset()
        contactsRepository.restoreAfterLogout()
    }
}
