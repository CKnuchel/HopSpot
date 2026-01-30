package com.kickpaws.hopspot.domain.model

data class User(
    val id: Int,
    val email: String,
    val displayName: String,
    val role: String,
    val createdAt: String
)
