package com.kickpaws.hopspot.ui.screens.benchlist

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.Bench
import com.kickpaws.hopspot.ui.components.BenchListSkeleton
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchListScreen(
    onBenchClick: (Int) -> Unit,
    onCreateBenchClick: () -> Unit,
    viewModel: BenchListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Navigate to random bench when ID is set
    LaunchedEffect(uiState.randomBenchId) {
        uiState.randomBenchId?.let { benchId ->
            onBenchClick(benchId)
            viewModel.clearRandomBenchId()
        }
    }

    // Location Permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch {
                val location = getCurrentLocation(context)
                if (location != null) {
                    viewModel.setUserLocation(location.first, location.second)
                }
            }
        }
    }

    // Request location on first load
    LaunchedEffect(Unit) {
        val hasFinePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFinePermission || hasCoarsePermission) {
            val location = getCurrentLocation(context)
            if (location != null) {
                viewModel.setUserLocation(location.first, location.second)
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

    // Load more when reaching end of list
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= uiState.benches.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasMorePages && !uiState.isLoadingMore) {
            viewModel.loadMoreBenches()
        }
    }

    // Filter Bottom Sheet
    if (uiState.isFilterSheetOpen) {
        FilterBottomSheet(
            uiState = uiState,
            onDismiss = viewModel::closeFilterSheet,
            onHasToiletChange = viewModel::setFilterHasToilet,
            onHasTrashBinChange = viewModel::setFilterHasTrashBin,
            onMinRatingChange = viewModel::setFilterMinRating,
            onSortByChange = viewModel::setSortBy,
            onApply = viewModel::applyFilters,
            onClear = viewModel::clearFilters
        )
    }

    // Delete Confirmation Dialog
    if (uiState.benchToDelete != null) {
        DeleteConfirmationDialog(
            benchName = uiState.benchToDelete!!.name,
            isDeleting = uiState.isDeleting,
            onConfirm = viewModel::deleteBench,
            onDismiss = viewModel::hideDeleteConfirmation
        )
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = viewModel::onSearchQueryChange,
                onClearSearch = viewModel::clearSearch,
                onFilterClick = viewModel::openFilterSheet,
                hasActiveFilters = uiState.filterHasToilet != null ||
                        uiState.filterHasTrashBin != null ||
                        uiState.filterMinRating != null
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Random Bench FAB
                SmallFloatingActionButton(
                    onClick = { viewModel.getRandomBench() },
                    containerColor = colorScheme.secondaryContainer,
                    contentColor = colorScheme.onSecondaryContainer
                ) {
                    if (uiState.isLoadingRandom) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = colorScheme.onSecondaryContainer
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = stringResource(R.string.cd_random_bench)
                        )
                    }
                }

                // Create Bench FAB
                FloatingActionButton(
                    onClick = onCreateBenchClick,
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_bench)
                    )
                }
            }
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
                    BenchListSkeleton()
                }

                uiState.errorMessage != null && uiState.benches.isEmpty() -> {
                    ErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::loadBenches,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.benches.isEmpty() -> {
                    EmptyView(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh
                    ) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.benches,
                                key = { it.id }
                            ) { bench ->
                                BenchListItem(
                                    bench = bench,
                                    onClick = { onBenchClick(bench.id) },
                                    onLongClick = { viewModel.showDeleteConfirmation(bench) }
                                )
                            }

                            // Loading more indicator
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(stringResource(R.string.hint_search)) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear_search)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    cursorColor = colorScheme.primary,
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            )
        },
        actions = {
            IconButton(onClick = onFilterClick) {
                BadgedBox(
                    badge = {
                        if (hasActiveFilters) {
                            Badge(containerColor = colorScheme.primary)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.cd_filter),
                        tint = colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.background
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BenchListItem(
    bench: Bench,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with Coil
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (bench.mainPhotoUrl != null) {
                    AsyncImage(
                        model = bench.mainPhotoUrl,
                        contentDescription = bench.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholder_bench),
                        error = painterResource(R.drawable.placeholder_bench)
                    )
                } else {
                    // Fallback wenn kein Bild
                    Icon(
                        imageVector = Icons.Default.Chair,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bench.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating
                if (bench.rating != null) {
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
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Icons for toilet & trash
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (bench.hasToilet) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wc,
                                contentDescription = stringResource(R.string.cd_toilet),
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = stringResource(R.string.amenity_wc),
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (bench.hasTrashBin) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_trash_bin),
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = stringResource(R.string.amenity_trash),
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Distance
                    if (bench.distance != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = stringResource(R.string.cd_distance),
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formatDistance(bench.distance),
                                fontSize = 12.sp,
                                color = colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    benchName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = colorScheme.error
            )
        },
        title = {
            Text(stringResource(R.string.dialog_delete_bench_title))
        },
        text = {
            Text(stringResource(R.string.dialog_delete_bench_message, benchName))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.error,
                    contentColor = colorScheme.onError
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.common_delete))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    uiState: BenchListUiState,
    onDismiss: () -> Unit,
    onHasToiletChange: (Boolean?) -> Unit,
    onHasTrashBinChange: (Boolean?) -> Unit,
    onMinRatingChange: (Int?) -> Unit,
    onSortByChange: (SortOption) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.bench_list_filter_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sort Options
            Text(
                text = stringResource(R.string.bench_list_sort_by),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = uiState.sortBy == option,
                        onClick = { onSortByChange(option) },
                        label = { Text(option.displayName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toilet Filter
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.bench_list_filter_toilet),
                        color = colorScheme.onSurface
                    )
                }

                Switch(
                    checked = uiState.filterHasToilet == true,
                    onCheckedChange = { checked ->
                        onHasToiletChange(if (checked) true else null)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.onPrimary,
                        checkedTrackColor = colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trash Bin Filter
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.bench_list_filter_trash),
                        color = colorScheme.onSurface
                    )
                }

                Switch(
                    checked = uiState.filterHasTrashBin == true,
                    onCheckedChange = { checked ->
                        onHasTrashBinChange(if (checked) true else null)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.onPrimary,
                        checkedTrackColor = colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Min Rating Filter
            Text(
                text = stringResource(R.string.bench_list_filter_min_rating),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null, 1, 2, 3, 4, 5).forEach { rating ->
                    FilterChip(
                        selected = uiState.filterMinRating == rating,
                        onClick = { onMinRatingChange(rating) },
                        label = {
                            if (rating == null) {
                                Text(stringResource(R.string.bench_list_filter_all))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$rating")
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.common_reset))
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.common_apply))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\uD83D\uDE15",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.common_retry), color = colorScheme.primary)
        }
    }
}

@Composable
private fun EmptyView(
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\uD83E\uDE91",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_benches_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_benches_subtitle),
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDistance(distanceMeters: Double): String {
    return when {
        distanceMeters < 1000 -> "${distanceMeters.toInt()}m"
        else -> String.format("%.1fkm", distanceMeters / 1000)
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
