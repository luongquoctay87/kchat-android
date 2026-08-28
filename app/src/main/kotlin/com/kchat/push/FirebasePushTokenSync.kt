package com.kchat.push

import com.google.firebase.messaging.FirebaseMessaging
import com.kchat.BuildConfig
import com.kchat.data.repository.PushTokenSync
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirebasePushTokenSync @Inject constructor(
    private val fcmTokenHandler: FcmTokenHandler,
) : PushTokenSync {
    override suspend fun syncCurrentToken(): Boolean {
        if (!BuildConfig.FCM_ENABLED) return false
        return runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            fcmTokenHandler.onNewToken(token)
        }.getOrDefault(false)
    }
}
