package com.kchat.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PushRoomTarget(
    val roomId: String,
    val title: String,
)

data class PushCallTarget(
    val callId: String,
    val roomId: String,
    val callerName: String,
    val callType: String,
)

/** Holds a pending navigation target from a push notification tap. */
class PushNavigationStore {
    private val _pending = MutableStateFlow<PushRoomTarget?>(null)
    val pending: StateFlow<PushRoomTarget?> = _pending.asStateFlow()

    private val _pendingCall = MutableStateFlow<PushCallTarget?>(null)
    val pendingCall: StateFlow<PushCallTarget?> = _pendingCall.asStateFlow()

    fun requestNavigation(roomId: String, title: String) {
        _pending.value = PushRoomTarget(roomId = roomId, title = title)
    }

    fun requestCallNavigation(callId: String, roomId: String, callerName: String, callType: String) {
        _pendingCall.value = PushCallTarget(
            callId = callId,
            roomId = roomId,
            callerName = callerName,
            callType = callType,
        )
    }

    fun consume() {
        _pending.value = null
    }

    fun consumeCall() {
        _pendingCall.value = null
    }
}
