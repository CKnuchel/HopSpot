package com.kickpaws.hopspot.ui.screens.benchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.domain.model.Bench
import com.kickpaws.hopspot.domain.repository.BenchFilter
import com.kickpaws.hopspot.domain.repository.BenchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BenchListViewModel @Inject constructor(
    private val benchRepository: BenchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BenchListUiState())
    val uiState: StateFlow<BenchListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadBenches()
    }

    fun loadBenches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val filter = buildFilter(page = 1)
            val result = benchRepository.getBenches(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            benches = paginated.benches,
                            currentPage = paginated.page,
                            totalPages = paginated.totalPages,
                            hasMorePages = paginated.page < paginated.totalPages
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

            val filter = buildFilter(page = 1)
            val result = benchRepository.getBenches(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            benches = paginated.benches,
                            currentPage = paginated.page,
                            totalPages = paginated.totalPages,
                            hasMorePages = paginated.page < paginated.totalPages,
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

    fun loadMoreBenches() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.currentPage + 1
            val filter = buildFilter(page = nextPage)
            val result = benchRepository.getBenches(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            benches = it.benches + paginated.benches,
                            currentPage = paginated.page,
                            totalPages = paginated.totalPages,
                            hasMorePages = paginated.page < paginated.totalPages
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            )
        }
    }

    // Search with debounce
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce 300ms
            loadBenches()
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        loadBenches()
    }

    // Filter Sheet
    fun openFilterSheet() {
        _uiState.update { it.copy(isFilterSheetOpen = true) }
    }

    fun closeFilterSheet() {
        _uiState.update { it.copy(isFilterSheetOpen = false) }
    }

    // Filter Options
    fun setFilterHasToilet(value: Boolean?) {
        _uiState.update { it.copy(filterHasToilet = value) }
    }

    fun setFilterHasTrashBin(value: Boolean?) {
        _uiState.update { it.copy(filterHasTrashBin = value) }
    }

    fun setFilterMinRating(value: Int?) {
        _uiState.update { it.copy(filterMinRating = value) }
    }

    fun setSortBy(option: SortOption) {
        _uiState.update { it.copy(sortBy = option) }
    }

    fun applyFilters() {
        closeFilterSheet()
        loadBenches()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                filterHasToilet = null,
                filterHasTrashBin = null,
                filterMinRating = null,
                sortBy = SortOption.NEWEST
            )
        }
        loadBenches()
    }

    // Delete
    fun showDeleteConfirmation(bench: Bench) {
        _uiState.update { it.copy(benchToDelete = bench) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(benchToDelete = null) }
    }

    fun deleteBench() {
        val bench = _uiState.value.benchToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            val result = benchRepository.deleteBench(bench.id)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            benchToDelete = null,
                            benches = it.benches.filter { b -> b.id != bench.id }
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            benchToDelete = null,
                            errorMessage = exception.message ?: "Löschen fehlgeschlagen"
                        )
                    }
                }
            )
        }
    }

    private fun buildFilter(page: Int): BenchFilter {
        val state = _uiState.value
        return BenchFilter(
            page = page,
            limit = 20,
            sortBy = state.sortBy.apiValue,
            sortOrder = if (state.sortBy == SortOption.NAME) "asc" else "desc",
            hasToilet = state.filterHasToilet,
            hasTrashBin = state.filterHasTrashBin,
            minRating = state.filterMinRating,
            search = state.searchQuery.takeIf { it.isNotBlank() }
        )
    }
}