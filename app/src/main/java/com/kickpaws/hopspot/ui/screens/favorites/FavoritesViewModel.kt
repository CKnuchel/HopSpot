package com.kickpaws.hopspot.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.data.analytics.AnalyticsManager
import com.kickpaws.hopspot.domain.model.Favorite
import com.kickpaws.hopspot.domain.repository.FavoriteFilter
import com.kickpaws.hopspot.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.logScreenView("Favorites")
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val filter = FavoriteFilter(page = 1)
            val result = favoriteRepository.getFavorites(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            favorites = paginated.favorites,
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

            val filter = FavoriteFilter(page = 1)
            val result = favoriteRepository.getFavorites(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            favorites = paginated.favorites,
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

    fun loadMoreFavorites() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.currentPage + 1
            val filter = FavoriteFilter(page = nextPage)
            val result = favoriteRepository.getFavorites(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            favorites = it.favorites + paginated.favorites,
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

    fun showRemoveDialog(favorite: Favorite) {
        _uiState.update { it.copy(favoriteToRemove = favorite) }
    }

    fun dismissRemoveDialog() {
        _uiState.update { it.copy(favoriteToRemove = null) }
    }

    fun confirmRemove() {
        val favoriteToRemove = _uiState.value.favoriteToRemove ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true) }

            val result = favoriteRepository.removeFavorite(favoriteToRemove.benchId)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isRemoving = false,
                            favoriteToRemove = null,
                            favorites = it.favorites.filter { f -> f.id != favoriteToRemove.id }
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isRemoving = false,
                            favoriteToRemove = null,
                            errorMessage = exception.message ?: "Fehler beim Entfernen"
                        )
                    }
                }
            )
        }
    }
}
