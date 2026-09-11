package com.kchat.data.network.auth

import com.kchat.data.repository.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Clears local session only for definitive server-side session revocation.
 *
 * Generic leftover 401s (e.g. after a transient refresh failure) must not wipe tokens —
 * [TokenAuthenticator] / [TokenRefreshCoordinator] already clear on invalid refresh.
 */
class UnauthorizedSessionInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code != 401) return response
        val path = chain.request().url.encodedPath
        if (path in PUBLIC_AUTH_PATHS) return response
        if (!isDeviceSessionRevoked(response)) return response
        runBlocking { tokenStore.clear() }
        return response
    }

    private fun isDeviceSessionRevoked(response: Response): Boolean {
        return runCatching {
            response.peekBody(DEVICE_REVOKED_PEEK_BYTES).string().contains(DEVICE_REVOKED_MESSAGE)
        }.getOrDefault(false)
    }

    private companion object {
        const val DEVICE_REVOKED_MESSAGE = "Device session revoked"
        const val DEVICE_REVOKED_PEEK_BYTES = 512L
        val PUBLIC_AUTH_PATHS = setOf(
            "/auth/send-registration-otp",
            "/auth/verify-registration-otp",
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
