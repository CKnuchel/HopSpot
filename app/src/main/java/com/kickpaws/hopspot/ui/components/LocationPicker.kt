package com.kickpaws.hopspot.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LocationPickerCard(
    locationText: String,
    hasLocation: Boolean,
    latitude: Double?,
    longitude: Double?,
    onLocationSet: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    // Default position (Switzerland center) or current location
    val defaultPosition = LatLng(47.3769, 8.5417)
    val initialPosition = if (latitude != null && longitude != null) {
        LatLng(latitude, longitude)
    } else {
        defaultPosition
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }

    // Update position when camera stops moving
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val center = cameraPositionState.position.target
            onLocationSet(center.latitude, center.longitude)
        }
    }

    // Sync camera with external position changes (e.g., GPS button)
    var shouldAnimateToPosition by remember { mutableStateOf(false) }
    var targetPosition by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null && shouldAnimateToPosition) {
            val newPos = LatLng(latitude, longitude)
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newPos, 15f))
            shouldAnimateToPosition = false
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            scope.launch {
                isLoadingLocation = true
                locationError = null
                try {
                    val location = getCurrentLocation(context)
                    if (location != null) {
                        shouldAnimateToPosition = true
                        onLocationSet(location.first, location.second)
                        // Animate camera to new position
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.first, location.second), 15f
                            )
                        )
                    } else {
                        locationError = "Standort konnte nicht ermittelt werden"
                    }
                } catch (e: Exception) {
                    locationError = "Fehler: ${e.message}"
                } finally {
                    isLoadingLocation = false
                }
            }
        } else {
            locationError = "Standort-Berechtigung wurde verweigert"
        }
    }

    fun requestLocation() {
        val hasFinePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFinePermission || hasCoarsePermission) {
            scope.launch {
                isLoadingLocation = true
                locationError = null
                try {
                    val location = getCurrentLocation(context)
                    if (location != null) {
                        onLocationSet(location.first, location.second)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.first, location.second), 15f
                            )
                        )
                    } else {
                        locationError = "Standort konnte nicht ermittelt werden"
                    }
                } catch (e: Exception) {
                    locationError = "Fehler: ${e.message}"
                } finally {
                    isLoadingLocation = false
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = if (hasLocation) colorScheme.primary else colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Standort *",
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = locationText,
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                // GPS Button
                Button(
                    onClick = { requestLocation() },
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoadingLocation,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini Map with fixed center marker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        mapToolbarEnabled = false,
                        myLocationButtonEnabled = false,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        rotationGesturesEnabled = false,
                        tiltGesturesEnabled = false
                    ),
                    properties = MapProperties(
                        mapType = MapType.NORMAL
                    )
                )

                // Fixed center marker overlay
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Marker",
                    tint = colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                        .offset(y = (-20).dp) // Offset so pin points to center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 Bewege die Karte um den Standort anzupassen",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )

            // Error message
            if (locationError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = locationError!!,
                    fontSize = 12.sp,
                    color = colorScheme.error
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()

    return try {
        val location = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).await()

        if (location != null) {
            Pair(location.latitude, location.longitude)
        } else {
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                Pair(lastLocation.latitude, lastLocation.longitude)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}