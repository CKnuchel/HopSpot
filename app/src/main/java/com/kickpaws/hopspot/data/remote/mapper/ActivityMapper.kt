package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.ActivityDto
import com.kickpaws.hopspot.domain.model.Activity

fun ActivityDto.toDomain(): Activity = Activity(
    id = id,
    actionType = actionType,
    userId = user.id,
    userDisplayName = user.displayName,
    benchId = bench?.id,
    benchName = bench?.name,
    benchPhotoUrl = bench?.mainPhotoUrl,
    description = description,
    createdAt = createdAt
)
