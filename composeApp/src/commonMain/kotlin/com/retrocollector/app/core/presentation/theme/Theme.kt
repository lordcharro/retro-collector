package com.retrocollector.app.core.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TactileDarkColorScheme = darkColorScheme(
    primary = ConsoleGamecube,
    onPrimary = Color.White,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = AccentBlue,
    onSecondary = Color.Black,
    background = SurfaceBase,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderStrong
)

@Composable
fun RetroTactileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TactileDarkColorScheme,
        content = content
    )
}
