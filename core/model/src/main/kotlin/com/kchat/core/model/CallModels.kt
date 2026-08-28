package com.kchat.core.model

data class CallInfo(
    val id: String,
    val roomId: String,
    val initiatorId: String,
    val initiatorName: String,
    val calleeId: String,
    val calleeName: String,
    val callType: String,
    val status: String,
    val createdAt: String? = null,
    val startedAt: String? = null,
    val endedAt: String? = null,
) {
    val isVideo: Boolean get() = callType.equals("video", ignoreCase = true)
}

/** One ICE server entry from GET /calls/ice-servers. */
data class RtcIceServer(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null,
)

sealed class CallRealtimeEvent {
    data class Incoming(val call: CallInfo) : CallRealtimeEvent()
    data class Accepted(val call: CallInfo) : CallRealtimeEvent()
    data class Rejected(val call: CallInfo) : CallRealtimeEvent()
    data class Ended(val call: CallInfo) : CallRealtimeEvent()
    data class IceOffer(
        val callId: String,
        val fromUserId: String,
        val sdp: String,
    ) : CallRealtimeEvent()

    data class IceAnswer(
        val callId: String,
        val fromUserId: String,
        val sdp: String,
    ) : CallRealtimeEvent()

    data class IceCandidate(
        val callId: String,
        val fromUserId: String,
        val candidate: String,
        val sdpMid: String?,
        val sdpMLineIndex: Int?,
    ) : CallRealtimeEvent()
}
