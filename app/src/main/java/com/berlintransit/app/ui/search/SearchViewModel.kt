package com.berlintransit.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlintransit.app.AppContainer
import com.berlintransit.app.data.Station
import com.berlintransit.app.ui.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(private val container: AppContainer) : ViewModel() {

    data class SearchUiState(
        val query: String = "",
        val isLoading: Boolean = false,
        val results: List<Station> = emptyList(),
        val error: String? = null,
        val hasSearched: Boolean = false
    )

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null) }
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(isLoading = false, results = emptyList(), hasSearched = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            runSearch(trimmed)
        }
    }

    fun retry() {
        val trimmed = _uiState.value.query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch(trimmed) }
    }

    private suspend fun runSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val results = container.repository.searchStations(query)
            _uiState.update {
                it.copy(isLoading = false, results = results, hasSearched = true)
            }
        } catch (t: Throwable) {
            _uiState.update { it.copy(isLoading = false, error = t.toUserMessage()) }
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val DEBOUNCE_MILLIS = 350L
    }
}
