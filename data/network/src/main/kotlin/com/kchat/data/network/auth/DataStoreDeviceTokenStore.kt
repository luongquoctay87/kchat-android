package com.kchat.data.network.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kchat.data.repository.DeviceTokenStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.deviceTokenDataStore by preferencesDataStore(name = "kchat_device")

@Singleton
class DataStoreDeviceTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) : DeviceTokenStore {
    private val dataStore = context.deviceTokenDataStore
    private val tokenKey = stringPreferencesKey("install_token")
    private val pendingFcmKey = stringPreferencesKey("pending_fcm_token")

    override suspend fun getOrCreate(): String {
        val existing = dataStore.data.first()[tokenKey]
        if (!existing.isNullOrBlank()) return existing
        val created = "dev:${UUID.randomUUID()}"
        dataStore.edit { it[tokenKey] = created }
        return created
    }

    override suspend fun savePendingFcmToken(token: String) {
        dataStore.edit { it[pendingFcmKey] = token.trim() }
    }

    override suspend fun consumePendingFcmToken(): String? {
        val pending = dataStore.data.first()[pendingFcmKey]?.trim().orEmpty()
        if (pending.isBlank()) return null
        dataStore.edit { it.remove(pendingFcmKey) }
        return pending
    }
}
