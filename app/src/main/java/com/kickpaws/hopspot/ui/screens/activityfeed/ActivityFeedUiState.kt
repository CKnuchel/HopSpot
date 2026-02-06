package com.kickpaws.hopspot.ui.screens.activityfeed

import com.kickpaws.hopspot.domain.model.Activity

data class ActivityFeedUiState(
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Pagination
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMorePages: Boolean = false,
    val isLoadingMore: Boolean = false
)
