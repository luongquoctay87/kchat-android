package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.model.ContactSummary
import com.kchat.data.repository.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContactsUiState(
    val query: String = "",
    val searchResults: List<ContactSummary> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val actionError: String? = null,
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
) : ViewModel() {
    val contacts: StateFlow<List<ContactSummary>> = contactsRepository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching { contactsRepository.refreshContacts() }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value, searchError = null, actionError = null) }
        searchJob?.cancel()
        val trimmed = value.trim()
        if (trimmed.length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isSearching = true, searchError = null) }
            contactsRepository.searchUsers(trimmed)
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(searchResults = results, isSearching = false, searchError = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            searchResults = emptyList(),
                            isSearching = false,
                            searchError = error.message ?: "Không tìm được người dùng",
                        )
                    }
                }
        }
    }

    fun addContact(userId: String) {
        viewModelScope.launch {
            contactsRepository.addContact(userId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            searchResults = state.searchResults.map { row ->
                                if (row.id == userId) row.copy(isContact = true) else row
                            },
                            actionError = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(actionError = error.message ?: "Không thêm được vào danh bạ")
                    }
                }
        }
    }

    fun removeContact(userId: String) {
        viewModelScope.launch {
            contactsRepository.removeContact(userId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            searchResults = state.searchResults.map { row ->
                                if (row.id == userId) row.copy(isContact = false) else row
                            },
                            actionError = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(actionError = error.message ?: "Không xóa được khỏi danh bạ")
                    }
                }
        }
    }

    suspend fun openDirectChat(contactId: String): Result<String> =
        contactsRepository.openDirectChat(contactId)
}
