package com.retrocollector.app.dashboard.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardEffect
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardViewModel
import com.retrocollector.app.discovery.presentation.viewmodel.DiscoveryViewModel
import com.retrocollector.app.dossier.presentation.ui.MobileGameDetailScreen
import com.retrocollector.app.scanner.presentation.ui.QuickScanDialog
import com.retrocollector.app.settings.presentation.ui.SettingsDialog
import com.retrocollector.app.wishlist.presentation.ui.ImportWishlistDialog
import com.retrocollector.app.wishlist.presentation.viewmodel.WishlistViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AdaptiveMainScreen(
    dashboardViewModel: DashboardViewModel,
    discoveryViewModel: DiscoveryViewModel,
    wishlistViewModel: WishlistViewModel,
    modifier: Modifier = Modifier
) {
    val state by dashboardViewModel.uiState.collectAsState()
    val discoveryState by discoveryViewModel.uiState.collectAsState()
    val wishlistState by wishlistViewModel.uiState.collectAsState()

    var mobileDetailGame by remember { mutableStateOf<GameItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Coleta de Efeitos One-Off via Channel
    LaunchedEffect(dashboardViewModel) {
        dashboardViewModel.effects.collectLatest { effect ->
            when (effect) {
                is DashboardEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DashboardEffect.NavigateToGameDetail -> {
                    mobileDetailGame = effect.game
                }
                is DashboardEffect.ScanCompleted -> {
                    // Jogo escaneado já salvo e selecionado
                }
            }
        }
    }

    val actions = remember(dashboardViewModel, discoveryViewModel, wishlistViewModel) {
        DashboardActions(
            onStatusSelect = dashboardViewModel::onStatusSelect,
            onPlatformSelect = dashboardViewModel::onPlatformSelect,
            onSearchQueryChange = dashboardViewModel::onSearchQueryChange,
            onToggleEnglishOnly = dashboardViewModel::toggleEnglishOnlyFilter,
            onToggleUskAlerts = dashboardViewModel::toggleUskAlertsFilter,
            onGameSelected = dashboardViewModel::onGameSelected,
            onUpdateGameStatus = dashboardViewModel::updateGameStatus,
            onOpenScanDialog = dashboardViewModel::openScanDialog,
            onOpenSettings = dashboardViewModel::openSettings,
            onSendFollowUpMessage = dashboardViewModel::sendFollowUpMessage,
            onSectionSelect = dashboardViewModel::onSectionSelect,
            onOpenImportDialog = wishlistViewModel::openImportDialog,
            onImportWishlistCsv = wishlistViewModel::importWishlistCsv,
            onMoveToHunting = wishlistViewModel::moveToHunting,
            onRetryEnrichment = wishlistViewModel::retryEnrichment,
            onDiscoveryGenreSelect = discoveryViewModel::onGenreSelect,
            onDiscoveryQueryChange = discoveryViewModel::onQueryChange,
            onDiscoverySearchSubmit = discoveryViewModel::onSearchSubmit,
            onDiscoveryPlatformSelect = discoveryViewModel::onPlatformSelect,
            onOpenDiscoveredDossier = dashboardViewModel::onOpenDiscoveredDossier,
            onAddDiscoveredToWishlist = discoveryViewModel::addToWishlist,
            onLoadSimilarGames = dashboardViewModel::loadSimilarGamesForSelectedGame
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isDesktop = maxWidth >= 850.dp

            if (isDesktop) {
                // Ecrã largo (macOS Desktop e Web Wasm): Split-View de duas colunas
                DesktopWorkstationScreen(
                    state = state,
                    discoveryState = discoveryState,
                    wishlistState = wishlistState,
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
                        similarGames = state.similarGamesForActiveGame,
                        isSimilarGamesLoading = state.isSimilarGamesLoading,
                        onBack = { mobileDetailGame = null },
                        onUpdateGameStatus = dashboardViewModel::updateGameStatus,
                        onSendFollowUpMessage = dashboardViewModel::sendFollowUpMessage,
                        onSelectSimilarGame = { sim ->
                            actions.onOpenDiscoveredDossier(sim)
                            mobileDetailGame = state.games.find {
                                it.title.equals(sim.title, ignoreCase = true) && it.platform == sim.platform
                            } ?: sim.toGameItem()
                        },
                        onAddSimilarGameToWishlist = discoveryViewModel::addToWishlist
                    )
                } else {
                    MobileFieldDashboardScreen(
                        state = state,
                        discoveryState = discoveryState,
                        wishlistState = wishlistState,
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
                    onAnalyze = dashboardViewModel::analyzeNewGame,
                    onDismiss = dashboardViewModel::closeScanDialog
                )
            }

            if (state.isSettingsOpen) {
                SettingsDialog(
                    settings = state.settings,
                    onSaveSettings = dashboardViewModel::saveSettings,
                    onDismiss = dashboardViewModel::closeSettings,
                    onTestGeminiConnection = dashboardViewModel::testGeminiConnection
                )
            }

            if (wishlistState.isImportDialogOpen) {
                ImportWishlistDialog(
                    importResult = wishlistState.importResult,
                    enrichmentProgress = wishlistState.enrichmentProgress,
                    onImport = wishlistViewModel::importWishlistCsv,
                    onDismiss = wishlistViewModel::closeImportDialog
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}
