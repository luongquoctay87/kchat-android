package com.kchat.data.repository

import com.kchat.core.model.AuthTokens
import kotlinx.coroutines.flow.Flow

interface TokenStore {
    val tokens: Flow<AuthTokens?>

    suspend fun currentTokens(): AuthTokens?

    suspend fun getAccessToken(): String?

    suspend fun saveTokens(tokens: AuthTokens)

    suspend fun clear()
}
