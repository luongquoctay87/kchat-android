package com.kchat.data.repository

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class SessionCoordinator(
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
    private val realtimeCoordinator: RealtimeCoordinator,
    private val settingsRepository: SettingsRepository,
    private val localCacheCleanupScheduler: LocalCacheCleanupScheduler,
    private val pushTokenSync: PushTokenSync,
    private val pendingFcmFlush: suspend () -> Boolean,
) {
    suspend fun onAuthenticated() {
        // Connect real-time WS immediately so calls & messages are active right away
        runCatching { realtimeCoordinator.connect() }
        // Session row (dev:*) for X-Device-Token — separate from FCM push token.
        runCatching { settingsRepository.registerCurrentDevice() }
        runCatching { ensurePushTokenRegistered(maxAttempts = 5) }
        runCatching { settingsRepository.refreshDevices() }
        runCatching { settingsRepository.refreshProfile() }
        runCatching { settingsRepository.refreshSettings() }
        runCatching { scheduleLocalCacheCleanup() }
        runCatching { chatRepository.refreshRooms() }
        runCatching { contactsRepository.refreshContacts() }
    }

    /** Retry FCM registration and re-establish real-time connection after network recovery or app foreground. */
    suspend fun onForeground() {
        runCatching { realtimeCoordinator.connect() }
        runCatching { ensurePushTokenRegistered(maxAttempts = 3) }
        runCatching { runLocalCacheCleanupNow() }
        runCatching { chatRepository.refreshRooms() }
        runCatching { contactsRepository.refreshContacts() }
    }

    /**
     * Fresh installs often need a retry: Play Services / FCM token can arrive slightly
     * after the first login bootstrap, and until a real token is POSTed the backend
     * only has a `dev:` row (skipped for push).
     */
    private suspend fun ensurePushTokenRegistered(maxAttempts: Int = 5) {
        repeat(maxAttempts) { attempt ->
            val synced = runCatching { pushTokenSync.syncCurrentToken() }.getOrDefault(false)
            val flushed = runCatching { pendingFcmFlush() }.getOrDefault(false)
            if (synced || flushed) {
                Log.i("SessionCoordinator", "Push token registered successfully (attempt ${attempt + 1})")
                return
            }
            if (attempt < maxAttempts - 1) {
                delay(800L * (attempt + 1))
            }
        }
        Log.w("SessionCoordinator", "Push token registration failed after $maxAttempts attempts")
    }

    private suspend fun scheduleLocalCacheCleanup() {
        val days = settingsRepository.settings.first()?.localCacheRetentionDays
        localCacheCleanupScheduler.scheduleForSettings(days)
    }

    private suspend fun runLocalCacheCleanupNow() {
        val days = settingsRepository.settings.first()?.localCacheRetentionDays
        localCacheCleanupScheduler.runNow(
            days ?: LocalCacheCleanupScheduler.DEFAULT_RETENTION_DAYS,
        )
    }
}
