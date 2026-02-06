package com.kickpaws.hopspot.ui.screens.benchdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.kickpaws.hopspot.ui.components.common.HopSpotErrorView
import com.kickpaws.hopspot.ui.components.common.HopSpotLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.LoadingSize
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotShapes

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

    var selectedPhotoIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(benchId) {
        viewModel.loadBench(benchId)
    }

    LaunchedEffect(uiState.visitAdded) {
        if (uiState.visitAdded) {
            viewModel.resetVisitAdded()
        }
    }

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
                        IconButton(
                            onClick = { viewModel.toggleFavorite(benchId) },
                            enabled = !uiState.isTogglingFavorite
                        ) {
                            if (uiState.isTogglingFavorite) {
                                HopSpotLoadingIndicator(
                                    size = LoadingSize.Button,
                                    color = colorScheme.onSurface
                                )
                            } else {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (uiState.isFavorite) {
                                        stringResource(R.string.cd_remove_from_favorites)
                                    } else {
                                        stringResource(R.string.cd_add_to_favorites)
                                    },
                                    tint = if (uiState.isFavorite) Color.Red else colorScheme.onSurface
                                )
                            }
                        }
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(HopSpotDimensions.Screen.padding)
                    ) {
                        com.kickpaws.hopspot.ui.components.ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = HopSpotShapes.card
                        )
                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))
                        BenchListItemSkeleton()
                    }
                }

                uiState.errorMessage != null && uiState.bench == null -> {
                    HopSpotErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.loadBench(benchId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                                onPhotoClick = { index -> selectedPhotoIndex = index }
                            )
                        } else if (!uiState.isLoadingPhotos) {
                            NoPhotosPlaceholder()
                        }

                        Column(
                            modifier = Modifier.padding(HopSpotDimensions.Screen.padding)
                        ) {
                            // Name & Rating Row with "Ich war hier" Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bench.name,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onBackground
                                    )

                                    if (bench.rating != null) {
                                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xxs))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            repeat(5) { index ->
                                                Icon(
                                                    imageVector = if (index < bench.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = null,
                                                    tint = if (index < bench.rating) colorScheme.primary else colorScheme.outline,
                                                    modifier = Modifier.size(HopSpotDimensions.Icon.medium)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))
                                            Text(
                                                text = stringResource(R.string.bench_detail_rating_format, bench.rating),
                                                color = colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))

                                // Compact "Ich war hier" Button
                                Button(
                                    onClick = { viewModel.addVisit(benchId) },
                                    enabled = !uiState.isAddingVisit,
                                    shape = HopSpotShapes.button,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    if (uiState.isAddingVisit) {
                                        HopSpotLoadingIndicator(
                                            size = LoadingSize.Small,
                                            color = colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(HopSpotDimensions.Icon.small)
                                        )
                                        Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xxs))
                                        Text(
                                            text = stringResource(R.string.btn_i_was_here),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            // Visit success message
                            if (uiState.visitAdded) {
                                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
                                Text(
                                    text = stringResource(R.string.bench_detail_visit_saved),
                                    color = colorScheme.primary,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

                            // Info Grid: Weather + Amenities (2 columns)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.sm)
                            ) {
                                // Weather Card
                                WeatherInfoCard(
                                    weather = uiState.weather,
                                    isLoading = uiState.isLoadingWeather,
                                    modifier = Modifier.weight(1f)
                                )

                                // Amenities Card
                                AmenitiesCard(
                                    hasToilet = bench.hasToilet,
                                    hasTrashBin = bench.hasTrashBin,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

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

                            // Description - only show if not blank
                            if (!bench.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

                                Text(
                                    text = stringResource(R.string.label_description),
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xxs))
                                Text(
                                    text = bench.description,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }

                            // Compact Visit Counter
                            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(HopSpotDimensions.Icon.small)
                                )
                                Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))
                                Text(
                                    text = stringResource(R.string.bench_detail_visits_count, uiState.visitCount),
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xl))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoPhotosPlaceholder() {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(HopSpotDimensions.Screen.padding)
            .clip(HopSpotShapes.card)
            .background(colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = colorScheme.onPrimaryContainer,
                modifier = Modifier.size(HopSpotDimensions.Icon.large)
            )
            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
            Text(
                text = stringResource(R.string.bench_detail_no_photos),
                color = colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WeatherInfoCard(
    weather: WeatherInfo?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val weatherIconFont = WeatherIcons.fontFamily

    Card(
        modifier = modifier.fillMaxHeight(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(HopSpotDimensions.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(HopSpotDimensions.Icon.small)
                )
                Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xxs))
                Text(
                    text = stringResource(R.string.bench_detail_current_weather),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            when {
                isLoading -> {
                    HopSpotLoadingIndicator(size = LoadingSize.Button)
                }
                weather != null -> {
                    Text(
                        text = WeatherIcons.getIconForCode(weather.weathercode).toString(),
                        fontFamily = weatherIconFont,
                        fontSize = 32.sp,
                        color = getWeatherColor(weather.weathercode, colorScheme)
                    )
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${weather.temperature.toInt()}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                        Text(
                            text = "°C",
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = getWeatherDescription(weather.weathercode),
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Text(
                        text = "—",
                        fontSize = 24.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AmenitiesCard(
    hasToilet: Boolean,
    hasTrashBin: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxHeight(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(HopSpotDimensions.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.bench_form_amenities),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            Row(
                horizontalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.sm)
            ) {
                AmenityIndicator(
                    icon = Icons.Default.Wc,
                    available = hasToilet
                )
                AmenityIndicator(
                    icon = Icons.Default.Delete,
                    available = hasTrashBin
                )
            }
        }
    }
}

@Composable
private fun AmenityIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    available: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (available) colorScheme.primary else colorScheme.outline,
            modifier = Modifier.size(HopSpotDimensions.Icon.medium)
        )
        Icon(
            imageVector = if (available) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (available) colorScheme.primary else colorScheme.outline,
            modifier = Modifier.size(14.dp)
        )
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(HopSpotDimensions.Icon.medium)
            )
            Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))
            Text(
                text = stringResource(R.string.bench_detail_location),
                fontWeight = FontWeight.Medium,
                color = colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(HopSpotShapes.card)
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

        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

        OutlinedButton(
            onClick = onOpenInMaps,
            modifier = Modifier.fillMaxWidth(),
            shape = HopSpotShapes.button
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(HopSpotDimensions.Icon.small)
            )
            Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))
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
            .padding(HopSpotDimensions.Screen.padding)
    ) {
        if (mainPhoto != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(HopSpotShapes.card)
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

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(HopSpotDimensions.Spacing.xs),
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

        val otherPhotosWithIndex = photos.mapIndexed { index, photo -> index to photo }
            .filter { !it.second.isMain }

        if (otherPhotosWithIndex.isNotEmpty()) {
            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

            Text(
                text = stringResource(R.string.bench_detail_more_photos),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.xs)
            ) {
                items(otherPhotosWithIndex.size) { i ->
                    val (index, photo) = otherPhotosWithIndex[i]
                    AsyncImage(
                        model = photo.urlThumbnail ?: photo.urlMedium,
                        contentDescription = stringResource(R.string.cd_photo),
                        modifier = Modifier
                            .size(80.dp)
                            .clip(HopSpotShapes.thumbnail)
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(
                    imageUrl = photos[page].urlOriginal ?: photos[page].urlMedium ?: "",
                    onTap = onDismiss
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(HopSpotDimensions.Spacing.md)
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

            if (photos.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.xs)
                ) {
                    repeat(photos.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.4f),
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
        if (scale > 1f) {
            offset += panChange
        } else {
            offset = Offset.Zero
        }
    }

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
                if (scale <= 1f) {
                    onTap()
                } else {
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

private fun getWeatherColor(code: Int, colorScheme: ColorScheme): Color {
    return when (code) {
        0, 1 -> Color(0xFFFFB300)
        2, 3 -> Color(0xFF78909C)
        45, 48 -> Color(0xFF90A4AE)
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Color(0xFF42A5F5)
        56, 57, 66, 67 -> Color(0xFF4FC3F7)
        71, 73, 75, 77, 85, 86 -> Color(0xFF81D4FA)
        95, 96, 99 -> Color(0xFFFFCA28)
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
