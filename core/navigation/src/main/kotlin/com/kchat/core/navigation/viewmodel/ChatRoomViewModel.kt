package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kchat.core.model.ChatMessage
import com.kchat.core.model.MentionUser
import com.kchat.core.model.PinnedMessage
import com.kchat.core.model.ReadReceipt
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.RoomMeta
import com.kchat.core.navigation.KChatRoute
import com.kchat.data.repository.ActiveRoomTracker
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.RealtimeCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val activeRoomTracker: ActiveRoomTracker,
    private val realtimeCoordinator: RealtimeCoordinator,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<KChatRoute.Chat>()
    val roomId: String = route.roomId.trim().lowercase()

    val messages: StateFlow<List<ChatMessage>> = chatRepository.observeMessages(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val roomMeta: StateFlow<RoomMeta> = chatRepository.observeRoomMeta(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoomMeta())

    private val _pinned = MutableStateFlow<PinnedMessage?>(null)
    val pinned: StateFlow<PinnedMessage?> = _pinned.asStateFlow()

    private val _readReceipts = MutableStateFlow<List<ReadReceipt>>(emptyList())
    val readReceipts: StateFlow<List<ReadReceipt>> = _readReceipts.asStateFlow()
    private var loadedReceiptMessageId: String? = null

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    /** Composer text restored after a failed send (cleared by [clearRestoreDraft]). */
    private val _restoreDraft = MutableStateFlow<String?>(null)
    val restoreDraft: StateFlow<String?> = _restoreDraft.asStateFlow()

    private val _restoreReply = MutableStateFlow<ReplyQuote?>(null)
    val restoreReply: StateFlow<ReplyQuote?> = _restoreReply.asStateFlow()

    private val mentionCandidates = MutableStateFlow<List<MentionUser>>(emptyList())
    private var typingHeartbeatJob: Job? = null
    private var isComposing = false

    init {
        activeRoomTracker.enter(roomId)
        viewModelScope.launch {
            runCatching { chatRepository.refreshMessages(roomId) }
            runCatching { chatRepository.markRoomRead(roomId) }
            refreshPinned()
            refreshMentionCandidates()
        }
    }

    override fun onCleared() {
        stopTypingHeartbeat()
        realtimeCoordinator.sendTyping(roomId, false)
        activeRoomTracker.leave(roomId)
        super.onCleared()
    }

    fun onInputChanged(text: String) {
        val typing = text.isNotBlank()
        if (typing) {
            if (!isComposing) {
                isComposing = true
                realtimeCoordinator.sendTyping(roomId, true)
                startTypingHeartbeat()
            }
        } else if (isComposing) {
            isComposing = false
            stopTypingHeartbeat()
            realtimeCoordinator.sendTyping(roomId, false)
        }
    }

    fun sendMessage(text: String, replyTo: ReplyQuote?) {
        if (text.isBlank()) return
        isComposing = false
        stopTypingHeartbeat()
        realtimeCoordinator.sendTyping(roomId, false)
        viewModelScope.launch {
            chatRepository.sendMessage(roomId, text, replyTo)
                .onFailure { error ->
                    _restoreDraft.value = text
                    _restoreReply.value = replyTo
                    _actionError.value = error.message ?: "Không gửi được tin nhắn"
                }
        }
    }

    private fun startTypingHeartbeat() {
        typingHeartbeatJob?.cancel()
        typingHeartbeatJob = viewModelScope.launch {
            while (isActive && isComposing) {
                delay(TYPING_HEARTBEAT_MS)
                if (isComposing) {
                    realtimeCoordinator.sendTyping(roomId, true)
                }
            }
        }
    }

    private fun stopTypingHeartbeat() {
        typingHeartbeatJob?.cancel()
        typingHeartbeatJob = null
    }

    fun editMessage(messageId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.editMessage(roomId, messageId, text)
                .onSuccess { updated ->
                    val pin = _pinned.value
                    if (pin?.messageId == messageId) {
                        _pinned.value = pin.copy(text = updated.text)
                    }
                }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(roomId, messageId)
                .onSuccess {
                    if (_pinned.value?.messageId == messageId) {
                        _pinned.value = null
                    }
                }
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            chatRepository.toggleReaction(roomId, messageId, emoji)
        }
    }

    fun sendMedia(uri: android.net.Uri, mimeType: String?, displayName: String?) {
        viewModelScope.launch {
            chatRepository.sendMedia(roomId, uri, mimeType, displayName)
                .onFailure { error ->
                    _actionError.value = error.message ?: "Không gửi được file"
                }
        }
    }

    fun pinMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.pinMessage(roomId, messageId)
                .onSuccess { _pinned.value = it }
        }
    }

    fun unpinMessage() {
        viewModelScope.launch {
            chatRepository.unpinMessage(roomId)
                .onSuccess { _pinned.value = null }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    fun clearRestoreDraft() {
        _restoreDraft.value = null
        _restoreReply.value = null
    }

    fun setDisappearing(seconds: Int?) {
        viewModelScope.launch {
            chatRepository.updateRoomDisappearing(roomId, seconds)
                .onSuccess { _actionError.value = null }
                .onFailure { error ->
                    _actionError.value = error.message ?: "Không lưu được tin biến mất"
                }
        }
    }

    fun setRoomMuted(durationSeconds: Int) {
        viewModelScope.launch {
            chatRepository.muteRoom(roomId, durationSeconds)
                .onSuccess { _actionError.value = null }
                .onFailure { error ->
                    _actionError.value = error.message ?: "Không lưu được tắt thông báo"
                }
        }
    }

    private suspend fun refreshPinned() {
        chatRepository.getPinnedMessage(roomId)
            .onSuccess { _pinned.value = it }
    }

    private suspend fun refreshMentionCandidates() {
        chatRepository.listMembers(roomId)
            .onSuccess { members ->
                mentionCandidates.value = members
                    .filterNot { it.isMe }
                    .mapNotNull { member ->
                        val username = member.username.trim()
                        if (username.isEmpty()) return@mapNotNull null
                        MentionUser(
                            username = username,
                            displayName = member.name.ifBlank { username },
                        )
                    }
            }
    }

    suspend fun downloadMedia(mediaUrl: String, fileName: String) =
        chatRepository.downloadMedia(mediaUrl, fileName)

    fun mentionUsers(query: String): List<MentionUser> {
        val q = query.trim()
        return mentionCandidates.value.filter { user ->
            q.isEmpty()
                || user.username.startsWith(q, ignoreCase = true)
                || user.displayName.contains(q, ignoreCase = true)
        }
    }

    fun loadReadReceipts(messageId: String) {
        viewModelScope.launch {
            if (loadedReceiptMessageId != messageId) {
                _readReceipts.value = emptyList()
                loadedReceiptMessageId = messageId
            }
            _readReceipts.value = runCatching {
                chatRepository.getReadReceipts(roomId, messageId)
            }.getOrDefault(emptyList())
        }
    }

    fun ensureMessageLoadedForScroll(messageId: String) {
        if (messages.value.any { it.id == messageId }) return
        viewModelScope.launch {
            runCatching { chatRepository.refreshMessages(roomId, limit = 100) }
        }
    }

    companion object {
        private const val TYPING_HEARTBEAT_MS = 2_000L
    }
}
