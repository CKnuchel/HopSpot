package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginationDto(
    val page: Int,
    val limit: Int,
    val total: Long,  // int64 im Backend
    @SerializedName("total_pages")
    val totalPages: Int
)