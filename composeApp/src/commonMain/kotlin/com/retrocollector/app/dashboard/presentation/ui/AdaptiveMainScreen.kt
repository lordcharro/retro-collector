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

    val actions = remember(viewModel) {
        DashboardActions(
            onStatusSelect = viewModel::onStatusSelect,
            onPlatformSelect = viewModel::onPlatformSelect,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onToggleEnglishOnly = viewModel::toggleEnglishOnlyFilter,
            onToggleUskAlerts = viewModel::toggleUskAlertsFilter,
            onGameSelected = viewModel::onGameSelected,
            onUpdateGameStatus = viewModel::updateGameStatus,
            onOpenScanDialog = viewModel::openScanDialog,
            onOpenSettings = viewModel::openSettings,
            onSendFollowUpMessage = viewModel::sendFollowUpMessage
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isDesktop = maxWidth >= 850.dp

        if (isDesktop) {
            // Ecrã largo (macOS Desktop e Web Wasm): Split-View de duas colunas
            DesktopWorkstationScreen(
                state = state,
                actions = actions
            )
        } else {
            // Ecrã estreito (Android Phone): Navegação com ecrã móvel
            if (mobileDetailGame != null) {
                val activeGame = state.games.find { it.id == mobileDetailGame?.id } ?: mobileDetailGame!!
                MobileGameDetailScreen(
                    game = activeGame,
                    chatMessages = state.activeChatMessages,
                    isAnalyzing = state.isAnalyzing,
                    onBack = { mobileDetailGame = null },
                    onUpdateGameStatus = viewModel::updateGameStatus,
                    onSendFollowUpMessage = viewModel::sendFollowUpMessage
                )
            } else {
                MobileFieldDashboardScreen(
                    state = state,
                    actions = actions,
                    onNavigateToDetail = { game ->
                        mobileDetailGame = game
                    }
                )
            }
        }

        // Modais globais
        if (state.isScanDialogOpen) {
            QuickScanDialog(
                currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                isAnalyzing = state.isAnalyzing,
                statusMessage = state.statusMessage,
                onAnalyze = viewModel::analyzeNewGame,
                onDismiss = viewModel::closeScanDialog
            )
        }

        if (state.isSettingsOpen) {
            SettingsDialog(
                settings = state.settings,
                onSaveSettings = viewModel::saveSettings,
                onDismiss = viewModel::closeSettings,
                onTestGeminiConnection = viewModel::testGeminiConnection
            )
        }
    }
}
