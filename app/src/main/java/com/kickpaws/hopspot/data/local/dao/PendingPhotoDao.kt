package com.kickpaws.hopspot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kickpaws.hopspot.data.local.entity.PendingPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPhotoDao {

    @Query("SELECT * FROM pending_photos ORDER BY createdAt ASC")
    fun getAllPendingPhotos(): Flow<List<PendingPhotoEntity>>

    @Query("SELECT * FROM pending_photos ORDER BY createdAt ASC")
    suspend fun getAllPendingPhotosOnce(): List<PendingPhotoEntity>

    @Query("SELECT * FROM pending_photos WHERE spotId = :spotId")
    suspend fun getPendingPhotosForSpot(spotId: Int): List<PendingPhotoEntity>

    @Insert
    suspend fun insert(photo: PendingPhotoEntity)

    @Query("DELETE FROM pending_photos WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM pending_photos WHERE spotId = :spotId")
    suspend fun deleteBySpotId(spotId: Int)

    @Query("SELECT COUNT(*) FROM pending_photos")
    fun getPendingCount(): Flow<Int>

    @Query("DELETE FROM pending_photos")
    suspend fun clearAll()
}
