package com.kickpaws.hopspot.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kickpaws.hopspot.ui.theme.HopSpotDimensions

enum class LoadingSize(val size: Dp, val strokeWidth: Dp) {
    Button(HopSpotDimensions.Loading.buttonSize, 2.dp),
    Center(HopSpotDimensions.Loading.centerSize, 3.dp),
    Small(16.dp, 2.dp)
}

@Composable
fun HopSpotLoadingIndicator(
    modifier: Modifier = Modifier,
    size: LoadingSize = LoadingSize.Center,
    color: Color = MaterialTheme.colorScheme.primary
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.size),
        color = color,
        strokeWidth = size.strokeWidth
    )
}

@Composable
fun HopSpotCenteredLoadingIndicator(
    modifier: Modifier = Modifier,
    size: LoadingSize = LoadingSize.Center,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        HopSpotLoadingIndicator(size = size, color = color)
    }
}
