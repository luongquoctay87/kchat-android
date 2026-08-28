package com.kchat.data.network.emergency

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.kchat.data.repository.EmergencyWipeStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.emergencyWipeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "kchat_emergency_wipe",
)

@Singleton
class DataStoreEmergencyWipeStore @Inject constructor(
    @ApplicationContext context: Context,
) : EmergencyWipeStore {
    private val dataStore = context.emergencyWipeDataStore
    private val activeKey = booleanPreferencesKey("active")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cached = MutableStateFlow(false)

    override val isActive: Flow<Boolean> = cached.asStateFlow()

    init {
        scope.launch {
            dataStore.data.map { prefs -> prefs[activeKey] == true }.collect { cached.value = it }
        }
    }

    override fun isActiveNow(): Boolean = cached.value

    override suspend fun activate() {
        dataStore.edit { it[activeKey] = true }
        cached.value = true
    }

    override suspend fun reset() {
        dataStore.edit { it.remove(activeKey) }
        cached.value = false
    }
}
