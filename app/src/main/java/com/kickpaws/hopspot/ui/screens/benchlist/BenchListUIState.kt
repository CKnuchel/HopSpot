package com.kickpaws.hopspot.ui.screens.benchlist

import com.kickpaws.hopspot.domain.model.Bench

/**
 * UI State for the Bench List Screen
 */
data class BenchListUiState(
    val benches: List<Bench> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Pagination
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMorePages: Boolean = false,
    val isLoadingMore: Boolean = false,

    // Filters
    val isFilterSheetOpen: Boolean = false,
    val searchQuery: String = "",
    val filterHasToilet: Boolean? = null,
    val filterHasTrashBin: Boolean? = null,
    val filterMinRating: Int? = null,
    val sortBy: SortOption = SortOption.NEWEST,

    // Delete
    val benchToDelete: Bench? = null,
    val isDeleting: Boolean = false
)

enum class SortOption(val apiValue: String, val displayName: String) {
    NEWEST("created_at", "Neueste"),
    NAME("name", "Name"),
    RATING("rating", "Bewertung"),
    // DISTANCE("distance", "Entfernung")  // Später mit GPS
}