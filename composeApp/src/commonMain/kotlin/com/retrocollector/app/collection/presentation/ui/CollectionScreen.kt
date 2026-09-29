package com.retrocollector.app.collection.presentation.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.collection.presentation.components.CollectionStatsBar
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.presentation.components.GameListItemRow
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.dashboard.presentation.ui.DashboardActions
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardUiState

/**
 * Ecrã da coleção pessoal — lista tática de jogos OWNED
 * com barra de estatísticas (contagem + valor total em CHF).
 */
@Composable
fun CollectionScreen(
    state: DashboardUiState,
    actions: DashboardActions,
    onNavigateToDetail: (GameItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currency = remember(state.settings) {
        state.settings.defaultCurrency.ifBlank { "CHF" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        // Barra de estatísticas da coleção
        CollectionStatsBar(
            games = state.collectionGames,
            currency = currency
        )

        // Lista de jogos da coleção
        if (state.collectionGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "📦", fontSize = 32.sp)
                    Text(
                        text = TextKeys.Collection.EMPTY_STATE,
                        style = BodyMd,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.collectionGames, key = { it.id }) { game ->
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
