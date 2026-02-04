package com.kickpaws.hopspot.data.repository

import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.CreateBenchRequest
import com.kickpaws.hopspot.data.remote.dto.UpdateBenchRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Bench
import com.kickpaws.hopspot.domain.repository.BenchFilter
import com.kickpaws.hopspot.domain.repository.BenchRepository
import com.kickpaws.hopspot.domain.repository.PaginatedBenches
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BenchRepositoryImpl @Inject constructor(
    private val api: HopSpotApi
) : BenchRepository {

    override suspend fun getBenches(filter: BenchFilter): Result<PaginatedBenches> {
        return try {
            val apiResponse = api.getBenches(
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
            val benches = response.benches?.map { it.toDomain() } ?: emptyList()
            val pagination = response.pagination

            Result.success(
                PaginatedBenches(
                    benches = benches,
                    page = pagination?.page ?: 1,
                    limit = pagination?.limit ?: filter.limit,
                    total = pagination?.total?.toInt() ?: benches.size,
                    totalPages = pagination?.totalPages ?: 1
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBench(id: Int): Result<Bench> {
        return try {
            val response = api.getBench(id)
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createBench(
        name: String,
        latitude: Double,
        longitude: Double,
        description: String?,
        rating: Int?,
        hasToilet: Boolean,
        hasTrashBin: Boolean
    ): Result<Bench> {
        return try {
            val request = CreateBenchRequest(
                name = name,
                latitude = latitude,
                longitude = longitude,
                description = description,
                rating = rating,
                hasToilet = hasToilet,
                hasTrashBin = hasTrashBin
            )
            val response = api.createBench(request)
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBench(id: Int, updates: Map<String, Any?>): Result<Bench> {
        return try {
            val request = UpdateBenchRequest(
                name = updates["name"] as? String,
                latitude = updates["latitude"] as? Double,
                longitude = updates["longitude"] as? Double,
                description = updates["description"] as? String,
                rating = updates["rating"] as? Int,
                hasToilet = updates["hasToilet"] as? Boolean,
                hasTrashBin = updates["hasTrashBin"] as? Boolean
            )
            val response = api.updateBench(id, request)
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBench(id: Int): Result<Unit> {
        return try {
            api.deleteBench(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}