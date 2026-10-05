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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.presentation.components.*
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.resources.stringResource
import com.retrocollector.app.collection.presentation.ui.CollectionScreen
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardUiState
import com.retrocollector.app.discovery.presentation.ui.DiscoveryScreen
import com.retrocollector.app.discovery.presentation.viewmodel.DiscoveryUiState
import com.retrocollector.app.wishlist.presentation.ui.WishlistScreen
import com.retrocollector.app.wishlist.presentation.viewmodel.WishlistUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MobileFieldDashboardScreen(
    state: DashboardUiState,
    discoveryState: DiscoveryUiState = DiscoveryUiState(),
    wishlistState: WishlistUiState = WishlistUiState(),
    actions: DashboardActions = DashboardActions(),
    onNavigateToDetail: (GameItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activePlatforms = remember(state.games, state.settings.pinnedPlatformIds) {
        val fromGames = state.games.map { it.platform }.toSet()
        val fromPinned = state.settings.pinnedPlatformIds.mapNotNull { ConsolePlatform.fromId(it) }.toSet()
        val combined = (fromGames + fromPinned).ifEmpty {
            setOf(ConsolePlatform.N64, ConsolePlatform.GAMECUBE, ConsolePlatform.PS3, ConsolePlatform.SWITCH)
        }
        ConsolePlatform.entries.filter { it in combined }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceBase,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(SurfaceBase.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(StatusEnglishFg, RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = "${state.activeSection.icon} ${state.activeSection.label}".uppercase(),
                        style = HeadlineSm,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sync Button
                    Box(
                        modifier = Modifier
                            .background(if (state.isSyncing) SurfaceElevated else SurfaceContainer, RoundedCornerShape(12.dp))
                            .clickable(enabled = !state.isSyncing) { actions.onSyncFromFirestore() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp,
                                    color = StatusEnglishFg
                                )
                                Text(text = stringResource(Res.string.dashboard_syncing), style = CodeSkuStyle.copy(fontSize = 10.sp), color = StatusEnglishFg)
                            } else {
                                Text(text = "🔄", fontSize = 10.sp)
                                Text(text = stringResource(Res.string.dashboard_sync), style = CodeSkuStyle.copy(fontSize = 10.sp), color = TextSecondary)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(SurfaceContainer, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(5.dp).background(StatusEnglishFg, RoundedCornerShape(2.5.dp)))
                            Text(text = "CH-PAL", style = CodeSkuStyle.copy(fontSize = 11.sp), color = TextSecondary)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { actions.onOpenScanDialog() },
                containerColor = ConsoleGamecube,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Text(text = stringResource(Res.string.dashboard_action_quick_scan), style = LabelFilterStyle)
            }
        },
        bottomBar = {
            TactileBottomNavigation(
                activeSection = state.activeSection,
                wishlistCount = wishlistState.allWishlistCount,
                collectionCount = state.collectionGames.size,
                onSectionSelect = actions.onSectionSelect,
                onOpenSettings = actions.onOpenSettings
            )
        }
    ) { paddingValues ->
        if (state.activeSection == AppSection.DISCOVER) {
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
                onOpenDossier = { discovered ->
                    actions.onOpenDiscoveredDossier(discovered)
                    val matchingGame = state.games.find {
                        it.title.equals(discovered.title, ignoreCase = true) && it.platform == discovered.platform
                    } ?: discovered.toGameItem()
                    onNavigateToDetail(matchingGame)
                },
                onAddToWishlist = actions.onAddDiscoveredToWishlist,
                currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                isAutoDiscoveryEnabled = discoveryState.isAutoDiscoveryEnabled,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Shared Tactical SKU Search Bar
                TactileSearchField(
                    query = state.searchQuery,
                    onQueryChange = { actions.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = stringResource(Res.string.dashboard_search_placeholder),
                    backgroundColor = SurfaceCard,
                    shape = RoundedCornerShape(8.dp),
                    textStyle = BodyMd.copy(color = TextPrimary),
                    placeholderStyle = BodySm.copy(color = TextSecondary),
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .background(SurfaceElevated, RoundedCornerShape(4.dp))
                                .clickable { actions.onOpenScanDialog() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(text = "📷", fontSize = 12.sp)
                        }
                    }
                )

                // Console Grid (Adaptive Active Platforms)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PLATFORM FILTER",
                            style = LabelFilterStyle.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { actions.onOpenSettings() }
                        ) {
                            Text(
                                text = "⚙️ PINS",
                                style = CodeSkuStyle.copy(fontSize = 10.sp),
                                color = StatusEnglishFg
                            )
                        }
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item(key = "all_platforms_chip") {
                            val isAllSelected = state.selectedPlatform == null
                            Box(
                                modifier = Modifier
                                    .background(if (isAllSelected) AccentBlue else SurfaceCard, RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isAllSelected) AccentBlue else BorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { actions.onPlatformSelect(null) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "ALL",
                                    style = LabelFilterStyle,
                                    color = if (isAllSelected) Color.White else TextPrimary
                                )
                            }
                        }

                        items(activePlatforms, key = { it.id }) { platform ->
                            val isSelected = state.selectedPlatform == platform
                            val color = Color(platform.brandColorHex)
                            val bg = if (isSelected) color else SurfaceCard
                            val count = state.games.count { it.platform == platform }

                            Box(
                                modifier = Modifier
                                    .background(bg, RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSelected) color else BorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { actions.onPlatformSelect(platform) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                    if (count > 0) {
                                        Text(
                                            text = "($count)",
                                            style = CodeSkuStyle.copy(fontSize = 10.sp),
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else StatusUnverifiedFg
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "manage_pins_chip") {
                            Box(
                                modifier = Modifier
                                    .background(SurfaceElevated, RoundedCornerShape(6.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { actions.onOpenSettings() }
                                    .padding(horizontal = 8.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "➕ Manage",
                                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                                    color = StatusEnglishFg
                                )
                            }
                        }
                    }
                }

                when (state.activeSection) {
                    AppSection.ACTIVITY -> {
                        // List Header and Counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stringResource(Res.string.dashboard_stats_tracked)} (${state.games.size})",
                                style = HeadlineSm,
                                color = TextPrimary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(if (state.filterEnglishOnly) StatusEnglishBg else SurfaceCard, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (state.filterEnglishOnly) StatusEnglishFg else BorderSubtle, RoundedCornerShape(4.dp))
                                        .clickable { actions.onToggleEnglishOnly() }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(text = stringResource(Res.string.dashboard_filter_en_badge), style = LabelBadgeStyle, color = StatusEnglishFg)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(if (state.filterUskAlertsOnly) StatusRiskBg else SurfaceCard, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (state.filterUskAlertsOnly) StatusRiskFg else BorderSubtle, RoundedCornerShape(4.dp))
                                        .clickable { actions.onToggleUskAlerts() }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(text = stringResource(Res.string.dashboard_filter_usk_badge), style = LabelBadgeStyle, color = StatusRiskFg)
                                }
                            }
                        }

                        // Game Cards List in Grouped Container
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceCard,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            if (state.games.isEmpty()) {
                                TacticalEmptyState(
                                    icon = "🔍",
                                    title = stringResource(Res.string.dashboard_empty_catalog),
                                    actionLabel = stringResource(Res.string.dashboard_clear_filters),
                                    onActionClick = actions.onClearFilters,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(state.games, key = { it.id }) { game ->
                                        GameListItemRow(
                                            game = game,
                                            isSelected = state.selectedGame?.id == game.id,
                                            currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                                            onClick = {
                                                actions.onGameSelected(game)
                                                onNavigateToDetail(game)
                                            }
                                        )
                                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }

                    AppSection.WISHLIST -> {
                        WishlistScreen(
                            state = wishlistState,
                            selectedGameId = state.selectedGame?.id,
                            currency = state.settings.defaultCurrency.ifBlank { "CHF" },
                            onGameSelected = actions.onGameSelected,
                            onOpenImportDialog = actions.onOpenImportDialog,
                            onRetryEnrichment = actions.onRetryEnrichment,
                            onNavigateToDetail = { game ->
                                actions.onGameSelected(game)
                                onNavigateToDetail(game)
                            },
                            onClearSearch = actions.onClearFilters,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    }

                    AppSection.COLLECTION -> {
                        CollectionScreen(
                            state = state,
                            actions = actions,
                            onNavigateToDetail = { game ->
                                actions.onGameSelected(game)
                                onNavigateToDetail(game)
                            },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    }

                    AppSection.DISCOVER -> Unit
                }
            }
        }
    }
}

@Preview
@Composable
fun MobileFieldDashboardScreenPreview() {
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
            collectionStatus = CollectionStatus.WISHLIST,
            languageStatus = LanguageStatus.SUBS_ONLY
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
        MobileFieldDashboardScreen(
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
