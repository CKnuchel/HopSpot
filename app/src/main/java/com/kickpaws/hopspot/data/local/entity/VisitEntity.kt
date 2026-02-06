package com.kickpaws.hopspot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey
    val id: Int,
    val spotId: Int,
    val spotName: String,
    val spotPhotoUrl: String?,
    val comment: String?,
    val visitedAt: String,
    val createdAt: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
