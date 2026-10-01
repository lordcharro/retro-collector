package com.retrocollector.app.core.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TactileColorScheme(
    val isDark: Boolean,
    val surfaceBase: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val statusEnglishBg: Color,
    val statusEnglishFg: Color,
    val statusEditionBg: Color,
    val statusEditionFg: Color,
    val statusRiskBg: Color,
    val statusRiskFg: Color,
    val statusUnverifiedBg: Color,
    val statusUnverifiedFg: Color,
    val accentBlue: Color,
    val accentGreen: Color,
    val userChatBg: Color,
    val userChatBorder: Color,
    val enrichmentPendingBg: Color,
    val enrichmentPendingFg: Color,
    val enrichmentRunningBg: Color,
    val enrichmentRunningFg: Color
)

// Dark Palette (Tactical Field Dark)
val TactileDarkColors = TactileColorScheme(
    isDark = true,
    surfaceBase = Color(0xFF09090B),
    surfaceCard = Color(0xFF18181B),
    surfaceElevated = Color(0xFF27272A),
    surfaceContainerLow = Color(0xFF1C1B1D),
    surfaceContainer = Color(0xFF201F22),
    surfaceContainerHigh = Color(0xFF2A2A2C),
    borderSubtle = Color(0xFF27272A),
    borderStrong = Color(0xFF3F3F46),
    textPrimary = Color(0xFFE5E1E4),
    textSecondary = Color(0xFFA1A1AA),
    textTertiary = Color(0xFF71717A),
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    secondary = Color(0xFF93CCFF),
    onSecondary = Color.Black,
    statusEnglishBg = Color(0xFF064E3B),
    statusEnglishFg = Color(0xFF10B981),
    statusEditionBg = Color(0xFF78350F),
    statusEditionFg = Color(0xFFF59E0B),
    statusRiskBg = Color(0xFF7F1D1D),
    statusRiskFg = Color(0xFFEF4444),
    statusUnverifiedBg = Color(0xFF1E293B),
    statusUnverifiedFg = Color(0xFF94A3B8),
    accentBlue = Color(0xFF93CCFF),
    accentGreen = Color(0xFF4EDEA3),
    userChatBg = Color(0xFF2E2A5C),
    userChatBorder = Color(0xFF3F3A78),
    enrichmentPendingBg = Color(0xFF3F3F46),
    enrichmentPendingFg = Color(0xFF94A3B8),
    enrichmentRunningBg = Color(0xFF1E3A5F),
    enrichmentRunningFg = Color(0xFF3B82F6)
)

// Light Palette (Tactical Field Light from Stitch)
val TactileLightColors = TactileColorScheme(
    isDark = false,
    surfaceBase = Color(0xFFF8FAFC),
    surfaceCard = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF1F5F9),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    borderSubtle = Color(0xFFE2E8F0),
    borderStrong = Color(0xFFCBD5E1),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    textTertiary = Color(0xFF94A3B8),
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    statusEnglishBg = Color(0xFFDCFCE7),
    statusEnglishFg = Color(0xFF15803D),
    statusEditionBg = Color(0xFFFEF3C7),
    statusEditionFg = Color(0xFF92400E),
    statusRiskBg = Color(0xFFFEF2F2),
    statusRiskFg = Color(0xFFB91C1C),
    statusUnverifiedBg = Color(0xFFF1F5F9),
    statusUnverifiedFg = Color(0xFF64748B),
    accentBlue = Color(0xFF0284C7),
    accentGreen = Color(0xFF10B981),
    userChatBg = Color(0xFFEEF2FF),
    userChatBorder = Color(0xFFC7D2FE),
    enrichmentPendingBg = Color(0xFFE2E8F0),
    enrichmentPendingFg = Color(0xFF64748B),
    enrichmentRunningBg = Color(0xFFDBEAFE),
    enrichmentRunningFg = Color(0xFF2563EB)
)

val LocalTactileColors = staticCompositionLocalOf { TactileDarkColors }

// Dynamic Composable Accessors
val SurfaceBase: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.surfaceBase

val SurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.surfaceCard

val SurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.surfaceElevated

val SurfaceContainerLow: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.surfaceContainerLow

val SurfaceContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.surfaceContainer

val SurfaceContainerHigh: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.surfaceContainerHigh

val BorderSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.borderSubtle

val BorderStrong: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.borderStrong

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.textSecondary

val TextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.textTertiary

val StatusEnglishBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.statusEnglishBg

val StatusEnglishFg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.statusEnglishFg

val StatusEditionBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.statusEditionBg

val StatusEditionFg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.statusEditionFg

val StatusRiskBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.statusRiskBg

val StatusRiskFg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.statusRiskFg

val StatusUnverifiedBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.statusUnverifiedBg

val StatusUnverifiedFg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.statusUnverifiedFg

val AccentBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.accentBlue

val AccentGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.accentGreen

val UserChatBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.userChatBg

val UserChatBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.userChatBorder

val EnrichmentPendingBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.enrichmentPendingBg

val EnrichmentPendingFg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.enrichmentPendingFg

val EnrichmentRunningBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.enrichmentRunningBg

val EnrichmentRunningFg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalTactileColors.current.enrichmentRunningFg

// Static Console Hardware Identity
val ConsoleN64 = Color(0xFFDC2626)
val ConsoleGamecube = Color(0xFF6366F1)
val ConsolePS3 = Color(0xFF0284C7)
val ConsoleSwitch = Color(0xFFEF4444)
