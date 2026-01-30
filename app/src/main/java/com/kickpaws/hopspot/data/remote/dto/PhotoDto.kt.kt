package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class `PhotoDto.kt`(
    val id: Int,
    @SerializedName("bench_id")
    val benchId: Int,
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