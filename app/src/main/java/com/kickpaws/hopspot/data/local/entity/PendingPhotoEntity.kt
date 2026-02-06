package com.kickpaws.hopspot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_photos")
data class PendingPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val benchId: Int,
    val localFilePath: String,
    val isMain: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
