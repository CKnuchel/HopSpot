package com.kickpaws.hopspot.data.sync

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kickpaws.hopspot.data.local.dao.PendingPhotoDao
import com.kickpaws.hopspot.data.local.dao.SpotDao
import com.kickpaws.hopspot.data.local.dao.VisitDao
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import com.kickpaws.hopspot.data.local.mapper.toEntity
import com.kickpaws.hopspot.data.network.NetworkMonitor
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.dto.CreateSpotRequest
import com.kickpaws.hopspot.data.remote.dto.CreateVisitRequest
import com.kickpaws.hopspot.data.remote.dto.UpdateSpotRequest
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_prefs")

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: HopSpotApi,
    private val spotDao: SpotDao,
    private val visitDao: VisitDao,
    private val pendingPhotoDao: PendingPhotoDao,
    private val networkMonitor: NetworkMonitor
) {
    companion object {
        private const val TAG = "SyncManager"
        private val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_time")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncProgress = MutableStateFlow(SyncProgress())
    val syncProgress: StateFlow<SyncProgress> = _syncProgress.asStateFlow()

    private val lastSyncTime = context.syncDataStore.data.map { prefs ->
        prefs[LAST_SYNC_TIME_KEY]
    }

    init {
        observePendingChanges()
        observeNetworkChanges()
    }

    private fun observePendingChanges() {
        combine(
            spotDao.getPendingChangesCount(),
            visitDao.getPendingChangesCount(),
            pendingPhotoDao.getPendingCount(),
            lastSyncTime
        ) { spotChanges, visitChanges, photoUploads, syncTime ->
            _syncProgress.update { current ->
                current.copy(
                    pendingSpotChanges = spotChanges,
                    pendingVisitChanges = visitChanges,
                    pendingPhotoUploads = photoUploads,
                    lastSyncTime = syncTime
                )
            }
        }.launchIn(scope)
    }

    private fun observeNetworkChanges() {
        networkMonitor.isOnline
            .onEach { isOnline ->
                if (isOnline && _syncProgress.value.hasPendingChanges) {
                    Log.d(TAG, "Network available, starting auto-sync")
                    syncAll()
                }
            }
            .launchIn(scope)
    }

    suspend fun syncAll() {
        if (_syncProgress.value.state == SyncState.Syncing) {
            Log.d(TAG, "Sync already in progress, skipping")
            return
        }

        if (!networkMonitor.isOnlineNow) {
            Log.d(TAG, "No network, skipping sync")
            return
        }

        _syncProgress.update { it.copy(state = SyncState.Syncing) }

        try {
            // 1. Download server changes
            downloadServerData()

            // 2. Upload pending creates
            uploadPendingCreates()

            // 3. Upload pending updates (Last-Write-Wins)
            uploadPendingUpdates()

            // 4. Upload pending deletes
            uploadPendingDeletes()

            // 5. Upload pending photos
            uploadPendingPhotos()

            // Update last sync time
            val syncTime = System.currentTimeMillis()
            context.syncDataStore.edit { prefs ->
                prefs[LAST_SYNC_TIME_KEY] = syncTime
            }

            _syncProgress.update {
                it.copy(
                    state = SyncState.Success(syncTime),
                    lastSyncTime = syncTime
                )
            }

            Log.d(TAG, "Sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            _syncProgress.update {
                it.copy(state = SyncState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    private suspend fun downloadServerData() {
        Log.d(TAG, "Downloading server data...")

        // Download all spots
        try {
            val response = api.getSpots(page = 1, limit = 1000)
            val spots = response.data.spots.map { it.toDomain() }

            // Get current pending changes to preserve them
            val pendingSpots = spotDao.getPendingChanges()
            val pendingIds = pendingSpots.map { it.id }.toSet()

            // Insert server spots, but don't overwrite pending local changes
            spots.filter { it.id !in pendingIds }.forEach { spot ->
                spotDao.insert(spot.toEntity(SyncStatus.SYNCED))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download spots", e)
        }

        // Download all visits
        try {
            val response = api.getVisits(page = 1, limit = 1000)
            val visits = response.visits.map { it.toDomain() }

            // Get current pending changes
            val pendingVisits = visitDao.getPendingChanges()
            val pendingIds = pendingVisits.map { it.id }.toSet()

            // Insert server visits, but don't overwrite pending local changes
            visits.filter { it.id !in pendingIds }.forEach { visit ->
                visitDao.insert(visit.toEntity(SyncStatus.SYNCED))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download visits", e)
        }
    }

    private suspend fun uploadPendingCreates() {
        // Upload pending spot creates
        val pendingSpotCreates = spotDao.getSpotsBySyncStatus(SyncStatus.PENDING_CREATE)
        Log.d(TAG, "Uploading ${pendingSpotCreates.size} pending spot creates")

        for (spot in pendingSpotCreates) {
            try {
                val request = CreateSpotRequest(
                    name = spot.name,
                    latitude = spot.latitude,
                    longitude = spot.longitude,
                    description = spot.description,
                    rating = spot.rating,
                    hasToilet = spot.hasToilet,
                    hasTrashBin = spot.hasTrashBin
                )
                val response = api.createSpot(request)
                val serverSpot = response.data.toDomain()

                // Delete local entity with temp ID and insert server entity
                spotDao.deleteById(spot.id)
                spotDao.insert(serverSpot.toEntity(SyncStatus.SYNCED))

                Log.d(TAG, "Uploaded spot: ${spot.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload spot: ${spot.name}", e)
            }
        }

        // Upload pending visit creates
        val pendingVisitCreates = visitDao.getPendingCreates()
        Log.d(TAG, "Uploading ${pendingVisitCreates.size} pending visit creates")

        for (visit in pendingVisitCreates) {
            try {
                val request = CreateVisitRequest(
                    spotId = visit.spotId,
                    visitedAt = visit.visitedAt,
                    comment = visit.comment
                )
                val response = api.createVisit(request)
                val serverVisit = response.toDomain()

                // Delete local entity with temp ID and insert server entity
                visitDao.deleteById(visit.id)
                visitDao.insert(serverVisit.toEntity(SyncStatus.SYNCED))

                Log.d(TAG, "Uploaded visit for spot: ${visit.spotId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload visit for spot: ${visit.spotId}", e)
            }
        }
    }

    private suspend fun uploadPendingUpdates() {
        val pendingUpdates = spotDao.getSpotsBySyncStatus(SyncStatus.PENDING_UPDATE)
        Log.d(TAG, "Uploading ${pendingUpdates.size} pending spot updates")

        for (spot in pendingUpdates) {
            try {
                val request = UpdateSpotRequest(
                    name = spot.name,
                    latitude = spot.latitude,
                    longitude = spot.longitude,
                    description = spot.description,
                    rating = spot.rating,
                    hasToilet = spot.hasToilet,
                    hasTrashBin = spot.hasTrashBin
                )
                val response = api.updateSpot(spot.id, request)
                val serverSpot = response.data.toDomain()

                // Update local entity with server response
                spotDao.insert(serverSpot.toEntity(SyncStatus.SYNCED))

                Log.d(TAG, "Updated spot: ${spot.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update spot: ${spot.name}", e)
            }
        }
    }

    private suspend fun uploadPendingDeletes() {
        // Upload pending spot deletes
        val pendingSpotDeletes = spotDao.getSpotsBySyncStatus(SyncStatus.PENDING_DELETE)
        Log.d(TAG, "Uploading ${pendingSpotDeletes.size} pending spot deletes")

        for (spot in pendingSpotDeletes) {
            try {
                api.deleteSpot(spot.id)
                spotDao.deleteById(spot.id)
                Log.d(TAG, "Deleted spot: ${spot.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete spot: ${spot.name}", e)
            }
        }

        // Upload pending visit deletes
        val pendingVisitDeletes = visitDao.getPendingDeletes()
        Log.d(TAG, "Uploading ${pendingVisitDeletes.size} pending visit deletes")

        for (visit in pendingVisitDeletes) {
            try {
                api.deleteVisit(visit.id)
                visitDao.deleteById(visit.id)
                Log.d(TAG, "Deleted visit: ${visit.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete visit: ${visit.id}", e)
            }
        }
    }

    private suspend fun uploadPendingPhotos() {
        val pendingPhotos = pendingPhotoDao.getAllPendingPhotosOnce()
        Log.d(TAG, "Uploading ${pendingPhotos.size} pending photos")

        for (photo in pendingPhotos) {
            try {
                val file = File(photo.localFilePath)
                if (!file.exists()) {
                    Log.w(TAG, "Photo file not found: ${photo.localFilePath}")
                    pendingPhotoDao.deleteById(photo.id)
                    continue
                }

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                val isMainBody = photo.isMain.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                api.uploadPhoto(photo.spotId, photoPart, isMainBody)
                pendingPhotoDao.deleteById(photo.id)

                Log.d(TAG, "Uploaded photo for spot: ${photo.spotId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload photo for spot: ${photo.spotId}", e)
            }
        }
    }

    fun triggerSync() {
        scope.launch {
            syncAll()
        }
    }

    fun resetSyncState() {
        _syncProgress.update { it.copy(state = SyncState.Idle) }
    }
}
