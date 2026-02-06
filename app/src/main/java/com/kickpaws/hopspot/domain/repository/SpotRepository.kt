package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.domain.model.Spot

data class SpotFilter(
    val page: Int = 1,
    val limit: Int = 50,
    val sortBy: String? = null,
    val sortOrder: String? = null,
    val hasToilet: Boolean? = null,
    val hasTrashBin: Boolean? = null,
    val minRating: Int? = null,
    val search: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val radius: Int? = null
)

data class PaginatedSpots(
    val spots: List<Spot>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

interface SpotRepository {
    suspend fun getSpots(filter: SpotFilter = SpotFilter()): Result<PaginatedSpots>
    suspend fun getSpot(id: Int): Result<Spot>
    suspend fun getRandomSpot(): Result<Spot>
    suspend fun createSpot(
        name: String,
        latitude: Double,
        longitude: Double,
        description: String? = null,
        rating: Int? = null,
        hasToilet: Boolean = false,
        hasTrashBin: Boolean = false
    ): Result<Spot>
    suspend fun updateSpot(id: Int, updates: Map<String, Any?>): Result<Spot>
    suspend fun deleteSpot(id: Int): Result<Unit>
}
