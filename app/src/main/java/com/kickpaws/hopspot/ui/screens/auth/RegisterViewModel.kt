package com.kickpaws.hopspot.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val errorMessageMapper: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onDisplayNameChange(displayName: String) {
        _uiState.update { it.copy(displayName = displayName, errorMessage = null) }
    }

    fun onInvitationCodeChange(code: String) {
        _uiState.update { it.copy(invitationCode = code, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun register() {
        val currentState = _uiState.value

        // Validation
        if (currentState.displayName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name darf nicht leer sein") }
            return
        }

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email darf nicht leer sein") }
            return
        }

        if (!currentState.email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Ungültige Email-Adresse") }
            return
        }

        if (currentState.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Passwort muss mindestens 8 Zeichen haben") }
            return
        }

        if (currentState.invitationCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Einladungscode erforderlich") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.register(
                email = currentState.email,
                password = currentState.password,
                displayName = currentState.displayName,
                invitationCode = currentState.invitationCode
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isRegistered = true) }
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
