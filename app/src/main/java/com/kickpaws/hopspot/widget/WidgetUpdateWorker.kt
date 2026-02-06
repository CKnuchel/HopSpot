package com.kickpaws.hopspot.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import coil.imageLoader
import coil.request.ImageRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val dataProvider: WidgetDataProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val data = dataProvider.getNearestSpotWithWeather()

        val glanceIds = GlanceAppWidgetManager(applicationContext)
            .getGlanceIds(NearestSpotWidget::class.java)

        if (glanceIds.isEmpty()) {
            return Result.success()
        }

        for (glanceId in glanceIds) {
            if (data != null) {
                // Load and cache bitmap
                val imagePath = loadAndCacheBitmap(data.spotImageUrl)

                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[stringPreferencesKey("spot_name")] = data.spotName
                    prefs[intPreferencesKey("spot_id")] = data.spotId
                    prefs[intPreferencesKey("spot_rating")] = data.spotRating
                    prefs[stringPreferencesKey("spot_distance")] = data.spotDistance
                    prefs[stringPreferencesKey("spot_image_path")] = imagePath ?: ""
                    prefs[stringPreferencesKey("weather_temp")] = data.weatherTemp
                    prefs[stringPreferencesKey("weather_icon")] = data.weatherIcon
                    prefs.remove(stringPreferencesKey("error_state"))
                }
            } else {
                // Set error state
                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[stringPreferencesKey("error_state")] = "no_data"
                }
            }

            NearestSpotWidget().update(applicationContext, glanceId)
        }

        return Result.success()
    }

    private suspend fun loadAndCacheBitmap(url: String?): String? {
        if (url.isNullOrBlank()) return null

        return try {
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .size(112, 112) // 56dp * 2 for higher density
                .build()

            val result = applicationContext.imageLoader.execute(request)
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return null

            val cacheFile = File(applicationContext.cacheDir, "widget_spot_image.png")
            FileOutputStream(cacheFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            }

            cacheFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "widget_update"
        private const val PERIODIC_WORK_NAME = "widget_periodic_update"

        fun enqueue(context: Context, expedited: Boolean = false) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .apply {
                    if (expedited) {
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    }
                }
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                30, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancelPeriodic(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
        }
    }
}
