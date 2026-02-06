package com.kickpaws.hopspot.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.kickpaws.hopspot.data.local.dao.SpotDao
import com.kickpaws.hopspot.data.local.entity.SpotEntity
import com.kickpaws.hopspot.data.network.NetworkMonitor
import com.kickpaws.hopspot.data.remote.api.HopSpotApi
import com.kickpaws.hopspot.data.remote.mapper.toDomain
import com.kickpaws.hopspot.domain.model.Spot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class WidgetDataProvider @Inject constructor(
    private val api: HopSpotApi,
    private val spotDao: SpotDao,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context
) {
    suspend fun getNearestSpotWithWeather(): WidgetData? {
        // 1. Get current location
        val location = getCurrentLocation() ?: return null

        // 2. Get nearest spot
        val spot = if (networkMonitor.isOnlineNow) {
            try {
                val response = api.getSpots(
                    page = 1,
                    limit = 1,
                    sortBy = "distance",
                    lat = location.first,
                    lon = location.second
                )
                response.data?.spots?.firstOrNull()?.toDomain()
            } catch (e: Exception) {
                // Fallback to local
                getNearestLocalSpot(location)
            }
        } else {
            getNearestLocalSpot(location)
        }

        spot ?: return null

        // 3. Get weather for spot
        val weather = try {
            if (networkMonitor.isOnlineNow) {
                api.getWeather(spot.latitude, spot.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        // Calculate distance if not provided
        val distance = spot.distance ?: calculateDistance(
            location.first, location.second,
            spot.latitude, spot.longitude
        )

        return WidgetData(
            spotId = spot.id,
            spotName = spot.name,
            spotRating = spot.rating ?: 0,
            spotDistance = formatDistance(distance),
            spotImageUrl = spot.mainPhotoUrl,
            weatherTemp = weather?.currentWeather?.temperature?.let { "${it.toInt()}\u00B0C" } ?: "",
            weatherIcon = weather?.currentWeather?.weathercode?.let { getWeatherEmoji(it) } ?: ""
        )
    }

    private fun getWeatherEmoji(code: Int): String = when (code) {
        0 -> "\u2600\uFE0F"           // Clear sky
        1, 2 -> "\uD83C\uDF24\uFE0F"  // Partly cloudy
        3 -> "\u2601\uFE0F"           // Cloudy
        in 45..48 -> "\uD83C\uDF2B\uFE0F" // Fog
        in 51..67 -> "\uD83C\uDF27\uFE0F" // Rain/Drizzle
        in 71..77 -> "\uD83C\uDF28\uFE0F" // Snow
        in 80..82 -> "\uD83C\uDF26\uFE0F" // Rain showers
        in 95..99 -> "\u26C8\uFE0F"   // Thunderstorm
        else -> "\uD83C\uDF21\uFE0F"  // Temperature icon as fallback
    }

    private fun formatDistance(meters: Double?): String {
        if (meters == null) return ""
        return if (meters < 1000) {
            "${meters.toInt()}m"
        } else {
            String.format("%.1fkm", meters / 1000)
        }
    }

    private suspend fun getCurrentLocation(): Pair<Double, Double>? {
        // Check permissions
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            return null
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        return try {
            // Try to get last known location first
            val lastLocation: Location? = fusedClient.lastLocation.await()
            if (lastLocation != null) {
                return Pair(lastLocation.latitude, lastLocation.longitude)
            }

            // If no last location, request current location
            val cancellationTokenSource = CancellationTokenSource()
            val priority = if (hasFineLocation) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            val currentLocation = fusedClient.getCurrentLocation(
                priority,
                cancellationTokenSource.token
            ).await()

            currentLocation?.let { Pair(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getNearestLocalSpot(location: Pair<Double, Double>): Spot? {
        val spots = spotDao.getAllSpotsOnce()
        val nearest = spots.minByOrNull { spot ->
            calculateDistance(location.first, location.second, spot.latitude, spot.longitude)
        }

        return nearest?.let { entity ->
            val distance = calculateDistance(
                location.first, location.second,
                entity.latitude, entity.longitude
            )
            entity.toDomain(distance)
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun SpotEntity.toDomain(calculatedDistance: Double? = null): Spot {
        return Spot(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            description = description,
            rating = rating,
            hasToilet = hasToilet,
            hasTrashBin = hasTrashBin,
            mainPhotoUrl = mainPhotoUrl,
            distance = calculatedDistance ?: distance,
            createdById = createdById,
            createdByName = createdByName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

data class WidgetData(
    val spotId: Int,
    val spotName: String,
    val spotRating: Int,
    val spotDistance: String,
    val spotImageUrl: String?,
    val weatherTemp: String,
    val weatherIcon: String
)
