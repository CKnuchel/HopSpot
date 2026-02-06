package com.kickpaws.hopspot.ui.screens.activityfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kickpaws.hopspot.domain.model.Activity
import com.kickpaws.hopspot.ui.components.common.HopSpotEmptyView
import com.kickpaws.hopspot.ui.components.common.HopSpotErrorView
import com.kickpaws.hopspot.ui.components.common.HopSpotLoadingIndicator
import com.kickpaws.hopspot.ui.components.common.LoadingSize
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions
import com.kickpaws.hopspot.ui.theme.HopSpotElevations
import com.kickpaws.hopspot.ui.theme.HopSpotShapes
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(
    onSpotClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ActivityFeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= uiState.activities.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasMorePages && !uiState.isLoadingMore) {
            viewModel.loadMoreActivities()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.activity_feed_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
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
                    ActivityFeedSkeleton()
                }

                uiState.errorMessage != null && uiState.activities.isEmpty() -> {
                    HopSpotErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::loadActivities,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.activities.isEmpty() -> {
                    HopSpotEmptyView(
                        icon = Icons.Default.Timeline,
                        title = stringResource(R.string.empty_activity_feed_title),
                        subtitle = stringResource(R.string.empty_activity_feed_subtitle),
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
                                items = uiState.activities,
                                key = { it.id }
                            ) { activity ->
                                ActivityListItem(
                                    activity = activity,
                                    onClick = {
                                        activity.spotId?.let { onSpotClick(it) }
                                    }
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

@Composable
private fun ActivityListItem(
    activity: Activity,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val relativeTime = remember(activity.createdAt) {
        formatRelativeTime(activity.createdAt)
    }

    val actionIcon = remember(activity.actionType) {
        when (activity.actionType) {
            "spot_created" -> Icons.Default.Add
            "visit_added" -> Icons.Default.CheckCircle
            "favorite_added" -> Icons.Default.Favorite
            else -> Icons.Default.Info
        }
    }

    val actionColor = remember(activity.actionType) {
        when (activity.actionType) {
            "spot_created" -> 0xFF4CAF50  // Green
            "visit_added" -> 0xFF2196F3    // Blue
            "favorite_added" -> 0xFFE91E63 // Pink
            else -> 0xFF9E9E9E            // Gray
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = activity.spotId != null,
                onClick = onClick
            ),
        shape = HopSpotShapes.card,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = HopSpotElevations.low)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HopSpotDimensions.Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar with action icon overlay
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activity.userDisplayName.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimaryContainer
                    )
                }

                // Action badge
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color(actionColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.userDisplayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = relativeTime,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xxs))

                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Spot thumbnail if available
                if (activity.spotName != null) {
                    Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

                    Row(
                        modifier = Modifier
                            .clip(HopSpotShapes.thumbnail)
                            .background(colorScheme.surfaceVariant)
                            .padding(HopSpotDimensions.Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(HopSpotShapes.thumbnail)
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activity.spotPhotoUrl != null) {
                                AsyncImage(
                                    model = activity.spotPhotoUrl,
                                    contentDescription = activity.spotName,
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
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.xs))

                        Text(
                            text = activity.spotName,
                            fontSize = 13.sp,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (activity.spotId != null) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(dateTimeString: String): String {
    return try {
        val dateTime = ZonedDateTime.parse(dateTimeString)
        val now = ZonedDateTime.now()
        val duration = Duration.between(dateTime, now)

        when {
            duration.toMinutes() < 1 -> "gerade eben"
            duration.toMinutes() < 60 -> "vor ${duration.toMinutes()}m"
            duration.toHours() < 24 -> "vor ${duration.toHours()}h"
            duration.toDays() < 7 -> "vor ${duration.toDays()}d"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN)
                dateTime.format(formatter)
            }
        }
    } catch (e: Exception) {
        dateTimeString
    }
}

@Composable
private fun ActivityFeedSkeleton() {
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
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant)
                    )

                    Spacer(modifier = Modifier.width(HopSpotDimensions.Spacing.md))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(16.dp)
                                    .clip(HopSpotShapes.thumbnail)
                                    .background(colorScheme.surfaceVariant)
                            )
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(12.dp)
                                    .clip(HopSpotShapes.thumbnail)
                                    .background(colorScheme.surfaceVariant)
                            )
                        }

                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.xs))

                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(14.dp)
                                .clip(HopSpotShapes.thumbnail)
                                .background(colorScheme.surfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(HopSpotDimensions.Spacing.sm))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(HopSpotShapes.thumbnail)
                                .background(colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
