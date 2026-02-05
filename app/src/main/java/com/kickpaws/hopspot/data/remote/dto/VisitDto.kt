package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VisitDto(
    val id: Int,
    val bench: VisitBenchDto,
    @SerializedName("visited_at")
    val visitedAt: String,
    val comment: String?,
    @SerializedName("created_at")
    val createdAt: String
)

data class VisitBenchDto(
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
    @SerializedName("bench_id")
    val benchId: Int,
    val count: Long
)

data class CreateVisitRequest(
    @SerializedName("bench_id")
    val benchId: Int,
    @SerializedName("visited_at")
    val visitedAt: String?,  // Optional, default "now" im Backend
    val comment: String?
)
