package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.domain.model.Visit

data class VisitFilter(
    val page: Int = 1,
    val limit: Int = 50,
    val sortOrder: String = "desc"
)

data class PaginatedVisits(
    val visits: List<Visit>,
    val page: Int,
    val totalPages: Int,
    val hasMorePages: Boolean
)

interface VisitRepository {
    suspend fun getVisits(filter: VisitFilter = VisitFilter()): Result<PaginatedVisits>
    suspend fun createVisit(spotId: Int): Result<Visit>
    suspend fun deleteVisit(visitId: Int): Result<Unit>
    suspend fun getVisitCount(spotId: Int): Result<Long>
}
