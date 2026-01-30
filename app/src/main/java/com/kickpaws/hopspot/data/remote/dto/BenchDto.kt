package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BenchDto(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val rating: Int?,
    @SerializedName("has_toilet")
    val hasToilet: Boolean,
    @SerializedName("has_trash_bin")
    val hasTrashBin: Boolean,
    @SerializedName("main_photo_url")
    val mainPhotoUrl: String?,
    @SerializedName("created_by")
    val createdBy: UserDto,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class BenchListItemDto(
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
    val mainPhotoUrl: String?,
    val distance: Double?
)

data class PaginatedBenchesDto(
    val benches: List<BenchListItemDto>,
    val pagination: PaginationDto
)

data class CreateBenchRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val rating: Int?,
    @SerializedName("has_toilet")
    val hasToilet: Boolean,
    @SerializedName("has_trash_bin")
    val hasTrashBin: Boolean
)

data class UpdateBenchRequest(
    val name: String?,
    val description: String?,
    val rating: Int?,
    @SerializedName("has_toilet")
    val hasToilet: Boolean?,
    @SerializedName("has_trash_bin")
    val hasTrashBin: Boolean?
)