package com.kchat.data.repository

import com.kchat.core.model.CallInfo
import com.kchat.core.model.CallRealtimeEvent
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Fan-out for call lifecycle + ICE signals from the WebSocket layer to UI / WebRTC.
 * Also tracks whether this device is already in a call UI (busy for incoming nav).
 */
class CallSignalBus {
    private val _events = MutableSharedFlow<CallRealtimeEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<CallRealtimeEvent> = _events.asSharedFlow()

    // replay=1 so NavHost still sees an incoming if it subscribed a moment late
    private val _incoming = MutableSharedFlow<CallInfo>(
        replay = 1,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val incoming: SharedFlow<CallInfo> = _incoming.asSharedFlow()

    private val activeCallId = AtomicReference<String?>(null)

    fun publish(event: CallRealtimeEvent) {
        _events.tryEmit(event)
        if (event is CallRealtimeEvent.Incoming) {
            _incoming.tryEmit(event.call)
        }
    }

    /** True only when already on a real call id (not the outgoing "pending" dial). */
    fun isBusy(): Boolean {
        val id = activeCallId.get() ?: return false
        return id != "pending"
    }

    fun activeCallId(): String? = activeCallId.get()?.takeUnless { it == "pending" }

    /** Marks this device as in a call screen (outgoing or incoming). */
    fun markActive(callId: String?) {
        if (callId.isNullOrBlank()) {
            activeCallId.compareAndSet(null, "pending")
        } else {
            activeCallId.set(callId)
        }
    }

    fun clearActive(callId: String?) {
        val current = activeCallId.get() ?: return
        if (callId.isNullOrBlank() || current == callId || current == "pending") {
            activeCallId.compareAndSet(current, null)
        }
    }

    /** Drop replay so a stale ringing call is not re-opened after hangup. */
    fun clearIncoming() {
        _incoming.resetReplayCache()
    }
}
