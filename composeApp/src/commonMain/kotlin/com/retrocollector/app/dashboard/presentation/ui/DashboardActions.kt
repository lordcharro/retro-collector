package com.retrocollector.app.dashboard.presentation.ui

import androidx.compose.runtime.Immutable
import com.retrocollector.app.core.domain.model.AppSection
import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.core.domain.model.GameItem

/**
 * Encapsulates Dashboard UI actions to decouple screens and components
 * from ViewModel, ensuring compliance with State Hoisting standards.
 */
@Immutable
data class DashboardActions(
    val onStatusSelect: (CollectionStatus?) -> Unit = {},
    val onPlatformSelect: (ConsolePlatform?) -> Unit = {},
    val onSearchQueryChange: (String) -> Unit = {},
    val onToggleEnglishOnly: () -> Unit = {},
    val onToggleUskAlerts: () -> Unit = {},
    val onGameSelected: (GameItem) -> Unit = {},
    val onUpdateGameStatus: (GameItem, CollectionStatus) -> Unit = { _, _ -> },
    val onUpdatePaidPrice: (GameItem, Double?) -> Unit = { _, _ -> },
    val onDeleteGame: (String) -> Unit = {},
    val onClearFilters: () -> Unit = {},
    val onOpenScanDialog: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onSendFollowUpMessage: (String) -> Unit = {},
    // Mobile Navigation
    val onOpenMobileDetail: (GameItem) -> Unit = {},
    val onCloseMobileDetail: () -> Unit = {},
    // Section Navigation
    val onSectionSelect: (AppSection) -> Unit = {},
    // Wishlist
    val onOpenImportDialog: () -> Unit = {},
    val onImportWishlistCsv: (String) -> Unit = {},
    val onResetWishlistImportResult: () -> Unit = {},
    val onRetryEnrichment: (GameItem) -> Unit = {},
    // Discovery
    val onDiscoveryGenreSelect: (GameGenre) -> Unit = {},
    val onDiscoveryQueryChange: (String) -> Unit = {},
    val onDiscoverySearchSubmit: (String) -> Unit = {},
    val onDiscoveryPlatformSelect: (ConsolePlatform?) -> Unit = {},
    val onOpenDiscoveredDossier: (DiscoveredGameItem) -> Unit = {},
    val onAddDiscoveredToWishlist: (DiscoveredGameItem) -> Unit = {},
    val onLoadSimilarGames: (GameItem) -> Unit = {}
)
