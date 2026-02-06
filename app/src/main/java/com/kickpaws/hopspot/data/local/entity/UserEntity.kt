package com.kickpaws.hopspot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val email: String,
    val displayName: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: String
)
