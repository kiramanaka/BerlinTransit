package com.berlintransit.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlintransit.app.AppContainer
import com.berlintransit.app.data.FavoriteEntry
import com.berlintransit.app.data.Station
import com.berlintransit.app.ui.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface NearbyState {
    data object Idle : NearbyState
    data object NeedsPermission : NearbyState
    data object Locating : NearbyState
    data object Unavailable : NearbyState
    data class Error(val message: String) : NearbyState
    data class Ready(val stations: List<Station>) : NearbyState
}

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    data class HomeUiState(
        val nearby: NearbyState = NearbyState.Idle
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteEntry>>(emptyList())
    val favorites: StateFlow<List<FavoriteEntry>> = _favorites.asStateFlow()

    private var locateJob: Job? = null

    init {
        viewModelScope.launch {
            container.favoritesStore.favorites.collect { list ->
                _favorites.value = list
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            locate()
        } else if (_uiState.value.nearby == NearbyState.Idle) {
            _uiState.update { it.copy(nearby = NearbyState.NeedsPermission) }
        }
    }

    fun checkPermissionAndStart() {
        if (!container.locationProvider.hasPermission()) {
            if (_uiState.value.nearby == NearbyState.Idle) {
                _uiState.update { it.copy(nearby = NearbyState.NeedsPermission) }
            }
            return
        }
        if (locateJob?.isActive == true) return
        if (_uiState.value.nearby is NearbyState.Ready) return
        locate()
    }

    fun retry() {
        locate()
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch { container.favoritesStore.remove(id) }
    }

    private fun locate() {
        if (locateJob?.isActive == true) return
        locateJob = viewModelScope.launch {
            _uiState.update { it.copy(nearby = NearbyState.Locating) }
            val location = container.locationProvider.currentLocation()
            if (location == null) {
                _uiState.update { it.copy(nearby = NearbyState.Unavailable) }
                return@launch
            }
            try {
                val stations = container.repository.nearbyStations(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                _uiState.update { it.copy(nearby = NearbyState.Ready(stations)) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(nearby = NearbyState.Error(t.toUserMessage())) }
            }
        }
    }
}
