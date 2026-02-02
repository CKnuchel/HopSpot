package com.kickpaws.hopspot.ui.screens.auth

/**
 * UI State for the Register Screen
 */
data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val invitationCode: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
)