package com.kchat.data.repository

/** Tracks which chat room is currently open (for unread suppression). */
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
}
