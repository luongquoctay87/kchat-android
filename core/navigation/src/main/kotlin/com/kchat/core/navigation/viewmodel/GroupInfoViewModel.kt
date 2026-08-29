package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kchat.core.model.ContactSummary
import com.kchat.core.model.GroupMember
import com.kchat.core.model.RoomSummary
import com.kchat.core.navigation.KChatRoute
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GroupInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<KChatRoute.GroupInfo>()
    val roomId: String = route.roomId
    val title: String = route.title

    private val _members = MutableStateFlow<List<GroupMember>>(emptyList())
    val members: StateFlow<List<GroupMember>> = _members.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _avatarUploading = MutableStateFlow(false)
    val avatarUploading: StateFlow<Boolean> = _avatarUploading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val room: StateFlow<RoomSummary?> = chatRepository.observeRooms()
        .map { rooms -> rooms.find { it.id.equals(roomId, ignoreCase = true) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val contacts: StateFlow<List<ContactSummary>> = contactsRepository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { reloadMembers() }
        viewModelScope.launch {
            runCatching { contactsRepository.refreshContacts() }
        }
        viewModelScope.launch {
            runCatching { chatRepository.refreshRooms() }
        }
    }

    fun refresh() {
        viewModelScope.launch { reloadMembers() }
    }

    private suspend fun reloadMembers() {
        _loading.value = true
        chatRepository.listMembers(roomId)
            .onSuccess { loaded ->
                // Owner/admin first, then alphabetical — keep "Bạn" easy to find.
                _members.value = loaded.sortedWith(
                    compareBy<GroupMember> {
                        when (it.role) {
                            "owner" -> 0
                            "admin" -> 1
                            else -> 2
                        }
                    }.thenBy { !it.isMe }
                        .thenBy { it.name.lowercase() },
                )
                _error.value = null
            }
            .onFailure { _error.value = it.message ?: "Không tải được thành viên" }
        _loading.value = false
    }

    suspend fun addMembers(userIds: List<String>): Result<Unit> {
        if (userIds.isEmpty()) return Result.success(Unit)
        if (_busy.value) return Result.failure(IllegalStateException("Đang xử lý"))
        _busy.value = true
        return try {
            chatRepository.addMembers(roomId, userIds)
                .onSuccess { reloadMembers() }
                .onFailure { _error.value = it.message }
        } finally {
            _busy.value = false
        }
    }

    suspend fun removeMember(userId: String): Result<Unit> {
        if (_busy.value) return Result.failure(IllegalStateException("Đang xử lý"))
        _busy.value = true
        return try {
            chatRepository.removeMember(roomId, userId)
                .onSuccess { reloadMembers() }
                .onFailure { _error.value = it.message }
        } finally {
            _busy.value = false
        }
    }

    suspend fun updateAvatar(
        uri: android.net.Uri,
        mimeType: String?,
        displayName: String?,
    ): Result<Unit> {
        if (_avatarUploading.value) return Result.failure(IllegalStateException("Đang tải ảnh"))
        _avatarUploading.value = true
        return try {
            chatRepository.updateGroupAvatar(roomId, uri, mimeType, displayName)
                .map { }
                .onFailure { _error.value = it.message ?: "Cập nhật ảnh nhóm thất bại" }
        } finally {
            _avatarUploading.value = false
        }
    }

    suspend fun leave(): Result<Unit> {
        if (_busy.value) return Result.failure(IllegalStateException("Đang xử lý"))
        _busy.value = true
        return try {
            chatRepository.leaveRoom(roomId)
                .onFailure { _error.value = it.message ?: "Rời nhóm thất bại" }
        } finally {
            _busy.value = false
        }
    }
}
