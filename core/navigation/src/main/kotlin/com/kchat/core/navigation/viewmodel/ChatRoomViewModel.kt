package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kchat.core.model.ChatMessage
import com.kchat.core.model.ContactSummary
import com.kchat.core.model.FileDownloadStatus
import com.kchat.core.model.GroupMember
import com.kchat.core.model.MediaDownloadResult
import com.kchat.core.model.MentionUser
import com.kchat.core.model.PinnedMessage
import com.kchat.core.model.PinMessageRules
import com.kchat.core.model.ReadReceipt
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.RoomMeta
import com.kchat.core.navigation.KChatRoute
import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.ActiveRoomTracker
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.ContactsRepository
import com.kchat.data.repository.DownloadedMediaStore
import com.kchat.data.repository.EmergencyWipeCoordinator
import com.kchat.data.repository.RealtimeCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
    private val downloadedMediaStore: DownloadedMediaStore,
    private val emergencyWipeCoordinator: EmergencyWipeCoordinator,
    private val activeRoomTracker: ActiveRoomTracker,
    private val realtimeCoordinator: RealtimeCoordinator,
    private val accessTokenHolder: AccessTokenHolder,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<KChatRoute.Chat>()
    val roomId: String = route.roomId.trim().lowercase()

    val messages: StateFlow<List<ChatMessage>> = chatRepository.observeMessages(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val roomMeta: StateFlow<RoomMeta> = chatRepository.observeRoomMeta(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoomMeta())

    private val _pinned = MutableStateFlow<List<PinnedMessage>>(emptyList())
    val pinned: StateFlow<List<PinnedMessage>> = _pinned.asStateFlow()

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
    private val peer = MutableStateFlow<GroupMember?>(null)

    /** True when this is a 1-1 chat and the peer is not yet in the viewer's address book. */
    val canAddPeerToContacts: StateFlow<Boolean> = combine(
        roomMeta,
        peer,
        contactsRepository.observeContacts(),
    ) { meta, peerMember, contacts ->
        if (meta.isGroup || meta.isChannel) return@combine false
        val peerId = peerMember?.id?.trim().orEmpty()
        if (peerId.isEmpty()) return@combine false
        contacts.none { it.id.equals(peerId, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float?>>(emptyMap())
    val fileDownloadStatuses: StateFlow<Map<String, FileDownloadStatus>> = combine(
        downloadedMediaStore.observe(),
        _downloadProgress,
    ) { saved, progress ->
        buildMap {
            saved.forEach { (url, result) ->
                put(url, FileDownloadStatus.Saved(result.contentUri, result.fileName))
            }
            progress.forEach { (url, value) ->
                put(url, FileDownloadStatus.Downloading(value))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _openDownloadedFile = MutableSharedFlow<MediaDownloadResult>(extraBufferCapacity = 1)
    val openDownloadedFile: SharedFlow<MediaDownloadResult> = _openDownloadedFile.asSharedFlow()

    private var typingHeartbeatJob: Job? = null
    private var isComposing = false

    init {
        viewModelScope.launch {
            runCatching { emergencyWipeCoordinator.clearAfterReengage() }
            runCatching { chatRepository.ensureLocalRoom(roomId, route.title) }
            runCatching { chatRepository.refreshMessages(roomId) }
            runCatching { chatRepository.markRoomRead(roomId) }
            refreshPinned()
            refreshMentionCandidates()
            runCatching { contactsRepository.refreshContacts() }
        }
    }

    override fun onCleared() {
        stopTypingHeartbeat()
        realtimeCoordinator.sendTyping(roomId, false)
        activeRoomTracker.leave(roomId)
        super.onCleared()
    }

    fun onScreenVisible() {
        activeRoomTracker.enter(roomId)
    }

    fun onScreenHidden() {
        activeRoomTracker.leave(roomId)
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
                    _pinned.update { current ->
                        current.map { pin ->
                            if (pin.messageId == messageId) pin.copy(text = updated.text) else pin
                        }
                    }
                }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(roomId, messageId)
                .onSuccess {
                    _pinned.update { current -> current.filterNot { it.messageId == messageId } }
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
            if (_pinned.value.any { it.messageId == messageId }) return@launch
            if (_pinned.value.size >= PinMessageRules.MAX_PER_ROOM) {
                _actionError.value = "Mỗi phòng chỉ ghim tối đa ${PinMessageRules.MAX_PER_ROOM} tin nhắn"
                return@launch
            }
            val preview = messages.value.find { it.id == messageId }?.let { msg ->
                msg.text.ifBlank { msg.fileName ?: "Tin nhắn" }
            } ?: "Tin nhắn"
            val optimistic = PinnedMessage(messageId, preview)
            _pinned.update { listOf(optimistic) + it.filterNot { pin -> pin.messageId == messageId } }

            chatRepository.pinMessage(roomId, messageId)
                .onSuccess { pinned ->
                    _pinned.update { current ->
                        listOf(pinned) + current.filterNot { it.messageId == pinned.messageId }
                    }
                }
                .onFailure { error ->
                    _pinned.update { current -> current.filterNot { it.messageId == messageId } }
                    _actionError.value = error.message ?: "Không ghim được tin nhắn"
                }
        }
    }

    fun unpinMessage(messageId: String) {
        viewModelScope.launch {
            val previous = _pinned.value
            _pinned.update { current -> current.filterNot { it.messageId == messageId } }
            chatRepository.unpinMessage(roomId, messageId)
                .onFailure { error ->
                    _pinned.value = previous
                    _actionError.value = error.message ?: "Không bỏ ghim được"
                }
        }
    }

    private suspend fun refreshPinned() {
        chatRepository.getPinnedMessages(roomId)
            .onSuccess { _pinned.value = it }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun clearRestoreDraft() {
        _restoreDraft.value = null
        _restoreReply.value = null
    }

    fun addPeerToContacts() {
        viewModelScope.launch {
            var member = peer.value
            if (member == null) {
                refreshMentionCandidates()
                member = peer.value
            }
            val resolved = member
            val peerId = resolved?.id?.trim().orEmpty()
            if (resolved == null || peerId.isEmpty()) {
                _actionError.value = "Không thêm được vào danh bạ"
                return@launch
            }
            val selfId = accessTokenHolder.userId
            if (!selfId.isNullOrBlank() && peerId.equals(selfId, ignoreCase = true)) {
                _actionError.value = "Không thể tự thêm mình vào danh bạ"
                return@launch
            }
            val name = resolved.name.trim()
                .takeIf { it.isNotBlank() && !it.equals("Bạn", ignoreCase = true) }
                ?: route.title.trim().ifBlank { "Chat" }
            contactsRepository.addContact(
                ContactSummary(
                    id = peerId,
                    name = name,
                    subtitle = resolved.username,
                    isOnline = resolved.isOnline,
                    username = resolved.username,
                ),
            )
                .onSuccess {
                    _infoMessage.value = "Đã thêm $name"
                    runCatching { contactsRepository.refreshContacts() }
                }
                .onFailure { error ->
                    _actionError.value = error.message ?: "Không thêm được vào danh bạ"
                }
        }
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

    private suspend fun refreshMentionCandidates() {
        chatRepository.listMembers(roomId)
            .onSuccess { members ->
                val selfId = accessTokenHolder.userId
                peer.value = pickDirectPeer(members, selfId)
                mentionCandidates.value = members
                    .filterNot { it.isSelf(selfId) }
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

    fun openOrDownloadFile(mediaUrl: String, fileName: String) {
        val url = mediaUrl.trim()
        if (url.isEmpty()) return
        viewModelScope.launch {
            val existing = downloadedMediaStore.get(url)
            if (existing != null && downloadedMediaStore.isAccessible(existing.contentUri)) {
                _openDownloadedFile.emit(existing)
                return@launch
            }
            if (existing != null) {
                downloadedMediaStore.remove(url)
            }
            if (_downloadProgress.value.containsKey(url)) return@launch

            _downloadProgress.update { it + (url to null) }
            chatRepository.downloadMedia(url, fileName) { bytesRead, contentLength ->
                val progress = if (contentLength > 0L) {
                    (bytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                } else {
                    null
                }
                _downloadProgress.update { it + (url to progress) }
            }.onSuccess { result ->
                downloadedMediaStore.put(url, result)
                _downloadProgress.update { it - url }
                _infoMessage.value = "Đã lưu vào Downloads"
                _openDownloadedFile.emit(result)
            }.onFailure { error ->
                _downloadProgress.update { it - url }
                _actionError.value = error.message ?: "Tải file thất bại"
            }
        }
    }

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

private fun GroupMember.isSelf(selfId: String?): Boolean {
    if (isMe) return true
    if (!selfId.isNullOrBlank() && id.equals(selfId, ignoreCase = true)) return true
    return name.equals("Bạn", ignoreCase = true)
}

private fun pickDirectPeer(members: List<GroupMember>, selfId: String?): GroupMember? {
    val others = members.filterNot { it.isSelf(selfId) }
    if (others.size == 1) return others.first()
    if (members.size == 2 && !selfId.isNullOrBlank()) {
        return members.firstOrNull { !it.id.equals(selfId, ignoreCase = true) }
    }
    return others.firstOrNull()
}
