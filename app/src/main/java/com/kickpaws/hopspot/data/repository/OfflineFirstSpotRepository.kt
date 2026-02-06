package com.kickpaws.hopspot.data.repository

import android.util.Log
import com.kickpaws.hopspot.data.local.dao.SpotDao
import com.kickpaws.hopspot.data.local.entity.SpotEntity
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import com.kickpaws.hopspot.data.local.mapper.toDomain
import com.kickpaws.hopspot.data.local.mapper.toDomainList
import com.kickpaws.hopspot.data.local.mapper.toEntity
import com.kickpaws.hopspot.data.network.NetworkMonitor
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.CreateSpotRequest
import com.kickpaws.hopspot.data.remote.dto.UpdateSpotRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Spot
import com.kickpaws.hopspot.domain.repository.SpotFilter
import com.kickpaws.hopspot.domain.repository.SpotRepository
import com.kickpaws.hopspot.domain.repository.PaginatedSpots
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

@Singleton
class OfflineFirstSpotRepository @Inject constructor(
    private val api: HopSpotApi,
    private val spotDao: SpotDao,
    private val networkMonitor: NetworkMonitor
) : SpotRepository {

    companion object {
        private const val TAG = "OfflineFirstSpotRepo"
        private const val TEMP_ID_START = -1_000_000
    }

    override suspend fun getSpots(filter: SpotFilter): Result<PaginatedSpots> {
        return if (networkMonitor.isOnlineNow) {
            fetchFromApiAndCache(filter)
        } else {
            fetchFromLocal(filter)
        }
    }

    private suspend fun fetchFromApiAndCache(filter: SpotFilter): Result<PaginatedSpots> {
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

            val response = apiResponse.data
            val spots = response.spots.map { it.toDomain() }
            val pagination = response.pagination

            // Cache to Room (only page 1 for simplicity, or could cache all)
            if (filter.page == 1) {
                // Get pending changes to preserve
                val pendingIds = spotDao.getPendingChanges().map { it.id }.toSet()

                // Insert server spots, don't overwrite pending local changes
                spots.filter { it.id !in pendingIds }.forEach { spot ->
                    spotDao.insert(spot.toEntity(SyncStatus.SYNCED))
                }
            }

            Result.success(
                PaginatedSpots(
                    spots = spots,
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

    private suspend fun fetchFromLocal(filter: SpotFilter): Result<PaginatedSpots> {
        return try {
            val offset = (filter.page - 1) * filter.limit
            val spots = spotDao.getSpotsFiltered(
                search = filter.search,
                hasToilet = filter.hasToilet,
                hasTrashBin = filter.hasTrashBin,
                minRating = filter.minRating,
                limit = filter.limit,
                offset = offset
            )
            val total = spotDao.getSpotsFilteredCount(
                search = filter.search,
                hasToilet = filter.hasToilet,
                hasTrashBin = filter.hasTrashBin,
                minRating = filter.minRating
            )

            Result.success(
                PaginatedSpots(
                    spots = spots.toDomainList(),
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

    override suspend fun getSpot(id: Int): Result<Spot> {
        return if (networkMonitor.isOnlineNow) {
            try {
                val response = api.getSpot(id)
                val spot = response.data.toDomain()

                // Cache to Room
                spotDao.insert(spot.toEntity(SyncStatus.SYNCED))

                Result.success(spot)
            } catch (e: Exception) {
                Log.e(TAG, "API fetch failed, falling back to local", e)
                getSpotFromLocal(id)
            }
        } else {
            getSpotFromLocal(id)
        }
    }

    private suspend fun getSpotFromLocal(id: Int): Result<Spot> {
        return try {
            val entity = spotDao.getSpotById(id)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Spot not found in local database"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRandomSpot(): Result<Spot> {
        return try {
            val response = api.getRandomSpot()
            val spot = response.data.toDomain()
            Result.success(spot)
        } catch (e: Exception) {
            Result.failure(e)
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
        return if (networkMonitor.isOnlineNow) {
            createSpotOnline(name, latitude, longitude, description, rating, hasToilet, hasTrashBin)
        } else {
            createSpotOffline(name, latitude, longitude, description, rating, hasToilet, hasTrashBin)
        }
    }

    private suspend fun createSpotOnline(
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
            val spot = response.data.toDomain()

            // Cache to Room
            spotDao.insert(spot.toEntity(SyncStatus.SYNCED))

            Result.success(spot)
        } catch (e: Exception) {
            Log.e(TAG, "Online create failed, creating offline", e)
            createSpotOffline(name, latitude, longitude, description, rating, hasToilet, hasTrashBin)
        }
    }

    private suspend fun createSpotOffline(
        name: String,
        latitude: Double,
        longitude: Double,
        description: String?,
        rating: Int?,
        hasToilet: Boolean,
        hasTrashBin: Boolean
    ): Result<Spot> {
        return try {
            // Generate temporary negative ID for offline-created spot
            val tempId = TEMP_ID_START - System.currentTimeMillis().toInt()
            val now = java.time.Instant.now().toString()

            val entity = SpotEntity(
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

            spotDao.insert(entity)

            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSpot(id: Int, updates: Map<String, Any?>): Result<Spot> {
        return if (networkMonitor.isOnlineNow) {
            updateSpotOnline(id, updates)
        } else {
            updateSpotOffline(id, updates)
        }
    }

    private suspend fun updateSpotOnline(id: Int, updates: Map<String, Any?>): Result<Spot> {
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
            val spot = response.data.toDomain()

            // Cache to Room
            spotDao.insert(spot.toEntity(SyncStatus.SYNCED))

            Result.success(spot)
        } catch (e: Exception) {
            Log.e(TAG, "Online update failed, updating offline", e)
            updateSpotOffline(id, updates)
        }
    }

    private suspend fun updateSpotOffline(id: Int, updates: Map<String, Any?>): Result<Spot> {
        return try {
            val existingEntity = spotDao.getSpotById(id)
                ?: return Result.failure(Exception("Spot not found"))

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

            spotDao.update(updatedEntity)

            Result.success(updatedEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSpot(id: Int): Result<Unit> {
        return if (networkMonitor.isOnlineNow) {
            deleteSpotOnline(id)
        } else {
            deleteSpotOffline(id)
        }
    }

    private suspend fun deleteSpotOnline(id: Int): Result<Unit> {
        return try {
            api.deleteSpot(id)
            spotDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Online delete failed, marking for offline delete", e)
            deleteSpotOffline(id)
        }
    }

    private suspend fun deleteSpotOffline(id: Int): Result<Unit> {
        return try {
            val existingEntity = spotDao.getSpotById(id)

            if (existingEntity != null) {
                if (existingEntity.syncStatus == SyncStatus.PENDING_CREATE) {
                    // Never synced to server, just delete locally
                    spotDao.deleteById(id)
                } else {
                    // Mark for deletion, will be deleted on server during sync
                    spotDao.updateSyncStatus(id, SyncStatus.PENDING_DELETE)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
