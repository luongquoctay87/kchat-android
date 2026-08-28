package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kchat.core.model.ContactSummary
import com.kchat.core.model.GroupMember
import com.kchat.core.navigation.KChatRoute
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val contacts: StateFlow<List<ContactSummary>> = contactsRepository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { reloadMembers() }
        viewModelScope.launch {
            runCatching { contactsRepository.refreshContacts() }
        }
    }

    fun refresh() {
        viewModelScope.launch { reloadMembers() }
    }

    private suspend fun reloadMembers() {
        _loading.value = true
        chatRepository.listMembers(roomId)
            .onSuccess {
                _members.value = it
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
