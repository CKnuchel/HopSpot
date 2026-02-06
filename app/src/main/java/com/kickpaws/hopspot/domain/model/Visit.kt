package com.kickpaws.hopspot.domain.model

data class Visit(
    val id: Int,
    val spotId: Int,
    val spotName: String,
    val spotPhotoUrl: String?,
    val comment: String?,
    val visitedAt: String,
    val createdAt: String
)
