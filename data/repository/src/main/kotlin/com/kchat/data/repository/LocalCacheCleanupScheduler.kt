package com.kchat.data.repository

import com.kchat.core.model.MessageStorageOptions

/** Schedules periodic local Room cache cleanup (#20). */
interface LocalCacheCleanupScheduler {
    /** Schedule daily cleanup; pass null to cancel. */
    fun schedule(retentionDays: Int?)

    /** Run cleanup once (e.g. after login, foreground, or settings change). */
    fun runNow(retentionDays: Int?)

    fun scheduleForSettings(retentionDays: Int?) {
        val days = MessageStorageOptions.resolveRetentionDays(retentionDays)
        schedule(days)
        runNow(days)
    }

    companion object {
        /** @see MessageStorageOptions.DEFAULT_RETENTION_DAYS */
        const val DEFAULT_RETENTION_DAYS = MessageStorageOptions.DEFAULT_RETENTION_DAYS
    }
}
