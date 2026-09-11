package com.kchat.data.network.auth

import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.DeviceTokenStore
import com.kchat.data.repository.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/** Attaches Bearer access token. Skips public auth endpoints only. */
class JwtAuthInterceptor(
    private val accessTokenHolder: AccessTokenHolder,
    private val tokenStore: TokenStore,
    private val deviceTokenStore: DeviceTokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        if (request.url.encodedPath !in PUBLIC_AUTH_PATHS) {
            var token = accessTokenHolder.accessToken
            if (token.isNullOrBlank()) {
                // Cold start race: holder may not be warmed yet.
                token = runBlocking { tokenStore.getAccessToken() }
            }
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }
        val deviceToken = runBlocking { runCatching { deviceTokenStore.getOrCreate() }.getOrNull() }
        if (!deviceToken.isNullOrBlank()) {
            builder.header("X-Device-Token", deviceToken)
        }
        return chain.proceed(builder.build())
    }

    private companion object {
        val PUBLIC_AUTH_PATHS = setOf(
            "/auth/send-registration-otp",
            "/auth/verify-registration-otp",
            "/auth/check-username",
            "/auth/check-email",
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/auth/logout",
            "/auth/forgot-password",
            "/auth/verify-reset-otp",
            "/auth/reset-password",
        )
    }
}
