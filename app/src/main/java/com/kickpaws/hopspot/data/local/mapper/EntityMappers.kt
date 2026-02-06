package com.kickpaws.hopspot.data.local.mapper

import com.kickpaws.hopspot.data.local.entity.SpotEntity
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import com.kickpaws.hopspot.data.local.entity.UserEntity
import com.kickpaws.hopspot.data.local.entity.VisitEntity
import com.kickpaws.hopspot.domain.model.Spot
import com.kickpaws.hopspot.domain.model.User
import com.kickpaws.hopspot.domain.model.Visit

// Spot Mappers
fun SpotEntity.toDomain(): Spot = Spot(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    description = description,
    rating = rating,
    hasToilet = hasToilet,
    hasTrashBin = hasTrashBin,
    mainPhotoUrl = mainPhotoUrl,
    distance = distance,
    createdById = createdById,
    createdByName = createdByName,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Spot.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    locallyModifiedAt: Long = System.currentTimeMillis()
): SpotEntity = SpotEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    description = description,
    rating = rating,
    hasToilet = hasToilet,
    hasTrashBin = hasTrashBin,
    mainPhotoUrl = mainPhotoUrl,
    distance = distance,
    createdById = createdById,
    createdByName = createdByName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    locallyModifiedAt = locallyModifiedAt
)

fun List<SpotEntity>.toDomainList(): List<Spot> = map { it.toDomain() }

fun List<Spot>.toEntityList(
    syncStatus: SyncStatus = SyncStatus.SYNCED
): List<SpotEntity> = map { it.toEntity(syncStatus) }

// Visit Mappers
fun VisitEntity.toDomain(): Visit = Visit(
    id = id,
    spotId = spotId,
    spotName = spotName,
    spotPhotoUrl = spotPhotoUrl,
    comment = comment,
    visitedAt = visitedAt,
    createdAt = createdAt
)

fun Visit.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED
): VisitEntity = VisitEntity(
    id = id,
    spotId = spotId,
    spotName = spotName,
    spotPhotoUrl = spotPhotoUrl,
    comment = comment,
    visitedAt = visitedAt,
    createdAt = createdAt,
    syncStatus = syncStatus
)

fun List<VisitEntity>.toVisitDomainList(): List<Visit> = map { it.toDomain() }

fun List<Visit>.toVisitEntityList(
    syncStatus: SyncStatus = SyncStatus.SYNCED
): List<VisitEntity> = map { it.toEntity(syncStatus) }

// User Mappers
fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    role = role,
    isActive = isActive,
    createdAt = createdAt
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    displayName = displayName,
    role = role,
    isActive = isActive,
    createdAt = createdAt
)
