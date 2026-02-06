package com.kickpaws.hopspot.data.repository

import android.util.Log
import com.kickpaws.hopspot.data.local.dao.SpotDao
import com.kickpaws.hopspot.data.local.dao.VisitDao
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import com.kickpaws.hopspot.data.local.entity.VisitEntity
import com.kickpaws.hopspot.data.local.mapper.toDomain
import com.kickpaws.hopspot.data.local.mapper.toEntity
import com.kickpaws.hopspot.data.local.mapper.toVisitDomainList
import com.kickpaws.hopspot.data.network.NetworkMonitor
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.CreateVisitRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Visit
import com.kickpaws.hopspot.domain.repository.PaginatedVisits
import com.kickpaws.hopspot.domain.repository.VisitFilter
import com.kickpaws.hopspot.domain.repository.VisitRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

@Singleton
class OfflineFirstVisitRepository @Inject constructor(
    private val api: HopSpotApi,
    private val visitDao: VisitDao,
    private val spotDao: SpotDao,
    private val networkMonitor: NetworkMonitor
) : VisitRepository {

    companion object {
        private const val TAG = "OfflineFirstVisitRepo"
        private const val TEMP_ID_START = -1_000_000
    }

    override suspend fun getVisits(filter: VisitFilter): Result<PaginatedVisits> {
        return if (networkMonitor.isOnlineNow) {
            fetchFromApiAndCache(filter)
        } else {
            fetchFromLocal(filter)
        }
    }

    private suspend fun fetchFromApiAndCache(filter: VisitFilter): Result<PaginatedVisits> {
        return try {
            val response = api.getVisits(
                page = filter.page,
                limit = filter.limit,
                sortOrder = filter.sortOrder
            )

            val visits = response.visits.map { it.toDomain() }
            val pagination = response.pagination

            // Cache to Room (only page 1 for simplicity)
            if (filter.page == 1) {
                // Get pending changes to preserve
                val pendingIds = visitDao.getPendingChanges().map { it.id }.toSet()

                // Insert server visits, don't overwrite pending local changes
                visits.filter { it.id !in pendingIds }.forEach { visit ->
                    visitDao.insert(visit.toEntity(SyncStatus.SYNCED))
                }
            }

            Result.success(
                PaginatedVisits(
                    visits = visits,
                    page = pagination.page,
                    totalPages = pagination.totalPages,
                    hasMorePages = pagination.page < pagination.totalPages
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "API fetch failed, falling back to local", e)
            fetchFromLocal(filter)
        }
    }

    private suspend fun fetchFromLocal(filter: VisitFilter): Result<PaginatedVisits> {
        return try {
            val offset = (filter.page - 1) * filter.limit
            val visits = visitDao.getVisitsPaginated(
                limit = filter.limit,
                offset = offset
            )
            val total = visitDao.getVisitsCount()
            val totalPages = ceil(total.toDouble() / filter.limit).toInt().coerceAtLeast(1)

            Result.success(
                PaginatedVisits(
                    visits = visits.toVisitDomainList(),
                    page = filter.page,
                    totalPages = totalPages,
                    hasMorePages = filter.page < totalPages
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Local fetch failed", e)
            Result.failure(e)
        }
    }

    override suspend fun createVisit(spotId: Int): Result<Visit> {
        return if (networkMonitor.isOnlineNow) {
            createVisitOnline(spotId)
        } else {
            createVisitOffline(spotId)
        }
    }

    private suspend fun createVisitOnline(spotId: Int): Result<Visit> {
        return try {
            val request = CreateVisitRequest(
                spotId = spotId,
                visitedAt = null,
                comment = null
            )
            val response = api.createVisit(request)
            val visit = response.toDomain()

            // Cache to Room
            visitDao.insert(visit.toEntity(SyncStatus.SYNCED))

            Result.success(visit)
        } catch (e: Exception) {
            Log.e(TAG, "Online create failed, creating offline", e)
            createVisitOffline(spotId)
        }
    }

    private suspend fun createVisitOffline(spotId: Int): Result<Visit> {
        return try {
            // Get spot info for the visit
            val spot = spotDao.getSpotById(spotId)
            val spotName = spot?.name ?: "Unknown Spot"
            val spotPhotoUrl = spot?.mainPhotoUrl

            // Generate temporary negative ID for offline-created visit
            val tempId = TEMP_ID_START - System.currentTimeMillis().toInt()
            val now = java.time.Instant.now().toString()

            val entity = VisitEntity(
                id = tempId,
                spotId = spotId,
                spotName = spotName,
                spotPhotoUrl = spotPhotoUrl,
                comment = null,
                visitedAt = now,
                createdAt = now,
                syncStatus = SyncStatus.PENDING_CREATE
            )

            visitDao.insert(entity)

            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVisit(visitId: Int): Result<Unit> {
        return if (networkMonitor.isOnlineNow) {
            deleteVisitOnline(visitId)
        } else {
            deleteVisitOffline(visitId)
        }
    }

    private suspend fun deleteVisitOnline(visitId: Int): Result<Unit> {
        return try {
            api.deleteVisit(visitId)
            visitDao.deleteById(visitId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Online delete failed, marking for offline delete", e)
            deleteVisitOffline(visitId)
        }
    }

    private suspend fun deleteVisitOffline(visitId: Int): Result<Unit> {
        return try {
            val existingEntity = visitDao.getVisitById(visitId)

            if (existingEntity != null) {
                if (existingEntity.syncStatus == SyncStatus.PENDING_CREATE) {
                    // Never synced to server, just delete locally
                    visitDao.deleteById(visitId)
                } else {
                    // Mark for deletion, will be deleted on server during sync
                    visitDao.updateSyncStatus(visitId, SyncStatus.PENDING_DELETE)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVisitCount(spotId: Int): Result<Long> {
        return if (networkMonitor.isOnlineNow) {
            try {
                val response = api.getVisitCount(spotId)
                Result.success(response.count)
            } catch (e: Exception) {
                Log.e(TAG, "API fetch failed, falling back to local", e)
                getVisitCountFromLocal(spotId)
            }
        } else {
            getVisitCountFromLocal(spotId)
        }
    }

    private suspend fun getVisitCountFromLocal(spotId: Int): Result<Long> {
        return try {
            val count = visitDao.getVisitCountForSpot(spotId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
