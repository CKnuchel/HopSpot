package com.kickpaws.hopspot.ui.screens.favorites

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.Favorite
import com.kickpaws.hopspot.ui.components.common.HopSpotConfirmationDialog
import com.kickpaws.hopspot.ui.components.common.HopSpotEmptyView
import com.kickpaws.hopspot.ui.components.common.HopSpotErrorView
import com.kickpaws.hopspot.ui.components.common.HopSpotLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.LoadingSize
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotElevations
import com.kickpaws.hopspot.ui.theme.HopSpotShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onSpotClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

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

    if (uiState.favoriteToRemove != null) {
        HopSpotConfirmationDialog(
            title = stringResource(R.string.dialog_remove_favorite_title),
            message = stringResource(R.string.dialog_remove_favorite_message, uiState.favoriteToRemove!!.spotName),
            confirmText = stringResource(R.string.common_remove),
            onConfirm = { viewModel.confirmRemove() },
            onDismiss = { viewModel.dismissRemoveDialog() },
            icon = Icons.Default.HeartBroken,
            isLoading = uiState.isRemoving,
            isDestructive = true
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
                    HopSpotErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::loadFavorites,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.favorites.isEmpty() -> {
                    HopSpotEmptyView(
                        icon = Icons.Default.FavoriteBorder,
                        title = stringResource(R.string.empty_favorites_title),
                        subtitle = stringResource(R.string.empty_favorites_subtitle),
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
                            contentPadding = PaddingValues(HopSpotDimensions.Screen.padding),
                            verticalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.sm)
                        ) {
                            items(
                                items = uiState.favorites,
                                key = { it.id }
                            ) { favorite ->
                                SwipeableFavoriteItem(
                                    favorite = favorite,
                                    onClick = { onSpotClick(favorite.spotId) },
                                    onRemove = { viewModel.showRemoveDialog(favorite) }
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(HopSpotDimensions.Spacing.md),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HopSpotLoadingIndicator(size = LoadingSize.Center)
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
                false
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
                    .background(backgroundColor, HopSpotShapes.card)
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
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = HopSpotElevations.low)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(HopSpotDimensions.ListItem.thumbnailSize)
                    .clip(HopSpotShapes.thumbnail)
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (favorite.spotPhotoUrl != null) {
                    AsyncImage(
                        model = favorite.spotPhotoUrl,
                        contentDescription = favorite.spotName,
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

            Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.spotName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xxs))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (favorite.spotRating != null) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${favorite.spotRating}/5",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.sm))
                    }

                    if (favorite.spotHasToilet) {
                        Icon(
                            imageVector = Icons.Default.Wc,
                            contentDescription = stringResource(R.string.cd_toilet),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xxs))
                    }
                    if (favorite.spotHasTrashBin) {
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

            Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FavoritesListSkeleton() {
    val colorScheme = MaterialTheme.colorScheme

    LazyColumn(
        contentPadding = PaddingValues(HopSpotDimensions.Screen.padding),
        verticalArrangement = Arrangement.spacedBy(HopSpotDimensions.Spacing.sm)
    ) {
        items(5) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = HopSpotShapes.card,
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(HopSpotDimensions.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(HopSpotDimensions.ListItem.thumbnailSize)
                            .clip(HopSpotShapes.thumbnail)
                            .background(colorScheme.surfaceVariant)
                    )

                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.md))

                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                                .clip(HopSpotShapes.thumbnail)
                                .background(colorScheme.surfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp)
                                .clip(HopSpotShapes.thumbnail)
                                .background(colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
