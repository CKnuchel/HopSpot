package com.kickpaws.hopspot.domain.repository

import android.net.Uri

interface PhotoRepository {
    suspend fun uploadPhoto(benchId: Int, photoUri: Uri, isMain: Boolean = true): Result<Unit>
    suspend fun deletePhoto(photoId: Int): Result<Unit>
}