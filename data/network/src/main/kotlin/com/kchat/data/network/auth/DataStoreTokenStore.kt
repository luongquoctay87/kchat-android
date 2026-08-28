package com.kchat.data.network.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kchat.core.model.AuthTokens
import com.kchat.data.network.auth.JwtPayload
import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.TokenStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "kchat_tokens")

@Singleton
class DataStoreTokenStore @Inject constructor(
    @ApplicationContext context: Context,
    private val accessTokenHolder: AccessTokenHolder,
) : TokenStore {
    private val dataStore = context.tokenDataStore

    private val accessKey = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")

    override val tokens: Flow<AuthTokens?> = dataStore.data.map { prefs ->
        val access = prefs[accessKey]
        val refresh = prefs[refreshKey]
        if (access != null && refresh != null) AuthTokens(access, refresh) else null
    }.onEach { tokens ->
        accessTokenHolder.set(tokens?.accessToken, JwtPayload.subject(tokens?.accessToken))
    }

    override suspend fun currentTokens(): AuthTokens? {
        val prefs = dataStore.data.first()
        val access = prefs[accessKey] ?: return null
        val refresh = prefs[refreshKey] ?: return null
        return AuthTokens(access, refresh)
    }

    override suspend fun getAccessToken(): String? {
        val token = dataStore.data.first()[accessKey]
        accessTokenHolder.set(token, JwtPayload.subject(token))
        return token
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        dataStore.edit { prefs ->
            prefs[accessKey] = tokens.accessToken
            prefs[refreshKey] = tokens.refreshToken
        }
        accessTokenHolder.set(tokens.accessToken, JwtPayload.subject(tokens.accessToken))
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
        accessTokenHolder.set(null, null)
    }
}
