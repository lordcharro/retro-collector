package com.retrocollector.app.dashboard.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.model.AppSection
import com.retrocollector.app.core.presentation.components.*
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardUiState
import com.retrocollector.app.wishlist.presentation.ui.WishlistScreen
import com.retrocollector.app.collection.presentation.ui.CollectionScreen
import com.retrocollector.app.discovery.presentation.ui.DiscoveryScreen

@Composable
fun MobileFieldDashboardScreen(
    state: DashboardUiState,
    actions: DashboardActions = DashboardActions(),
    onNavigateToDetail: (GameItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSearchFocused by remember { mutableStateOf(false) }

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
                        text = TextKeys.Navigation.TAB_CATALOG.uppercase(),
                        style = HeadlineSm,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(SurfaceContainer, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
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
                Text(text = TextKeys.Dashboard.ACTION_QUICK_SCAN, style = LabelFilterStyle)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 8.dp
            ) {
                    NavigationBarItem(
                        selected = state.activeSection == AppSection.CATALOG,
                        onClick = { actions.onSectionSelect(AppSection.CATALOG) },
                        icon = { Text("\uD83D\uDCCB", fontSize = 18.sp) },
                        label = { Text(TextKeys.Navigation.TAB_CATALOG, style = LabelFilterStyle) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceElevated
                        )
                    )

                    NavigationBarItem(
                        selected = state.activeSection == AppSection.DISCOVER,
                        onClick = { actions.onSectionSelect(AppSection.DISCOVER) },
                        icon = { Text("🧭", fontSize = 18.sp) },
                        label = { Text("Discover", style = LabelFilterStyle) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceElevated
                        )
                    )

                    NavigationBarItem(
                        selected = state.activeSection == AppSection.WISHLIST,
                        onClick = { actions.onSectionSelect(AppSection.WISHLIST) },
                        icon = { Text("\uD83D\uDC9D", fontSize = 18.sp) },
                        label = { Text(TextKeys.Navigation.TAB_WISHLIST, style = LabelFilterStyle) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceElevated
                        )
                    )

                NavigationBarItem(
                    selected = state.activeSection == AppSection.COLLECTION,
                    onClick = { actions.onSectionSelect(AppSection.COLLECTION) },
                    icon = { Text("\uD83D\uDCE6", fontSize = 18.sp) },
                    label = { Text(TextKeys.Navigation.TAB_COLLECTION, style = LabelFilterStyle) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        selectedTextColor = AccentBlue,
                        indicatorColor = SurfaceElevated
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { actions.onOpenSettings() },
                    icon = { Text("\u2699\uFE0F", fontSize = 18.sp) },
                    label = { Text(TextKeys.Navigation.TAB_SETTINGS, style = LabelFilterStyle) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        selectedTextColor = AccentBlue,
                        indicatorColor = SurfaceElevated
                    )
                )
            }
        }
    ) { paddingValues ->
        when (state.activeSection) {
            AppSection.CATALOG -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Barra de Pesquisa de SKU Tática
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🔍", fontSize = 14.sp)
                        androidx.compose.foundation.text.BasicTextField(
                            value = state.searchQuery,
                            onValueChange = { actions.onSearchQueryChange(it) },
                            textStyle = BodyMd.copy(color = TextPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isSearchFocused = it.isFocused },
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (state.searchQuery.isEmpty() && !isSearchFocused) {
                                        Text(
                                            text = TextKeys.Dashboard.SEARCH_PLACEHOLDER,
                                            style = BodySm.copy(color = TextSecondary)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        Box(
                            modifier = Modifier
                                .background(SurfaceElevated, RoundedCornerShape(4.dp))
                                .clickable { actions.onOpenScanDialog() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(text = "📷", fontSize = 12.sp)
                        }
                    }

                    // Grelha de 4 colunas de Consola com contadores
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "PLATFORM FILTER",
                            style = LabelFilterStyle.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceCard, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ConsolePlatform.entries.forEach { platform ->
                                val isSelected = state.selectedPlatform == platform
                                val color = when (platform) {
                                    ConsolePlatform.N64 -> ConsoleN64
                                    ConsolePlatform.GAMECUBE -> ConsoleGamecube
                                    ConsolePlatform.PS3 -> ConsolePS3
                                    ConsolePlatform.SWITCH -> ConsoleSwitch
                                }

                                val bg = if (isSelected) color else Color.Transparent

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(bg, RoundedCornerShape(6.dp))
                                        .clickable { actions.onPlatformSelect(platform) }
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = platform.shortName,
                                        style = LabelFilterStyle,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Cabeçalho da Lista e Contador
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${TextKeys.Dashboard.STATS_TRACKED} (${state.games.size})",
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
                                Text(text = "✔ EN", style = LabelBadgeStyle, color = StatusEnglishFg)
                            }

                            Box(
                                modifier = Modifier
                                    .background(if (state.filterUskAlertsOnly) StatusRiskBg else SurfaceCard, RoundedCornerShape(4.dp))
                                    .border(1.dp, if (state.filterUskAlertsOnly) StatusRiskFg else BorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { actions.onToggleUskAlerts() }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(text = "🔴 USK", style = LabelBadgeStyle, color = StatusRiskFg)
                            }
                        }
                    }

                    // Lista de Cartões de Jogos em Contentor Agrupado
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceCard,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(state.games, key = { it.id }) { game ->
                                GameListItemRow(
                                    game = game,
                                    isSelected = state.selectedGame?.id == game.id,
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

            AppSection.DISCOVER -> {
                DiscoveryScreen(
                    discoveredGames = state.discoveredGames,
                    selectedGenre = state.selectedDiscoveryGenre,
                    selectedPlatform = state.selectedDiscoveryPlatform,
                    isDiscovering = state.isDiscovering,
                    searchQuery = state.discoverySearchQuery,
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }

            AppSection.WISHLIST -> {
                WishlistScreen(
                    state = state,
                    actions = actions,
                    onNavigateToDetail = { game ->
                        actions.onGameSelected(game)
                        onNavigateToDetail(game)
                    },
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }
        }
    }
}
