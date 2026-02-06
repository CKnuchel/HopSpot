package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PhotoDto(
    val id: Int,
    @SerializedName("spot_id")
    val spotId: Int,
    @SerializedName("is_main")
    val isMain: Boolean,
    @SerializedName("url_original")
    val urlOriginal: String?,
    @SerializedName("url_medium")
    val urlMedium: String?,
    @SerializedName("url_thumbnail")
    val urlThumbnail: String?,
    @SerializedName("uploaded_by")
    val uploadedBy: Int,
    @SerializedName("created_at")
    val createdAt: String
)