package com.kickpaws.hopspot.domain.model

data class Favorite(
    val id: Int,
    val spotId: Int,
    val spotName: String,
    val spotLatitude: Double,
    val spotLongitude: Double,
    val spotRating: Int?,
    val spotHasToilet: Boolean,
    val spotHasTrashBin: Boolean,
    val spotPhotoUrl: String?,
    val createdAt: String
)
