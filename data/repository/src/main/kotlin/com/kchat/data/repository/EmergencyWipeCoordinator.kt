package com.kchat.data.repository

/** Orchestrates local + server cleanup after emergency PIN entry. */
interface EmergencyWipeCoordinator {
    /**
     * Clears local chat/contacts, best-effort server wipe, enters restricted UI.
     * Always activates suppression even if the network call fails.
     */
    suspend fun execute()

    /**
     * Ends wipe suppression after the user intentionally opens a chat again,
     * then re-syncs rooms/contacts so send/receive works normally.
     */
    suspend fun clearAfterReengage()
}
