package com.kchat.push

import android.util.Log
import com.kchat.data.repository.DeviceTokenStore
import com.kchat.data.repository.PushTokenRegistrar
import com.kchat.data.repository.TokenStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenHandler @Inject constructor(
    private val pushTokenRegistrar: PushTokenRegistrar,
    private val tokenStore: TokenStore,
    private val deviceTokenStore: DeviceTokenStore,
) {
    suspend fun onNewToken(token: String): Boolean {
        if (tokenStore.getAccessToken().isNullOrBlank()) {
            deviceTokenStore.savePendingFcmToken(token)
            return false
        }
        return pushTokenRegistrar.registerFcmToken(token).fold(
            onSuccess = {
                Log.i(TAG, "FCM token registered with backend: prefix=${token.take(15)}")
                true
            },
            onFailure = {
                Log.w(TAG, "FCM register failed", it)
                deviceTokenStore.savePendingFcmToken(token)
                false
            },
        )
    }

    suspend fun flushPendingToken(): Boolean {
        val pending = deviceTokenStore.consumePendingFcmToken() ?: return false
        if (tokenStore.getAccessToken().isNullOrBlank()) {
            deviceTokenStore.savePendingFcmToken(pending)
            return false
        }
        return pushTokenRegistrar.registerFcmToken(pending).fold(
            onSuccess = { true },
            onFailure = {
                Log.w(TAG, "Pending FCM register failed", it)
                deviceTokenStore.savePendingFcmToken(pending)
                false
            },
        )
    }

    companion object {
        private const val TAG = "KChatFCM"
    }
}
