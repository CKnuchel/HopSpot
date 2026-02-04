package com.kickpaws.hopspot.domain.model

data class User(
    val id: Int,
    val email: String,
    val displayName: String,
    val role: String,
    val isActive: Boolean = true,
    val createdAt: String
)
