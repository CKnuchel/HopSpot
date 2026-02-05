package com.kickpaws.hopspot.ui.screens.visits

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kickpaws.hopspot.R
import com.kickpaws.hopspot.domain.model.Visit
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

    // Load more when reaching end of list
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meine Besuche") },
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
                    ErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::loadVisits,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.visits.isEmpty() -> {
                    EmptyVisitsView(modifier = Modifier.align(Alignment.Center))
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
                                items = uiState.visits,
                                key = { it.id }
                            ) { visit ->
                                VisitListItem(
                                    visit = visit,
                                    onClick = { onBenchClick(visit.benchId) }
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

@Composable
private fun VisitListItem(
    visit: Visit,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Parse and format date
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = visit.benchName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
private fun EmptyVisitsView(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Noch keine Besuche",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Besuche eine Bank und markiere deinen Besuch!",
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
            text = "Fehler",
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
            Text("Erneut versuchen")
        }
    }
}

@Composable
private fun VisitsListSkeleton() {
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

                        // Date placeholder
                        Box(
                            modifier = Modifier
                                .width(160.dp)
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
