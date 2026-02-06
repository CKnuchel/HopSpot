package com.kickpaws.hopspot.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.analytics.AnalyticsManager
import com.kickpaws.hopspot.data.remote.error.ErrorMessageMapper
import com.kickpaws.hopspot.domain.model.Spot
import com.kickpaws.hopspot.domain.repository.SpotFilter
import com.kickpaws.hopspot.domain.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val spots: List<Spot> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedSpot: Spot? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val analyticsManager: AnalyticsManager,
    private val errorMessageMapper: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.logScreenView("Map")
        loadAllSpots()
    }

    fun loadAllSpots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Lade alle Spots (100 = max)
            val filter = SpotFilter(page = 1, limit = 100)
            val result = spotRepository.getSpots(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            spots = paginated.spots
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMessageMapper.getMessage(exception)
                        )
                    }
                }
            )
        }
    }

    fun selectSpot(spot: Spot?) {
        _uiState.update { it.copy(selectedSpot = spot) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedSpot = null) }
    }
}