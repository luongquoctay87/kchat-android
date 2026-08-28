package com.kchat.data.fake

import com.kchat.core.model.AuthTokens
import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.PushTokenRegistrar
import com.kchat.data.repository.RealtimeCoordinator
import com.kchat.data.repository.TokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTokenStore @Inject constructor(
    private val accessTokenHolder: AccessTokenHolder,
) : TokenStore {
    private val state = MutableStateFlow<AuthTokens?>(null)

    override val tokens: Flow<AuthTokens?> = state.asStateFlow()

    override suspend fun currentTokens(): AuthTokens? = state.value

    override suspend fun getAccessToken(): String? = state.value?.accessToken

    override suspend fun saveTokens(tokens: AuthTokens) {
        state.value = tokens
        accessTokenHolder.set(tokens.accessToken)
    }

    override suspend fun clear() {
        state.value = null
        accessTokenHolder.set(null)
    }
}

@Singleton
class FakePushTokenRegistrar @Inject constructor() : PushTokenRegistrar {
    override suspend fun registerFcmToken(token: String): Result<Unit> = Result.success(Unit)
}

@Singleton
class FakeRealtimeCoordinator @Inject constructor() : RealtimeCoordinator {
    override fun connect() = Unit

    override fun disconnect() = Unit

    override fun sendTyping(roomId: String, typing: Boolean) = Unit

    override fun sendIceOffer(callId: String, sdp: String) = Unit

    override fun sendIceAnswer(callId: String, sdp: String) = Unit

    override fun sendIceCandidate(
        callId: String,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int?,
    ) = Unit
}
