package com.kchat.data.repository

/** Tracks which chat room is currently visible (RESUMED) for unread / notify suppression. */
class ActiveRoomTracker {
    @Volatile
    var roomId: String? = null
        private set

    fun enter(roomId: String) {
        this.roomId = roomId.trim().lowercase()
    }

    fun leave(roomId: String) {
        val key = roomId.trim().lowercase()
        if (this.roomId == key) {
            this.roomId = null
        }
    }

    fun clear() {
        roomId = null
    }

    fun isActive(roomId: String): Boolean =
        this.roomId == roomId.trim().lowercase()

    /** Suppress tray only when the user is looking at this room right now. */
    fun shouldSuppressTray(roomId: String, appForeground: Boolean): Boolean =
        appForeground && isActive(roomId)
}
