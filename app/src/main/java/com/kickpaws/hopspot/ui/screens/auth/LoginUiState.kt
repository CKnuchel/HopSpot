package com.kickpaws.hopspot.ui.screens.auth

/**
 * UI State for the Login Screen
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)