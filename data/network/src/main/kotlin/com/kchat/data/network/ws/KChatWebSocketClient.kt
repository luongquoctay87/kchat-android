package com.kchat.data.network.ws

import com.kchat.data.repository.AccessTokenHolder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.atomic.AtomicReference

object WsEventType {
    const val MESSAGE_NEW = "message_new"
    const val MESSAGE_UPDATED = "message_updated"
    const val MESSAGE_DELETED = "message_deleted"
    const val MESSAGES_READ = "messages_read"
    const val PRESENCE = "presence"
    const val TYPING = "typing"
    const val PONG = "pong"
    const val CALL_INCOMING = "call_incoming"
    const val CALL_ACCEPTED = "call_accepted"
    const val CALL_REJECTED = "call_rejected"
    const val CALL_ENDED = "call_ended"
    const val ICE_OFFER = "ice_offer"
    const val ICE_ANSWER = "ice_answer"
    const val ICE_CANDIDATE = "ice_candidate"
    const val DEVICE_SESSION_REVOKED = "device_session_revoked"
}

sealed class WsEvent {
    data object Connected : WsEvent()
    data object Disconnected : WsEvent()
    data class Message(val type: String, val raw: String) : WsEvent()
    data class Error(val message: String) : WsEvent()
}

/**
 * WebSocket client. Uses a dedicated [OkHttpClient] (no HTTP authenticator/JWT interceptor).
 *
 * Socket reference is cleared only for the instance that closed, so a reconnect race
 * cannot null out a newer socket and break outbound frames (typing / ICE).
 */
class KChatWebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val accessTokenHolder: AccessTokenHolder,
    private val wsBaseUrl: String,
) {
    private val socketRef = AtomicReference<WebSocket?>(null)

    @Volatile
    private var opened = false

    val isConnected: Boolean
        get() = opened && socketRef.get() != null

    fun connect(): Flow<WsEvent> = callbackFlow {
        val token = accessTokenHolder.accessToken
        if (token.isNullOrBlank()) {
            trySend(WsEvent.Error("Chưa đăng nhập"))
            close()
            return@callbackFlow
        }

        val request = Request.Builder()
            .url("${wsBaseUrl.trimEnd('/')}/ws")
            .header("Authorization", "Bearer $token")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                opened = true
                trySend(WsEvent.Connected)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val type = text.substringAfter("\"type\":\"", "").substringBefore('"')
                trySend(WsEvent.Message(type.ifBlank { "unknown" }, text))
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onMessage(webSocket, bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                opened = false
                socketRef.compareAndSet(webSocket, null)
                trySend(WsEvent.Disconnected)
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                opened = false
                socketRef.compareAndSet(webSocket, null)
                trySend(WsEvent.Error(t.message ?: "WebSocket lỗi"))
                // Close without cause — otherwise collectors throw and crash the app.
                close()
            }
        }

        val socket = okHttpClient.newWebSocket(request, listener)
        socketRef.set(socket)

        awaitClose {
            opened = false
            if (socketRef.compareAndSet(socket, null)) {
                runCatching { socket.close(1000, "Client closed") }
            }
        }
    }

    fun send(rawJson: String): Boolean {
        val ws = socketRef.get() ?: return false
        return runCatching { ws.send(rawJson) }.getOrDefault(false)
    }

    fun disconnect() {
        opened = false
        socketRef.getAndSet(null)?.let { ws ->
            runCatching { ws.close(1000, "Logout") }
        }
    }
}
