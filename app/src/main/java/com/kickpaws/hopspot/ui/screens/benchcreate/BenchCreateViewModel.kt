package com.kickpaws.hopspot.ui.screens.benchcreate

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.domain.repository.BenchRepository
import com.kickpaws.hopspot.domain.repository.PhotoRepository
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class BenchCreateViewModel @Inject constructor(
    private val benchRepository: BenchRepository,
    private val photoRepository: PhotoRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(BenchCreateUiState())
    val uiState: StateFlow<BenchCreateUiState> = _uiState.asStateFlow()

    init {
        loadInitialLocation()
    }

    @SuppressLint("MissingPermission")
    private fun loadInitialLocation() {
        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

                val hasFinePermission = ContextCompat.checkSelfPermission(
                    application, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val hasCoarsePermission = ContextCompat.checkSelfPermission(
                    application, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasFinePermission || hasCoarsePermission) {
                    val location = fusedLocationClient.lastLocation.await()
                    if (location != null) {
                        setLocation(location.latitude, location.longitude)
                    }
                }
            } catch (e: Exception) {
                // Ignore - user can set location manually via GPS button
            }
        }
    }

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

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri, errorMessage = null) }
    }

    fun removePhoto() {
        _uiState.update { it.copy(photoUri = null) }
    }

    fun saveBench() {
        val state = _uiState.value

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