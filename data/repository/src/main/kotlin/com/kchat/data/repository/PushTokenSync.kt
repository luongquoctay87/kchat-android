package com.kchat.data.repository

interface PushTokenSync {
    /** @return true when a real FCM token was registered with the server */
    suspend fun syncCurrentToken(): Boolean
}
