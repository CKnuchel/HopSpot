package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.FavoriteDto
import com.kickpaws.hopspot.domain.model.Favorite

fun FavoriteDto.toDomain(): Favorite = Favorite(
    id = id,
    spotId = spot.id,
    spotName = spot.name,
    spotLatitude = spot.latitude,
    spotLongitude = spot.longitude,
    spotRating = spot.rating,
    spotHasToilet = spot.hasToilet,
    spotHasTrashBin = spot.hasTrashBin,
    spotPhotoUrl = spot.mainPhotoUrl,
    createdAt = createdAt
)
