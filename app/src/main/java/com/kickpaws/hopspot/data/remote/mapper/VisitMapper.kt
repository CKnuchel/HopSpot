package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.VisitDto
import com.kickpaws.hopspot.domain.model.Visit

fun VisitDto.toDomain(): Visit {
    return Visit(
        id = id,
        spotId = spot.id,
        spotName = spot.name,
        spotPhotoUrl = spot.mainPhotoUrl,
        comment = comment,
        visitedAt = visitedAt,
        createdAt = createdAt
    )
}