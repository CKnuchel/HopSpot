@file:Suppress("GoogleMapComposable", "KotlinConstantConditions")

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    var showFullscreenMap by remember { mutableStateOf(false) }

    // Default position (Switzerland center) or current location
    val defaultPosition = LatLng(47.3769, 8.5417)
    val currentPosition = if (latitude != null && longitude != null) {
        LatLng(latitude, longitude)
    } else {
        defaultPosition
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
                        onLocationSet(location.first, location.second)
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

    // Fullscreen Map Dialog
    if (showFullscreenMap) {
        FullscreenMapDialog(
            initialPosition = currentPosition,
            onDismiss = { showFullscreenMap = false },
            onLocationSelected = { lat, lon ->
                onLocationSet(lat, lon)
                showFullscreenMap = false
            },
            onRequestGps = { requestLocation() },
            isLoadingGps = isLoadingLocation
        )
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

            // Mini Map Preview (clickable to open fullscreen)
            val previewCameraState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentPosition, 17f)
            }

            // Update preview when position changes
            LaunchedEffect(currentPosition) {
                previewCameraState.animate(CameraUpdateFactory.newLatLngZoom(currentPosition, 17f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = previewCameraState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        mapToolbarEnabled = false,
                        myLocationButtonEnabled = false,
                        scrollGesturesEnabled = false,
                        zoomGesturesEnabled = false,
                        rotationGesturesEnabled = false,
                        tiltGesturesEnabled = false,
                        scrollGesturesEnabledDuringRotateOrZoom = false
                    ),
                    properties = MapProperties(mapType = MapType.NORMAL),
                    onMapClick = { showFullscreenMap = true }
                ) {
                    Marker(
                        state = rememberMarkerState(position = currentPosition)
                    )
                }

                // Fullscreen button overlay
                FilledIconButton(
                    onClick = { showFullscreenMap = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Vollbild",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 Tippe auf die Karte um den Standort anzupassen",
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

@Composable
private fun FullscreenMapDialog(
    initialPosition: LatLng,
    onDismiss: () -> Unit,
    onLocationSelected: (Double, Double) -> Unit,
    onRequestGps: () -> Unit,
    isLoadingGps: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 17f)
    }

    // Track current center position
    var currentCenter by remember { mutableStateOf(initialPosition) }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            currentCenter = cameraPositionState.position.target
        }
    }

    // Sync with GPS updates
    LaunchedEffect(initialPosition) {
        if (initialPosition != currentCenter) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(initialPosition, 17f))
            currentCenter = initialPosition
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fullscreen Map
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        mapToolbarEnabled = false,
                        myLocationButtonEnabled = false,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        rotationGesturesEnabled = false,
                        tiltGesturesEnabled = false
                    ),
                    properties = MapProperties(mapType = MapType.NORMAL)
                )

                // Fixed center marker
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Marker",
                    tint = colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .offset(y = (-24).dp)
                )

                // Top bar with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colorScheme.surface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schliessen"
                        )
                    }

                    // GPS Button
                    FilledIconButton(
                        onClick = onRequestGps,
                        enabled = !isLoadingGps,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colorScheme.surface
                        )
                    ) {
                        if (isLoadingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "GPS"
                            )
                        }
                    }
                }

                // Confirm button
                Button(
                    onClick = { onLocationSelected(currentCenter.latitude, currentCenter.longitude) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Standort bestätigen",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
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
    } catch (_: Exception) {
        null
    }
}