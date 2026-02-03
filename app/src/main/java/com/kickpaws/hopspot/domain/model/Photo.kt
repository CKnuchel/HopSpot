package com.kickpaws.hopspot.domain.model

data class Photo(
    val id: Int,
    val benchId: Int,
    val isMain: Boolean,
    val urlOriginal: String?,
    val urlMedium: String?,
    val urlThumbnail: String?,
    val uploadedBy: Int,
    val createdAt: String
)