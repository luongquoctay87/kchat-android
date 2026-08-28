package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.model.ContactSummary
import com.kchat.core.model.RoomSummary
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
class CreateGroupViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    val contacts: StateFlow<List<ContactSummary>> = contactsRepository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _creating = MutableStateFlow(false)
    val creating: StateFlow<Boolean> = _creating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { contactsRepository.refreshContacts() }
        }
    }

    suspend fun createGroup(name: String, memberIds: List<String>): Result<RoomSummary> {
        if (_creating.value) return Result.failure(IllegalStateException("Đang tạo nhóm"))
        _creating.value = true
        _error.value = null
        return try {
            chatRepository.createGroup(name.trim(), memberIds)
                .onFailure { _error.value = it.message ?: "Tạo nhóm thất bại" }
        } finally {
            _creating.value = false
        }
    }
}
