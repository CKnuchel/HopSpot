package com.kickpaws.hopspot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Amber500,
    onPrimary = Black,
    primaryContainer = Amber300,
    onPrimaryContainer = Brown700,
    secondary = Brown500,
    onSecondary = White,
    secondaryContainer = Brown300,
    onSecondaryContainer = Brown700,
    background = Cream,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = CreamDark,
    onSurfaceVariant = BlackLight,
    error = Color(0xFFBA1A1A),
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = Amber300,
    onPrimary = Brown700,
    primaryContainer = Amber700,
    onPrimaryContainer = Cream,
    secondary = Brown300,
    onSecondary = Brown700,
    secondaryContainer = Brown500,
    onSecondaryContainer = Cream,
    background = Black,
    onBackground = Cream,
    surface = Color(0xFF2D2D2D),
    onSurface = Cream,
    surfaceVariant = Brown700,
    onSurfaceVariant = Brown300,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun HopSpotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}