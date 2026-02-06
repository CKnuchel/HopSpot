package com.kickpaws.hopspot.ui.screens.benchcreate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.ui.components.LocationPickerCard
import com.kickpaws.hopspot.ui.components.PhotoPickerDialog
import com.kickpaws.hopspot.ui.components.common.HopSpotLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.LoadingSize
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchCreateScreen(
    onNavigateBack: () -> Unit,
    onBenchCreated: (Int) -> Unit,
    viewModel: BenchCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    var showPhotoPickerDialog by remember { mutableStateOf(false) }

    // Photo Picker Dialog
    if (showPhotoPickerDialog) {
        PhotoPickerDialog(
            onDismiss = { showPhotoPickerDialog = false },
            onPhotoSelected = { uri ->
                viewModel.onPhotoSelected(uri)
            }
        )
    }

    // Navigate when bench created
    LaunchedEffect(uiState.createdBenchId) {
        uiState.createdBenchId?.let { benchId ->
            onBenchCreated(benchId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bench_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.common_cancel)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveBench,
                        enabled = !uiState.isSaving && !uiState.isUploadingPhoto
                    ) {
                        if (uiState.isSaving || uiState.isUploadingPhoto) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorScheme.background)
                .verticalScroll(scrollState)
                .padding(HopSpotDimensions.Screen.padding),
            verticalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.md)
        ) {
            // Photo Card
            PhotoCard(
                photoUri = uiState.photoUri,
                isUploading = uiState.isUploadingPhoto,
                onSelectPhoto = { showPhotoPickerDialog = true },
                onRemovePhoto = viewModel::removePhoto
            )

            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.label_name_required)) },
                placeholder = { Text(stringResource(R.string.hint_bench_name)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Chair,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                },
                singleLine = true,
                shape = HopSpotShapes.textField,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    cursorColor = colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Location Card
            LocationPickerCard(
                locationText = uiState.locationText,
                hasLocation = uiState.latitude != null,
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                onLocationSet = { lat, lon -> viewModel.setLocation(lat, lon) }
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.label_description)) },
                placeholder = { Text(stringResource(R.string.hint_bench_description)) },
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    cursorColor = colorScheme.primary
                ),
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
        }
    }
}

@Composable
private fun PhotoCard(
    photoUri: Uri?,
    isUploading: Boolean,
    onSelectPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md)
        ) {
            Text(
                text = stringResource(R.string.label_photo),
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

            if (photoUri != null) {
                // Show selected photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(HopSpotShapes.thumbnail)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.cd_selected_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colorScheme.surface.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                HopSpotLoadingIndicator(
                                    size = LoadingSize.Center,
                                    color = colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
                                Text(
                                    text = stringResource(R.string.bench_form_uploading),
                                    color = colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        // Remove button
                        IconButton(
                            onClick = onRemovePhoto,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_remove_photo),
                                tint = colorScheme.onSurface,
                                modifier = Modifier
                                    .background(
                                        colorScheme.surface.copy(alpha = 0.8f),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            } else {
                // Photo placeholder
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
                        .clickable(onClick = onSelectPhoto),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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

            if (photoUri != null && !isUploading) {
                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))
                TextButton(
                    onClick = onSelectPhoto,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(HopSpotDimensions.Icon.small)
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xxs))
                    Text(stringResource(R.string.btn_choose_other_photo))
                }
            }
        }
    }
}

@Composable
private fun LocationCard(
    locationText: String,
    hasLocation: Boolean,
    onSetLocation: (String, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (hasLocation) colorScheme.primary else colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))
                    Column {
                        Text(
                            text = stringResource(R.string.label_location_required),
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

                Button(
                    onClick = { showDialog = true },
                    shape = HopSpotShapes.button,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.common_set))
                }
            }

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            Text(
                text = stringResource(R.string.bench_form_location_hint),
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }

    // Manual Location Dialog
    if (showDialog) {
        ManualLocationDialog(
            onDismiss = { showDialog = false },
            onConfirm = { lat, lon ->
                onSetLocation(lat, lon)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ManualLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_enter_coordinates_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_enter_coordinates_hint),
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(stringResource(R.string.label_latitude)) },
                    placeholder = { Text(stringResource(R.string.hint_latitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(stringResource(R.string.label_longitude)) },
                    placeholder = { Text(stringResource(R.string.hint_longitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(latitude, longitude) },
                enabled = latitude.isNotBlank() && longitude.isNotBlank()
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
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
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
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
                    IconButton(
                        onClick = {
                            onRatingChange(if (rating == star) null else star)
                        }
                    ) {
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
                    text = stringResource(R.string.bench_form_rating_format, rating),
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
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md)
        ) {
            Text(
                text = stringResource(R.string.bench_form_amenities),
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

            // Toilet
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wc,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))
                    Text(stringResource(R.string.bench_form_toilet_nearby))
                }

                Switch(
                    checked = hasToilet,
                    onCheckedChange = onHasToiletChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.onPrimary,
                        checkedTrackColor = colorScheme.primary
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = HopSpotDimensions.Spacing.xs),
                color = colorScheme.outlineVariant
            )

            // Trash Bin
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))
                    Text(stringResource(R.string.bench_form_trash_nearby))
                }

                Switch(
                    checked = hasTrashBin,
                    onCheckedChange = onHasTrashBinChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.onPrimary,
                        checkedTrackColor = colorScheme.primary
                    )
                )
            }
        }
    }
}
