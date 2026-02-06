package com.kickpaws.hopspot.domain.repository

import com.kickpaws.hopspot.domain.model.Activity

data class ActivityFilter(
    val page: Int = 1,
    val limit: Int = 50,
    val actionType: String? = null
)

data class PaginatedActivities(
    val activities: List<Activity>,
    val page: Int,
    val totalPages: Int,
    val hasMorePages: Boolean
)

interface ActivityRepository {
    suspend fun getActivities(filter: ActivityFilter = ActivityFilter()): Result<PaginatedActivities>
}
