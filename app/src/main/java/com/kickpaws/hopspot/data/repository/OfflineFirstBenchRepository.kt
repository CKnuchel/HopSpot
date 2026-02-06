package com.kickpaws.hopspot.data.repository

import android.util.Log
import com.kickpaws.hopspot.data.local.dao.BenchDao
import com.kickpaws.hopspot.data.local.entity.BenchEntity
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import com.kickpaws.hopspot.data.local.mapper.toDomain
import com.kickpaws.hopspot.data.local.mapper.toDomainList
import com.kickpaws.hopspot.data.local.mapper.toEntity
import com.kickpaws.hopspot.data.network.NetworkMonitor
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
import kotlin.math.ceil

@Singleton
class OfflineFirstBenchRepository @Inject constructor(
    private val api: HopSpotApi,
    private val benchDao: BenchDao,
    private val networkMonitor: NetworkMonitor
) : BenchRepository {

    companion object {
        private const val TAG = "OfflineFirstBenchRepo"
        private const val TEMP_ID_START = -1_000_000
    }

    override suspend fun getBenches(filter: BenchFilter): Result<PaginatedBenches> {
        return if (networkMonitor.isOnlineNow) {
            fetchFromApiAndCache(filter)
        } else {
            fetchFromLocal(filter)
        }
    }

    private suspend fun fetchFromApiAndCache(filter: BenchFilter): Result<PaginatedBenches> {
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

            val response = apiResponse.data
            val benches = response.benches.map { it.toDomain() }
            val pagination = response.pagination

            // Cache to Room (only page 1 for simplicity, or could cache all)
            if (filter.page == 1) {
                // Get pending changes to preserve
                val pendingIds = benchDao.getPendingChanges().map { it.id }.toSet()

                // Insert server benches, don't overwrite pending local changes
                benches.filter { it.id !in pendingIds }.forEach { bench ->
                    benchDao.insert(bench.toEntity(SyncStatus.SYNCED))
                }
            }

            Result.success(
                PaginatedBenches(
                    benches = benches,
                    page = pagination.page,
                    limit = pagination.limit,
                    total = pagination.total.toInt(),
                    totalPages = pagination.totalPages
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "API fetch failed, falling back to local", e)
            fetchFromLocal(filter)
        }
    }

    private suspend fun fetchFromLocal(filter: BenchFilter): Result<PaginatedBenches> {
        return try {
            val offset = (filter.page - 1) * filter.limit
            val benches = benchDao.getBenchesFiltered(
                search = filter.search,
                hasToilet = filter.hasToilet,
                hasTrashBin = filter.hasTrashBin,
                minRating = filter.minRating,
                limit = filter.limit,
                offset = offset
            )
            val total = benchDao.getBenchesFilteredCount(
                search = filter.search,
                hasToilet = filter.hasToilet,
                hasTrashBin = filter.hasTrashBin,
                minRating = filter.minRating
            )

            Result.success(
                PaginatedBenches(
                    benches = benches.toDomainList(),
                    page = filter.page,
                    limit = filter.limit,
                    total = total,
                    totalPages = ceil(total.toDouble() / filter.limit).toInt().coerceAtLeast(1)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Local fetch failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getBench(id: Int): Result<Bench> {
        return if (networkMonitor.isOnlineNow) {
            try {
                val response = api.getBench(id)
                val bench = response.data.toDomain()

                // Cache to Room
                benchDao.insert(bench.toEntity(SyncStatus.SYNCED))

                Result.success(bench)
            } catch (e: Exception) {
                Log.e(TAG, "API fetch failed, falling back to local", e)
                getBenchFromLocal(id)
            }
        } else {
            getBenchFromLocal(id)
        }
    }

    private suspend fun getBenchFromLocal(id: Int): Result<Bench> {
        return try {
            val entity = benchDao.getBenchById(id)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Bench not found in local database"))
            }
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
        return if (networkMonitor.isOnlineNow) {
            createBenchOnline(name, latitude, longitude, description, rating, hasToilet, hasTrashBin)
        } else {
            createBenchOffline(name, latitude, longitude, description, rating, hasToilet, hasTrashBin)
        }
    }

    private suspend fun createBenchOnline(
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
            val bench = response.data.toDomain()

            // Cache to Room
            benchDao.insert(bench.toEntity(SyncStatus.SYNCED))

            Result.success(bench)
        } catch (e: Exception) {
            Log.e(TAG, "Online create failed, creating offline", e)
            createBenchOffline(name, latitude, longitude, description, rating, hasToilet, hasTrashBin)
        }
    }

    private suspend fun createBenchOffline(
        name: String,
        latitude: Double,
        longitude: Double,
        description: String?,
        rating: Int?,
        hasToilet: Boolean,
        hasTrashBin: Boolean
    ): Result<Bench> {
        return try {
            // Generate temporary negative ID for offline-created bench
            val tempId = TEMP_ID_START - System.currentTimeMillis().toInt()
            val now = java.time.Instant.now().toString()

            val entity = BenchEntity(
                id = tempId,
                name = name,
                latitude = latitude,
                longitude = longitude,
                description = description,
                rating = rating,
                hasToilet = hasToilet,
                hasTrashBin = hasTrashBin,
                mainPhotoUrl = null,
                distance = null,
                createdById = null,
                createdByName = null,
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING_CREATE,
                locallyModifiedAt = System.currentTimeMillis()
            )

            benchDao.insert(entity)

            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBench(id: Int, updates: Map<String, Any?>): Result<Bench> {
        return if (networkMonitor.isOnlineNow) {
            updateBenchOnline(id, updates)
        } else {
            updateBenchOffline(id, updates)
        }
    }

    private suspend fun updateBenchOnline(id: Int, updates: Map<String, Any?>): Result<Bench> {
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
            val bench = response.data.toDomain()

            // Cache to Room
            benchDao.insert(bench.toEntity(SyncStatus.SYNCED))

            Result.success(bench)
        } catch (e: Exception) {
            Log.e(TAG, "Online update failed, updating offline", e)
            updateBenchOffline(id, updates)
        }
    }

    private suspend fun updateBenchOffline(id: Int, updates: Map<String, Any?>): Result<Bench> {
        return try {
            val existingEntity = benchDao.getBenchById(id)
                ?: return Result.failure(Exception("Bench not found"))

            val updatedEntity = existingEntity.copy(
                name = updates["name"] as? String ?: existingEntity.name,
                latitude = updates["latitude"] as? Double ?: existingEntity.latitude,
                longitude = updates["longitude"] as? Double ?: existingEntity.longitude,
                description = updates["description"] as? String ?: existingEntity.description,
                rating = updates["rating"] as? Int ?: existingEntity.rating,
                hasToilet = updates["hasToilet"] as? Boolean ?: existingEntity.hasToilet,
                hasTrashBin = updates["hasTrashBin"] as? Boolean ?: existingEntity.hasTrashBin,
                syncStatus = if (existingEntity.syncStatus == SyncStatus.PENDING_CREATE) {
                    SyncStatus.PENDING_CREATE // Keep as create if not yet synced
                } else {
                    SyncStatus.PENDING_UPDATE
                },
                locallyModifiedAt = System.currentTimeMillis()
            )

            benchDao.update(updatedEntity)

            Result.success(updatedEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBench(id: Int): Result<Unit> {
        return if (networkMonitor.isOnlineNow) {
            deleteBenchOnline(id)
        } else {
            deleteBenchOffline(id)
        }
    }

    private suspend fun deleteBenchOnline(id: Int): Result<Unit> {
        return try {
            api.deleteBench(id)
            benchDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Online delete failed, marking for offline delete", e)
            deleteBenchOffline(id)
        }
    }

    private suspend fun deleteBenchOffline(id: Int): Result<Unit> {
        return try {
            val existingEntity = benchDao.getBenchById(id)

            if (existingEntity != null) {
                if (existingEntity.syncStatus == SyncStatus.PENDING_CREATE) {
                    // Never synced to server, just delete locally
                    benchDao.deleteById(id)
                } else {
                    // Mark for deletion, will be deleted on server during sync
                    benchDao.updateSyncStatus(id, SyncStatus.PENDING_DELETE)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
