package com.kickpaws.hopspot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "benches")
data class BenchEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val rating: Int?,
    val hasToilet: Boolean,
    val hasTrashBin: Boolean,
    val mainPhotoUrl: String?,
    val distance: Double?,
    val createdById: Int?,
    val createdByName: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val locallyModifiedAt: Long = System.currentTimeMillis()
)
