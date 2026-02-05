package com.kickpaws.hopspot.ui.screens.benchdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.CreateVisitRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Bench
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

data class BenchDetailUiState(
    val bench: Bench? = null,
    val photos: List<Photo> = emptyList(),
    val visitCount: Long = 0,
    val weather: WeatherInfo? = null,
    val isLoading: Boolean = true,
    val isLoadingPhotos: Boolean = true,
    val isLoadingWeather: Boolean = false,
    val isAddingVisit: Boolean = false,
    val errorMessage: String? = null,
    val visitAdded: Boolean = false
)

@HiltViewModel
class BenchDetailViewModel @Inject constructor(
    private val api: HopSpotApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(BenchDetailUiState())
    val uiState: StateFlow<BenchDetailUiState> = _uiState.asStateFlow()

    fun loadBench(benchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val response = api.getBench(benchId)
                val bench = response.data.toDomain()

                _uiState.update {
                    it.copy(
                        bench = bench,
                        isLoading = false
                    )
                }

                loadPhotos(benchId)
                loadVisitCount(benchId)
                loadWeather(bench.latitude, bench.longitude)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Fehler beim Laden: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadPhotos(benchId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPhotos = true) }

            try {
                val photos = api.getPhotos(benchId).map { it.toDomain() }
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

    private fun loadVisitCount(benchId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getVisitCount(benchId)
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

    fun addVisit(benchId: Int, comment: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingVisit = true) }

            try {
                api.createVisit(
                    CreateVisitRequest(
                        benchId = benchId,
                        visitedAt = null,
                        comment = comment
                    )
                )

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
                        errorMessage = "Besuch konnte nicht gespeichert werden"
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