package com.kchat.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Peer typing indicators per room. Entries auto-clear after [DEFAULT_TTL_MS]
 * so the UI drops "đang nhập..." even if a stop event is lost.
 */
class TypingStateStore {
    private val typingRooms = MutableStateFlow<Set<String>>(emptySet())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val clearJobs = ConcurrentHashMap<String, Job>()

    /** All rooms currently showing a peer typing indicator. */
    fun observeTypingRooms(): Flow<Set<String>> = typingRooms

    fun observeTyping(roomId: String): Flow<Boolean> {
        val key = roomId.trim().lowercase()
        return typingRooms.map { key in it }.distinctUntilChanged()
    }

    fun setTyping(roomId: String, typing: Boolean, ttlMs: Long = DEFAULT_TTL_MS) {
        val key = roomId.trim().lowercase()
        if (key.isBlank()) return
        clearJobs.remove(key)?.cancel()
        if (!typing) {
            typingRooms.update { it - key }
            return
        }
        typingRooms.update { it + key }
        clearJobs[key] = scope.launch {
            delay(ttlMs)
            typingRooms.update { rooms -> rooms - key }
            clearJobs.remove(key)
        }
    }

    fun clear(roomId: String) {
        setTyping(roomId.trim().lowercase(), false)
    }

    companion object {
        const val DEFAULT_TTL_MS = 5_000L
    }
}
