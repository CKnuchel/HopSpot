package com.kickpaws.hopspot.ui.screens.spotedit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.UpdateSpotRequest
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
class SpotEditViewModel @Inject constructor(
    private val api: HopSpotApi,
    private val photoRepository: PhotoRepository,
    private val errorMessageMapper: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotEditUiState())
    val uiState: StateFlow<SpotEditUiState> = _uiState.asStateFlow()

    private var spotId: Int = 0

    fun loadSpot(id: Int) {
        spotId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val response = api.getSpot(id)
                val spot = response.data.toDomain()

                _uiState.update {
                    it.copy(
                        spot = spot,
                        name = spot.name,
                        description = spot.description ?: "",
                        rating = spot.rating,
                        hasToilet = spot.hasToilet,
                        hasTrashBin = spot.hasTrashBin,
                        latitude = spot.latitude,
                        longitude = spot.longitude,
                        locationText = "%.5f, %.5f".format(spot.latitude, spot.longitude),
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
                val photos = api.getPhotos(spotId).map { it.toDomain() }
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
                spotId = spotId,
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
    fun saveSpot() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name darf nicht leer sein") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                api.updateSpot(
                    id = spotId,
                    request = UpdateSpotRequest(
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

    // Delete spot
    fun deleteSpot() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }

            try {
                api.deleteSpot(spotId)
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
