package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VisitDto(
    val id: Int,
    val spot: VisitSpotDto,
    @SerializedName("visited_at")
    val visitedAt: String,
    val comment: String?,
    @SerializedName("created_at")
    val createdAt: String
)

data class VisitSpotDto(
    val id: Int,
    val name: String,
    @SerializedName("main_photo_url")
    val mainPhotoUrl: String?
)

data class PaginatedVisitsDto(
    val visits: List<VisitDto>,
    val pagination: PaginationDto
)

data class VisitCountDto(
    @SerializedName("spot_id")
    val spotId: Int,
    val count: Long
)

data class CreateVisitRequest(
    @SerializedName("spot_id")
    val spotId: Int,
    @SerializedName("visited_at")
    val visitedAt: String?,  // Optional, default "now" im Backend
    val comment: String?
)
