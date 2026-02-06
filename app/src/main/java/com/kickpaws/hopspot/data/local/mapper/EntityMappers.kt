package com.kickpaws.hopspot.data.local.mapper

import com.kickpaws.hopspot.data.local.entity.BenchEntity
import com.kickpaws.hopspot.data.local.entity.SyncStatus
import com.kickpaws.hopspot.data.local.entity.UserEntity
import com.kickpaws.hopspot.data.local.entity.VisitEntity
import com.kickpaws.hopspot.domain.model.Bench
import com.kickpaws.hopspot.domain.model.User
import com.kickpaws.hopspot.domain.model.Visit

// Bench Mappers
fun BenchEntity.toDomain(): Bench = Bench(
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

fun Bench.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    locallyModifiedAt: Long = System.currentTimeMillis()
): BenchEntity = BenchEntity(
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

fun List<BenchEntity>.toDomainList(): List<Bench> = map { it.toDomain() }

fun List<Bench>.toEntityList(
    syncStatus: SyncStatus = SyncStatus.SYNCED
): List<BenchEntity> = map { it.toEntity(syncStatus) }

// Visit Mappers
fun VisitEntity.toDomain(): Visit = Visit(
    id = id,
    benchId = benchId,
    benchName = benchName,
    benchPhotoUrl = benchPhotoUrl,
    comment = comment,
    visitedAt = visitedAt,
    createdAt = createdAt
)

fun Visit.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED
): VisitEntity = VisitEntity(
    id = id,
    benchId = benchId,
    benchName = benchName,
    benchPhotoUrl = benchPhotoUrl,
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
