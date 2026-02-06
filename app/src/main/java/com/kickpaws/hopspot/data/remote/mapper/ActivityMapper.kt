package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.ActivityDto
import com.kickpaws.hopspot.domain.model.Activity

fun ActivityDto.toDomain(): Activity = Activity(
    id = id,
    actionType = actionType,
    userId = user.id,
    userDisplayName = user.displayName,
    spotId = spot?.id,
    spotName = spot?.name,
    spotPhotoUrl = spot?.mainPhotoUrl,
    description = description,
    createdAt = createdAt
)
