package com.kickpaws.hopspot.data.repository

import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.CreateVisitRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Visit
import com.kickpaws.hopspot.domain.repository.PaginatedVisits
import com.kickpaws.hopspot.domain.repository.VisitFilter
import com.kickpaws.hopspot.domain.repository.VisitRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitRepositoryImpl @Inject constructor(
    private val api: HopSpotApi
) : VisitRepository {

    override suspend fun getVisits(filter: VisitFilter): Result<PaginatedVisits> {
        return try {
            val response = api.getVisits(
                page = filter.page,
                limit = filter.limit,
                sortOrder = filter.sortOrder
            )

            val visits = response.visits.map { it.toDomain() }
            val pagination = response.pagination

            Result.success(
                PaginatedVisits(
                    visits = visits,
                    page = pagination.page,
                    totalPages = pagination.totalPages,
                    hasMorePages = pagination.page < pagination.totalPages
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createVisit(benchId: Int): Result<Visit> {
        return try {
            val request = CreateVisitRequest(
                benchId = benchId,
                visitedAt = null,
                comment = null
            )
            val response = api.createVisit(request)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVisitCount(benchId: Int): Result<Long> {
        return try {
            val response = api.getVisitCount(benchId)
            Result.success(response.count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
