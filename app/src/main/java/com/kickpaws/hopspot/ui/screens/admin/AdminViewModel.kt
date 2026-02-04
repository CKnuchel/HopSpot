package com.kickpaws.hopspot.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.domain.model.InvitationCode
import com.kickpaws.hopspot.domain.model.User
import com.kickpaws.hopspot.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    // Invitation Codes
    val invitationCodes: List<InvitationCode> = emptyList(),
    val isLoadingCodes: Boolean = false,
    val codesError: String? = null,
    val isCreatingCode: Boolean = false,
    val newCodeComment: String = "",
    val showCreateCodeDialog: Boolean = false,
    val createdCode: String? = null,

    // Delete Code
    val showDeleteCodeDialog: Boolean = false,
    val codeToDelete: InvitationCode? = null,
    val isDeletingCode: Boolean = false,

    // Users
    val users: List<User> = emptyList(),
    val isLoadingUsers: Boolean = false,
    val usersError: String? = null,

    // General
    val selectedTabIndex: Int = 0,
    val successMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadInvitationCodes()
        loadUsers()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    // === Invitation Codes ===

    fun loadInvitationCodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCodes = true, codesError = null) }

            adminRepository.getInvitationCodes()
                .onSuccess { codes ->
                    _uiState.update { it.copy(
                        invitationCodes = codes,
                        isLoadingCodes = false
                    )}
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        codesError = e.message ?: "Fehler beim Laden",
                        isLoadingCodes = false
                    )}
                }
        }
    }

    fun showCreateCodeDialog() {
        _uiState.update { it.copy(showCreateCodeDialog = true, newCodeComment = "") }
    }

    fun hideCreateCodeDialog() {
        _uiState.update { it.copy(showCreateCodeDialog = false) }
    }

    fun setNewCodeComment(comment: String) {
        _uiState.update { it.copy(newCodeComment = comment) }
    }

    fun createInvitationCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingCode = true) }

            val comment = _uiState.value.newCodeComment.ifBlank { null }

            adminRepository.createInvitationCode(comment)
                .onSuccess { code ->
                    _uiState.update { it.copy(
                        isCreatingCode = false,
                        showCreateCodeDialog = false,
                        createdCode = code.code,
                        successMessage = "Code erstellt: ${code.code}"
                    )}
                    loadInvitationCodes()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        isCreatingCode = false,
                        codesError = e.message ?: "Fehler beim Erstellen"
                    )}
                }
        }
    }

    fun clearCreatedCode() {
        _uiState.update { it.copy(createdCode = null) }
    }

    // === Delete Invitation Code ===

    fun showDeleteCodeDialog(code: InvitationCode) {
        _uiState.update { it.copy(showDeleteCodeDialog = true, codeToDelete = code) }
    }

    fun hideDeleteCodeDialog() {
        _uiState.update { it.copy(showDeleteCodeDialog = false, codeToDelete = null) }
    }

    fun deleteInvitationCode() {
        val code = _uiState.value.codeToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingCode = true) }

            adminRepository.deleteInvitationCode(code.id)
                .onSuccess {
                    _uiState.update { it.copy(
                        isDeletingCode = false,
                        showDeleteCodeDialog = false,
                        codeToDelete = null,
                        successMessage = "Code gelöscht"
                    )}
                    loadInvitationCodes()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        isDeletingCode = false,
                        codesError = e.message ?: "Fehler beim Löschen"
                    )}
                }
        }
    }

    // === Users ===

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUsers = true, usersError = null) }

            adminRepository.getUsers()
                .onSuccess { users ->
                    _uiState.update { it.copy(
                        users = users,
                        isLoadingUsers = false
                    )}
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        usersError = e.message ?: "Fehler beim Laden",
                        isLoadingUsers = false
                    )}
                }
        }
    }

    fun toggleUserRole(user: User) {
        viewModelScope.launch {
            val newRole = if (user.role == "admin") "user" else "admin"

            adminRepository.updateUser(user.id, role = newRole, isActive = null)
                .onSuccess {
                    _uiState.update { it.copy(
                        successMessage = "${user.displayName} ist jetzt ${newRole.uppercase()}"
                    )}
                    loadUsers()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        usersError = e.message ?: "Fehler beim Aktualisieren"
                    )}
                }
        }
    }

    fun toggleUserActive(user: User, isActive: Boolean) {
        viewModelScope.launch {
            adminRepository.updateUser(user.id, role = null, isActive = isActive)
                .onSuccess {
                    loadUsers()
                    _uiState.update { it.copy(
                        successMessage = if (isActive) "${user.displayName} aktiviert" else "${user.displayName} deaktiviert"
                    )}
                }
                .onFailure { e ->
                    _uiState.update { it.copy(usersError = e.message) }
                }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            adminRepository.deleteUser(user.id)
                .onSuccess {
                    _uiState.update { it.copy(
                        successMessage = "${user.displayName} wurde gelöscht"
                    )}
                    loadUsers()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        usersError = e.message ?: "Fehler beim Löschen"
                    )}
                }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearErrors() {
        _uiState.update { it.copy(codesError = null, usersError = null) }
    }
}