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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

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

    // Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.onPhotoSelected(uri)
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
                title = { Text("Neue Bank") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Abbrechen"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveBench,
                        enabled = !uiState.isSaving && !uiState.isUploadingPhoto
                    ) {
                        if (uiState.isSaving || uiState.isUploadingPhoto) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Card
            PhotoCard(
                photoUri = uiState.photoUri,
                isUploading = uiState.isUploadingPhoto,
                onSelectPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemovePhoto = viewModel::removePhoto
            )

            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name *") },
                placeholder = { Text("z.B. Schöne Waldbank") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Chair,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
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
            LocationCard(
                locationText = uiState.locationText,
                hasLocation = uiState.latitude != null,
                onSetLocation = viewModel::setManualLocation
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Beschreibung") },
                placeholder = { Text("Was macht diese Bank besonders?") },
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Foto",
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (photoUri != null) {
                // Show selected photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Ausgewähltes Foto",
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
                                CircularProgressIndicator(
                                    color = colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Wird hochgeladen...",
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
                                contentDescription = "Foto entfernen",
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
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
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

            if (photoUri != null && !isUploading) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSelectPhoto,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Anderes Foto wählen")
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text("Setzen")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Tipp: GPS-Funktion kommt bald! Vorerst Koordinaten manuell eingeben.",
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
        title = { Text("Koordinaten eingeben") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Gib die GPS-Koordinaten ein (z.B. von Google Maps)",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )

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
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
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
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Toilette in der Nähe")
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
                modifier = Modifier.padding(vertical = 8.dp),
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Abfalleimer vorhanden")
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