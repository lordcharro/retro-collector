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

import com.retrocollector.app.core.presentation.util.PlatformBackHandler

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

    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept native Back button (Android)
    PlatformBackHandler(enabled = state.isMobileDetailOpen) {
        dashboardViewModel.closeMobileDetail()
    }

    // Collect One-Off Effects via Channel
    LaunchedEffect(dashboardViewModel) {
        dashboardViewModel.effects.collectLatest { effect ->
            when (effect) {
                is DashboardEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DashboardEffect.NavigateToGameDetail -> {
                    dashboardViewModel.openMobileDetail(effect.game)
                }
                is DashboardEffect.ScanCompleted -> {
                    // Scanned game already saved and selected
                }
            }
        }
    }

    val actions = remember(dashboardViewModel, discoveryViewModel, wishlistViewModel) {
        DashboardActions(
            onStatusSelect = dashboardViewModel::onStatusSelect,
            onPlatformSelect = dashboardViewModel::onPlatformSelect,
            onSearchQueryChange = { query ->
                dashboardViewModel.onSearchQueryChange(query)
                wishlistViewModel.onSearchQueryChange(query)
            },
            onToggleEnglishOnly = dashboardViewModel::toggleEnglishOnlyFilter,
            onToggleUskAlerts = dashboardViewModel::toggleUskAlertsFilter,
            onGameSelected = dashboardViewModel::onGameSelected,
            onUpdateGameStatus = dashboardViewModel::updateGameStatus,
            onUpdatePaidPrice = dashboardViewModel::updateGamePaidPrice,
            onUpdateProductCode = dashboardViewModel::updateGameProductCode,
            onDeleteGame = dashboardViewModel::deleteGame,
            onClearFilters = {
                dashboardViewModel.clearFilters()
                wishlistViewModel.clearSearch()
            },
            onOpenScanDialog = { dashboardViewModel.setScanDialogOpen(true) },
            onOpenSettings = { dashboardViewModel.setSettingsOpen(true) },
            onSendFollowUpMessage = dashboardViewModel::sendFollowUpMessage,
            onOpenMobileDetail = dashboardViewModel::openMobileDetail,
            onCloseMobileDetail = dashboardViewModel::closeMobileDetail,
            onSectionSelect = dashboardViewModel::onSectionSelect,
            onOpenImportDialog = wishlistViewModel::openImportDialog,
            onImportWishlistCsv = wishlistViewModel::importWishlistCsv,
            onResetWishlistImportResult = wishlistViewModel::resetImportResult,
            onRetryEnrichment = wishlistViewModel::retryEnrichment,
            onDiscoveryGenreSelect = discoveryViewModel::onGenreSelect,
            onDiscoveryQueryChange = discoveryViewModel::onQueryChange,
            onDiscoverySearchSubmit = discoveryViewModel::onSearchSubmit,
            onDiscoveryPlatformSelect = discoveryViewModel::onPlatformSelect,
            onOpenDiscoveredDossier = dashboardViewModel::onOpenDiscoveredDossier,
            onAddDiscoveredToWishlist = discoveryViewModel::addToWishlist,
            onLoadSimilarGames = { game -> dashboardViewModel.loadSimilarGamesForSelectedGame(game, forceRefresh = true) },
            onSyncFromFirestore = dashboardViewModel::refreshFromFirestore,
            onAddOrUpdateOffer = dashboardViewModel::addOrUpdateOffer,
            onDeleteOffer = dashboardViewModel::deleteOffer,
            onConvertOfferToOwned = dashboardViewModel::convertOfferToOwned
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isDesktop = maxWidth >= 850.dp

            if (isDesktop) {
                // Wide screen (macOS Desktop and Web Wasm): Two-column Split-View
                DesktopWorkstationScreen(
                    state = state,
                    discoveryState = discoveryState,
                    wishlistState = wishlistState,
                    actions = actions
                )
            } else {
                // Narrow screen (Android Phone): Mobile navigation screen
                val activeGame = state.selectedGame
                if (state.isMobileDetailOpen && activeGame != null) {
                    MobileGameDetailScreen(
                        game = activeGame,
                        chatMessages = state.activeChatMessages,
                        isAnalyzing = state.isAnalyzing,
                        currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                        similarGames = state.similarGamesForActiveGame,
                        isSimilarGamesLoading = state.isSimilarGamesLoading,
                        onBack = dashboardViewModel::closeMobileDetail,
                        onDeleteGame = dashboardViewModel::deleteGame,
                        onUpdateGameStatus = dashboardViewModel::updateGameStatus,
                        onUpdatePaidPrice = dashboardViewModel::updateGamePaidPrice,
                        onUpdateProductCode = dashboardViewModel::updateGameProductCode,
                        onSendFollowUpMessage = dashboardViewModel::sendFollowUpMessage,
                        onSelectSimilarGame = { sim ->
                            actions.onOpenDiscoveredDossier(sim)
                        },
                        onAddSimilarGameToWishlist = discoveryViewModel::addToWishlist,
                        onRefreshSimilarGames = {
                            actions.onLoadSimilarGames(activeGame)
                        },
                        onAddOrUpdateOffer = dashboardViewModel::addOrUpdateOffer,
                        onDeleteOffer = dashboardViewModel::deleteOffer,
                        onConvertOfferToOwned = dashboardViewModel::convertOfferToOwned
                    )
                } else {
                    MobileFieldDashboardScreen(
                        state = state,
                        discoveryState = discoveryState,
                        wishlistState = wishlistState,
                        actions = actions,
                        onNavigateToDetail = { game ->
                            dashboardViewModel.openMobileDetail(game)
                        }
                    )
                }
            }

            // Global Modals
            if (state.isScanDialogOpen) {
                QuickScanDialog(
                    currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                    isAnalyzing = state.isAnalyzing,
                    statusMessage = state.scanErrorMessage,
                    onAnalyze = dashboardViewModel::analyzeNewGame,
                    onDismiss = { dashboardViewModel.setScanDialogOpen(false) }
                )
            }

            if (state.isSettingsOpen) {
                SettingsDialog(
                    settings = state.settings,
                    onSaveSettings = dashboardViewModel::saveSettings,
                    onDismiss = { dashboardViewModel.setSettingsOpen(false) },
                    onTestAiConnection = dashboardViewModel::testAiConnection,
                    onTestFirestoreConnection = dashboardViewModel::testFirestoreConnection
                )
            }

            if (wishlistState.isImportDialogOpen) {
                ImportWishlistDialog(
                    importResult = wishlistState.importResult,
                    enrichmentProgress = wishlistState.enrichmentProgress,
                    onImport = wishlistViewModel::importWishlistCsv,
                    onDismiss = wishlistViewModel::closeImportDialog,
                    onResetImportResult = wishlistViewModel::resetImportResult
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}
