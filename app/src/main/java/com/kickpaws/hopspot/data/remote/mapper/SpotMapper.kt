package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.SpotDto
import com.kickpaws.hopspot.data.remote.dto.SpotListItemDto
import com.kickpaws.hopspot.domain.model.Spot

fun SpotDto.toDomain(): Spot {
    return Spot(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        description = description,
        rating = rating,
        hasToilet = hasToilet,
        hasTrashBin = hasTrashBin,
        mainPhotoUrl = mainPhotoUrl,
        distance = null,
        createdById = createdBy.id,
        createdByName = createdBy.displayName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SpotListItemDto.toDomain(): Spot {
    return Spot(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        description = null,
        rating = rating,
        hasToilet = hasToilet,
        hasTrashBin = hasTrashBin,
        mainPhotoUrl = mainPhotoUrl,
        distance = distance,
        createdById = null,
        createdByName = null,
        createdAt = null,
        updatedAt = null
    )
}
