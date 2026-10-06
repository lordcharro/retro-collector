package com.retrocollector.app.dashboard.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.presentation.components.*
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.core.presentation.util.labelRes
import org.jetbrains.compose.resources.stringResource
import com.retrocollector.app.collection.presentation.ui.CollectionScreen
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardUiState
import com.retrocollector.app.discovery.presentation.components.SimilarGamesShelf
import com.retrocollector.app.discovery.presentation.ui.DiscoveryScreen
import com.retrocollector.app.discovery.presentation.viewmodel.DiscoveryUiState
import com.retrocollector.app.wishlist.presentation.ui.WishlistScreen
import com.retrocollector.app.wishlist.presentation.viewmodel.WishlistUiState
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesktopWorkstationScreen(
    state: DashboardUiState,
    discoveryState: DiscoveryUiState = DiscoveryUiState(),
    wishlistState: WishlistUiState = WishlistUiState(),
    actions: DashboardActions = DashboardActions(),
    modifier: Modifier = Modifier
) {
    var followUpQuestion by remember { mutableStateOf("") }
    var editingOffer by remember { mutableStateOf<com.retrocollector.app.core.domain.model.GameOffer?>(null) }
    var isAddOfferOpen by remember { mutableStateOf(false) }
    var acquisitionOffer by remember { mutableStateOf<com.retrocollector.app.core.domain.model.GameOffer?>(null) }
    var isAcquisitionOpen by remember { mutableStateOf(false) }

    val activePlatforms = remember(state.games, state.settings.pinnedPlatformIds) {
        val fromGames = state.games.map { it.platform }.toSet()
        val fromPinned = state.settings.pinnedPlatformIds.mapNotNull { ConsolePlatform.fromId(it) }.toSet()
        val combined = (fromGames + fromPinned).ifEmpty {
            setOf(ConsolePlatform.N64, ConsolePlatform.GAMECUBE, ConsolePlatform.PS3, ConsolePlatform.SWITCH)
        }
        ConsolePlatform.entries.filter { it in combined }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        // -------------------------------------------------------------
        // BARRA LATERAL ESQUERDA: INTELLIGENCE LIBRARY (Largura fixa 240dp)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .background(SurfaceCard)
                .border(BorderStroke(1.dp, BorderSubtle))
        ) {
            // Header da Sidebar com as luzes do Mac (Traffic lights)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Space for macOS native traffic light buttons (Close, Minimize, Expand)
                Spacer(modifier = Modifier.width(68.dp))

                Text(
                    text = stringResource(Res.string.app_name),
                    style = HeadlineSm,
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(StatusEnglishFg, RoundedCornerShape(3.dp))
                )
            }

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

            // Library Navigation Section
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(Res.string.app_intelligence_library),
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp)
                )

                SidebarSectionItem(
                    section = AppSection.ACTIVITY,
                    count = state.games.size,
                    isSelected = state.activeSection == AppSection.ACTIVITY,
                    onClick = { actions.onSectionSelect(AppSection.ACTIVITY) }
                )

                SidebarSectionItem(
                    section = AppSection.DISCOVER,
                    count = discoveryState.discoveredGames.size,
                    isSelected = state.activeSection == AppSection.DISCOVER,
                    onClick = { actions.onSectionSelect(AppSection.DISCOVER) },
                    badgeColor = if (discoveryState.discoveredGames.isNotEmpty()) ConsoleGamecube else StatusUnverifiedFg
                )

                SidebarSectionItem(
                    section = AppSection.WISHLIST,
                    count = wishlistState.allWishlistCount,
                    isSelected = state.activeSection == AppSection.WISHLIST,
                    onClick = { actions.onSectionSelect(AppSection.WISHLIST) },
                    badgeColor = if (wishlistState.allWishlistCount > 0) StatusEditionFg else StatusUnverifiedFg
                )

                SidebarSectionItem(
                    section = AppSection.COLLECTION,
                    count = state.collectionGames.size,
                    isSelected = state.activeSection == AppSection.COLLECTION,
                    onClick = { actions.onSectionSelect(AppSection.COLLECTION) }
                )

                // Item: Settings & Sync
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { actions.onOpenSettings() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                            Text(text = "⚙️", fontSize = 15.sp, textAlign = TextAlign.Center)
                        }
                        Text(text = stringResource(Res.string.nav_settings), style = BodyMd, color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(SurfaceBase, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = state.settings.defaultCurrency.ifBlank { "CHF" },
                            style = CodeSkuStyle.copy(fontSize = 10.sp),
                            color = StatusUnverifiedFg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.dashboard_platform_filter),
                        style = LabelFilterStyle.copy(fontSize = 11.sp),
                        color = StatusUnverifiedFg
                    )
                    Text(
                        text = "⚙️",
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { actions.onOpenSettings() }
                            .padding(2.dp)
                    )
                }

                activePlatforms.forEach { platform ->
                    val isSelected = state.selectedPlatform == platform
                    val color = Color(platform.brandColorHex)
                    val count = state.games.count { it.platform == platform }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) SurfaceElevated else Color.Transparent, RoundedCornerShape(4.dp))
                            .clickable { actions.onPlatformSelect(platform) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
                            Text(
                                text = platform.shortName,
                                style = BodyMd,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (count > 0) {
                            Text(
                                text = "$count",
                                style = CodeSkuStyle.copy(fontSize = 10.sp),
                                color = if (isSelected) StatusEnglishFg else StatusUnverifiedFg
                            )
                        }
                    }
                }
            }

            // Sidebar Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, BorderSubtle))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(StatusEnglishFg, RoundedCornerShape(3.dp)))
                    Text(
                        text = "RetroCollector v1.0.0",
                        style = CodeSkuStyle.copy(fontSize = 10.sp),
                        color = StatusUnverifiedFg
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // MAIN CONTENT (SPLIT-VIEW: List Panel + Dossier Panel)
        // -------------------------------------------------------------
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Workstation Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(SurfaceCard)
                    .border(BorderStroke(1.dp, BorderSubtle))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Search Field (32dp height aligned)
                TactileSearchField(
                    query = state.searchQuery,
                    onQueryChange = { actions.onSearchQueryChange(it) },
                    placeholder = stringResource(Res.string.dashboard_search_placeholder),
                    modifier = Modifier.width(420.dp),
                    minHeight = 32.dp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // INDEX: Currency Badge (clickable to open Settings)
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .background(SurfaceBase, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .clickable { actions.onOpenSettings() }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.dashboard_index_label),
                                style = LabelBadgeStyle.copy(fontSize = 10.sp),
                                color = StatusUnverifiedFg
                            )
                            Text(
                                text = state.settings.defaultCurrency.ifBlank { "CHF" },
                                style = CodePriceStyle.copy(fontSize = 12.sp),
                                color = TextPrimary
                            )
                        }
                    }

                    // Cloud Sync Button
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .background(if (state.isSyncing) SurfaceElevated else SurfaceBase, RoundedCornerShape(4.dp))
                            .border(1.dp, if (state.isSyncing) StatusEnglishFg else BorderSubtle, RoundedCornerShape(4.dp))
                            .clickable(enabled = !state.isSyncing) { actions.onSyncFromFirestore() }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = StatusEnglishFg
                                )
                                Text(
                                    text = stringResource(Res.string.dashboard_syncing),
                                    style = LabelBadgeStyle.copy(fontSize = 11.sp),
                                    color = StatusEnglishFg
                                )
                            } else {
                                Text(text = "🔄", fontSize = 11.sp)
                                Text(
                                    text = stringResource(Res.string.dashboard_sync),
                                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                                    color = if (state.settings.firebaseProjectId.isNotBlank()) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }

                    // Elegant Quick Scan button (32dp height aligned)
                    Button(
                        onClick = { actions.onOpenScanDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(32.dp).defaultMinSize(minHeight = 32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.dashboard_action_quick_scan),
                            style = LabelFilterStyle.copy(fontSize = 12.sp),
                            color = Color.White
                        )
                    }
                }
            }

            // Split 2 Columns: Active Section Panel (42%) and Dossier/AI Chat (58%)
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // LEFT COLUMN: ACTIVE SECTION PANEL (~42%)
                Box(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .background(SurfaceBase)
                        .border(BorderStroke(1.dp, BorderSubtle))
                ) {
                    when (state.activeSection) {
                        AppSection.ACTIVITY -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                            // Console Platform Selector and Quick Filters
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard)
                                    .border(BorderStroke(1.dp, BorderSubtle))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val isAllSelected = state.selectedPlatform == null
                                    Box(
                                        modifier = Modifier
                                            .background(if (isAllSelected) AccentBlue else SurfaceElevated, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isAllSelected) AccentBlue else BorderSubtle, RoundedCornerShape(4.dp))
                                            .clickable { actions.onPlatformSelect(null) }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.dashboard_filter_all),
                                            style = LabelFilterStyle,
                                            color = if (isAllSelected) Color.White else TextPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    activePlatforms.forEach { platform ->
                                        val isSelected = state.selectedPlatform == platform
                                        val color = Color(platform.brandColorHex)
                                        val count = state.games.count { it.platform == platform }

                                        Box(
                                            modifier = Modifier
                                                .background(if (isSelected) color else SurfaceElevated, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (isSelected) color else BorderSubtle, RoundedCornerShape(4.dp))
                                                .clickable { actions.onPlatformSelect(platform) }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                if (!isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(color, RoundedCornerShape(3.dp))
                                                    )
                                                }
                                                Text(
                                                    text = platform.shortName,
                                                    style = LabelFilterStyle,
                                                    color = if (isSelected) Color.White else TextPrimary,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                if (count > 0) {
                                                    Text(
                                                        text = "$count",
                                                        style = CodeSkuStyle.copy(fontSize = 10.sp),
                                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else StatusUnverifiedFg,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
        
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(if (state.filterEnglishOnly) StatusEnglishBg else SurfaceElevated, RoundedCornerShape(4.dp))
                                            .clickable { actions.onToggleEnglishOnly() }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(text = stringResource(Res.string.dashboard_filter_english_only), style = LabelBadgeStyle, color = StatusEnglishFg)
                                    }
        
                                    Box(
                                        modifier = Modifier
                                            .background(if (state.filterUskAlertsOnly) StatusRiskBg else SurfaceElevated, RoundedCornerShape(4.dp))
                                            .clickable { actions.onToggleUskAlerts() }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(text = stringResource(Res.string.dashboard_filter_usk_alerts), style = LabelBadgeStyle, color = StatusRiskFg)
                                    }
        
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(text = stringResource(Res.string.dashboard_items_count, state.games.size), style = CodeSkuStyle.copy(fontSize = 11.sp), color = StatusUnverifiedFg)
                                }
                            }
        
                            // Game rows feed or Empty State
                            if (state.games.isEmpty()) {
                                TacticalEmptyState(
                                    icon = "🔍",
                                    title = stringResource(Res.string.dashboard_empty_catalog),
                                    actionLabel = stringResource(Res.string.dashboard_clear_filters),
                                    onActionClick = actions.onClearFilters,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                ) {
                                    items(state.games, key = { it.id }) { game ->
                                        GameListItemRow(
                                            game = game,
                                            isSelected = state.selectedGame?.id == game.id,
                                            currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                                            onClick = { actions.onGameSelected(game) }
                                        )
                                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }

                    AppSection.DISCOVER -> {
                        DiscoveryScreen(
                            discoveredGames = discoveryState.discoveredGames,
                            selectedGenre = discoveryState.selectedGenre,
                            selectedPlatform = discoveryState.selectedPlatform,
                            isDiscovering = discoveryState.isDiscovering,
                            searchQuery = discoveryState.searchQuery,
                            onQueryChange = actions.onDiscoveryQueryChange,
                            onSearchSubmit = actions.onDiscoverySearchSubmit,
                            onGenreSelect = actions.onDiscoveryGenreSelect,
                            onPlatformSelect = actions.onDiscoveryPlatformSelect,
                            onOpenDossier = actions.onOpenDiscoveredDossier,
                            onAddToWishlist = actions.onAddDiscoveredToWishlist,
                            currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                            isAutoDiscoveryEnabled = discoveryState.isAutoDiscoveryEnabled,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AppSection.WISHLIST -> {
                        WishlistScreen(
                            state = wishlistState,
                            selectedGameId = state.selectedGame?.id,
                            currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                            onGameSelected = actions.onGameSelected,
                            onOpenImportDialog = actions.onOpenImportDialog,
                            onRetryEnrichment = actions.onRetryEnrichment,
                            onNavigateToDetail = { actions.onGameSelected(it) },
                            onClearSearch = actions.onClearFilters,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                        AppSection.COLLECTION -> {
                            CollectionScreen(
                                state = state,
                                actions = actions,
                                onNavigateToDetail = { actions.onGameSelected(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // RIGHT COLUMN: GAME INTELLIGENCE & GEMINI CHAT PANE (~58%)
                val game = state.selectedGame
                if (game != null) {
                    Column(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                            .background(SurfaceBase)
                    ) {
                                // Selected Game Dossier Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 64.dp)
                                        .background(SurfaceCard)
                                        .border(BorderStroke(1.dp, BorderSubtle))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = game.title,
                                            style = HeadlineMd,
                                            color = TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            PlatformBadge(platform = game.platform)
                                            if (game.releaseYear.isNotBlank()) {
                                                Text(
                                                    text = "•  ${game.releaseYear}",
                                                    style = BodySm,
                                                    color = TextSecondary
                                                )
                                            }
                                            if (game.spottedLocation.isNotBlank()) {
                                                Text(
                                                    text = "•  ${stringResource(Res.string.dossier_spotted_location)}: ${game.spottedLocation}",
                                                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                                                    color = StatusUnverifiedFg,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
        
                                    // Collection Status and Delete Actions
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CollectionStatusSelector(
                                            currentStatus = game.collectionStatus,
                                            onStatusSelect = { newStatus ->
                                                if (newStatus == CollectionStatus.OWNED && game.collectionStatus == CollectionStatus.WISHLIST) {
                                                    acquisitionOffer = game.bestOffer
                                                    isAcquisitionOpen = true
                                                } else {
                                                    actions.onUpdateGameStatus(game, newStatus)
                                                }
                                            }
                                        )

                                        var showDeleteConfirm by remember(game.id) { mutableStateOf(false) }

                                        IconButton(
                                            onClick = { showDeleteConfirm = true }
                                        ) {
                                            Text("🗑️", fontSize = 16.sp)
                                        }

                                        if (showDeleteConfirm) {
                                            TactileConfirmDialog(
                                                title = stringResource(Res.string.dossier_delete_confirm_title),
                                                message = stringResource(Res.string.dossier_delete_confirm_message),
                                                confirmLabel = stringResource(Res.string.dossier_delete_confirm_button),
                                                dismissLabel = stringResource(Res.string.dossier_delete_cancel_button),
                                                isDestructive = true,
                                                onConfirm = {
                                                    showDeleteConfirm = false
                                                    actions.onDeleteGame(game.id)
                                                },
                                                onDismiss = { showDeleteConfirm = false }
                                            )
                                        }
                                    }
                                }

                                // Tactical Dossier Section: SKU Matrix and Market Radar
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceCard.copy(alpha = 0.5f))
                                        .border(BorderStroke(1.dp, BorderSubtle))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                        val currentCurrency = state.settings.defaultCurrency.ifBlank { "CHF" }
                                        if (maxWidth >= 520.dp) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                 SafeSkuMatrixView(
                                                    safeSkus = game.safeSkus,
                                                    riskySkus = game.riskySkus,
                                                    activeSkuCode = game.productCode,
                                                    onSelectSku = { skuCode ->
                                                        actions.onUpdateProductCode(game, skuCode)
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )

                                                game.marketRadar?.let { radar ->
                                                    SwissMarketRadarView(
                                                        radar = radar,
                                                        currency = currentCurrency,
                                                        paidPriceChf = game.paidPriceChf,
                                                        isOwned = game.collectionStatus == CollectionStatus.OWNED,
                                                        onPriceSubmitted = { parsed ->
                                                            actions.onUpdatePaidPrice(game, parsed)
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }

                                                if (game.marketRadar == null && game.collectionStatus == CollectionStatus.OWNED) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.End
                                                    ) {
                                                        PaidPriceInput(
                                                            paidPrice = game.paidPriceChf,
                                                            currency = currentCurrency,
                                                            label = "${stringResource(Res.string.dossier_paid_price_label)}:",
                                                            onPriceSubmitted = { parsed ->
                                                                actions.onUpdatePaidPrice(game, parsed)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                SafeSkuMatrixView(
                                                    safeSkus = game.safeSkus,
                                                    riskySkus = game.riskySkus,
                                                    activeSkuCode = game.productCode,
                                                    onSelectSku = { skuCode ->
                                                        actions.onUpdateProductCode(game, skuCode)
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                game.marketRadar?.let { radar ->
                                                    SwissMarketRadarView(
                                                        radar = radar,
                                                        currency = currentCurrency,
                                                        paidPriceChf = game.paidPriceChf,
                                                        isOwned = game.collectionStatus == CollectionStatus.OWNED,
                                                        onPriceSubmitted = { parsed ->
                                                            actions.onUpdatePaidPrice(game, parsed)
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                if (game.marketRadar == null && game.collectionStatus == CollectionStatus.OWNED) {
                                                    PaidPriceInput(
                                                        paidPrice = game.paidPriceChf,
                                                        currency = currentCurrency,
                                                        label = "${stringResource(Res.string.dossier_paid_price_label)}:",
                                                        onPriceSubmitted = { parsed ->
                                                            actions.onUpdatePaidPrice(game, parsed)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Stores & Live Offers Section
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    val currentCurrency = state.settings.defaultCurrency.ifBlank { "CHF" }
                                    com.retrocollector.app.offers.presentation.components.StoreOffersSection(
                                        offers = game.offers,
                                        currency = currentCurrency,
                                        isCompactLayout = false,
                                        onAddOfferClick = { isAddOfferOpen = true },
                                        onEditOfferClick = { offer -> editingOffer = offer },
                                        onDeleteOfferClick = { offer -> actions.onDeleteOffer(game, offer.id) },
                                        onMarkAsBoughtClick = { offer ->
                                            acquisitionOffer = offer
                                            isAcquisitionOpen = true
                                        }
                                    )
                                }

                                // Shelf of Similar Games / Same Genre
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    SimilarGamesShelf(
                                        similarGames = state.similarGamesForActiveGame,
                                        isLoading = state.isSimilarGamesLoading,
                                        currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                                        onSelectGame = { actions.onOpenDiscoveredDossier(it) },
                                        onAddToWishlist = { actions.onAddDiscoveredToWishlist(it) },
                                        onRefreshSimilarGames = { actions.onLoadSimilarGames(game) }
                                    )
                                }

                                // Contextual Gemini Conversation List
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.activeChatMessages, key = { it.id }) { msg ->
                                        DeletableChatBubble(
                                            message = msg,
                                            onDeleteMessage = actions.onDeleteChatMessage
                                        )
                                    }
        
                                    if (state.isAnalyzing) {
                                        item(key = "loading_typing_bubble") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    color = StatusEnglishFg,
                                                    strokeWidth = 2.dp
                                                )
                                                Text(
                                                    text = stringResource(Res.string.dossier_analyzing_verification),
                                                    style = BodySm.copy(fontSize = 12.sp),
                                                    color = StatusEnglishFg
                                                )
                                            }
                                        }
                                    }
                                }
        
                                // Question Input Field with Fixed Bottom Context Bar
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceCard)
                                        .border(BorderStroke(1.dp, BorderSubtle))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.dossier_context, game.title, game.productCode ?: "PAL"),
                                            style = CodeSkuStyle.copy(fontSize = 11.sp),
                                            color = StatusUnverifiedFg
                                        )
                                    }
        
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TactileTextField(
                                            value = followUpQuestion,
                                            onValueChange = { followUpQuestion = it },
                                            placeholder = stringResource(Res.string.dossier_chat_placeholder),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
        
                                        Button(
                                            onClick = {
                                                val q = followUpQuestion.trim()
                                                if (q.isNotEmpty()) {
                                                    actions.onSendFollowUpMessage(q)
                                                    followUpQuestion = ""
                                                }
                                            },
                                            enabled = !state.isAnalyzing && followUpQuestion.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(32.dp).defaultMinSize(minHeight = 32.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            if (state.isAnalyzing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(12.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text(text = "${stringResource(Res.string.dossier_chat_send)} \uD83D\uDE80", style = LabelFilterStyle, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .weight(0.58f)
                                    .fillMaxHeight()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🎮", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = stringResource(Res.string.dashboard_no_game_selected_title),
                                    style = HeadlineSm,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(Res.string.dashboard_no_game_selected),
                                    style = BodySm,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = actions.onOpenScanDialog,
                                        colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(text = stringResource(Res.string.dashboard_action_quick_scan), style = LabelFilterStyle, color = Color.White)
                                    }
                                    OutlinedButton(
                                        onClick = { actions.onSectionSelect(AppSection.DISCOVER) },
                                        border = BorderStroke(1.dp, BorderStrong),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(text = stringResource(Res.string.dashboard_action_discover), style = LabelFilterStyle, color = TextPrimary)
                                    }
                                    OutlinedButton(
                                        onClick = actions.onOpenImportDialog,
                                        border = BorderStroke(1.dp, BorderStrong),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(text = stringResource(Res.string.dashboard_action_import_wishlist), style = LabelFilterStyle, color = TextPrimary)
                                    }
                                }
                            }
                        }
            }
        }
    }

    val selected = state.selectedGame
    if (selected != null && (isAddOfferOpen || editingOffer != null)) {
        com.retrocollector.app.offers.presentation.dialog.AddEditOfferDialog(
            initialOffer = editingOffer,
            currency = state.settings.defaultCurrency.ifBlank { "CHF" },
            onSave = { offer ->
                actions.onAddOrUpdateOffer(selected, offer)
                isAddOfferOpen = false
                editingOffer = null
            },
            onDismiss = {
                isAddOfferOpen = false
                editingOffer = null
            }
        )
    }

    if (selected != null && isAcquisitionOpen) {
        com.retrocollector.app.offers.presentation.dialog.AcquisitionModal(
            game = selected,
            selectedOffer = acquisitionOffer,
            currency = state.settings.defaultCurrency.ifBlank { "CHF" },
            onConfirm = { finalPrice, condition, offerId ->
                actions.onConvertOfferToOwned(selected, offerId, finalPrice, condition)
                isAcquisitionOpen = false
                acquisitionOffer = null
            },
            onDismiss = {
                isAcquisitionOpen = false
                acquisitionOffer = null
            }
        )
    }
}

@Preview
@Composable
fun DesktopWorkstationScreenPreview() {
    RetroTactileTheme {
        val sampleGame = GameItem(
            id = "gc_re4",
            title = "Resident Evil 4",
            franchiseName = "Resident Evil",
            platform = ConsolePlatform.GAMECUBE,
            releaseYear = "2005",
            productCode = "DOL-P-G4BE",
            spottedLocation = "Brockenhaus Bern",
            askingPriceChf = 35.0,
            paidPriceChf = 35.0,
            collectionStatus = CollectionStatus.WISHLIST,
            languageStatus = LanguageStatus.SUBS_ONLY,
            marketRadar = SwissMarketRadar(
                spottedPriceChf = 35.0,
                medianPriceChf = 31.50,
                historicalMinChf = 28.0,
                historicalMaxChf = 36.0,
                trend = "Stable"
            ),
            safeSkus = listOf(
                SkuInfo(code = "DOL-P-G4BE", region = "UKV", editionNote = "EN audio + EN/FR/DE/ES/IT subs", isSafe = true)
            ),
            collectorVerdict = "UKV/EUR edition recommended with full multilingual subtitles."
        )
        val sampleGames = listOf(
            sampleGame,
            GameItem(
                id = "n64_sm64",
                title = "Super Mario 64",
                franchiseName = "Mario",
                platform = ConsolePlatform.N64,
                releaseYear = "1997",
                productCode = "NUS-NSMP-EUR",
                spottedLocation = "Ricardo.ch",
                askingPriceChf = 45.0,
                collectionStatus = CollectionStatus.OWNED,
                languageStatus = LanguageStatus.FULL_ENGLISH
            )
        )
        DesktopWorkstationScreen(
            state = DashboardUiState(
                games = sampleGames,
                selectedGame = sampleGame,
                activeSection = AppSection.ACTIVITY
            ),
            discoveryState = DiscoveryUiState(),
            wishlistState = WishlistUiState(),
            actions = DashboardActions()
        )
    }
}

@Composable
private fun SidebarSectionItem(
    section: AppSection,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeColor: Color = StatusUnverifiedFg
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isSelected) SurfaceElevated else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                Text(text = section.icon, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
            Text(
                text = stringResource(section.labelRes),
                style = BodyMd,
                color = if (isSelected) TextPrimary else TextSecondary
            )
        }
        Box(
            modifier = Modifier
                .background(SurfaceBase, RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "$count",
                style = CodeSkuStyle.copy(fontSize = 11.sp),
                color = badgeColor
            )
        }
    }
}
