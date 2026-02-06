package com.kickpaws.hopspot.data.repository

import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.error.ApiErrorParser
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.repository.ActivityFilter
import com.kickpaws.hopspot.domain.repository.ActivityRepository
import com.kickpaws.hopspot.domain.repository.PaginatedActivities
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val api: HopSpotApi
) : ActivityRepository {

    override suspend fun getActivities(filter: ActivityFilter): Result<PaginatedActivities> {
        return try {
            val response = api.getActivities(
                page = filter.page,
                limit = filter.limit,
                actionType = filter.actionType
            )

            val activities = response.activities.map { it.toDomain() }
            val pagination = response.pagination

            Result.success(
                PaginatedActivities(
                    activities = activities,
                    page = pagination.page,
                    totalPages = pagination.totalPages,
                    hasMorePages = pagination.page < pagination.totalPages
                )
            )
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.parse(e))
        }
    }
}
