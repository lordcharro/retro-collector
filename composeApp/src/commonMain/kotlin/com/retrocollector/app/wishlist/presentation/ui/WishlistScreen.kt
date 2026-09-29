package com.retrocollector.app.wishlist.presentation.ui

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.presentation.components.EnrichmentBadge
import com.retrocollector.app.core.presentation.components.PlatformBadge
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.dashboard.presentation.ui.DashboardActions
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardUiState

/**
 * Ecrã da Wishlist — lista tática de jogos desejados com suporte
 * a importação CSV e enriquecimento por IA em background.
 */
@Composable
fun WishlistScreen(
    state: DashboardUiState,
    actions: DashboardActions,
    onNavigateToDetail: (GameItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSearchFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        // Barra de topo com pesquisa e botão Import
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .border(BorderStroke(1.dp, BorderSubtle))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pesquisa
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .background(SurfaceBase, RoundedCornerShape(4.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🔍", fontSize = 12.sp)
                androidx.compose.foundation.text.BasicTextField(
                    value = state.searchQuery,
                    onValueChange = { actions.onSearchQueryChange(it) },
                    textStyle = CodeSkuStyle.copy(color = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isSearchFocused = it.isFocused },
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (state.searchQuery.isEmpty() && !isSearchFocused) {
                                Text(
                                    text = TextKeys.Dashboard.SEARCH_PLACEHOLDER,
                                    style = CodeSkuStyle.copy(fontSize = 11.sp, color = StatusUnverifiedFg)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // Botão Import
            Button(
                onClick = { actions.onOpenImportDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(32.dp).defaultMinSize(minHeight = 32.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text(
                    text = TextKeys.Wishlist.IMPORT_BUTTON,
                    style = LabelFilterStyle.copy(fontSize = 12.sp),
                    color = Color.White
                )
            }
        }

        // Filtro de plataformas + contadores
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .border(BorderStroke(1.dp, BorderSubtle))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ConsolePlatform.entries.toList()) { platform ->
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
                        verticalAlignment = Alignment.CenterVertically
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.wishlistGames.size} items",
                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg
                )

                // Progresso de enriquecimento global
                state.enrichmentProgress?.let { (completed, total) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = ConsoleGamecube,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "${TextKeys.Wishlist.ENRICHMENT_BANNER}: $completed/$total",
                            style = CodeSkuStyle.copy(fontSize = 11.sp),
                            color = ConsoleGamecube
                        )
                    }
                }
            }
        }

        // Lista de jogos da Wishlist
        if (state.wishlistGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💝",
                        fontSize = 32.sp
                    )
                    Text(
                        text = TextKeys.Wishlist.EMPTY_STATE,
                        style = BodyMd,
                        color = TextSecondary
                    )
                    Button(
                        onClick = { actions.onOpenImportDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = TextKeys.Wishlist.IMPORT_BUTTON,
                            style = LabelFilterStyle,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.wishlistGames, key = { it.id }) { game ->
                    WishlistGameRow(
                        game = game,
                        isSelected = state.selectedGame?.id == game.id,
                        onClick = {
                            actions.onGameSelected(game)
                            onNavigateToDetail(game)
                        },
                        onMoveToHunting = { actions.onMoveToHunting(game) },
                        onRetryEnrichment = { actions.onRetryEnrichment(game) }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                }
            }
        }
    }
}

/**
 * Linha de jogo da Wishlist com badge de enriquecimento e ação de migração.
 */
@Composable
private fun WishlistGameRow(
    game: GameItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onMoveToHunting: () -> Unit,
    onRetryEnrichment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isSelected) SurfaceElevated else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Info do jogo
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = game.title,
                    style = BodyMd,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                PlatformBadge(platform = game.platform)
            }
            if (game.releaseYear.isNotBlank()) {
                Text(
                    text = game.releaseYear,
                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg
                )
            }
        }

        // Badge de enriquecimento
        EnrichmentBadge(status = game.enrichmentStatus)

        // Ação: mover para Hunting ou retry
        if (game.enrichmentStatus == EnrichmentStatus.FAILED) {
            Box(
                modifier = Modifier
                    .background(StatusRiskBg, RoundedCornerShape(4.dp))
                    .clickable { onRetryEnrichment() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "🔄 Retry",
                    style = LabelBadgeStyle.copy(fontSize = 10.sp),
                    color = StatusRiskFg
                )
            }
        } else if (game.enrichmentStatus == EnrichmentStatus.COMPLETE) {
            Box(
                modifier = Modifier
                    .background(StatusEditionBg, RoundedCornerShape(4.dp))
                    .clickable { onMoveToHunting() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = TextKeys.Wishlist.MOVE_TO_HUNTING,
                    style = LabelBadgeStyle.copy(fontSize = 10.sp),
                    color = StatusEditionFg
                )
            }
        }
    }
}
