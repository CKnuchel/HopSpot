package com.kickpaws.hopspot.domain.model

data class Favorite(
    val id: Int,
    val benchId: Int,
    val benchName: String,
    val benchLatitude: Double,
    val benchLongitude: Double,
    val benchRating: Int?,
    val benchHasToilet: Boolean,
    val benchHasTrashBin: Boolean,
    val benchPhotoUrl: String?,
    val createdAt: String
)
