package com.kickpaws.hopspot.ui.screens.activityfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.analytics.AnalyticsManager
import com.kickpaws.hopspot.data.remote.error.ErrorMessageMapper
import com.kickpaws.hopspot.domain.repository.ActivityFilter
import com.kickpaws.hopspot.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityFeedViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val analyticsManager: AnalyticsManager,
    private val errorMessageMapper: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityFeedUiState())
    val uiState: StateFlow<ActivityFeedUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.logScreenView("ActivityFeed")
        loadActivities()
    }

    fun loadActivities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val filter = ActivityFilter(page = 1)
            val result = activityRepository.getActivities(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activities = paginated.activities,
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

            val filter = ActivityFilter(page = 1)
            val result = activityRepository.getActivities(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            activities = paginated.activities,
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
                            errorMessage = errorMessageMapper.getMessage(exception)
                        )
                    }
                }
            )
        }
    }

    fun loadMoreActivities() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.currentPage + 1
            val filter = ActivityFilter(page = nextPage)
            val result = activityRepository.getActivities(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            activities = it.activities + paginated.activities,
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
}
