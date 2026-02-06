package com.kickpaws.hopspot.ui.screens.favorites

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.Favorite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBenchClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    // Load more when reaching end of list
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= uiState.favorites.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasMorePages && !uiState.isLoadingMore) {
            viewModel.loadMoreFavorites()
        }
    }

    // Remove confirmation dialog
    if (uiState.favoriteToRemove != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRemoveDialog() },
            title = { Text(stringResource(R.string.dialog_remove_favorite_title)) },
            text = {
                Text(stringResource(R.string.dialog_remove_favorite_message, uiState.favoriteToRemove!!.benchName))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmRemove() },
                    enabled = !uiState.isRemoving,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) {
                    if (uiState.isRemoving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.common_remove))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissRemoveDialog() },
                    enabled = !uiState.isRemoving
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
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
                    FavoritesListSkeleton()
                }

                uiState.errorMessage != null && uiState.favorites.isEmpty() -> {
                    ErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::loadFavorites,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.favorites.isEmpty() -> {
                    EmptyFavoritesView(modifier = Modifier.align(Alignment.Center))
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
                                items = uiState.favorites,
                                key = { it.id }
                            ) { favorite ->
                                SwipeableFavoriteItem(
                                    favorite = favorite,
                                    onClick = { onBenchClick(favorite.benchId) },
                                    onRemove = { viewModel.showRemoveDialog(favorite) }
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
private fun SwipeableFavoriteItem(
    favorite: Favorite,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                false // Don't dismiss yet, wait for dialog confirmation
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val backgroundColor by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> colorScheme.errorContainer
                    else -> Color.Transparent
                },
                label = "backgroundColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.HeartBroken,
                        contentDescription = stringResource(R.string.common_remove),
                        tint = colorScheme.onErrorContainer
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        FavoriteListItem(favorite = favorite, onClick = onClick)
    }
}

@Composable
private fun FavoriteListItem(
    favorite: Favorite,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bench photo or placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (favorite.benchPhotoUrl != null) {
                    AsyncImage(
                        model = favorite.benchPhotoUrl,
                        contentDescription = favorite.benchName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_beer_marker),
                        error = painterResource(R.drawable.ic_beer_marker)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Chair,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.benchName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rating
                    if (favorite.benchRating != null) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${favorite.benchRating}/5",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // Amenities icons
                    if (favorite.benchHasToilet) {
                        Icon(
                            imageVector = Icons.Default.Wc,
                            contentDescription = stringResource(R.string.cd_toilet),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (favorite.benchHasTrashBin) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_trash_bin),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyFavoritesView(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_favorites_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_favorites_subtitle),
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.common_error),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.common_retry))
        }
    }
}

@Composable
private fun FavoritesListSkeleton() {
    val colorScheme = MaterialTheme.colorScheme

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo placeholder
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surfaceVariant)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Name placeholder
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colorScheme.surfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating placeholder
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
