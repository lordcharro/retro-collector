package com.retrocollector.app.core.presentation.util

import androidx.compose.runtime.Composable

/**
 * Multiplatform wrapper for handling system back events (e.g. Android back button / predictive back).
 * Maps to BackHandler on Android and no-ops on Desktop and Web Wasm.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
