package com.kchat.core.navigation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kchat.core.model.CallRealtimeEvent
import com.kchat.core.model.RtcIceServer
import com.kchat.core.navigation.KChatRoute
import com.kchat.core.ui.call.WebRtcSession
import com.kchat.core.ui.screens.CallPhase
import com.kchat.core.ui.screens.CallType
import com.kchat.data.repository.CallRepository
import com.kchat.data.repository.CallSignalBus
import com.kchat.data.repository.RealtimeCoordinator
import com.kchat.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.SurfaceViewRenderer

@HiltViewModel
class CallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val callRepository: CallRepository,
    private val callSignalBus: CallSignalBus,
    private val realtimeCoordinator: RealtimeCoordinator,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<KChatRoute.Call>()
    private val callType = runCatching { CallType.valueOf(route.callType) }
        .getOrDefault(CallType.Voice)

    val contactName: String = route.contactName
    val isOutgoing: Boolean = callType == CallType.Voice || callType == CallType.Video
    val isVideo: Boolean = callType == CallType.Video || callType == CallType.IncomingVideo

    private val roomId = route.roomId
    private var callId: String = route.callId

    private val _phase = MutableStateFlow(
        when {
            isOutgoing -> CallPhase.OutgoingRinging
            else -> CallPhase.IncomingRinging
        },
    )
    val phase: StateFlow<CallPhase> = _phase.asStateFlow()

    private val _statusText = MutableStateFlow(
        if (isOutgoing) "Đang gọi..." else if (isVideo) "Cuộc gọi video" else "Cuộc gọi thoại",
    )
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _durationLabel = MutableStateFlow("00:00")
    val durationLabel: StateFlow<String> = _durationLabel.asStateFlow()

    private val _micMuted = MutableStateFlow(false)
    val micMuted: StateFlow<Boolean> = _micMuted.asStateFlow()

    private val _cameraOff = MutableStateFlow(false)
    val cameraOff: StateFlow<Boolean> = _cameraOff.asStateFlow()

    private val _speakerOn = MutableStateFlow(isVideo)
    val speakerOn: StateFlow<Boolean> = _speakerOn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _shouldClose = MutableStateFlow(false)
    val shouldClose: StateFlow<Boolean> = _shouldClose.asStateFlow()

    private var webRtc: WebRtcSession? = null
    private var mediaReady = false
    private var started = false
    private var accepting = false
    private var localAcceptRequested = false
    private var eventsJob: Job? = null
    private var durationJob: Job? = null
    private var connectingTimeoutJob: Job? = null
    private var demoFallbackJob: Job? = null
    private var ending = false
    private var cachedSelfId: String? = null
    private var cachedIceServers: List<RtcIceServer> = emptyList()
    private var iceServersJob: Job? = null
    private val webRtcMutex = Mutex()

    init {
        callSignalBus.markActive(callId.ifBlank { null })
        viewModelScope.launch {
            cachedSelfId = settingsRepository.profile.firstOrNull()?.id
        }
        iceServersJob = viewModelScope.launch {
            cachedIceServers = callRepository.getIceServers().getOrDefault(emptyList())
        }
        if (!isOutgoing && callId.isNotBlank()) {
            collectSignals()
        }
    }

    fun onMediaReady() {
        if (mediaReady) return
        mediaReady = true
        if (isOutgoing) {
            startOutgoing()
        }
    }

    fun onMediaDenied() {
        _error.value = "Cần quyền micro" + if (isVideo) " và camera" else ""
        _phase.value = CallPhase.Ended
        _statusText.value = "Thiếu quyền"
        viewModelScope.launch {
            delay(900)
            finishAndClose()
        }
    }

    fun accept() {
        if (isOutgoing || _phase.value != CallPhase.IncomingRinging || accepting) return
        accepting = true
        localAcceptRequested = true
        viewModelScope.launch {
            if (callId.isBlank()) {
                accepting = false
                localAcceptRequested = false
                _error.value = "Thiếu mã cuộc gọi"
                return@launch
            }
            // Warm WebRTC before accept so early ICE offer from caller is queued, not dropped.
            if (!ensureWebRtc()) {
                accepting = false
                localAcceptRequested = false
                return@launch
            }
            callRepository.accept(callId)
                .onSuccess {
                    enterConnecting()
                    webRtc?.startAsCallee()
                    accepting = false
                }
                .onFailure { e ->
                    accepting = false
                    localAcceptRequested = false
                    _error.value = e.message ?: "Không trả lời được"
                }
        }
    }

    fun decline() {
        viewModelScope.launch {
            ending = true
            if (callId.isNotBlank()) {
                runCatching { callRepository.decline(callId) }
            }
            finishAndClose()
        }
    }

    fun end() {
        viewModelScope.launch {
            ending = true
            if (callId.isNotBlank() && _phase.value != CallPhase.Ended) {
                runCatching { callRepository.end(callId) }
            }
            finishAndClose()
        }
    }

    fun toggleMic() {
        val next = !_micMuted.value
        _micMuted.value = next
        webRtc?.setMicEnabled(!next)
    }

    fun toggleCamera() {
        if (!isVideo) return
        val next = !_cameraOff.value
        _cameraOff.value = next
        webRtc?.setCameraEnabled(!next)
    }

    fun toggleSpeaker() {
        val next = !_speakerOn.value
        _speakerOn.value = next
        webRtc?.setSpeakerphone(next)
    }

    fun bindLocalRenderer(renderer: SurfaceViewRenderer) {
        webRtc?.attachLocalRenderer(renderer)
    }

    fun bindRemoteRenderer(renderer: SurfaceViewRenderer) {
        webRtc?.attachRemoteRenderer(renderer)
    }

    fun disposeRenderers() {
        webRtc?.detachRenderers()
    }

    private fun startOutgoing() {
        if (started) return
        started = true
        viewModelScope.launch {
            val type = if (isVideo) "video" else "voice"
            callRepository.initiate(roomId, type)
                .onSuccess { info ->
                    callId = info.id
                    callSignalBus.markActive(callId)
                    _phase.value = CallPhase.OutgoingRinging
                    _statusText.value = "Đang gọi..."
                    collectSignals()
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Không gọi được"
                    _phase.value = CallPhase.Ended
                    _statusText.value = "Cuộc gọi thất bại"
                    delay(800)
                    finishAndClose()
                }
        }
    }

    private fun collectSignals() {
        if (eventsJob != null || callId.isBlank()) return
        eventsJob = viewModelScope.launch {
            callSignalBus.events
                .filter { event -> event.matchesCall(callId) }
                .collect { event -> handleSignal(event) }
        }
    }

    private fun handleSignal(event: CallRealtimeEvent) {
        when (event) {
            is CallRealtimeEvent.Accepted -> {
                if (!isOutgoing) {
                    if (localAcceptRequested || accepting ||
                        _phase.value == CallPhase.Connecting ||
                        _phase.value == CallPhase.Active
                    ) {
                        if (_phase.value == CallPhase.IncomingRinging) {
                            enterConnecting()
                        }
                        return
                    }
                    // Another device of the callee accepted — leave this ringing UI.
                    if (_phase.value == CallPhase.IncomingRinging && !ending) {
                        ending = true
                        _statusText.value = "Đã nhận trên thiết bị khác"
                        viewModelScope.launch {
                            delay(500)
                            finishAndClose()
                        }
                    }
                    return
                }
                demoFallbackJob?.cancel()
                enterConnecting()
                viewModelScope.launch {
                    if (!ensureWebRtc()) return@launch
                    webRtc?.startAsCaller()
                }
                if (callRepository.usesSimulatedPeer) {
                    demoFallbackJob = viewModelScope.launch {
                        delay(1_200)
                        if (_phase.value == CallPhase.Connecting) {
                            enterActive(skipWebRtc = true)
                        }
                    }
                }
            }
            is CallRealtimeEvent.Rejected, is CallRealtimeEvent.Ended -> {
                if (ending) return
                ending = true
                disposeWebRtc()
                connectingTimeoutJob?.cancel()
                _phase.value = CallPhase.Ended
                _statusText.value = if (event is CallRealtimeEvent.Rejected) {
                    "Cuộc gọi bị từ chối"
                } else {
                    "Cuộc gọi đã kết thúc"
                }
                viewModelScope.launch {
                    delay(600)
                    finishAndClose()
                }
            }
            is CallRealtimeEvent.IceOffer -> {
                if (isOutgoing) return
                if (isSelf(event.fromUserId)) return
                viewModelScope.launch {
                    ensureWebRtc()
                    webRtc?.onRemoteOffer(event.sdp)
                }
            }
            is CallRealtimeEvent.IceAnswer -> {
                if (!isOutgoing) return
                if (isSelf(event.fromUserId)) return
                viewModelScope.launch {
                    ensureWebRtc()
                    webRtc?.onRemoteAnswer(event.sdp)
                }
            }
            is CallRealtimeEvent.IceCandidate -> {
                if (isSelf(event.fromUserId)) return
                viewModelScope.launch {
                    ensureWebRtc()
                    webRtc?.onRemoteIceCandidate(
                        event.candidate,
                        event.sdpMid,
                        event.sdpMLineIndex,
                    )
                }
            }
            is CallRealtimeEvent.Incoming -> Unit
        }
    }

    private fun isSelf(userId: String): Boolean {
        val self = cachedSelfId ?: return false
        return userId.equals(self, ignoreCase = true)
    }

    private fun enterConnecting() {
        _phase.value = CallPhase.Connecting
        _statusText.value = "Đang kết nối..."
        connectingTimeoutJob?.cancel()
        connectingTimeoutJob = viewModelScope.launch {
            delay(CONNECTING_TIMEOUT_MS)
            if (_phase.value == CallPhase.Connecting) {
                _error.value = "Không nối được media. Thử lại."
                _statusText.value = "Media lỗi"
            }
        }
    }

    private fun enterActive(skipWebRtc: Boolean) {
        connectingTimeoutJob?.cancel()
        demoFallbackJob?.cancel()
        if (skipWebRtc) {
            disposeWebRtc()
        }
        _phase.value = CallPhase.Active
        _statusText.value = "Đang trò chuyện"
        startDurationTimer()
        webRtc?.setSpeakerphone(_speakerOn.value)
    }

    /** @return false if WebRTC failed to initialize */
    private suspend fun ensureWebRtc(): Boolean {
        if (webRtc != null) return true
        return webRtcMutex.withLock {
            if (webRtc != null) return@withLock true
            iceServersJob?.join()
            if (cachedIceServers.isEmpty()) {
                cachedIceServers = callRepository.getIceServers().getOrDefault(emptyList())
            }
            val session = WebRtcSession(
                appContext = appContext,
                iceServerConfigs = cachedIceServers,
                callbacks = object : WebRtcSession.Callbacks {
                    override fun onLocalOffer(sdp: String) {
                        val id = callId
                        if (id.isNotBlank()) realtimeCoordinator.sendIceOffer(id, sdp)
                    }

                    override fun onLocalAnswer(sdp: String) {
                        val id = callId
                        if (id.isNotBlank()) realtimeCoordinator.sendIceAnswer(id, sdp)
                    }

                    override fun onLocalIceCandidate(
                        candidate: String,
                        sdpMid: String?,
                        sdpMLineIndex: Int,
                    ) {
                        val id = callId
                        if (id.isNotBlank()) {
                            realtimeCoordinator.sendIceCandidate(id, candidate, sdpMid, sdpMLineIndex)
                        }
                    }

                    override fun onConnectionState(state: WebRtcSession.ConnectionState) {
                        when (state) {
                            WebRtcSession.ConnectionState.Connected -> {
                                if (_phase.value == CallPhase.Connecting ||
                                    _phase.value == CallPhase.OutgoingRinging
                                ) {
                                    enterActive(skipWebRtc = false)
                                }
                            }
                            WebRtcSession.ConnectionState.Failed -> {
                                if (_phase.value == CallPhase.Connecting || _phase.value == CallPhase.Active) {
                                    connectingTimeoutJob?.cancel()
                                    _error.value = "Không nối được media. Kiểm tra mạng hoặc TURN."
                                    _statusText.value = "Media lỗi"
                                }
                            }
                            WebRtcSession.ConnectionState.Disconnected -> {
                                if (_phase.value == CallPhase.Active) {
                                    _statusText.value = "Mất kết nối"
                                }
                            }
                        }
                    }
                },
            )
            val ok = runCatching { session.initializeBlocking(isVideo) }
                .onFailure { t ->
                    _error.value = t.message ?: "Không khởi tạo được WebRTC"
                    session.dispose()
                }
                .isSuccess
            if (ok) {
                webRtc = session
            }
            ok
        }
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        val startedAt = System.currentTimeMillis()
        durationJob = viewModelScope.launch {
            while (true) {
                val secs = ((System.currentTimeMillis() - startedAt) / 1000).toInt()
                val m = secs / 60
                val s = secs % 60
                _durationLabel.value = "%02d:%02d".format(m, s)
                delay(1_000)
            }
        }
    }

    private fun finishAndClose() {
        disposeWebRtc()
        durationJob?.cancel()
        demoFallbackJob?.cancel()
        connectingTimeoutJob?.cancel()
        callSignalBus.clearActive(callId.ifBlank { null })
        callSignalBus.clearIncoming()
        _phase.value = CallPhase.Ended
        _statusText.value = "Đã kết thúc"
        _shouldClose.value = true
    }

    private fun disposeWebRtc() {
        webRtc?.dispose()
        webRtc = null
    }

    override fun onCleared() {
        val id = callId
        // Only auto-hangup when media session was up; OutgoingRinging cleanup is explicit (Back/end).
        val shouldHangup = !ending &&
            id.isNotBlank() &&
            (_phase.value == CallPhase.Connecting || _phase.value == CallPhase.Active)
        ending = true
        disposeWebRtc()
        durationJob?.cancel()
        demoFallbackJob?.cancel()
        connectingTimeoutJob?.cancel()
        eventsJob?.cancel()
        callSignalBus.clearActive(id.ifBlank { null })
        callSignalBus.clearIncoming()
        if (shouldHangup) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { callRepository.end(id) }
            }
        }
        super.onCleared()
    }

    companion object {
        private const val CONNECTING_TIMEOUT_MS = 30_000L
    }
}

private fun CallRealtimeEvent.matchesCall(callId: String): Boolean = when (this) {
    is CallRealtimeEvent.Incoming -> call.id == callId
    is CallRealtimeEvent.Accepted -> call.id == callId
    is CallRealtimeEvent.Rejected -> call.id == callId
    is CallRealtimeEvent.Ended -> call.id == callId
    is CallRealtimeEvent.IceOffer -> this.callId == callId
    is CallRealtimeEvent.IceAnswer -> this.callId == callId
    is CallRealtimeEvent.IceCandidate -> this.callId == callId
}
