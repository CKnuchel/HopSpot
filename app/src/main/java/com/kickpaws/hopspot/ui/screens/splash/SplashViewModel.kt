package com.kickpaws.hopspot.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.local.CurrentUserManager
import com.kickpaws.hopspot.data.local.TokenManager
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val api: HopSpotApi,
    private val currentUserManager: CurrentUserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val isLoggedIn = tokenManager.isLoggedIn()

            if (isLoggedIn) {
                try {
                    val user = api.getMe().toDomain()
                    currentUserManager.setUser(user)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } catch (e: Exception) {
                    tokenManager.clearTokens()
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
            }
        }
    }
}
