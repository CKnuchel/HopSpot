package com.kickpaws.hopspot.domain.model

data class Activity(
    val id: Int,
    val actionType: String,
    val userId: Int,
    val userDisplayName: String,
    val spotId: Int?,
    val spotName: String?,
    val spotPhotoUrl: String?,
    val description: String,
    val createdAt: String
)
