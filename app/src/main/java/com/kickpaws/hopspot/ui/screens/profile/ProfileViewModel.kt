package com.kickpaws.hopspot.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.analytics.AnalyticsManager
import com.kickpaws.hopspot.domain.repository.AuthRepository
import com.kickpaws.hopspot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.logScreenView("Profile")
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = userRepository.getMe()

            result.fold(
                onSuccess = { user ->
                    analyticsManager.setUserId(user.id)
                    analyticsManager.setUserRole(user.role)
                    _uiState.update { it.copy(isLoading = false, user = user) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Profil konnte nicht geladen werden"
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }

            // Logout (clears tokens)
            authRepository.logout()

            analyticsManager.logLogout()
            analyticsManager.setUserId(null)

            _uiState.update { it.copy(isLoggingOut = false, isLoggedOut = true) }
        }
    }

    // Edit Functions
    fun openEditDialog() {
        val currentName = _uiState.value.user?.displayName ?: ""
        _uiState.update {
            it.copy(
                isEditDialogOpen = true,
                editDisplayName = currentName,
                saveError = null
            )
        }
    }

    fun closeEditDialog() {
        _uiState.update {
            it.copy(
                isEditDialogOpen = false,
                editDisplayName = "",
                saveError = null
            )
        }
    }

    fun onEditDisplayNameChange(name: String) {
        _uiState.update { it.copy(editDisplayName = name, saveError = null) }
    }

    fun saveProfile() {
        val newName = _uiState.value.editDisplayName.trim()

        if (newName.isBlank()) {
            _uiState.update { it.copy(saveError = "Name darf nicht leer sein") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            val result = userRepository.updateProfile(newName)

            result.fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isEditDialogOpen = false,
                            user = updatedUser,
                            editDisplayName = ""
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = exception.message ?: "Speichern fehlgeschlagen"
                        )
                    }
                }
            )
        }
    }
}