package com.kchat.data.repository

import kotlinx.coroutines.flow.first

class SessionCoordinator(
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
    private val realtimeCoordinator: RealtimeCoordinator,
    private val settingsRepository: SettingsRepository,
    private val localCacheCleanupScheduler: LocalCacheCleanupScheduler,
    private val pushTokenSync: PushTokenSync,
    private val pendingFcmFlush: suspend () -> Unit,
) {
    suspend fun onAuthenticated() {
        // Session row (dev:*) for X-Device-Token — separate from FCM push token.
        runCatching { settingsRepository.registerCurrentDevice() }
        runCatching { pushTokenSync.syncCurrentToken() }
        runCatching { pendingFcmFlush() }
        // FCM register may remove dev: row on older backends — restore session token.
        runCatching { settingsRepository.registerCurrentDevice() }
        runCatching { settingsRepository.refreshDevices() }
        runCatching { settingsRepository.refreshProfile() }
        runCatching { settingsRepository.refreshSettings() }
        runCatching { scheduleLocalCacheCleanup() }
        runCatching { realtimeCoordinator.connect() }
        runCatching { chatRepository.refreshRooms() }
        runCatching { contactsRepository.refreshContacts() }
    }

    /** Retry FCM registration after network recovery or app foreground. */
    suspend fun onForeground() {
        runCatching { pushTokenSync.syncCurrentToken() }
        runCatching { pendingFcmFlush() }
        runCatching { runLocalCacheCleanupNow() }
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
