package com.kickpaws.hopspot.ui.screens.benchlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kickpaws.hopspot.R
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

    // Location
    val userLat: Double? = null,
    val userLon: Double? = null,

    // Delete
    val benchToDelete: Bench? = null,
    val isDeleting: Boolean = false,

    // Random Bench
    val randomBenchId: Int? = null,
    val isLoadingRandom: Boolean = false
)

enum class SortOption(val apiValue: String) {
    NEWEST("created_at"),
    NAME("name"),
    RATING("rating"),
    DISTANCE("distance")
}

@Composable
fun SortOption.displayName(): String = when (this) {
    SortOption.NEWEST -> stringResource(R.string.sort_newest)
    SortOption.NAME -> stringResource(R.string.sort_name)
    SortOption.RATING -> stringResource(R.string.sort_rating)
    SortOption.DISTANCE -> stringResource(R.string.sort_distance)
}
