package com.berlintransit.app.ui.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlintransit.app.AppContainer
import com.berlintransit.app.data.DepartureUi
import com.berlintransit.app.data.FavoriteToggleResult
import com.berlintransit.app.data.MAX_FAVORITES
import com.berlintransit.app.ui.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardViewModel(
    private val container: AppContainer,
    private val stopId: String,
    private val stopName: String
) : ViewModel() {

    data class BoardUiState(
        val isLoading: Boolean = true,
        val departures: List<DepartureUi> = emptyList(),
        val error: String? = null,
        val updatedAtMs: Long? = null
    )

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            container.favoritesStore.favorites.collect { favorites ->
                _isFavorite.value = favorites.any { it.id == stopId }
            }
        }
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val board = container.repository.departures(stopId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        departures = board.departures,
                        error = null,
                        updatedAtMs = board.realtimeDataUpdatedAtMs ?: System.currentTimeMillis()
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, error = t.toUserMessage()) }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            when (container.favoritesStore.toggle(stopId, stopName)) {
                FavoriteToggleResult.Added ->
                    _events.tryEmit("$stopName added to favorites")
                FavoriteToggleResult.Removed ->
                    _events.tryEmit("$stopName removed from favorites")
                FavoriteToggleResult.LimitReached ->
                    _events.tryEmit("Favorites are limited to $MAX_FAVORITES stations")
            }
        }
    }
}
