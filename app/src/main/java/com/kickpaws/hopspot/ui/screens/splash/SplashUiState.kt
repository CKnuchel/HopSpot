package com.kickpaws.hopspot.ui.screens.splash

data class SplashUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean? = null,
    val error: String? = null
)
