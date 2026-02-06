package com.kickpaws.hopspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kickpaws.hopspot.data.local.entity.BenchEntity
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BenchDao {

    @Query("SELECT * FROM benches WHERE syncStatus != :deleted ORDER BY name ASC")
    fun getAllBenches(deleted: SyncStatus = SyncStatus.PENDING_DELETE): Flow<List<BenchEntity>>

    @Query("SELECT * FROM benches WHERE syncStatus != :deleted ORDER BY name ASC")
    suspend fun getAllBenchesOnce(deleted: SyncStatus = SyncStatus.PENDING_DELETE): List<BenchEntity>

    @Query("SELECT * FROM benches WHERE id = :id")
    suspend fun getBenchById(id: Int): BenchEntity?

    @Query("SELECT * FROM benches WHERE id = :id")
    fun getBenchByIdFlow(id: Int): Flow<BenchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bench: BenchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(benches: List<BenchEntity>)

    @Update
    suspend fun update(bench: BenchEntity)

    @Query("UPDATE benches SET syncStatus = :status, locallyModifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: SyncStatus, modifiedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM benches WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM benches WHERE syncStatus = :status")
    suspend fun deleteBySyncStatus(status: SyncStatus)

    @Query("SELECT * FROM benches WHERE syncStatus = :status")
    suspend fun getBenchesBySyncStatus(status: SyncStatus): List<BenchEntity>

    @Query("SELECT * FROM benches WHERE syncStatus IN (:statuses)")
    suspend fun getPendingChanges(
        statuses: List<SyncStatus> = listOf(
            SyncStatus.PENDING_CREATE,
            SyncStatus.PENDING_UPDATE,
            SyncStatus.PENDING_DELETE
        )
    ): List<BenchEntity>

    @Query("SELECT COUNT(*) FROM benches WHERE syncStatus IN (:statuses)")
    fun getPendingChangesCount(
        statuses: List<SyncStatus> = listOf(
            SyncStatus.PENDING_CREATE,
            SyncStatus.PENDING_UPDATE,
            SyncStatus.PENDING_DELETE
        )
    ): Flow<Int>

    @Query("DELETE FROM benches")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM benches
        WHERE syncStatus != :deleted
        AND (:search IS NULL OR name LIKE '%' || :search || '%' OR description LIKE '%' || :search || '%')
        AND (:hasToilet IS NULL OR hasToilet = :hasToilet)
        AND (:hasTrashBin IS NULL OR hasTrashBin = :hasTrashBin)
        AND (:minRating IS NULL OR rating >= :minRating)
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getBenchesFiltered(
        search: String? = null,
        hasToilet: Boolean? = null,
        hasTrashBin: Boolean? = null,
        minRating: Int? = null,
        limit: Int = 50,
        offset: Int = 0,
        deleted: SyncStatus = SyncStatus.PENDING_DELETE
    ): List<BenchEntity>

    @Query("""
        SELECT COUNT(*) FROM benches
        WHERE syncStatus != :deleted
        AND (:search IS NULL OR name LIKE '%' || :search || '%' OR description LIKE '%' || :search || '%')
        AND (:hasToilet IS NULL OR hasToilet = :hasToilet)
        AND (:hasTrashBin IS NULL OR hasTrashBin = :hasTrashBin)
        AND (:minRating IS NULL OR rating >= :minRating)
    """)
    suspend fun getBenchesFilteredCount(
        search: String? = null,
        hasToilet: Boolean? = null,
        hasTrashBin: Boolean? = null,
        minRating: Int? = null,
        deleted: SyncStatus = SyncStatus.PENDING_DELETE
    ): Int
}
