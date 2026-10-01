package com.retrocollector.app.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.retrocollector.app.settings.domain.model.ThemeMode

private val M3DarkColorScheme = darkColorScheme(
    primary = TactileDarkColors.primary,
    onPrimary = TactileDarkColors.onPrimary,
    primaryContainer = TactileDarkColors.surfaceElevated,
    onPrimaryContainer = TactileDarkColors.textPrimary,
    secondary = TactileDarkColors.secondary,
    onSecondary = TactileDarkColors.onSecondary,
    background = TactileDarkColors.surfaceBase,
    onBackground = TactileDarkColors.textPrimary,
    surface = TactileDarkColors.surfaceCard,
    onSurface = TactileDarkColors.textPrimary,
    surfaceVariant = TactileDarkColors.surfaceContainer,
    onSurfaceVariant = TactileDarkColors.textSecondary,
    outline = TactileDarkColors.borderSubtle,
    outlineVariant = TactileDarkColors.borderStrong
)

private val M3LightColorScheme = lightColorScheme(
    primary = TactileLightColors.primary,
    onPrimary = TactileLightColors.onPrimary,
    primaryContainer = TactileLightColors.surfaceElevated,
    onPrimaryContainer = TactileLightColors.textPrimary,
    secondary = TactileLightColors.secondary,
    onSecondary = TactileLightColors.onSecondary,
    background = TactileLightColors.surfaceBase,
    onBackground = TactileLightColors.textPrimary,
    surface = TactileLightColors.surfaceCard,
    onSurface = TactileLightColors.textPrimary,
    surfaceVariant = TactileLightColors.surfaceContainer,
    onSurfaceVariant = TactileLightColors.textSecondary,
    outline = TactileLightColors.borderSubtle,
    outlineVariant = TactileLightColors.borderStrong
)

@Composable
fun RetroTactileTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemInDark
    }

    val tactileColors = if (isDark) TactileDarkColors else TactileLightColors
    val m3Colors = if (isDark) M3DarkColorScheme else M3LightColorScheme

    CompositionLocalProvider(
        LocalTactileColors provides tactileColors
    ) {
        MaterialTheme(
            colorScheme = m3Colors,
            content = content
        )
    }
}
