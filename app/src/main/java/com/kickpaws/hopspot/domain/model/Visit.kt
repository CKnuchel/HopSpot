package com.kickpaws.hopspot.domain.model

data class Visit(
    val id: Int,
    val benchId: Int,
    val benchName: String,
    val benchPhotoUrl: String?,
    val comment: String?,
    val visitedAt: String,
    val createdAt: String
)