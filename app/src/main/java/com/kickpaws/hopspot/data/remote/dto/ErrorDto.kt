package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for API error responses.
 * Maps to the backend's ErrorResponse structure.
 */
data class ErrorResponseDto(
    @SerializedName("error_code") val errorCode: String,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: String? = null
)
