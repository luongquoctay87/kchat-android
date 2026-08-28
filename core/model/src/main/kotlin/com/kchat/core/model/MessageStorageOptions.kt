package com.kchat.core.model

/** Shared message storage limits — mirrors backend `MessageStorageOptions`. */
object MessageStorageOptions {
    const val DAY_MILLIS = 24L * 60L * 60L * 1000L

    val CACHE_RETENTION_DAYS = setOf(7, 30, 90)

    const val DEFAULT_RETENTION_DAYS = 30

    /** 0 clears default disappearing (stored as NULL). */
    const val CLEAR_DEFAULT_DISAPPEARING = 0

    val DEFAULT_DISAPPEARING_SECONDS = setOf(
        86_400,
        604_800,
        2_592_000,
        7_776_000,
    )

    fun isValidCacheRetention(days: Int?): Boolean =
        days != null && days in CACHE_RETENTION_DAYS

    fun resolveRetentionDays(days: Int?): Int =
        days?.takeIf { isValidCacheRetention(it) } ?: DEFAULT_RETENTION_DAYS
}
