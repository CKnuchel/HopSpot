package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.domain.model.Bench

data class BenchFilter(
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

data class PaginatedBenches(
    val benches: List<Bench>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

interface BenchRepository {
    suspend fun getBenches(filter: BenchFilter = BenchFilter()): Result<PaginatedBenches>
    suspend fun getBench(id: Int): Result<Bench>
    suspend fun createBench(
        name: String,
        latitude: Double,
        longitude: Double,
        description: String? = null,
        rating: Int? = null,
        hasToilet: Boolean = false,
        hasTrashBin: Boolean = false
    ): Result<Bench>
    suspend fun updateBench(id: Int, updates: Map<String, Any?>): Result<Bench>
    suspend fun deleteBench(id: Int): Result<Unit>
}