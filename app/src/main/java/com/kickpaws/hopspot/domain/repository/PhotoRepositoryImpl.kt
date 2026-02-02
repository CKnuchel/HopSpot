package com.kickpaws.hopspot.data.repository

import android.content.Context
import android.net.Uri
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.domain.repository.PhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val api: HopSpotApi,
    @param:ApplicationContext private val context: Context
) : PhotoRepository {

    override suspend fun uploadPhoto(benchId: Int, photoUri: Uri, isMain: Boolean): Result<Unit> {
        return try {
            val file = uriToFile(photoUri)

            // MIME-Type ermitteln
            val mimeType = context.contentResolver.getType(photoUri) ?: "image/jpeg"

            val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", file.name, requestBody)
            val isMainBody = isMain.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            api.uploadPhoto(benchId, photoPart, isMainBody)

            file.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePhoto(photoId: Int): Result<Unit> {
        return try {
            api.deletePhoto(photoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")

        val tempFile = File.createTempFile("photo_", ".jpg", context.cacheDir)

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        return tempFile
    }
}