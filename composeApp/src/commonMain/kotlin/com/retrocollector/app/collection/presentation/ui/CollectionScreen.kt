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
    var isSearchFocused by remember { mutableStateOf(false) }
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

        // Barra de pesquisa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .border(BorderStroke(1.dp, BorderSubtle))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        }

        // Filtro de plataformas
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

            Text(
                text = "${state.collectionGames.size} items",
                style = CodeSkuStyle.copy(fontSize = 11.sp),
                color = StatusUnverifiedFg
            )
        }

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
