package com.kickpaws.hopspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import com.kickpaws.hopspot.data.local.entity.VisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {

    @Query("SELECT * FROM visits WHERE syncStatus != :deleted ORDER BY visitedAt DESC")
    fun getAllVisits(deleted: SyncStatus = SyncStatus.PENDING_DELETE): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE syncStatus != :deleted ORDER BY visitedAt DESC")
    suspend fun getAllVisitsOnce(deleted: SyncStatus = SyncStatus.PENDING_DELETE): List<VisitEntity>

    @Query("SELECT * FROM visits WHERE syncStatus != :deleted ORDER BY visitedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getVisitsPaginated(
        limit: Int,
        offset: Int,
        deleted: SyncStatus = SyncStatus.PENDING_DELETE
    ): List<VisitEntity>

    @Query("SELECT COUNT(*) FROM visits WHERE syncStatus != :deleted")
    suspend fun getVisitsCount(deleted: SyncStatus = SyncStatus.PENDING_DELETE): Int

    @Query("SELECT * FROM visits WHERE id = :id")
    suspend fun getVisitById(id: Int): VisitEntity?

    @Query("SELECT COUNT(*) FROM visits WHERE benchId = :benchId AND syncStatus != :deleted")
    suspend fun getVisitCountForBench(benchId: Int, deleted: SyncStatus = SyncStatus.PENDING_DELETE): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visit: VisitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visits: List<VisitEntity>)

    @Query("UPDATE visits SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: SyncStatus)

    @Query("DELETE FROM visits WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM visits WHERE syncStatus = :status")
    suspend fun getPendingCreates(status: SyncStatus = SyncStatus.PENDING_CREATE): List<VisitEntity>

    @Query("SELECT * FROM visits WHERE syncStatus = :status")
    suspend fun getPendingDeletes(status: SyncStatus = SyncStatus.PENDING_DELETE): List<VisitEntity>

    @Query("SELECT * FROM visits WHERE syncStatus IN (:statuses)")
    suspend fun getPendingChanges(
        statuses: List<SyncStatus> = listOf(
            SyncStatus.PENDING_CREATE,
            SyncStatus.PENDING_DELETE
        )
    ): List<VisitEntity>

    @Query("SELECT COUNT(*) FROM visits WHERE syncStatus IN (:statuses)")
    fun getPendingChangesCount(
        statuses: List<SyncStatus> = listOf(
            SyncStatus.PENDING_CREATE,
            SyncStatus.PENDING_DELETE
        )
    ): Flow<Int>

    @Query("DELETE FROM visits")
    suspend fun clearAll()
}
