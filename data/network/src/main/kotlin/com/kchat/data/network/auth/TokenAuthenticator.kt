package com.kchat.data.network.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * On HTTP 401, refreshes access token once via [TokenRefreshCoordinator] and retries.
 */
class TokenAuthenticator(
    private val tokenRefreshCoordinator: TokenRefreshCoordinator,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            runBlocking { tokenRefreshCoordinator.clearSession() }
            return null
        }
        if (response.code == 401 && isDeviceSessionRevoked(response)) {
            runBlocking { tokenRefreshCoordinator.clearSession() }
            return null
        }
        val path = response.request.url.encodedPath
        if (path.startsWith("/auth/")) return null

        val requestToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        return when (val result = runBlocking { tokenRefreshCoordinator.refresh(requestToken) }) {
            is TokenRefreshResult.Success -> response.request.newBuilder()
                .header("Authorization", "Bearer ${result.accessToken}")
                .build()
            is TokenRefreshResult.SessionInvalid,
            is TokenRefreshResult.TransientFailure,
            is TokenRefreshResult.NoTokens,
            -> null
        }
    }

    private fun isDeviceSessionRevoked(response: Response): Boolean {
        return runCatching {
            response.peekBody(DEVICE_REVOKED_PEEK_BYTES).string().contains(DEVICE_REVOKED_MESSAGE)
        }.getOrDefault(false)
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val DEVICE_REVOKED_MESSAGE = "Device session revoked"
        const val DEVICE_REVOKED_PEEK_BYTES = 512L
    }
}
