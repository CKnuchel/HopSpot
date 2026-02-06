package com.kickpaws.hopspot.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.analytics.AnalyticsManager
import com.kickpaws.hopspot.data.remote.error.ErrorMessageMapper
import com.kickpaws.hopspot.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsManager: AnalyticsManager,
    private val errorMessageMapper: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentState = _uiState.value

        // Validation
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email darf nicht leer sein") }
            return
        }

        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Passwort darf nicht leer sein") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.login(
                email = currentState.email,
                password = currentState.password
            )

            result.fold(
                onSuccess = {
                    analyticsManager.logLogin("email")
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMessageMapper.getMessage(exception)
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
