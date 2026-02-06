package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FavoriteDto(
    val id: Int,
    val spot: FavoriteSpotDto,
    @SerializedName("created_at")
    val createdAt: String
)

data class FavoriteSpotDto(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Int?,
    @SerializedName("has_toilet")
    val hasToilet: Boolean,
    @SerializedName("has_trash_bin")
    val hasTrashBin: Boolean,
    @SerializedName("main_photo_url")
    val mainPhotoUrl: String?
)

data class PaginatedFavoritesDto(
    val favorites: List<FavoriteDto>,
    val pagination: PaginationDto
)
