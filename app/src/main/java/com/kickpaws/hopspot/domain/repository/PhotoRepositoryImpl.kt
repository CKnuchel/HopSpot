package com.kickpaws.hopspot.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
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
            val file = uriToFileWithCorrectOrientation(photoUri)

            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
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

    private fun uriToFileWithCorrectOrientation(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")

        // Erst in temporaere Datei kopieren um EXIF lesen zu koennen
        val tempFile = File.createTempFile("photo_temp_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        // EXIF Orientation auslesen
        val exif = ExifInterface(tempFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        // Rotation bestimmen
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        // Wenn keine Rotation noetig, Original zurueckgeben
        if (rotationDegrees == 0f) {
            return tempFile
        }

        // Bitmap laden, rotieren und speichern
        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
        val matrix = Matrix().apply { postRotate(rotationDegrees) }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        // Rotiertes Bild in neue Datei speichern
        val outputFile = File.createTempFile("photo_rotated_", ".jpg", context.cacheDir)
        FileOutputStream(outputFile).use { outputStream ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }

        // Aufraeumen
        bitmap.recycle()
        rotatedBitmap.recycle()
        tempFile.delete()

        return outputFile
    }
}