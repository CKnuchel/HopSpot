package com.kickpaws.hopspot.ui.screens.benchdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.Photo
import com.kickpaws.hopspot.ui.components.BenchListItemSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchDetailScreen(
    benchId: Int,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: BenchDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Fullscreen image viewer
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Load bench on first composition
    LaunchedEffect(benchId) {
        viewModel.loadBench(benchId)
    }

    // Show snackbar when visit added
    LaunchedEffect(uiState.visitAdded) {
        if (uiState.visitAdded) {
            viewModel.resetVisitAdded()
        }
    }

    // Fullscreen Photo Dialog
    if (selectedPhotoUrl != null) {
        Dialog(onDismissRequest = { selectedPhotoUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedPhotoUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedPhotoUrl,
                    contentDescription = "Foto gross",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.bench?.name ?: "Bank Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                },
                actions = {
                    if (uiState.bench != null) {
                        IconButton(onClick = { onEditClick(benchId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Bearbeiten"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    // Skeleton loading
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Photo skeleton
                        com.kickpaws.hopspot.ui.components.ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BenchListItemSkeleton()
                    }
                }

                uiState.errorMessage != null && uiState.bench == null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.loadBench(benchId) }) {
                            Text("Erneut versuchen")
                        }
                    }
                }

                uiState.bench != null -> {
                    val bench = uiState.bench!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        // Photo Gallery
                        if (uiState.photos.isNotEmpty()) {
                            PhotoGallery(
                                photos = uiState.photos,
                                onPhotoClick = { photo ->
                                    selectedPhotoUrl = photo.urlMedium ?: photo.urlOriginal
                                }
                            )
                        } else if (!uiState.isLoadingPhotos) {
                            // No photos placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Keine Fotos",
                                        color = colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Info Section
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Name & Rating
                            Text(
                                text = bench.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground
                            )

                            if (bench.rating != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < bench.rating) {
                                                Icons.Default.Star
                                            } else {
                                                Icons.Default.StarBorder
                                            },
                                            contentDescription = null,
                                            tint = if (index < bench.rating) {
                                                colorScheme.primary
                                            } else {
                                                colorScheme.outline
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${bench.rating}/5",
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Amenities
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AmenityChip(
                                    icon = Icons.Default.Wc,
                                    label = "Toilette",
                                    available = bench.hasToilet
                                )
                                AmenityChip(
                                    icon = Icons.Default.Delete,
                                    label = "Abfalleimer",
                                    available = bench.hasTrashBin
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Location with Mini Map
                            LocationSection(
                                latitude = bench.latitude,
                                longitude = bench.longitude,
                                benchName = bench.name,
                                onOpenInMaps = {
                                    val uri = Uri.parse("geo:${bench.latitude},${bench.longitude}?q=${bench.latitude},${bench.longitude}(${Uri.encode(bench.name)})")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Description
                            if (!bench.description.isNullOrBlank()) {
                                Text(
                                    text = "Beschreibung",
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bench.description,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Visit Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Besuche",
                                        fontWeight = FontWeight.Medium,
                                        color = colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${uiState.visitCount} Besuche insgesamt",
                                        fontSize = 14.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // "Ich war hier" Button
                            Button(
                                onClick = { viewModel.addVisit(benchId) },
                                enabled = !uiState.isAddingVisit,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary
                                )
                            ) {
                                if (uiState.isAddingVisit) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ich war hier! 🍺")
                                }
                            }

                            // Show success message
                            if (uiState.visitAdded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✓ Besuch gespeichert!",
                                    color = colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationSection(
    latitude: Double,
    longitude: Double,
    benchName: String,
    onOpenInMaps: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val position = LatLng(latitude, longitude)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Standort",
                fontWeight = FontWeight.Medium,
                color = colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mini Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = rememberCameraPositionState {
                    this.position = CameraPosition.fromLatLngZoom(position, 15f)
                },
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false
                ),
                properties = MapProperties(mapType = MapType.NORMAL)
            ) {
                Marker(state = MarkerState(position = position), title = benchName)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Open in Google Maps Button
        OutlinedButton(
            onClick = onOpenInMaps,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("In Google Maps öffnen")
        }
    }
}

@Composable
private fun PhotoGallery(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val mainPhoto = photos.find { it.isMain }
    val otherPhotos = photos.filter { !it.isMain }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Main Photo (large)
        if (mainPhoto != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPhotoClick(mainPhoto) }
            ) {
                AsyncImage(
                    model = mainPhoto.urlMedium ?: mainPhoto.urlThumbnail,
                    contentDescription = "Hauptbild",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder_bench),
                    error = painterResource(R.drawable.placeholder_bench)
                )

                // Main badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hauptbild",
                            fontSize = 12.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Other photos (horizontal scroll)
        if (otherPhotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Weitere Fotos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(otherPhotos) { photo ->
                    AsyncImage(
                        model = photo.urlThumbnail ?: photo.urlMedium,
                        contentDescription = "Foto",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPhotoClick(photo) },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholder_bench),
                        error = painterResource(R.drawable.placeholder_bench)
                    )
                }
            }
        }
    }
}

@Composable
private fun AmenityChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    available: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (available) {
            colorScheme.primaryContainer
        } else {
            colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (available) {
                    colorScheme.onPrimaryContainer
                } else {
                    colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (available) {
                    colorScheme.onPrimaryContainer
                } else {
                    colorScheme.onSurfaceVariant
                }
            )
            if (available) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}