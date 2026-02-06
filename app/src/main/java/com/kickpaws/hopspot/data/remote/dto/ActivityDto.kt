package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ActivityDto(
    val id: Int,
    @SerializedName("action_type")
    val actionType: String,
    val user: ActivityUserDto,
    val bench: ActivityBenchDto?,
    val description: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class ActivityUserDto(
    val id: Int,
    @SerializedName("display_name")
    val displayName: String
)

data class ActivityBenchDto(
    val id: Int,
    val name: String,
    @SerializedName("main_photo_url")
    val mainPhotoUrl: String?
)

data class PaginatedActivitiesDto(
    val activities: List<ActivityDto>,
    val pagination: PaginationDto
)
