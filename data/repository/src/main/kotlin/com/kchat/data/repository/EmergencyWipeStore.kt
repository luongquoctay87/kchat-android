package com.kchat.data.repository

import kotlinx.coroutines.flow.Flow

/** Suppresses re-sync of wiped local chat/contacts until logout. */
interface EmergencyWipeStore {
    val isActive: Flow<Boolean>

    fun isActiveNow(): Boolean

    suspend fun activate()

    suspend fun reset()
}
