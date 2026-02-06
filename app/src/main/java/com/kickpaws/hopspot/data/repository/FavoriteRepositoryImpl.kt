package com.kickpaws.hopspot.data.repository

import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.error.ApiErrorParser
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.repository.FavoriteFilter
import com.kickpaws.hopspot.domain.repository.FavoriteRepository
import com.kickpaws.hopspot.domain.repository.PaginatedFavorites
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val api: HopSpotApi
) : FavoriteRepository {

    override suspend fun getFavorites(filter: FavoriteFilter): Result<PaginatedFavorites> {
        return try {
            val response = api.getFavorites(
                page = filter.page,
                limit = filter.limit
            )

            val favorites = response.favorites.map { it.toDomain() }
            val pagination = response.pagination

            Result.success(
                PaginatedFavorites(
                    favorites = favorites,
                    page = pagination.page,
                    totalPages = pagination.totalPages,
                    hasMorePages = pagination.page < pagination.totalPages
                )
            )
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun addFavorite(benchId: Int): Result<Boolean> {
        return try {
            val response = api.addFavorite(benchId)
            Result.success(response["is_favorite"] == true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun removeFavorite(benchId: Int): Result<Boolean> {
        return try {
            val response = api.removeFavorite(benchId)
            Result.success(response["is_favorite"] == false)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun isFavorite(benchId: Int): Result<Boolean> {
        return try {
            val response = api.checkFavorite(benchId)
            Result.success(response["is_favorite"] == true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }
}
