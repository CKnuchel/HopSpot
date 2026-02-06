package com.kickpaws.hopspot.ui.screens.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.domain.repository.VisitFilter
import com.kickpaws.hopspot.domain.repository.VisitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitsViewModel @Inject constructor(
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitsUiState())
    val uiState: StateFlow<VisitsUiState> = _uiState.asStateFlow()

    init {
        loadVisits()
    }

    fun loadVisits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val filter = VisitFilter(page = 1, sortOrder = "desc")
            val result = visitRepository.getVisits(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            visits = paginated.visits,
                            currentPage = paginated.page,
                            totalPages = paginated.totalPages,
                            hasMorePages = paginated.hasMorePages
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Fehler beim Laden"
                        )
                    }
                }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val filter = VisitFilter(page = 1, sortOrder = "desc")
            val result = visitRepository.getVisits(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            visits = paginated.visits,
                            currentPage = paginated.page,
                            totalPages = paginated.totalPages,
                            hasMorePages = paginated.hasMorePages,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = exception.message ?: "Fehler beim Laden"
                        )
                    }
                }
            )
        }
    }

    fun loadMoreVisits() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.currentPage + 1
            val filter = VisitFilter(page = nextPage, sortOrder = "desc")
            val result = visitRepository.getVisits(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            visits = it.visits + paginated.visits,
                            currentPage = paginated.page,
                            totalPages = paginated.totalPages,
                            hasMorePages = paginated.hasMorePages
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            )
        }
    }

    fun showDeleteDialog(visit: com.kickpaws.hopspot.domain.model.Visit) {
        _uiState.update { it.copy(visitToDelete = visit) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(visitToDelete = null) }
    }

    fun confirmDelete() {
        val visitToDelete = _uiState.value.visitToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            val result = visitRepository.deleteVisit(visitToDelete.id)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            visitToDelete = null,
                            visits = it.visits.filter { v -> v.id != visitToDelete.id }
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            visitToDelete = null,
                            errorMessage = exception.message ?: "Fehler beim Loeschen"
                        )
                    }
                }
            )
        }
    }
}
