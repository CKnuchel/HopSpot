package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.domain.model.Favorite

data class FavoriteFilter(
    val page: Int = 1,
    val limit: Int = 50
)

data class PaginatedFavorites(
    val favorites: List<Favorite>,
    val page: Int,
    val totalPages: Int,
    val hasMorePages: Boolean
)

interface FavoriteRepository {
    suspend fun getFavorites(filter: FavoriteFilter = FavoriteFilter()): Result<PaginatedFavorites>
    suspend fun addFavorite(benchId: Int): Result<Boolean>
    suspend fun removeFavorite(benchId: Int): Result<Boolean>
    suspend fun isFavorite(benchId: Int): Result<Boolean>
}
