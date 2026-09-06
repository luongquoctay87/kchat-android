package com.kchat.data.network.emergency

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kchat.data.network.auth.ReplaceEmptyPreferences
import com.kchat.data.repository.EmergencyWipeStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val Context.emergencyWipeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "kchat_emergency_wipe",
    corruptionHandler = ReplaceEmptyPreferences,
)

@Singleton
class DataStoreEmergencyWipeStore @Inject constructor(
    @ApplicationContext context: Context,
) : EmergencyWipeStore {
    private val dataStore = context.emergencyWipeDataStore
    private val activeKey = booleanPreferencesKey("active")
    private val realtimeMutedKey = booleanPreferencesKey("realtime_muted")
    private val epochKey = intPreferencesKey("epoch")
    private val appliedEpoch = AtomicInteger(-1)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cached = MutableStateFlow(false)
    private val realtimeMuted = MutableStateFlow(false)

    override val isActive: Flow<Boolean> = cached.asStateFlow()

    init {
        scope.launch {
            dataStore.data.collect { prefs ->
                applyPrefs(
                    epoch = prefs[epochKey] ?: 0,
                    history = prefs[activeKey] == true,
                    muted = prefs[realtimeMutedKey] == true,
                )
            }
        }
    }

    override fun isActiveNow(): Boolean = cached.value

    override fun isRealtimeMuted(): Boolean = realtimeMuted.value

    override suspend fun activate() {
        var epoch = 0
        dataStore.edit {
            epoch = (it[epochKey] ?: 0) + 1
            it[epochKey] = epoch
            it[activeKey] = true
            it[realtimeMutedKey] = false
        }
        applyPrefs(epoch, history = true, muted = false)
    }

    override suspend fun allowRealtime() {
        var epoch = 0
        dataStore.edit {
            epoch = (it[epochKey] ?: 0) + 1
            it[epochKey] = epoch
            it[realtimeMutedKey] = false
        }
        applyPrefs(epoch, history = cached.value, muted = false)
    }

    override suspend fun reset() {
        var epoch = 0
        dataStore.edit {
            epoch = (it[epochKey] ?: 0) + 1
            it[epochKey] = epoch
            it.remove(activeKey)
            it.remove(realtimeMutedKey)
        }
        applyPrefs(epoch, history = false, muted = false)
    }

    private fun applyPrefs(epoch: Int, history: Boolean, muted: Boolean) {
        while (true) {
            val current = appliedEpoch.get()
            if (epoch < current) return
            if (appliedEpoch.compareAndSet(current, epoch)) {
                cached.value = history
                realtimeMuted.value = muted
                return
            }
        }
    }
}
