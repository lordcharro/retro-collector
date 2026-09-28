package com.retrocollector.app.dashboard.presentation.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardViewModel
import com.retrocollector.app.dossier.presentation.ui.MobileGameDetailScreen
import com.retrocollector.app.scanner.presentation.ui.QuickScanDialog
import com.retrocollector.app.settings.presentation.ui.SettingsDialog

@Composable
fun AdaptiveMainScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var mobileDetailGame by remember { mutableStateOf<GameItem?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isDesktop = maxWidth >= 850.dp

        if (isDesktop) {
            // Ecrã largo (macOS Desktop e Web Wasm): Split-View de duas colunas
            DesktopWorkstationScreen(
                state = state,
                viewModel = viewModel
            )
        } else {
            // Ecrã estreito (Android Phone): Navegação com ecrã móvel
            if (mobileDetailGame != null) {
                val activeGame = state.games.find { it.id == mobileDetailGame?.id } ?: mobileDetailGame!!
                MobileGameDetailScreen(
                    game = activeGame,
                    state = state,
                    viewModel = viewModel,
                    onBack = { mobileDetailGame = null }
                )
            } else {
                MobileFieldDashboardScreen(
                    state = state,
                    viewModel = viewModel,
                    onNavigateToDetail = { game ->
                        mobileDetailGame = game
                    }
                )
            }
        }

        // Modais globais
        if (state.isScanDialogOpen) {
            QuickScanDialog(
                state = state,
                viewModel = viewModel,
                onDismiss = { viewModel.closeScanDialog() }
            )
        }

        if (state.isSettingsOpen) {
            SettingsDialog(
                state = state,
                viewModel = viewModel,
                onDismiss = { viewModel.closeSettings() }
            )
        }
    }
}
