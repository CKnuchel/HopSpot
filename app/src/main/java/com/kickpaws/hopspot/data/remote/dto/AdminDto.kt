package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AdminUpdateUserRequest(
    val role: String?,
    @SerializedName("is_active")
    val isActive: Boolean?
)

data class CreateInvitationCodeRequest(
    val comment: String?
)

data class InvitationCodeDto(
    val id: Int,
    val code: String,
    val comment: String?,
    @SerializedName("created_by")
    val createdBy: UserDto,
    @SerializedName("redeemed_by")
    val redeemedBy: UserDto?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("redeemed_at")
    val redeemedAt: String?
)

data class PaginatedInvitationCodesDto(
    val codes: List<InvitationCodeDto>,
    val pagination: PaginationDto
)

data class PaginatedUsersDto(
    val users: List<UserDto>,
    val pagination: PaginationDto
)