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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.model.AppSection
import com.retrocollector.app.core.presentation.components.*
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.collection.presentation.ui.CollectionScreen
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardUiState
import com.retrocollector.app.discovery.presentation.components.SimilarGamesShelf
import com.retrocollector.app.discovery.presentation.ui.DiscoveryScreen
import com.retrocollector.app.discovery.presentation.viewmodel.DiscoveryUiState
import com.retrocollector.app.wishlist.presentation.ui.WishlistScreen
import com.retrocollector.app.wishlist.presentation.viewmodel.WishlistUiState

@Composable
fun DesktopWorkstationScreen(
    state: DashboardUiState,
    discoveryState: DiscoveryUiState = DiscoveryUiState(),
    wishlistState: WishlistUiState = WishlistUiState(),
    actions: DashboardActions = DashboardActions(),
    modifier: Modifier = Modifier
) {
    var followUpQuestion by remember { mutableStateOf("") }

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
                // Espaço para os botões nativos do macOS (Fechar, Minimizar, Expandir)
                Spacer(modifier = Modifier.width(68.dp))

                Text(
                    text = TextKeys.App.TITLE,
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

            // Secção de Menus de Biblioteca
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "INTELLIGENCE LIBRARY",
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp)
                )

                // Item: Activity (Prospeção ativa)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (state.activeSection == AppSection.ACTIVITY) SurfaceElevated else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { actions.onSectionSelect(AppSection.ACTIVITY) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                            Text(text = AppSection.ACTIVITY.icon, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                        Text(text = AppSection.ACTIVITY.label, style = BodyMd, color = if (state.activeSection == AppSection.ACTIVITY) TextPrimary else TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(SurfaceBase, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = "${state.games.size}", style = CodeSkuStyle.copy(fontSize = 11.sp), color = StatusUnverifiedFg)
                    }
                }

                // Item: Discover (Radar de Descoberta)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (state.activeSection == AppSection.DISCOVER) SurfaceElevated else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { actions.onSectionSelect(AppSection.DISCOVER) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                            Text(text = AppSection.DISCOVER.icon, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                        Text(text = AppSection.DISCOVER.label, style = BodyMd, color = if (state.activeSection == AppSection.DISCOVER) TextPrimary else TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(SurfaceBase, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        val discCount = discoveryState.discoveredGames.size
                        Text(
                            text = "$discCount",
                            style = CodeSkuStyle.copy(fontSize = 11.sp),
                            color = if (discCount > 0) ConsoleGamecube else StatusUnverifiedFg
                        )
                    }
                }

                // Item: Wishlist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (state.activeSection == AppSection.WISHLIST) SurfaceElevated else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { actions.onSectionSelect(AppSection.WISHLIST) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                            Text(text = AppSection.WISHLIST.icon, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                        Text(text = AppSection.WISHLIST.label, style = BodyMd, color = if (state.activeSection == AppSection.WISHLIST) TextPrimary else TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(SurfaceBase, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        val wishCount = wishlistState.wishlistGames.size
                        Text(
                            text = "$wishCount",
                            style = CodeSkuStyle.copy(fontSize = 11.sp),
                            color = if (wishCount > 0) StatusEditionFg else StatusUnverifiedFg
                        )
                    }
                }

                // Item: Collection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (state.activeSection == AppSection.COLLECTION) SurfaceElevated else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { actions.onSectionSelect(AppSection.COLLECTION) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                            Text(text = AppSection.COLLECTION.icon, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                        Text(text = AppSection.COLLECTION.label, style = BodyMd, color = if (state.activeSection == AppSection.COLLECTION) TextPrimary else TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(SurfaceBase, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = "${state.collectionGames.size}", style = CodeSkuStyle.copy(fontSize = 11.sp), color = StatusUnverifiedFg)
                    }
                }

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
                        Text(text = TextKeys.Navigation.TAB_SETTINGS, style = BodyMd, color = TextSecondary)
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

                Text(
                    text = "PLATFORM FILTER",
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp)
                )

                val platforms = remember { ConsolePlatform.entries }
                platforms.forEach { platform ->
                    val isSelected = state.selectedPlatform == platform
                    val color = when (platform) {
                        ConsolePlatform.N64 -> ConsoleN64
                        ConsolePlatform.GAMECUBE -> ConsoleGamecube
                        ConsolePlatform.PS3 -> ConsolePS3
                        ConsolePlatform.SWITCH -> ConsoleSwitch
                    }

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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
                            Text(text = platform.displayName, style = BodyMd, color = if (isSelected) TextPrimary else TextSecondary)
                        }
                    }
                }
            }

            // Rodapé da Sidebar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, BorderSubtle))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(StatusEnglishFg, RoundedCornerShape(3.dp)))
                        Text(text = "Clean Architecture KMP", style = CodeSkuStyle.copy(fontSize = 10.sp), color = StatusUnverifiedFg)
                    }
                    Text(text = "READY", style = LabelBadgeStyle.copy(fontSize = 10.sp), color = StatusEnglishFg)
                }
            }
        }

        // -------------------------------------------------------------
        // CONTEÚDO PRINCIPAL (SPLIT-VIEW: 5 colunas Lista + 7 colunas Dossiê)
        // -------------------------------------------------------------
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Barra de Topo da Workstation
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
                // Caixa de Pesquisa rápida (altura 32dp alinhada)
                TactileSearchField(
                    query = state.searchQuery,
                    onQueryChange = { actions.onSearchQueryChange(it) },
                    placeholder = TextKeys.Dashboard.SEARCH_PLACEHOLDER,
                    modifier = Modifier.width(420.dp),
                    minHeight = 32.dp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // INDEX: CHF / Currency Badge (clicável para abrir Settings)
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
                                text = "INDEX:",
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

                    // Botão Quick Scan elegante (altura 32dp alinhada)
                    Button(
                        onClick = { actions.onOpenScanDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(32.dp).defaultMinSize(minHeight = 32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = TextKeys.Dashboard.ACTION_QUICK_SCAN,
                            style = LabelFilterStyle.copy(fontSize = 12.sp),
                            color = Color.White
                        )
                    }
                }
            }

            // Split 2 Colunas: Painel da Secção Ativa (42%) e Dossier/Chat de IA (58%)
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // COLUNA ESQUERDA: PAINEL DA SECÇÃO ATIVA (~42%)
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
                            // Seletor de Consolas e Toggles Rápidos
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard)
                                    .border(BorderStroke(1.dp, BorderSubtle))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(ConsolePlatform.entries.toList(), key = { it.id }) { platform ->
                                        val isSelected = state.selectedPlatform == platform
                                        val color = when (platform) {
                                            ConsolePlatform.N64 -> ConsoleN64
                                            ConsolePlatform.GAMECUBE -> ConsoleGamecube
                                            ConsolePlatform.PS3 -> ConsolePS3
                                            ConsolePlatform.SWITCH -> ConsoleSwitch
                                        }
        
                                        Row(
                                            modifier = Modifier
                                                .background(if (isSelected) color else SurfaceElevated, RoundedCornerShape(4.dp))
                                                .clickable { actions.onPlatformSelect(platform) }
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = platform.displayName,
                                                style = LabelFilterStyle,
                                                color = Color.White,
                                                maxLines = 1,
                                                softWrap = false
                                            )
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
                                        Text(text = TextKeys.Dashboard.FILTER_ENGLISH_ONLY, style = LabelBadgeStyle, color = StatusEnglishFg)
                                    }
        
                                    Box(
                                        modifier = Modifier
                                            .background(if (state.filterUskAlertsOnly) StatusRiskBg else SurfaceElevated, RoundedCornerShape(4.dp))
                                            .clickable { actions.onToggleUskAlerts() }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(text = TextKeys.Dashboard.FILTER_USK_ALERTS, style = LabelBadgeStyle, color = StatusRiskFg)
                                    }
        
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(text = "${state.games.size} items", style = CodeSkuStyle.copy(fontSize = 11.sp), color = StatusUnverifiedFg)
                                }
                            }
        
                            // Feed com as linhas de jogos
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(state.games, key = { it.id }) { game ->
                                    GameListItemRow(
                                        game = game,
                                        isSelected = state.selectedGame?.id == game.id,
                                        onClick = { actions.onGameSelected(game) }
                                    )
                                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AppSection.WISHLIST -> {
                        WishlistScreen(
                            state = wishlistState,
                            selectedGameId = state.selectedGame?.id,
                            onGameSelected = actions.onGameSelected,
                            onOpenImportDialog = actions.onOpenImportDialog,
                            onRetryEnrichment = actions.onRetryEnrichment,
                            onNavigateToDetail = { actions.onGameSelected(it) },
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

                // COLUNA DIREITA: GAME INTELLIGENCE & GEMINI CHAT PANE (~58%)
                val game = state.selectedGame
                if (game != null) {
                    Column(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                            .background(SurfaceBase)
                    ) {
                                // Header do Dossiê do Jogo Selecionado
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
                                            .padding(end = 12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = game.title,
                                                style = HeadlineMd,
                                                color = TextPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            PlatformBadge(platform = game.platform)
                                        }
                                        if (game.spottedLocation.isNotBlank()) {
                                            Text(
                                                text = "${TextKeys.Dossier.SPOTTED_LOCATION}: ${game.spottedLocation}",
                                                style = CodeSkuStyle.copy(fontSize = 11.sp),
                                                color = StatusUnverifiedFg,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
        
                                    // Botões de Estado de Coleção: Hunting, Owned, Avoid
                                    CollectionStatusSelector(
                                        currentStatus = game.collectionStatus,
                                        onStatusSelect = { actions.onUpdateGameStatus(game, it) }
                                    )
                                }
        
                                // Faixa de Dossiê Tático: Matriz de SKUs e Radar de Preço
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceCard.copy(alpha = 0.5f))
                                        .border(BorderStroke(1.dp, BorderSubtle))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                        if (maxWidth >= 520.dp) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                SafeSkuMatrixView(
                                                    safeSkus = game.safeSkus,
                                                    riskySkus = game.riskySkus,
                                                    modifier = Modifier.weight(1f)
                                                )
        
                                                game.marketRadar?.let { radar ->
                                                    SwissMarketRadarView(
                                                        radar = radar,
                                                        modifier = Modifier.weight(1f)
                                                    )
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
                                                    modifier = Modifier.fillMaxWidth()
                                                )
        
                                                game.marketRadar?.let { radar ->
                                                    SwissMarketRadarView(
                                                        radar = radar,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
        
                                // Prateleira de Jogos Semelhantes / Mesmo Género
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    SimilarGamesShelf(
                                        similarGames = state.similarGamesForActiveGame,
                                        isLoading = state.isSimilarGamesLoading,
                                        onSelectGame = { actions.onOpenDiscoveredDossier(it) },
                                        onAddToWishlist = { actions.onAddDiscoveredToWishlist(it) }
                                    )
                                }

                                // Lista de Conversa Contextual com o Gemini
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.activeChatMessages, key = { it.id }) { msg ->
                                        GeminiChatBubble(message = msg)
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
                                                    text = "Gemini a analisar verificação regional...",
                                                    style = BodySm.copy(fontSize = 12.sp),
                                                    color = StatusEnglishFg
                                                )
                                            }
                                        }
                                    }
                                }
        
                                // Caixa de Entrada de Pergunta com Contexto Fixo no Rodapé
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
                                            text = "Context: ${game.title} (${game.productCode ?: "PAL"})",
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
                                            placeholder = TextKeys.Dossier.CHAT_PLACEHOLDER,
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
                                                Text(text = "${TextKeys.Dossier.CHAT_SEND} \uD83D\uDE80", style = LabelFilterStyle, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.weight(0.58f).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = TextKeys.Dashboard.NO_GAME_SELECTED, style = BodyMd, color = TextSecondary)
                            }
                        }
            }
        }
    }
}
