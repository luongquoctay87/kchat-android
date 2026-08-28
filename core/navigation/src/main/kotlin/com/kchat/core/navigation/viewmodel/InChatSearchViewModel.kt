package com.kchat.core.navigation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kchat.core.model.SearchResult
import com.kchat.core.navigation.KChatRoute
import com.kchat.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class InChatSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<KChatRoute.InChatSearch>()
    val roomId: String = route.roomId

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(text: String) {
        _query.value = text
        searchJob?.cancel()
        val q = text.trim()
        if (q.isBlank()) {
            _results.value = emptyList()
            _isLoading.value = false
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _isLoading.value = true
            _results.value = runCatching { chatRepository.searchInRoom(roomId, q) }
                .getOrElse { emptyList() }
            _isLoading.value = false
        }
    }
}
