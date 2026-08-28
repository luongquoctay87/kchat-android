package com.kchat.data.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>

    suspend fun login(identifier: String, password: String): Result<Unit>

    suspend fun sendRegistrationOtp(email: String): Result<Unit>

    suspend fun verifyRegistrationOtp(email: String, otp: String): Result<String>

    suspend fun register(
        registrationToken: String,
        displayName: String,
        email: String,
        username: String,
        password: String,
    ): Result<Unit>

    suspend fun forgotPassword(email: String): Result<Unit>

    suspend fun verifyResetOtp(email: String, otp: String): Result<String>

    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    suspend fun logout()
}
