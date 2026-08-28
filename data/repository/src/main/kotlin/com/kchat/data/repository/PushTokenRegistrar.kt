package com.kchat.data.repository

interface PushTokenRegistrar {
    suspend fun registerFcmToken(token: String): Result<Unit>
}
