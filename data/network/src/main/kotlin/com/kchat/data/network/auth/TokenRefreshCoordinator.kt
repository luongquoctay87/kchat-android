package com.kchat.data.network.auth

import com.kchat.core.model.AuthTokens
import com.kchat.data.network.dto.AuthResponse
import com.kchat.data.network.dto.RefreshRequest
import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.DeviceTokenStore
import com.kchat.data.repository.TokenStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Outcome of a coordinated access-token refresh. */
sealed class TokenRefreshResult {
    data class Success(val accessToken: String) : TokenRefreshResult()

    /** Network / 5xx / rate-limit — keep the local session. */
    data object TransientFailure : TokenRefreshResult()

    /** Refresh rejected or device revoked — local session already cleared. */
    data object SessionInvalid : TokenRefreshResult()

    data object NoTokens : TokenRefreshResult()
}

/**
 * Single serialized refresh path shared by [TokenAuthenticator] and WebSocket reconnect.
 * Prevents single-use refresh-token rotation races that force unexpected logout.
 */
@Singleton
class TokenRefreshCoordinator @Inject constructor(
    private val tokenStore: TokenStore,
    private val accessTokenHolder: AccessTokenHolder,
    private val deviceTokenStore: DeviceTokenStore,
    @Named("apiBaseUrl") private val apiBaseUrl: String,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val mutex = Mutex()
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Refreshes the access token. Concurrent callers wait on one in-flight refresh.
     *
     * @param staleAccessToken When set (HTTP 401 retry), skip the network call if another
     *   caller already stored a different access token.
     */
    suspend fun refresh(staleAccessToken: String? = null): TokenRefreshResult = mutex.withLock {
        var currentAccess = accessTokenHolder.accessToken
        if (currentAccess.isNullOrBlank()) {
            currentAccess = tokenStore.getAccessToken()
        }
        if (!staleAccessToken.isNullOrBlank() &&
            !currentAccess.isNullOrBlank() &&
            currentAccess != staleAccessToken
        ) {
            return@withLock TokenRefreshResult.Success(currentAccess)
        }
        if (staleAccessToken == null &&
            !currentAccess.isNullOrBlank() &&
            !JwtPayload.isExpiredOrExpiring(currentAccess)
        ) {
            return@withLock TokenRefreshResult.Success(currentAccess)
        }

        val tokens = tokenStore.currentTokens() ?: return@withLock TokenRefreshResult.NoTokens
        val refreshUrl = apiBaseUrl.trimEnd('/') + "/auth/refresh"
        val bodyJson = json.encodeToString(RefreshRequest(tokens.refreshToken))
        val refreshRequestBuilder = Request.Builder()
            .url(refreshUrl)
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
        val deviceToken = runCatching { deviceTokenStore.getOrCreate() }.getOrNull()
        if (!deviceToken.isNullOrBlank()) {
            refreshRequestBuilder.header("X-Device-Token", deviceToken)
        }

        return@withLock try {
            refreshClient.newCall(refreshRequestBuilder.build()).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) {
                    // Only drop session when refresh token is rejected, not on network/5xx/429
                    if (refreshResponse.code == 401 || refreshResponse.code == 403) {
                        tokenStore.clear()
                        TokenRefreshResult.SessionInvalid
                    } else {
                        TokenRefreshResult.TransientFailure
                    }
                } else {
                    val responseBody = refreshResponse.body?.string().orEmpty()
                    val auth = runCatching {
                        json.decodeFromString<AuthResponse>(responseBody)
                    }.getOrNull()
                    if (auth == null) {
                        TokenRefreshResult.TransientFailure
                    } else {
                        tokenStore.saveTokens(AuthTokens(auth.accessToken, auth.refreshToken))
                        TokenRefreshResult.Success(auth.accessToken)
                    }
                }
            }
        } catch (_: Exception) {
            TokenRefreshResult.TransientFailure
        }
    }

    suspend fun clearSession() {
        tokenStore.clear()
    }
}
