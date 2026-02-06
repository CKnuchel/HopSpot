package com.kickpaws.hopspot.ui.screens.spotlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.analytics.AnalyticsManager
import com.kickpaws.hopspot.data.remote.error.ErrorMessageMapper
import com.kickpaws.hopspot.domain.model.Spot
import com.kickpaws.hopspot.domain.repository.SpotFilter
import com.kickpaws.hopspot.domain.repository.SpotRepository
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
class SpotListViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val analyticsManager: AnalyticsManager,
    private val errorMessageMapper: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotListUiState())
    val uiState: StateFlow<SpotListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        analyticsManager.logScreenView("SpotList")
        loadSpots()
    }

    fun loadSpots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val filter = buildFilter(page = 1)
            val result = spotRepository.getSpots(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            spots = paginated.spots,
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
                            errorMessage = errorMessageMapper.getMessage(exception)
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
            val result = spotRepository.getSpots(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            spots = paginated.spots,
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
                            errorMessage = errorMessageMapper.getMessage(exception)
                        )
                    }
                }
            )
        }
    }

    fun loadMoreSpots() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.currentPage + 1
            val filter = buildFilter(page = nextPage)
            val result = spotRepository.getSpots(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            spots = it.spots + paginated.spots,
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
            loadSpots()
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        loadSpots()
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

    fun setUserLocation(lat: Double, lon: Double) {
        _uiState.update { it.copy(userLat = lat, userLon = lon) }
        loadSpots()
    }

    fun applyFilters() {
        closeFilterSheet()
        loadSpots()
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
        loadSpots()
    }

    // Delete
    fun showDeleteConfirmation(spot: Spot) {
        _uiState.update { it.copy(spotToDelete = spot) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(spotToDelete = null) }
    }

    fun deleteSpot() {
        val spot = _uiState.value.spotToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            val result = spotRepository.deleteSpot(spot.id)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            spotToDelete = null,
                            spots = it.spots.filter { s -> s.id != spot.id }
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            spotToDelete = null,
                            errorMessage = errorMessageMapper.getMessage(exception)
                        )
                    }
                }
            )
        }
    }

    // Random Spot
    fun getRandomSpot() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRandom = true) }

            val result = spotRepository.getRandomSpot()

            result.fold(
                onSuccess = { spot ->
                    _uiState.update {
                        it.copy(
                            isLoadingRandom = false,
                            randomSpotId = spot.id
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoadingRandom = false,
                            errorMessage = errorMessageMapper.getMessage(exception)
                        )
                    }
                }
            )
        }
    }

    fun clearRandomSpotId() {
        _uiState.update { it.copy(randomSpotId = null) }
    }

    private fun buildFilter(page: Int): SpotFilter {
        val state = _uiState.value
        return SpotFilter(
            page = page,
            limit = 20,
            sortBy = state.sortBy.apiValue,
            sortOrder = when (state.sortBy) {
                SortOption.NAME -> "asc"
                SortOption.DISTANCE -> "asc"
                else -> "desc"
            },
            hasToilet = state.filterHasToilet,
            hasTrashBin = state.filterHasTrashBin,
            minRating = state.filterMinRating,
            search = state.searchQuery.takeIf { it.isNotBlank() },
            lat = state.userLat,
            lon = state.userLon
        )
    }
}
