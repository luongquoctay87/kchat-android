package com.kchat.data.network.auth

import com.kchat.data.repository.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Clears local session when the server still returns 401 after [TokenAuthenticator] recovery.
 * Covers revoked-device sessions and expired refresh tokens without relying on callers to handle errors.
 */
class UnauthorizedSessionInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code != 401) return response
        val path = chain.request().url.encodedPath
        if (path in PUBLIC_AUTH_PATHS) return response
        runBlocking { tokenStore.clear() }
        return response
    }

    private companion object {
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
