package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.VisitDto
import com.kickpaws.hopspot.domain.model.Visit

fun VisitDto.toDomain(): Visit {
    return Visit(
        id = id,
        benchId = bench.id,
        benchName = bench.name,
        benchPhotoUrl = bench.mainPhotoUrl,
        comment = comment,
        visitedAt = visitedAt,
        createdAt = createdAt
    )
}