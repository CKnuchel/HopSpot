package com.kickpaws.hopspot.ui.screens.spotdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.analytics.AnalyticsManager
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.CreateVisitRequest
import com.kickpaws.hopspot.data.remote.error.ApiErrorParser
import com.kickpaws.hopspot.data.remote.error.ErrorMessageMapper
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Spot
import com.kickpaws.hopspot.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherInfo(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Int,
    val weathercode: Int
)

data class SpotDetailUiState(
    val spot: Spot? = null,
    val photos: List<Photo> = emptyList(),
    val visitCount: Long = 0,
    val weather: WeatherInfo? = null,
    val isLoading: Boolean = true,
    val isLoadingPhotos: Boolean = true,
    val isLoadingWeather: Boolean = false,
    val isAddingVisit: Boolean = false,
    val errorMessage: String? = null,
    val visitAdded: Boolean = false,
    val isFavorite: Boolean = false,
    val isTogglingFavorite: Boolean = false
)

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    private val api: HopSpotApi,
    private val analyticsManager: AnalyticsManager,
    private val errorMessageMapper: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotDetailUiState())
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()

    fun loadSpot(spotId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val response = api.getSpot(spotId)
                val spot = response.data.toDomain()

                analyticsManager.logScreenView("SpotDetail")
                analyticsManager.logSpotViewed(spotId)

                _uiState.update {
                    it.copy(
                        spot = spot,
                        isLoading = false
                    )
                }

                loadPhotos(spotId)
                loadVisitCount(spotId)
                loadWeather(spot.latitude, spot.longitude)
                loadFavoriteStatus(spotId)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMessageMapper.getMessage(ApiErrorParser.parse(e))
                    )
                }
            }
        }
    }

    private fun loadPhotos(spotId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPhotos = true) }

            try {
                val photos = api.getPhotos(spotId).map { it.toDomain() }
                _uiState.update {
                    it.copy(
                        photos = photos,
                        isLoadingPhotos = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingPhotos = false) }
            }
        }
    }

    private fun loadVisitCount(spotId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getVisitCount(spotId)
                _uiState.update { it.copy(visitCount = response.count) }
            } catch (e: Exception) {
                // Ignore - visit count is optional
            }
        }
    }

    private fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWeather = true) }

            try {
                val response = api.getWeather(lat, lon)
                val weather = WeatherInfo(
                    temperature = response.currentWeather.temperature,
                    windspeed = response.currentWeather.windspeed,
                    winddirection = response.currentWeather.winddirection,
                    weathercode = response.currentWeather.weathercode
                )
                _uiState.update {
                    it.copy(
                        weather = weather,
                        isLoadingWeather = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingWeather = false) }
            }
        }
    }

    private fun loadFavoriteStatus(spotId: Int) {
        viewModelScope.launch {
            try {
                val response = api.checkFavorite(spotId)
                _uiState.update { it.copy(isFavorite = response["is_favorite"] == true) }
            } catch (e: Exception) {
                // Ignore - favorite status is optional
            }
        }
    }

    fun toggleFavorite(spotId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingFavorite = true) }

            try {
                val currentState = _uiState.value.isFavorite
                if (currentState) {
                    api.removeFavorite(spotId)
                } else {
                    api.addFavorite(spotId)
                }
                analyticsManager.logSpotFavorited(spotId, added = !currentState)
                _uiState.update {
                    it.copy(
                        isFavorite = !currentState,
                        isTogglingFavorite = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTogglingFavorite = false,
                        errorMessage = errorMessageMapper.getMessage(ApiErrorParser.parse(e))
                    )
                }
            }
        }
    }

    fun addVisit(spotId: Int, comment: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingVisit = true) }

            try {
                api.createVisit(
                    CreateVisitRequest(
                        spotId = spotId,
                        visitedAt = null,
                        comment = comment
                    )
                )

                analyticsManager.logVisitAdded(spotId)

                _uiState.update {
                    it.copy(
                        isAddingVisit = false,
                        visitAdded = true,
                        visitCount = it.visitCount + 1
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingVisit = false,
                        errorMessage = errorMessageMapper.getMessage(ApiErrorParser.parse(e))
                    )
                }
            }
        }
    }

    fun resetVisitAdded() {
        _uiState.update { it.copy(visitAdded = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
