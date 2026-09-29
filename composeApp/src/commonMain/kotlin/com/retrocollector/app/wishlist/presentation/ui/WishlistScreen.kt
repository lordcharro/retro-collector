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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        // Barra de Ações da Wishlist: Contagem, Progresso de Enriquecimento e Botão Import
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .border(BorderStroke(1.dp, BorderSubtle))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${state.wishlistGames.size} ${TextKeys.Collection.STATS_GAMES}",
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

            // Botão Import CSV
            Button(
                onClick = { actions.onOpenImportDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(30.dp).defaultMinSize(minHeight = 30.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "${TextKeys.Wishlist.IMPORT_BUTTON} 📥",
                    style = LabelFilterStyle.copy(fontSize = 12.sp),
                    color = Color.White
                )
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
                        onRetryEnrichment = { actions.onRetryEnrichment(game) }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                }
            }
        }
    }
}

/**
 * Linha de jogo da Wishlist com badge de enriquecimento.
 */
@Composable
private fun WishlistGameRow(
    game: GameItem,
    isSelected: Boolean,
    onClick: () -> Unit,
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

        // Ação de retry caso enriquecimento tenha falhado
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
        }
    }
}
