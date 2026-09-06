package com.kchat.data.repository

import kotlinx.coroutines.flow.Flow

/** Suppresses re-sync of wiped local chat/contacts until logout. */
interface EmergencyWipeStore {
    val isActive: Flow<Boolean>

    /** True until logout — blocks GET rooms/messages/contacts so wiped history stays gone. */
    fun isActiveNow(): Boolean

    /** Legacy flag; new inbound WS/FCM is never blocked by wipe. */
    fun isRealtimeMuted(): Boolean

    suspend fun activate()

    /** Clears leftover realtime-mute from older builds; does not restore history. */
    suspend fun allowRealtime()

    suspend fun reset()
}
