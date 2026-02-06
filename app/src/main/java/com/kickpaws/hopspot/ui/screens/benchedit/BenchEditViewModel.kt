package com.kickpaws.hopspot.ui.screens.benchedit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.UpdateBenchRequest
import com.kickpaws.hopspot.data.remote.error.ErrorMessageMapper
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Photo
import com.kickpaws.hopspot.domain.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BenchEditViewModel @Inject constructor(
    private val api: HopSpotApi,
    private val photoRepository: PhotoRepository,
    private val errorMessageMapper: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(BenchEditUiState())
    val uiState: StateFlow<BenchEditUiState> = _uiState.asStateFlow()

    private var benchId: Int = 0

    fun loadBench(id: Int) {
        benchId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val response = api.getBench(id)
                val bench = response.data.toDomain()

                _uiState.update {
                    it.copy(
                        bench = bench,
                        name = bench.name,
                        description = bench.description ?: "",
                        rating = bench.rating,
                        hasToilet = bench.hasToilet,
                        hasTrashBin = bench.hasTrashBin,
                        latitude = bench.latitude,
                        longitude = bench.longitude,
                        locationText = "%.5f, %.5f".format(bench.latitude, bench.longitude),
                        isLoading = false
                    )
                }

                // Load photos
                loadPhotos()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMessageMapper.getMessage(e)
                    )
                }
            }
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            try {
                val photos = api.getPhotos(benchId).map { it.toDomain() }
                _uiState.update { it.copy(photos = photos) }
            } catch (e: Exception) {
                // Ignore - photos are optional
            }
        }
    }

    // Field updates
    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onRatingChange(rating: Int?) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun onHasToiletChange(hasToilet: Boolean) {
        _uiState.update { it.copy(hasToilet = hasToilet) }
    }

    fun onHasTrashBinChange(hasTrashBin: Boolean) {
        _uiState.update { it.copy(hasTrashBin = hasTrashBin) }
    }

    fun setLocation(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                latitude = latitude,
                longitude = longitude,
                locationText = "%.5f, %.5f".format(latitude, longitude),
                errorMessage = null
            )
        }
    }

    fun setManualLocation(latStr: String, lonStr: String) {
        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()

        if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
            setLocation(lat, lon)
        } else {
            _uiState.update { it.copy(errorMessage = "Ungültige Koordinaten") }
        }
    }

    // Photo management
    fun onPhotoSelected(uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null) }

            val isFirstPhoto = _uiState.value.photos.isEmpty()

            val result = photoRepository.uploadPhoto(
                benchId = benchId,
                photoUri = uri,
                isMain = isFirstPhoto
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isUploadingPhoto = false) }
                    loadPhotos() // Reload photos
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage = errorMessageMapper.getMessage(e)
                        )
                    }
                }
            )
        }
    }

    fun setMainPhoto(photo: Photo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSettingMainPhoto = true) }

            try {
                api.setMainPhoto(photo.id)
                loadPhotos() // Reload to update main status
                _uiState.update { it.copy(isSettingMainPhoto = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSettingMainPhoto = false,
                        errorMessage = errorMessageMapper.getMessage(e)
                    )
                }
            }
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingPhoto = true) }

            try {
                api.deletePhoto(photo.id)
                loadPhotos() // Reload photos
                _uiState.update { it.copy(isDeletingPhoto = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeletingPhoto = false,
                        errorMessage = errorMessageMapper.getMessage(e)
                    )
                }
            }
        }
    }

    // Save changes
    fun saveBench() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name darf nicht leer sein") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                api.updateBench(
                    id = benchId,
                    request = UpdateBenchRequest(
                        name = state.name.trim(),
                        latitude = state.latitude,
                        longitude = state.longitude,
                        description = state.description.takeIf { it.isNotBlank() },
                        rating = state.rating,
                        hasToilet = state.hasToilet,
                        hasTrashBin = state.hasTrashBin
                    )
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = errorMessageMapper.getMessage(e)
                    )
                }
            }
        }
    }

    // Delete bench
    fun deleteBench() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }

            try {
                api.deleteBench(benchId)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deleteSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = errorMessageMapper.getMessage(e)
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}