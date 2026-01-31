package com.kickpaws.hopspot.data.remote.mapper

import com.kickpaws.hopspot.data.remote.dto.UserDto
import com.kickpaws.hopspot.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        role = role,
        createdAt = createdAt
    )
}