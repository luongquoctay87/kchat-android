package com.kchat.data.repository

import android.os.SystemClock

/**
 * Suppresses PIN lock while the app briefly leaves for a system UI we opened
 * (photo/file picker, permission dialog). Uses a deadline so a missed [end]
 * cannot leave the app unlocked forever.
 */
class PinLockTransientLeave {
    @Volatile
    private var suppressUntilElapsedMs: Long = 0L

    fun begin(ttlMs: Long = DEFAULT_TTL_MS) {
        val until = SystemClock.elapsedRealtime() + ttlMs.coerceAtLeast(1L)
        // Nested begins extend the window; never shrink an existing longer one.
        if (until > suppressUntilElapsedMs) {
            suppressUntilElapsedMs = until
        }
    }

    fun end() {
        suppressUntilElapsedMs = 0L
    }

    fun isActive(): Boolean = SystemClock.elapsedRealtime() < suppressUntilElapsedMs

    companion object {
        const val DEFAULT_TTL_MS: Long = 60_000L
    }
}
