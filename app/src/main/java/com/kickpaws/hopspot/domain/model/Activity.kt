package com.kickpaws.hopspot.domain.model

data class Activity(
    val id: Int,
    val actionType: String,
    val userId: Int,
    val userDisplayName: String,
    val benchId: Int?,
    val benchName: String?,
    val benchPhotoUrl: String?,
    val description: String,
    val createdAt: String
)
