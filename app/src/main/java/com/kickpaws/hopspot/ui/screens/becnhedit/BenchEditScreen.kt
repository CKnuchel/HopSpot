package com.kickpaws.hopspot.ui.screens.benchedit

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchEditScreen(
    benchId: Int,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: BenchEditViewModel = hiltViewModel()
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

    // Load bench on first composition
    LaunchedEffect(benchId) {
        viewModel.loadBench(benchId)
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
                    contentDescription = "Foto gross",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    // Delete Bench Confirmation Dialog
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
            title = { Text("Bank löschen?") },
            text = {
                Text("Möchtest du \"${uiState.name}\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteBench()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    )
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
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
            title = { Text("Foto löschen?") },
            text = { Text("Möchtest du dieses Foto wirklich löschen?") },
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
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveBench,
                        enabled = !uiState.isSaving && !uiState.isUploadingPhoto
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Speichern",
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
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

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            label = { Text("Name *") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Chair,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Location Card
                        LocationPickerCard(
                            locationText = uiState.locationText,
                            hasLocation = uiState.latitude != null,
                            latitude = uiState.latitude,
                            longitude = uiState.longitude,
                            onLocationSet = { lat, lon ->
                                viewModel.setManualLocation(lat.toString(), lon.toString())
                            }
                        )

                        // Description
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChange,
                            label = { Text("Beschreibung") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                            },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
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
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = uiState.errorMessage!!,
                                        color = colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Delete Button
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.error
                            ),
                            border = BorderStroke(1.dp, colorScheme.error),
                            enabled = !uiState.isDeleting
                        ) {
                            if (uiState.isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = colorScheme.error
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bank löschen")
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Fotos",
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Photo (large)
            if (mainPhoto != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
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

                    // Delete button
                    IconButton(
                        onClick = { onDeleteClick(mainPhoto) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Löschen",
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
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Foto hinzufügen",
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Upload progress
            if (isUploading) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wird hochgeladen...", color = colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Other photos (horizontal scroll)
            Text(
                text = "Weitere Fotos",
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 2.dp,
                                color = colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Foto hinzufügen",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tippe auf ☆ um ein Foto als Hauptbild zu setzen",
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = photo.urlThumbnail,
            contentDescription = "Foto",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholder_bench),
            error = painterResource(R.drawable.placeholder_bench)
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
                    contentDescription = "Als Hauptbild",
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
                    contentDescription = "Löschen",
                    tint = colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
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

                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ändern")
                }
            }
        }
    }

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
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Koordinaten eingeben") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    placeholder = { Text("z.B. 47.3769") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    placeholder = { Text("z.B. 8.5417") },
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
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Bewertung",
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
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
                            contentDescription = "$star Sterne",
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
                    text = "$rating von 5 Sternen",
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ausstattung",
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Toilette in der Nähe")
                }
                Switch(
                    checked = hasToilet,
                    onCheckedChange = onHasToiletChange
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Abfalleimer vorhanden")
                }
                Switch(
                    checked = hasTrashBin,
                    onCheckedChange = onHasTrashBinChange
                )
            }
        }
    }
}