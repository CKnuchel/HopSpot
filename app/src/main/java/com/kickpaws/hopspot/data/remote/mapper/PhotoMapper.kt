package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.PhotoDto
import com.kickpaws.hopspot.domain.model.Photo

fun PhotoDto.toDomain(): Photo {
    return Photo(
        id = id,
        benchId = benchId,
        isMain = isMain,
        urlOriginal = urlOriginal,
        urlMedium = urlMedium,
        urlThumbnail = urlThumbnail,
        uploadedBy = uploadedBy,
        createdAt = createdAt
    )
}