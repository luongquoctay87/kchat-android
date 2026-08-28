package com.kchat.data.network.auth

import com.kchat.core.model.AuthTokens
import com.kchat.data.network.dto.AuthResponse
import com.kchat.data.network.dto.RefreshRequest
import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.DeviceTokenStore
import com.kchat.data.repository.TokenStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

/**
 * On HTTP 401, refreshes access token once and retries the original request.
 * Uses a plain OkHttpClient (no authenticator) to avoid refresh loops.
 */
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val accessTokenHolder: AccessTokenHolder,
    private val deviceTokenStore: DeviceTokenStore,
    private val apiBaseUrl: String,
) : Authenticator {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            runBlocking { tokenStore.clear() }
            return null
        }
        if (response.code == 401 && isDeviceSessionRevoked(response)) {
            runBlocking { tokenStore.clear() }
            return null
        }
        val path = response.request.url.encodedPath
        if (path.startsWith("/auth/")) return null

        synchronized(this) {
            val currentAccess = accessTokenHolder.accessToken
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()
            // Another thread may have already refreshed
            if (!currentAccess.isNullOrBlank() &&
                !requestToken.isNullOrBlank() &&
                currentAccess != requestToken
            ) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccess")
                    .build()
            }

            val tokens = runBlocking { tokenStore.currentTokens() } ?: return null
            val refreshUrl = apiBaseUrl.trimEnd('/') + "/auth/refresh"
            val bodyJson = json.encodeToString(RefreshRequest(tokens.refreshToken))

            val refreshRequestBuilder = Request.Builder()
                .url(refreshUrl)
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
            val deviceToken = runBlocking { runCatching { deviceTokenStore.getOrCreate() }.getOrNull() }
            if (!deviceToken.isNullOrBlank()) {
                refreshRequestBuilder.header("X-Device-Token", deviceToken)
            }
            val refreshRequest = refreshRequestBuilder.build()

            return refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) {
                    // Only drop session when refresh token is rejected, not on network/5xx
                    if (refreshResponse.code == 401 || refreshResponse.code == 403) {
                        runBlocking { tokenStore.clear() }
                    }
                    return null
                }
                val responseBody = refreshResponse.body?.string().orEmpty()
                val auth = runCatching { json.decodeFromString<AuthResponse>(responseBody) }.getOrNull()
                    ?: return null

                runBlocking {
                    tokenStore.saveTokens(AuthTokens(auth.accessToken, auth.refreshToken))
                }

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${auth.accessToken}")
                    .build()
            }
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
