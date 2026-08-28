package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.core.model.RoomSummary
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.TypingStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListItem(
    val room: RoomSummary,
    val isTyping: Boolean = false,
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    typingStateStore: TypingStateStore,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val rooms: StateFlow<List<RoomSummary>> = chatRepository.observeRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val items: StateFlow<List<ChatListItem>> = combine(
        chatRepository.observeRooms(),
        typingStateStore.observeTypingRooms(),
    ) { rooms, typingRooms ->
        rooms.map { room ->
            ChatListItem(room = room, isTyping = room.id.trim().lowercase() in typingRooms)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching { chatRepository.refreshRooms() }
            _isRefreshing.value = false
        }
    }
}
