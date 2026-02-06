package com.kickpaws.hopspot.ui.screens.favorites

import com.kickpaws.hopspot.domain.model.Favorite

data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Pagination
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMorePages: Boolean = false,
    val isLoadingMore: Boolean = false,

    // Remove
    val favoriteToRemove: Favorite? = null,
    val isRemoving: Boolean = false
)
