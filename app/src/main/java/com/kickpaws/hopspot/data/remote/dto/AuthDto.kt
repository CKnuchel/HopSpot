package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("invitation_code")
    val invitationCode: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class RefreshFCMTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)

data class AuthResponseDto(
    val user: UserDto,
    val token: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class UserDto(
    val id: Int,
    val email: String,
    @SerializedName("display_name")
    val displayName: String,
    val role: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class UpdateProfileRequest(
    @SerializedName("display_name")
    val displayName: String?
)

data class ChangePasswordRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)