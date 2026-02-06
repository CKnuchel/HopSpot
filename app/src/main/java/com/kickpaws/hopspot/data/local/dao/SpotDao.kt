package com.kickpaws.hopspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kickpaws.hopspot.data.local.entity.SpotEntity
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotDao {

    @Query("SELECT * FROM spots WHERE syncStatus != :deleted ORDER BY name ASC")
    fun getAllSpots(deleted: SyncStatus = SyncStatus.PENDING_DELETE): Flow<List<SpotEntity>>

    @Query("SELECT * FROM spots WHERE syncStatus != :deleted ORDER BY name ASC")
    suspend fun getAllSpotsOnce(deleted: SyncStatus = SyncStatus.PENDING_DELETE): List<SpotEntity>

    @Query("SELECT * FROM spots WHERE id = :id")
    suspend fun getSpotById(id: Int): SpotEntity?

    @Query("SELECT * FROM spots WHERE id = :id")
    fun getSpotByIdFlow(id: Int): Flow<SpotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spot: SpotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(spots: List<SpotEntity>)

    @Update
    suspend fun update(spot: SpotEntity)

    @Query("UPDATE spots SET syncStatus = :status, locallyModifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: SyncStatus, modifiedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM spots WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM spots WHERE syncStatus = :status")
    suspend fun deleteBySyncStatus(status: SyncStatus)

    @Query("SELECT * FROM spots WHERE syncStatus = :status")
    suspend fun getSpotsBySyncStatus(status: SyncStatus): List<SpotEntity>

    @Query("SELECT * FROM spots WHERE syncStatus IN (:statuses)")
    suspend fun getPendingChanges(
        statuses: List<SyncStatus> = listOf(
            SyncStatus.PENDING_CREATE,
            SyncStatus.PENDING_UPDATE,
            SyncStatus.PENDING_DELETE
        )
    ): List<SpotEntity>

    @Query("SELECT COUNT(*) FROM spots WHERE syncStatus IN (:statuses)")
    fun getPendingChangesCount(
        statuses: List<SyncStatus> = listOf(
            SyncStatus.PENDING_CREATE,
            SyncStatus.PENDING_UPDATE,
            SyncStatus.PENDING_DELETE
        )
    ): Flow<Int>

    @Query("DELETE FROM spots")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM spots
        WHERE syncStatus != :deleted
        AND (:search IS NULL OR name LIKE '%' || :search || '%' OR description LIKE '%' || :search || '%')
        AND (:hasToilet IS NULL OR hasToilet = :hasToilet)
        AND (:hasTrashBin IS NULL OR hasTrashBin = :hasTrashBin)
        AND (:minRating IS NULL OR rating >= :minRating)
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getSpotsFiltered(
        search: String? = null,
        hasToilet: Boolean? = null,
        hasTrashBin: Boolean? = null,
        minRating: Int? = null,
        limit: Int = 50,
        offset: Int = 0,
        deleted: SyncStatus = SyncStatus.PENDING_DELETE
    ): List<SpotEntity>

    @Query("""
        SELECT COUNT(*) FROM spots
        WHERE syncStatus != :deleted
        AND (:search IS NULL OR name LIKE '%' || :search || '%' OR description LIKE '%' || :search || '%')
        AND (:hasToilet IS NULL OR hasToilet = :hasToilet)
        AND (:hasTrashBin IS NULL OR hasTrashBin = :hasTrashBin)
        AND (:minRating IS NULL OR rating >= :minRating)
    """)
    suspend fun getSpotsFilteredCount(
        search: String? = null,
        hasToilet: Boolean? = null,
        hasTrashBin: Boolean? = null,
        minRating: Int? = null,
        deleted: SyncStatus = SyncStatus.PENDING_DELETE
    ): Int
}
