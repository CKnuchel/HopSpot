package com.kickpaws.hopspot.ui.screens.spotedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.Photo
import com.kickpaws.hopspot.ui.components.LocationPickerCard
import com.kickpaws.hopspot.ui.components.PhotoPickerDialog
import com.kickpaws.hopspot.ui.components.ShimmerBox
import com.kickpaws.hopspot.ui.components.common.HopSpotDeleteConfirmationDialog
import com.kickpaws.hopspot.ui.components.common.HopSpotErrorView
import com.kickpaws.hopspot.ui.components.common.HopSpotLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.LoadingSize
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotEditScreen(
    spotId: Int,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: SpotEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    // Photo picker dialog state
    var showPhotoPickerDialog by remember { mutableStateOf(false) }

    // Fullscreen photo viewer
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Photo to delete confirmation
    var photoToDelete by remember { mutableStateOf<Photo?>(null) }

    // Load spot on first composition
    LaunchedEffect(spotId) {
        viewModel.loadSpot(spotId)
    }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    // Handle delete success
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onDeleted()
        }
    }

    // Photo Picker Dialog (Kamera oder Galerie)
    if (showPhotoPickerDialog) {
        PhotoPickerDialog(
            onDismiss = { showPhotoPickerDialog = false },
            onPhotoSelected = { uri ->
                viewModel.onPhotoSelected(uri)
            }
        )
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
                    contentDescription = stringResource(R.string.cd_photo_large),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    // Delete Spot Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.dialog_delete_spot_title)) },
            text = {
                Text(stringResource(R.string.dialog_delete_spot_message, uiState.name))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSpot()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Delete Photo Confirmation Dialog
    if (photoToDelete != null) {
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.dialog_delete_photo_title)) },
            text = { Text(stringResource(R.string.dialog_delete_photo_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        photoToDelete?.let { viewModel.deletePhoto(it) }
                        photoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.spot_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveSpot,
                        enabled = !uiState.isSaving && !uiState.isUploadingPhoto
                    ) {
                        if (uiState.isSaving) {
                            HopSpotLoadingIndicator(
                                size = LoadingSize.Button,
                                color = colorScheme.primary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.common_save),
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold
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
                            .padding(HopSpotDimensions.Screen.padding),
                        verticalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.md)
                    ) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = HopSpotShapes.card
                        )
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = HopSpotShapes.card
                        )
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = HopSpotShapes.card
                        )
                    }
                }

                uiState.errorMessage != null && uiState.spot == null -> {
                    HopSpotErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.loadSpot(spotId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(HopSpotDimensions.Screen.padding),
                        verticalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.md)
                    ) {
                        // Photo Section
                        PhotoSection(
                            photos = uiState.photos,
                            isUploading = uiState.isUploadingPhoto,
                            isSettingMain = uiState.isSettingMainPhoto,
                            onPhotoClick = { photo ->
                                selectedPhotoUrl = photo.urlMedium ?: photo.urlOriginal
                            },
                            onSetMainClick = { photo ->
                                viewModel.setMainPhoto(photo)
                            },
                            onDeleteClick = { photo ->
                                photoToDelete = photo
                            },
                            onAddClick = { showPhotoPickerDialog = true }
                        )

                        // Name
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = viewModel::onNameChange,
                            label = { Text(stringResource(R.string.label_name_required)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Chair,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                            },
                            singleLine = true,
                            shape = HopSpotShapes.textField,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Location Card
                        LocationPickerCard(
                            locationText = uiState.locationText,
                            hasLocation = uiState.latitude != null,
                            latitude = uiState.latitude,
                            longitude = uiState.longitude,
                            onLocationSet = { lat, lon ->
                                viewModel.setLocation(lat, lon)
                            }
                        )

                        // Description
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChange,
                            label = { Text(stringResource(R.string.label_description)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                            },
                            minLines = 3,
                            maxLines = 5,
                            shape = HopSpotShapes.textField,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Rating
                        RatingSelector(
                            rating = uiState.rating,
                            onRatingChange = viewModel::onRatingChange
                        )

                        // Amenities
                        AmenitiesCard(
                            hasToilet = uiState.hasToilet,
                            hasTrashBin = uiState.hasTrashBin,
                            onHasToiletChange = viewModel::onHasToiletChange,
                            onHasTrashBinChange = viewModel::onHasTrashBinChange
                        )

                        // Error Message
                        if (uiState.errorMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorScheme.errorContainer
                                ),
                                shape = HopSpotShapes.card
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(HopSpotDimensions.Spacing.md),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))
                                    Text(
                                        text = uiState.errorMessage!!,
                                        color = colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.md))

                        // Delete Button
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = HopSpotShapes.button,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.error
                            ),
                            border = BorderStroke(1.dp, colorScheme.error),
                            enabled = !uiState.isDeleting
                        ) {
                            if (uiState.isDeleting) {
                                HopSpotLoadingIndicator(
                                    size = LoadingSize.Button,
                                    color = colorScheme.error
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))
                                Text(stringResource(R.string.btn_delete_spot))
                            }
                        }

                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xl))
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoSection(
    photos: List<Photo>,
    isUploading: Boolean,
    isSettingMain: Boolean,
    onPhotoClick: (Photo) -> Unit,
    onSetMainClick: (Photo) -> Unit,
    onDeleteClick: (Photo) -> Unit,
    onAddClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val mainPhoto = photos.find { it.isMain }
    val otherPhotos = photos.filter { !it.isMain }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md)
        ) {
            Text(
                text = stringResource(R.string.label_photos),
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

            // Main Photo (large)
            if (mainPhoto != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(HopSpotShapes.thumbnail)
                        .clickable { onPhotoClick(mainPhoto) }
                ) {
                    AsyncImage(
                        model = mainPhoto.urlMedium ?: mainPhoto.urlThumbnail,
                        contentDescription = stringResource(R.string.cd_main_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholder_spot),
                        error = painterResource(R.drawable.placeholder_spot)
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
                                text = stringResource(R.string.spot_detail_main_image),
                                fontSize = 12.sp,
                                color = colorScheme.onPrimary
                            )
                        }
                    }

                    // Delete button
                    IconButton(
                        onClick = { onDeleteClick(mainPhoto) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = colorScheme.error,
                            modifier = Modifier
                                .background(
                                    colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                                .padding(4.dp)
                        )
                    }
                }
            } else if (!isUploading) {
                // No main photo placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(HopSpotShapes.thumbnail)
                        .border(
                            width = 2.dp,
                            color = colorScheme.outline.copy(alpha = 0.5f),
                            shape = HopSpotShapes.thumbnail
                        )
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(HopSpotDimensions.Icon.large)
                        )
                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
                        Text(
                            text = stringResource(R.string.btn_add_photo),
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Upload progress
            if (isUploading) {
                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HopSpotLoadingIndicator(
                        size = LoadingSize.Button,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))
                    Text(stringResource(R.string.spot_form_uploading), color = colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

            // Other photos (horizontal scroll)
            Text(
                text = stringResource(R.string.spot_detail_more_photos),
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.xs)
            ) {
                items(otherPhotos) { photo ->
                    PhotoThumbnail(
                        photo = photo,
                        isSettingMain = isSettingMain,
                        onClick = { onPhotoClick(photo) },
                        onSetMainClick = { onSetMainClick(photo) },
                        onDeleteClick = { onDeleteClick(photo) }
                    )
                }

                // Add photo button
                item {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(HopSpotShapes.thumbnail)
                            .border(
                                width = 2.dp,
                                color = colorScheme.outline.copy(alpha = 0.5f),
                                shape = HopSpotShapes.thumbnail
                            )
                            .clickable(onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_add_photo),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            Text(
                text = stringResource(R.string.spot_form_set_main_hint),
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: Photo,
    isSettingMain: Boolean,
    onClick: () -> Unit,
    onSetMainClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(HopSpotShapes.thumbnail)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = photo.urlThumbnail,
            contentDescription = stringResource(R.string.cd_photo),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholder_spot),
            error = painterResource(R.drawable.placeholder_spot)
        )

        // Action buttons overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(colorScheme.surface.copy(alpha = 0.8f))
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Set as main
            IconButton(
                onClick = onSetMainClick,
                enabled = !isSettingMain,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.StarBorder,
                    contentDescription = stringResource(R.string.cd_set_as_main),
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RatingSelector(
    rating: Int?,
    onRatingChange: (Int?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md)
        ) {
            Text(
                text = stringResource(R.string.label_rating),
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                (1..5).forEach { star ->
                    IconButton(onClick = {
                        onRatingChange(if (rating == star) null else star)
                    }) {
                        Icon(
                            imageVector = if (rating != null && star <= rating) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = stringResource(R.string.cd_stars, star),
                            tint = if (rating != null && star <= rating) {
                                colorScheme.primary
                            } else {
                                colorScheme.outline
                            },
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            if (rating != null) {
                Text(
                    text = stringResource(R.string.spot_form_rating_format, rating),
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AmenitiesCard(
    hasToilet: Boolean,
    hasTrashBin: Boolean,
    onHasToiletChange: (Boolean) -> Unit,
    onHasTrashBinChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md)
        ) {
            Text(
                text = stringResource(R.string.spot_form_amenities),
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wc,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))
                    Text(stringResource(R.string.spot_form_toilet_nearby))
                }
                Switch(
                    checked = hasToilet,
                    onCheckedChange = onHasToiletChange
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = HopSpotDimensions.Spacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))
                    Text(stringResource(R.string.spot_form_trash_nearby))
                }
                Switch(
                    checked = hasTrashBin,
                    onCheckedChange = onHasTrashBinChange
                )
            }
        }
    }
}
