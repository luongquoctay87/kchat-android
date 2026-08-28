package com.kchat.data.repository

interface RealtimeCoordinator {
    fun connect()

    fun disconnect()

    /** Notify peers in [roomId] that this user is typing (or stopped). */
    fun sendTyping(roomId: String, typing: Boolean)

    fun sendIceOffer(callId: String, sdp: String)

    fun sendIceAnswer(callId: String, sdp: String)

    fun sendIceCandidate(
        callId: String,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int?,
    )
}
