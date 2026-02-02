package com.kickpaws.hopspot.ui.screens.profile

import com.kickpaws.hopspot.domain.model.User

/**
 * UI State for the Profile Screen
 */
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
    // Edit Mode
    val isEditDialogOpen: Boolean = false,
    val editDisplayName: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null
)