package com.retrocollector.app.core.presentation.util

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on Web Wasm
}
