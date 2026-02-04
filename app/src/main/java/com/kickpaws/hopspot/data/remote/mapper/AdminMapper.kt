// data/mapper/AdminMapper.kt
package com.kickpaws.hopspot.data.mapper

import com.kickpaws.hopspot.data.remote.dto.InvitationCodeDto
import com.kickpaws.hopspot.data.remote.dto.UserDto
import com.kickpaws.hopspot.domain.model.InvitationCode
import com.kickpaws.hopspot.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun InvitationCodeDto.toDomain(): InvitationCode {
    return InvitationCode(
        id = id,
        code = code,
        comment = comment,
        createdBy = createdBy.toDomain(),
        redeemedBy = redeemedBy?.toDomain(),
        createdAt = createdAt,
        redeemedAt = redeemedAt
    )
}