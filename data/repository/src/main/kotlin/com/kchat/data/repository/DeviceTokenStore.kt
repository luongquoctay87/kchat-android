package com.kchat.data.repository

/** Stable per-install token sent as {@code X-Device-Token} and registered as FCM token surrogate. */
interface DeviceTokenStore {
    suspend fun getOrCreate(): String

    suspend fun savePendingFcmToken(token: String)

    suspend fun consumePendingFcmToken(): String?
}
