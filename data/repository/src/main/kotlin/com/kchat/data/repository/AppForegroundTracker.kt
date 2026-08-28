package com.kchat.data.repository

/** Tracks whether the app process is in the foreground (visible to user). */
class AppForegroundTracker {
    @Volatile
    var isForeground: Boolean = true
        private set

    fun onForeground() {
        isForeground = true
    }

    fun onBackground() {
        isForeground = false
    }
}
