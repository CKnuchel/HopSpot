package com.kickpaws.hopspot.ui.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.Bench
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.core.graphics.scale

@Composable
fun MapScreen(
    onBenchClick: (Int) -> Unit,
    onCreateBenchClick: () -> Unit,
    onActivityFeedClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var beerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Default: Schweiz Mitte (Fallback)
    val defaultPosition = LatLng(46.8182, 8.2275)
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasInitializedCamera by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 8f)
    }

    // Location Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            scope.launch {
                val location = getCurrentLocation(context)
                if (location != null) {
                    userLocation = LatLng(location.first, location.second)
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(userLocation!!, 14f)
                    )
                }
            }
        }
    }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Result not critical */ }

    // Request notification permission on first load (Android 13+)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Request location on first load
    LaunchedEffect(Unit) {
        if (hasInitializedCamera) return@LaunchedEffect

        val hasFinePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFinePermission || hasCoarsePermission) {
            val location = getCurrentLocation(context)
            if (location != null) {
                userLocation = LatLng(location.first, location.second)
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(userLocation!!, 14f)
                )
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        hasInitializedCamera = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false, // Pinch will work
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            ),
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = userLocation != null
            ),
            onMapClick = { viewModel.clearSelection() },
            onMapLoaded = {
                // Bitmap laden und skalieren
                val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_beer_marker)
                val scaledBitmap = originalBitmap.scale(96, 96, false)
                beerIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
            }
        ) {
            // Markers für alle Bänke
            uiState.benches.forEach { bench ->
                Marker(
                    state = MarkerState(position = LatLng(bench.latitude, bench.longitude)),
                    title = bench.name,
                    snippet = bench.description?.take(50),
                    icon = beerIcon,
                    onClick = {
                        viewModel.selectBench(bench)
                        true
                    }
                )
            }
        }

        // Loading Indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Error Message
        if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = viewModel::loadAllBenches) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }
        }

        // Bench Count Badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Chair,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.map_benches_count, uiState.benches.size),
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        // Top End Buttons (Activity Feed + My Location)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Activity Feed Button
            FilledIconButton(
                onClick = onActivityFeedClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = stringResource(R.string.cd_activity_feed),
                    tint = colorScheme.primary
                )
            }

            // My Location Button
            FilledIconButton(
                onClick = {
                    scope.launch {
                        val location = getCurrentLocation(context)
                        if (location != null) {
                            userLocation = LatLng(location.first, location.second)
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(userLocation!!, 14f)
                            )
                        }
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.cd_my_location),
                    tint = colorScheme.primary
                )
            }
        }

        // FAB für neue Bank
        FloatingActionButton(
            onClick = onCreateBenchClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.cd_new_bench)
            )
        }

        // Selected Bench Bottom Card
        uiState.selectedBench?.let { bench ->
            BenchPreviewCard(
                bench = bench,
                onClick = { onBenchClick(bench.id) },
                onDismiss = { viewModel.clearSelection() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
            )
        }
    }
}

@Composable
private fun BenchPreviewCard(
    bench: Bench,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = bench.mainPhotoUrl,
                contentDescription = bench.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder_bench),
                error = painterResource(R.drawable.placeholder_bench)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bench.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (bench.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < bench.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index < bench.rating) colorScheme.primary else colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (!bench.description.isNullOrBlank()) {
                    Text(
                        text = bench.description,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Amenities
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (bench.hasToilet) {
                        Icon(
                            imageVector = Icons.Default.Wc,
                            contentDescription = stringResource(R.string.cd_toilet),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (bench.hasTrashBin) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_trash_bin),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.cd_details),
                tint = colorScheme.primary
            )
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
