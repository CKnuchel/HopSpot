package com.kickpaws.hopspot.ui.screens.benchcreate

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.domain.repository.BenchRepository
import com.kickpaws.hopspot.domain.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BenchCreateViewModel @Inject constructor(
    private val benchRepository: BenchRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BenchCreateUiState())
    val uiState: StateFlow<BenchCreateUiState> = _uiState.asStateFlow()

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

    // Temporär: Manuelle Koordinaten-Eingabe
    fun setManualLocation(latStr: String, lonStr: String) {
        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()

        if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
            setLocation(lat, lon)
        } else {
            _uiState.update { it.copy(errorMessage = "Ungültige Koordinaten") }
        }
    }

    // Photo
    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri, errorMessage = null) }
    }

    fun removePhoto() {
        _uiState.update { it.copy(photoUri = null) }
    }

    fun saveBench() {
        val state = _uiState.value

        // Validierung
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name darf nicht leer sein") }
            return
        }

        if (state.latitude == null || state.longitude == null) {
            _uiState.update { it.copy(errorMessage = "Standort muss gesetzt werden") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            // 1. Create Bench
            val result = benchRepository.createBench(
                name = state.name.trim(),
                latitude = state.latitude,
                longitude = state.longitude,
                description = state.description.takeIf { it.isNotBlank() },
                rating = state.rating,
                hasToilet = state.hasToilet,
                hasTrashBin = state.hasTrashBin
            )

            result.fold(
                onSuccess = { bench ->
                    // 2. Upload Photo if selected
                    if (state.photoUri != null) {
                        _uiState.update { it.copy(isUploadingPhoto = true) }

                        val photoResult = photoRepository.uploadPhoto(
                            benchId = bench.id,
                            photoUri = state.photoUri,
                            isMain = true
                        )

                        photoResult.fold(
                            onSuccess = {
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        isUploadingPhoto = false,
                                        createdBenchId = bench.id
                                    )
                                }
                            },
                            onFailure = { photoError ->
                                // Bank wurde erstellt, aber Photo fehlgeschlagen
                                // Trotzdem zur Detail-Seite navigieren
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        isUploadingPhoto = false,
                                        createdBenchId = bench.id,
                                        errorMessage = "Bank erstellt, aber Photo-Upload fehlgeschlagen"
                                    )
                                }
                            }
                        )
                    } else {
                        // Keine Photo, direkt fertig
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                createdBenchId = bench.id
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = exception.message ?: "Speichern fehlgeschlagen"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}