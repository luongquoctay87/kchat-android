package com.kchat.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PushRoomTarget(
    val roomId: String,
    val title: String,
)

/** Holds a pending navigation target from a push notification tap. */
class PushNavigationStore {
    private val _pending = MutableStateFlow<PushRoomTarget?>(null)
    val pending: StateFlow<PushRoomTarget?> = _pending.asStateFlow()

    fun requestNavigation(roomId: String, title: String) {
        _pending.value = PushRoomTarget(roomId = roomId, title = title)
    }

    fun consume() {
        _pending.value = null
    }
}
