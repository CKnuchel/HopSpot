package com.kickpaws.hopspot.domain.model

data class Spot(
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
    val updatedAt: String?
)
