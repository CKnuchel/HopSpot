package com.kickpaws.hopspot.ui.screens.benchdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.Photo
import com.kickpaws.hopspot.ui.components.BenchListItemSkeleton
import com.kickpaws.hopspot.ui.components.WeatherIcons

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

    // Fullscreen image viewer - track index for swipe support
    var selectedPhotoIndex by remember { mutableIntStateOf(-1) }

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

    // Fullscreen Photo Dialog with Zoom & Swipe
    if (selectedPhotoIndex >= 0 && uiState.photos.isNotEmpty()) {
        FullscreenImageViewer(
            photos = uiState.photos,
            initialIndex = selectedPhotoIndex,
            onDismiss = { selectedPhotoIndex = -1 }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.bench?.name ?: stringResource(R.string.bench_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (uiState.bench != null) {
                        // Favorite Button
                        IconButton(
                            onClick = { viewModel.toggleFavorite(benchId) },
                            enabled = !uiState.isTogglingFavorite
                        ) {
                            if (uiState.isTogglingFavorite) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (uiState.isFavorite) {
                                        Icons.Default.Favorite
                                    } else {
                                        Icons.Default.FavoriteBorder
                                    },
                                    contentDescription = if (uiState.isFavorite) {
                                        stringResource(R.string.cd_remove_from_favorites)
                                    } else {
                                        stringResource(R.string.cd_add_to_favorites)
                                    },
                                    tint = if (uiState.isFavorite) {
                                        Color.Red
                                    } else {
                                        colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        // Edit Button
                        IconButton(onClick = { onEditClick(benchId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.cd_edit)
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
                            Text(stringResource(R.string.common_retry))
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
                                onPhotoClick = { index ->
                                    selectedPhotoIndex = index
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
                                        text = stringResource(R.string.bench_detail_no_photos),
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
                                        text = stringResource(R.string.bench_detail_rating_format, bench.rating),
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
                                    label = stringResource(R.string.amenity_toilet),
                                    available = bench.hasToilet
                                )
                                AmenityChip(
                                    icon = Icons.Default.Delete,
                                    label = stringResource(R.string.amenity_trash_bin),
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
                                    text = stringResource(R.string.label_description),
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

                            // Weather Section
                            if (uiState.weather != null || uiState.isLoadingWeather) {
                                WeatherCard(
                                    weather = uiState.weather,
                                    isLoading = uiState.isLoadingWeather
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
                                        text = stringResource(R.string.bench_detail_visits),
                                        fontWeight = FontWeight.Medium,
                                        color = colorScheme.onBackground
                                    )
                                    Text(
                                        text = stringResource(R.string.bench_detail_visits_count, uiState.visitCount),
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
                                    Text(stringResource(R.string.btn_i_was_here))
                                }
                            }

                            // Show success message
                            if (uiState.visitAdded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.bench_detail_visit_saved),
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
                text = stringResource(R.string.bench_detail_location),
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
            Text(stringResource(R.string.btn_open_in_maps))
        }
    }
}

@Composable
private fun PhotoGallery(
    photos: List<Photo>,
    onPhotoClick: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val mainPhoto = photos.find { it.isMain }
    val mainPhotoIndex = photos.indexOfFirst { it.isMain }.takeIf { it >= 0 } ?: 0

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
                    .clickable { onPhotoClick(mainPhotoIndex) }
            ) {
                AsyncImage(
                    model = mainPhoto.urlMedium ?: mainPhoto.urlThumbnail,
                    contentDescription = stringResource(R.string.bench_detail_main_image),
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
                            text = stringResource(R.string.bench_detail_main_image),
                            fontSize = 12.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Other photos (horizontal scroll)
        val otherPhotosWithIndex = photos.mapIndexed { index, photo -> index to photo }
            .filter { !it.second.isMain }

        if (otherPhotosWithIndex.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.bench_detail_more_photos),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(otherPhotosWithIndex.size) { i ->
                    val (index, photo) = otherPhotosWithIndex[i]
                    AsyncImage(
                        model = photo.urlThumbnail ?: photo.urlMedium,
                        contentDescription = stringResource(R.string.cd_photo),
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPhotoClick(index) },
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

@Composable
private fun FullscreenImageViewer(
    photos: List<Photo>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // Pager for swiping between images
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(
                    imageUrl = photos[page].urlOriginal ?: photos[page].urlMedium ?: "",
                    onTap = onDismiss
                )
            }

            // Close button (top right)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Page indicator (bottom center)
            if (photos.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(photos.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (index == pagerState.currentPage) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.4f)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    imageUrl: String,
    onTap: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)

        // Only allow panning when zoomed in
        if (scale > 1f) {
            offset += panChange
        } else {
            offset = Offset.Zero
        }
    }

    // Reset zoom on double tap or when switching pages
    LaunchedEffect(imageUrl) {
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                // Tap to close only when not zoomed
                if (scale <= 1f) {
                    onTap()
                } else {
                    // Reset zoom on tap when zoomed
                    scale = 1f
                    offset = Offset.Zero
                }
            }
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.cd_photo_fullscreen),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun WeatherCard(
    weather: WeatherInfo?,
    isLoading: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val weatherIconFont = WeatherIcons.fontFamily

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.bench_detail_current_weather),
                fontWeight = FontWeight.Medium,
                color = colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            // Loading skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surfaceVariant)
                    )
                }
            }
        } else if (weather != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Weather Icon & Condition
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = WeatherIcons.getIconForCode(weather.weathercode).toString(),
                            fontFamily = weatherIconFont,
                            fontSize = 42.sp,
                            color = getWeatherColor(weather.weathercode, colorScheme)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getWeatherDescription(weather.weathercode),
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    // Temperature
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${weather.temperature.toInt()}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground
                            )
                            Text(
                                text = "°C",
                                fontSize = 16.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = stringResource(R.string.bench_detail_temperature),
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    // Wind
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = WeatherIcons.getWindDirectionIcon(weather.winddirection).toString(),
                                fontFamily = weatherIconFont,
                                fontSize = 24.sp,
                                color = colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${weather.windspeed.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onBackground
                            )
                            Text(
                                text = "km/h",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = stringResource(R.string.wind_format, getWindDirectionText(weather.winddirection)),
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getWeatherColor(code: Int, colorScheme: ColorScheme): Color {
    return when (code) {
        0, 1 -> Color(0xFFFFB300)         // Sunny - amber/gold
        2, 3 -> Color(0xFF78909C)          // Cloudy - blue grey
        45, 48 -> Color(0xFF90A4AE)        // Fog - light grey
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Color(0xFF42A5F5)  // Rain - blue
        56, 57, 66, 67 -> Color(0xFF4FC3F7) // Freezing rain - light blue
        71, 73, 75, 77, 85, 86 -> Color(0xFF81D4FA) // Snow - very light blue
        95, 96, 99 -> Color(0xFFFFCA28)    // Thunderstorm - yellow
        else -> colorScheme.primary
    }
}

@Composable
private fun getWeatherDescription(code: Int): String = when (code) {
    0 -> stringResource(R.string.weather_clear)
    1 -> stringResource(R.string.weather_fair)
    2 -> stringResource(R.string.weather_partly_cloudy)
    3 -> stringResource(R.string.weather_cloudy)
    45, 48 -> stringResource(R.string.weather_fog)
    51, 53, 55 -> stringResource(R.string.weather_drizzle)
    56, 57 -> stringResource(R.string.weather_freezing_drizzle)
    61, 63, 65 -> stringResource(R.string.weather_rain)
    66, 67 -> stringResource(R.string.weather_freezing_rain)
    71, 73, 75 -> stringResource(R.string.weather_snow)
    77 -> stringResource(R.string.weather_snow_grains)
    80, 81, 82 -> stringResource(R.string.weather_rain_showers)
    85, 86 -> stringResource(R.string.weather_snow_showers)
    95 -> stringResource(R.string.weather_thunderstorm)
    96, 99 -> stringResource(R.string.weather_thunderstorm_hail)
    else -> stringResource(R.string.weather_unknown)
}

@Composable
private fun getWindDirectionText(degrees: Int): String = when ((degrees + 22) % 360 / 45) {
    0 -> stringResource(R.string.wind_n)
    1 -> stringResource(R.string.wind_ne)
    2 -> stringResource(R.string.wind_e)
    3 -> stringResource(R.string.wind_se)
    4 -> stringResource(R.string.wind_s)
    5 -> stringResource(R.string.wind_sw)
    6 -> stringResource(R.string.wind_w)
    7 -> stringResource(R.string.wind_nw)
    else -> ""
}
