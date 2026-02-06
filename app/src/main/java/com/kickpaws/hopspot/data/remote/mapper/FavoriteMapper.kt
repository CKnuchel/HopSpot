package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.FavoriteDto
import com.kickpaws.hopspot.domain.model.Favorite

fun FavoriteDto.toDomain(): Favorite = Favorite(
    id = id,
    benchId = bench.id,
    benchName = bench.name,
    benchLatitude = bench.latitude,
    benchLongitude = bench.longitude,
    benchRating = bench.rating,
    benchHasToilet = bench.hasToilet,
    benchHasTrashBin = bench.hasTrashBin,
    benchPhotoUrl = bench.mainPhotoUrl,
    createdAt = createdAt
)
