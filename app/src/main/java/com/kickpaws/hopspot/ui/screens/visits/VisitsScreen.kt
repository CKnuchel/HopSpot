package com.kickpaws.hopspot.ui.screens.visits

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
import com.kickpaws.hopspot.domain.model.Visit
import com.kickpaws.hopspot.ui.components.common.HopSpotDeleteConfirmationDialog
import com.kickpaws.hopspot.ui.components.common.HopSpotEmptyView
import com.kickpaws.hopspot.ui.components.common.HopSpotErrorView
import com.kickpaws.hopspot.ui.components.common.HopSpotLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.LoadingSize
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotElevations
import com.kickpaws.hopspot.ui.theme.HopSpotShapes
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsScreen(
    onBenchClick: (Int) -> Unit,
    viewModel: VisitsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= uiState.visits.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasMorePages && !uiState.isLoadingMore) {
            viewModel.loadMoreVisits()
        }
    }

    if (uiState.visitToDelete != null) {
        HopSpotDeleteConfirmationDialog(
            title = stringResource(R.string.dialog_delete_visit_title),
            message = stringResource(R.string.dialog_delete_visit_message, uiState.visitToDelete!!.benchName),
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.dismissDeleteDialog() },
            icon = Icons.Default.Delete,
            isLoading = uiState.isDeleting
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.visits_title)) },
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
                    VisitsListSkeleton()
                }

                uiState.errorMessage != null && uiState.visits.isEmpty() -> {
                    HopSpotErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::loadVisits,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.visits.isEmpty() -> {
                    HopSpotEmptyView(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.empty_visits_title),
                        subtitle = stringResource(R.string.empty_visits_subtitle),
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
                                items = uiState.visits,
                                key = { it.id }
                            ) { visit ->
                                SwipeableVisitItem(
                                    visit = visit,
                                    onClick = { onBenchClick(visit.benchId) },
                                    onDelete = { viewModel.showDeleteDialog(visit) }
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
private fun SwipeableVisitItem(
    visit: Visit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
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
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_delete),
                        tint = colorScheme.onErrorContainer
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        VisitListItem(visit = visit, onClick = onClick)
    }
}

@Composable
private fun VisitListItem(
    visit: Visit,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val formattedDate = remember(visit.visitedAt) {
        try {
            val zonedDateTime = ZonedDateTime.parse(visit.visitedAt)
            val formatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy, HH:mm", Locale.GERMAN)
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            visit.visitedAt
        }
    }

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
                if (visit.benchPhotoUrl != null) {
                    AsyncImage(
                        model = visit.benchPhotoUrl,
                        contentDescription = visit.benchName,
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
                    text = visit.benchName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xxs))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xxs))
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
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
private fun VisitsListSkeleton() {
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
                                .width(160.dp)
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
