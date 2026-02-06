package com.kickpaws.hopspot.ui.screens.benchedit

import android.net.Uri
import com.kickpaws.hopspot.domain.model.Bench
import com.kickpaws.hopspot.domain.model.Photo

data class BenchEditUiState(
    // Bench data
    val bench: Bench? = null,
    val name: String = "",
    val description: String = "",
    val rating: Int? = null,
    val hasToilet: Boolean = false,
    val hasTrashBin: Boolean = false,

    // Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationText: String = "Standort nicht gesetzt",

    // Photos
    val photos: List<Photo> = emptyList(),
    val pendingPhotoUri: Uri? = null,

    // State
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isDeletingPhoto: Boolean = false,
    val isSettingMainPhoto: Boolean = false,
    val isDeleting: Boolean = false,

    // Results
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false
)