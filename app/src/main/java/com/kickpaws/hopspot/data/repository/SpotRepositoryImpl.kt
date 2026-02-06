package com.kickpaws.hopspot.data.repository

import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.CreateSpotRequest
import com.kickpaws.hopspot.data.remote.dto.UpdateSpotRequest
import com.kickpaws.hopspot.data.remote.error.ApiErrorParser
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Spot
import com.kickpaws.hopspot.domain.repository.SpotFilter
import com.kickpaws.hopspot.domain.repository.SpotRepository
import com.kickpaws.hopspot.domain.repository.PaginatedSpots
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotRepositoryImpl @Inject constructor(
    private val api: HopSpotApi
) : SpotRepository {

    override suspend fun getSpots(filter: SpotFilter): Result<PaginatedSpots> {
        return try {
            val apiResponse = api.getSpots(
                page = filter.page,
                limit = filter.limit,
                sortBy = filter.sortBy,
                sortOrder = filter.sortOrder,
                hasToilet = filter.hasToilet,
                hasTrashBin = filter.hasTrashBin,
                minRating = filter.minRating,
                search = filter.search,
                lat = filter.lat,
                lon = filter.lon,
                radius = filter.radius
            )

            // Unwrap from "data" wrapper
            val response = apiResponse.data
            val spots = response.spots?.map { it.toDomain() } ?: emptyList()
            val pagination = response.pagination

            Result.success(
                PaginatedSpots(
                    spots = spots,
                    page = pagination?.page ?: 1,
                    limit = pagination?.limit ?: filter.limit,
                    total = pagination?.total?.toInt() ?: spots.size,
                    totalPages = pagination?.totalPages ?: 1
                )
            )
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun getSpot(id: Int): Result<Spot> {
        return try {
            val response = api.getSpot(id)
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun getRandomSpot(): Result<Spot> {
        return try {
            val response = api.getRandomSpot()
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun createSpot(
        name: String,
        latitude: Double,
        longitude: Double,
        description: String?,
        rating: Int?,
        hasToilet: Boolean,
        hasTrashBin: Boolean
    ): Result<Spot> {
        return try {
            val request = CreateSpotRequest(
                name = name,
                latitude = latitude,
                longitude = longitude,
                description = description,
                rating = rating,
                hasToilet = hasToilet,
                hasTrashBin = hasTrashBin
            )
            val response = api.createSpot(request)
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun updateSpot(id: Int, updates: Map<String, Any?>): Result<Spot> {
        return try {
            val request = UpdateSpotRequest(
                name = updates["name"] as? String,
                latitude = updates["latitude"] as? Double,
                longitude = updates["longitude"] as? Double,
                description = updates["description"] as? String,
                rating = updates["rating"] as? Int,
                hasToilet = updates["hasToilet"] as? Boolean,
                hasTrashBin = updates["hasTrashBin"] as? Boolean
            )
            val response = api.updateSpot(id, request)
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }

    override suspend fun deleteSpot(id: Int): Result<Unit> {
        return try {
            api.deleteSpot(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }
}
