package com.kickpaws.hopspot.ui.screens.visits

import com.kickpaws.hopspot.domain.model.Visit

data class VisitsUiState(
    val visits: List<Visit> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Pagination
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMorePages: Boolean = false,
    val isLoadingMore: Boolean = false
)
