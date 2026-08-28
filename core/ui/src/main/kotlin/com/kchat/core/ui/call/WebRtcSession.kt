package com.kchat.core.ui.call

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.kchat.core.model.RtcIceServer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

/**
 * WebRTC 1:1 helper. ICE servers come from the API ([GET /calls/ice-servers]);
 * never hardcode production TURN credentials in the APK.
 */
class WebRtcSession(
    private val appContext: Context,
    private val callbacks: Callbacks,
    private val iceServerConfigs: List<RtcIceServer> = emptyList(),
) {
    interface Callbacks {
        fun onLocalOffer(sdp: String)
        fun onLocalAnswer(sdp: String)
        fun onLocalIceCandidate(candidate: String, sdpMid: String?, sdpMLineIndex: Int)
        fun onConnectionState(state: ConnectionState)
    }

    enum class ConnectionState { Connected, Failed, Disconnected }

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "kchat-webrtc").apply { isDaemon = true }
    }

    private var eglBase: EglBase? = null
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null

    private var audioSource: AudioSource? = null
    private var audioTrack: AudioTrack? = null
    private var videoSource: VideoSource? = null
    private var videoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var localRenderer: SurfaceViewRenderer? = null
    private var remoteRenderer: SurfaceViewRenderer? = null
    private var remoteVideoTrack: VideoTrack? = null

    private var isVideo = false
    private var isCallerRole = false
    private var isPeerReady = false
    private var remoteDescriptionSet = false
    private val pendingRemoteIce = mutableListOf<IceCandidate>()
    private var pendingRemoteOffer: String? = null
    private var pendingRemoteAnswer: String? = null
    private val disposed = AtomicBoolean(false)
    private var iceRestartAttempted = false

    fun initialize(isVideo: Boolean) {
        runOnPc {
            if (disposed.get()) return@runOnPc
            this.isVideo = isVideo
            ensureFactory()
            createPeerConnection()
            createLocalTracks()
            setSpeakerphone(isVideo)
            isPeerReady = true
            drainPendingSdp()
        }
    }

    /**
     * Blocks until the peer connection is created (or fails / disposed).
     * Prefer this from ViewModel so ICE can be applied immediately after return.
     */
    fun initializeBlocking(isVideo: Boolean, timeoutMs: Long = 8_000L) {
        val latch = java.util.concurrent.CountDownLatch(1)
        var error: Throwable? = null
        if (disposed.get() || executor.isShutdown) {
            throw IllegalStateException("WebRTC session already disposed")
        }
        executor.execute {
            synchronized(this) {
                try {
                    if (!disposed.get()) {
                        this.isVideo = isVideo
                        ensureFactory()
                        createPeerConnection()
                        createLocalTracks()
                        setSpeakerphone(isVideo)
                        isPeerReady = true
                        drainPendingSdp()
                    }
                } catch (t: Throwable) {
                    error = t
                    Log.e(TAG, "initializeBlocking failed", t)
                } finally {
                    latch.countDown()
                }
            }
        }
        if (!latch.await(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)) {
            throw IllegalStateException("WebRTC initialize timeout")
        }
        error?.let { throw it }
        if (disposed.get() || peerConnection == null) {
            throw IllegalStateException("WebRTC peer connection not ready")
        }
    }

    fun startAsCaller() {
        runOnPc {
            isCallerRole = true
            createOfferInternal(iceRestart = false)
        }
    }

    fun startAsCallee() {
        // Wait for remote offer via [onRemoteOffer].
    }

    fun onRemoteOffer(sdp: String) {
        runOnPc {
            if (!isPeerReady || peerConnection == null) {
                pendingRemoteOffer = sdp
                Log.d(TAG, "queue remote OFFER until peer ready")
                return@runOnPc
            }
            applyRemoteOffer(sdp)
        }
    }

    fun onRemoteAnswer(sdp: String) {
        runOnPc {
            if (!isPeerReady || peerConnection == null) {
                pendingRemoteAnswer = sdp
                Log.d(TAG, "queue remote ANSWER until peer ready")
                return@runOnPc
            }
            applyRemoteAnswer(sdp)
        }
    }

    private fun applyRemoteOffer(sdp: String) {
        val pc = peerConnection ?: return
        Log.d(TAG, "setRemoteDescription OFFER (${sdp.length} chars)")
        val remote = SessionDescription(SessionDescription.Type.OFFER, sdp)
        pc.setRemoteDescription(
            object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    runOnPc {
                        remoteDescriptionSet = true
                        drainPendingIce()
                        pc.createAnswer(
                            object : SimpleSdpObserver() {
                                override fun onCreateSuccess(desc: SessionDescription?) {
                                    if (desc == null) return
                                    runOnPc {
                                        pc.setLocalDescription(
                                            object : SimpleSdpObserver() {
                                                override fun onSetSuccess() {
                                                    Log.d(TAG, "local ANSWER ready")
                                                    callbacks.onLocalAnswer(desc.description)
                                                }

                                                override fun onSetFailure(error: String?) {
                                                    Log.e(TAG, "setLocalDescription answer failed: $error")
                                                }
                                            },
                                            desc,
                                        )
                                    }
                                }

                                override fun onCreateFailure(error: String?) {
                                    Log.e(TAG, "createAnswer failed: $error")
                                }
                            },
                            mediaConstraints(),
                        )
                    }
                }

                override fun onSetFailure(error: String?) {
                    Log.e(TAG, "setRemoteDescription offer failed: $error")
                }
            },
            remote,
        )
    }

    private fun applyRemoteAnswer(sdp: String) {
        val pc = peerConnection ?: return
        Log.d(TAG, "setRemoteDescription ANSWER (${sdp.length} chars)")
        val remote = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        pc.setRemoteDescription(
            object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    runOnPc {
                        remoteDescriptionSet = true
                        drainPendingIce()
                    }
                }

                override fun onSetFailure(error: String?) {
                    Log.e(TAG, "setRemoteDescription answer failed: $error")
                }
            },
            remote,
        )
    }

    fun onRemoteIceCandidate(candidate: String, sdpMid: String?, sdpMLineIndex: Int?) {
        runOnPc {
            val ice = IceCandidate(sdpMid.orEmpty(), sdpMLineIndex ?: 0, candidate)
            if (!remoteDescriptionSet) {
                pendingRemoteIce += ice
                Log.d(TAG, "queue remote ICE (${pendingRemoteIce.size})")
            } else {
                val ok = peerConnection?.addIceCandidate(ice) == true
                Log.d(TAG, "add remote ICE ok=$ok mid=${sdpMid.orEmpty()} m=$sdpMLineIndex")
            }
        }
    }

    fun setMicEnabled(enabled: Boolean) {
        runOnPc { audioTrack?.setEnabled(enabled) }
    }

    fun setCameraEnabled(enabled: Boolean) {
        runOnPc {
            videoTrack?.setEnabled(enabled)
            if (enabled) {
                runCatching { videoCapturer?.startCapture(640, 480, 24) }
            } else {
                runCatching { videoCapturer?.stopCapture() }
            }
        }
    }

    fun setSpeakerphone(enabled: Boolean) {
        val am = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        @Suppress("DEPRECATION")
        am.isSpeakerphoneOn = enabled
    }

    fun attachLocalRenderer(renderer: SurfaceViewRenderer) {
        runOnPc {
            val egl = eglBase ?: return@runOnPc
            localRenderer?.let { detachLocal(it) }
            localRenderer = renderer
            runCatching {
                renderer.init(egl.eglBaseContext, null)
                renderer.setMirror(true)
                renderer.setEnableHardwareScaler(true)
            }
            videoTrack?.addSink(renderer)
        }
    }

    fun attachRemoteRenderer(renderer: SurfaceViewRenderer) {
        runOnPc {
            val egl = eglBase ?: return@runOnPc
            remoteRenderer?.let { detachRemote(it) }
            remoteRenderer = renderer
            runCatching {
                renderer.init(egl.eglBaseContext, null)
                renderer.setMirror(false)
                renderer.setEnableHardwareScaler(true)
            }
            remoteVideoTrack?.addSink(renderer)
        }
    }

    fun detachRenderers() {
        runOnPc {
            localRenderer?.let { videoTrack?.removeSink(it) }
            remoteRenderer?.let { remoteVideoTrack?.removeSink(it) }
            localRenderer = null
            remoteRenderer = null
        }
    }

    fun dispose() {
        if (!disposed.compareAndSet(false, true)) return
        val latch = java.util.concurrent.CountDownLatch(1)
        if (!executor.isShutdown) {
            executor.execute {
                synchronized(this) {
                    cleanupResources()
                }
                latch.countDown()
            }
            runCatching { latch.await(3, java.util.concurrent.TimeUnit.SECONDS) }
            executor.shutdown()
        } else {
            synchronized(this) { cleanupResources() }
        }
        runCatching {
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            @Suppress("DEPRECATION")
            am.isSpeakerphoneOn = false
            am.mode = AudioManager.MODE_NORMAL
        }
    }

    private fun cleanupResources() {
        detachLocal(localRenderer)
        detachRemote(remoteRenderer)
        localRenderer = null
        remoteRenderer = null
        remoteVideoTrack = null

        runCatching { videoCapturer?.stopCapture() }
        videoCapturer?.dispose()
        videoCapturer = null
        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null

        videoTrack?.dispose()
        videoTrack = null
        videoSource?.dispose()
        videoSource = null
        audioTrack?.dispose()
        audioTrack = null
        audioSource?.dispose()
        audioSource = null

        peerConnection?.close()
        peerConnection?.dispose()
        peerConnection = null

        factory?.dispose()
        factory = null
        eglBase?.release()
        eglBase = null
        pendingRemoteIce.clear()
        pendingRemoteOffer = null
        pendingRemoteAnswer = null
        remoteDescriptionSet = false
        isPeerReady = false
        iceRestartAttempted = false
    }

    private fun drainPendingSdp() {
        pendingRemoteOffer?.let { offer ->
            pendingRemoteOffer = null
            Log.d(TAG, "drain queued OFFER")
            applyRemoteOffer(offer)
        }
        pendingRemoteAnswer?.let { answer ->
            pendingRemoteAnswer = null
            Log.d(TAG, "drain queued ANSWER")
            applyRemoteAnswer(answer)
        }
    }

    private fun ensureFactory() {
        initializeFactoryOnce(appContext)
        if (eglBase == null) {
            eglBase = EglBase.create()
        }
        if (factory == null) {
            val egl = eglBase!!
            val encoder = DefaultVideoEncoderFactory(egl.eglBaseContext, true, true)
            val decoder = DefaultVideoDecoderFactory(egl.eglBaseContext)
            factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoder)
                .setVideoDecoderFactory(decoder)
                .createPeerConnectionFactory()
        }
    }

    private fun createPeerConnection() {
        val f = factory ?: return
        val servers = resolveIceServers()
        val rtcConfig = PeerConnection.RTCConfiguration(servers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            // Emulators often need TURN; keep ALL so host/srflx still tried first.
            iceTransportsType = PeerConnection.IceTransportsType.ALL
        }
        peerConnection = f.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(newState: PeerConnection.SignalingState?) {
                Log.d(TAG, "signaling=$newState")
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                Log.i(TAG, "iceConnection=$newState")
                when (newState) {
                    PeerConnection.IceConnectionState.CONNECTED,
                    PeerConnection.IceConnectionState.COMPLETED,
                    -> callbacks.onConnectionState(ConnectionState.Connected)
                    PeerConnection.IceConnectionState.FAILED -> {
                        if (!iceRestartAttempted && !disposed.get()) {
                            iceRestartAttempted = true
                            Log.w(TAG, "ICE failed — attempting iceRestart (caller=$isCallerRole)")
                            runOnPc { createOfferInternal(iceRestart = true) }
                        } else {
                            callbacks.onConnectionState(ConnectionState.Failed)
                        }
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED,
                    PeerConnection.IceConnectionState.CLOSED,
                    -> callbacks.onConnectionState(ConnectionState.Disconnected)
                    else -> Unit
                }
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {
                Log.d(TAG, "iceGathering=$newState")
            }

            override fun onIceCandidate(candidate: IceCandidate?) {
                if (candidate == null) return
                // Skip useless emulator loopback-ish host candidates that collide (both AVD = 10.0.2.15)
                if (isEmulator() && candidate.sdp.contains(" 10.0.2.")) {
                    Log.d(TAG, "skip emulator host ICE: ${candidate.sdp}")
                    return
                }
                Log.d(TAG, "local ICE mid=${candidate.sdpMid} m=${candidate.sdpMLineIndex}")
                callbacks.onLocalIceCandidate(
                    candidate.sdp,
                    candidate.sdpMid,
                    candidate.sdpMLineIndex,
                )
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) = Unit
            override fun onAddStream(stream: MediaStream?) = Unit
            override fun onRemoveStream(stream: MediaStream?) = Unit
            override fun onDataChannel(dc: DataChannel?) = Unit
            override fun onRenegotiationNeeded() = Unit
            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                val track = receiver?.track() as? VideoTrack ?: return
                runOnPc {
                    remoteVideoTrack = track
                    remoteRenderer?.let { track.addSink(it) }
                }
            }
        })
        Log.d(TAG, "PeerConnection created iceServers=${servers.size}")
    }

    private fun resolveIceServers(): List<PeerConnection.IceServer> {
        val fromApi = iceServerConfigs.flatMap { cfg ->
            cfg.urls.filter { it.isNotBlank() }.map { url ->
                val builder = PeerConnection.IceServer.builder(url)
                if (!cfg.username.isNullOrBlank() && !cfg.credential.isNullOrBlank()) {
                    builder.setUsername(cfg.username).setPassword(cfg.credential)
                }
                builder.createIceServer()
            }
        }
        if (fromApi.isNotEmpty()) return fromApi
        // Offline / API failure fallback: STUN only (no public TURN in APK).
        return listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        )
    }

    private fun createOfferInternal(iceRestart: Boolean) {
        val pc = peerConnection ?: return
        val constraints = mediaConstraints().apply {
            if (iceRestart) {
                mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
            }
        }
        Log.d(TAG, "createOffer iceRestart=$iceRestart")
        pc.createOffer(
            object : SimpleSdpObserver() {
                override fun onCreateSuccess(desc: SessionDescription?) {
                    if (desc == null) return
                    runOnPc {
                        pc.setLocalDescription(
                            object : SimpleSdpObserver() {
                                override fun onSetSuccess() {
                                    Log.d(TAG, "local OFFER ready")
                                    callbacks.onLocalOffer(desc.description)
                                }

                                override fun onSetFailure(error: String?) {
                                    Log.e(TAG, "setLocalDescription offer failed: $error")
                                }
                            },
                            desc,
                        )
                    }
                }

                override fun onCreateFailure(error: String?) {
                    Log.e(TAG, "createOffer failed: $error")
                }
            },
            constraints,
        )
    }

    private fun createLocalTracks() {
        val f = factory ?: return
        val pc = peerConnection ?: return

        audioSource = f.createAudioSource(MediaConstraints())
        audioTrack = f.createAudioTrack(AUDIO_TRACK_ID, audioSource).also {
            it.setEnabled(true)
            pc.addTrack(it, listOf(STREAM_ID))
        }

        if (!isVideo) return

        val enumerator: CameraEnumerator = Camera2Enumerator(appContext)
        val capturer = createCameraCapturer(enumerator)
        if (capturer == null) {
            Log.w(TAG, "No camera — video call will be audio-only receive")
            return
        }
        videoCapturer = capturer
        surfaceTextureHelper = SurfaceTextureHelper.create("kchat-capture", eglBase!!.eglBaseContext)
        videoSource = f.createVideoSource(capturer.isScreencast)
        capturer.initialize(surfaceTextureHelper, appContext, videoSource!!.capturerObserver)
        runCatching { capturer.startCapture(640, 480, 24) }
            .onFailure { Log.e(TAG, "startCapture failed", it) }
        videoTrack = f.createVideoTrack(VIDEO_TRACK_ID, videoSource).also { track ->
            track.setEnabled(true)
            pc.addTrack(track, listOf(STREAM_ID))
            localRenderer?.let { track.addSink(it) }
        }
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                enumerator.createCapturer(name, null)?.let { return it }
            }
        }
        for (name in deviceNames) {
            enumerator.createCapturer(name, null)?.let { return it }
        }
        return null
    }

    private fun mediaConstraints() = MediaConstraints().apply {
        // Still set for older stacks; UNIFIED_PLAN mainly uses addTrack/transceivers.
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isVideo) "true" else "false"))
    }

    private fun drainPendingIce() {
        val pc = peerConnection ?: return
        Log.d(TAG, "drain ${pendingRemoteIce.size} queued ICE")
        pendingRemoteIce.forEach { pc.addIceCandidate(it) }
        pendingRemoteIce.clear()
    }

    private fun detachLocal(renderer: SurfaceViewRenderer?) {
        if (renderer == null) return
        videoTrack?.removeSink(renderer)
    }

    private fun detachRemote(renderer: SurfaceViewRenderer?) {
        if (renderer == null) return
        remoteVideoTrack?.removeSink(renderer)
    }

    private fun runOnPc(block: () -> Unit) {
        if (disposed.get() || executor.isShutdown) return
        executor.execute {
            synchronized(this) {
                if (disposed.get()) return@synchronized
                runCatching(block).onFailure { Log.e(TAG, "PC task failed", it) }
            }
        }
    }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription?) = Unit
        override fun onSetSuccess() = Unit
        override fun onCreateFailure(error: String?) = Unit
        override fun onSetFailure(error: String?) = Unit
    }

    companion object {
        private const val TAG = "KChatWebRTC"
        private const val STREAM_ID = "kchat_stream"
        private const val AUDIO_TRACK_ID = "kchat_audio"
        private const val VIDEO_TRACK_ID = "kchat_video"

        private val factoryLock = Any()
        private var factoryInitialized = false

        private fun isEmulator(): Boolean {
            return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk", ignoreCase = true) ||
                Build.MODEL.contains("Emulator", ignoreCase = true) ||
                Build.MODEL.contains("Android SDK built for", ignoreCase = true) ||
                Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
                Build.PRODUCT.contains("sdk", ignoreCase = true) ||
                Build.PRODUCT.contains("emulator", ignoreCase = true) ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")
        }

        private fun initializeFactoryOnce(context: Context) {
            synchronized(factoryLock) {
                if (factoryInitialized) return
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                        .setEnableInternalTracer(false)
                        .createInitializationOptions(),
                )
                factoryInitialized = true
            }
        }
    }
}
