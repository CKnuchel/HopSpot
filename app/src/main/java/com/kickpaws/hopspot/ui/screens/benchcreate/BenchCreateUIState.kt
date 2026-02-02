package com.kickpaws.hopspot.ui.screens.benchcreate

import android.net.Uri

/**
 * UI State for the Bench Create Screen
 */
data class BenchCreateUiState(
    val name: String = "",
    val description: String = "",
    val rating: Int? = null,
    val hasToilet: Boolean = false,
    val hasTrashBin: Boolean = false,

    // Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationText: String = "Standort nicht gesetzt",

    // Photo
    val photoUri: Uri? = null,
    val isUploadingPhoto: Boolean = false,

    // State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val createdBenchId: Int? = null
)