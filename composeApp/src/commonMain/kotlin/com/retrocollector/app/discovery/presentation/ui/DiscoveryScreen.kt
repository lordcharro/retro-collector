package com.retrocollector.app.discovery.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.discovery.presentation.components.DiscoveredGameCard
import com.retrocollector.app.discovery.presentation.components.GenreFilterBar

@Suppress("LongParameterList")
@Composable
fun DiscoveryScreen(
    discoveredGames: List<DiscoveredGameItem>,
    selectedGenre: GameGenre,
    selectedPlatform: ConsolePlatform?,
    isDiscovering: Boolean,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onGenreSelect: (GameGenre) -> Unit,
    onPlatformSelect: (ConsolePlatform?) -> Unit,
    onOpenDossier: (DiscoveredGameItem) -> Unit,
    onAddToWishlist: (DiscoveredGameItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // -------------------------------------------------------------
        // CABEÇALHO & BARRA DE PROMPT IA
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "DISCOVERY RADAR", style = HeadlineMd, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .background(ConsoleGamecube, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "GEMINI AI", style = LabelBadgeStyle.copy(fontSize = 10.sp), color = Color.White)
                    }
                }
                Text(
                    text = "Explora pérolas PAL, recomendações por género e edições seguras europeias",
                    style = BodySm,
                    color = TextSecondary
                )
            }
        }

        // Barra de Pesquisa com IA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .background(SurfaceCard, RoundedCornerShape(4.dp))
                    .border(BorderStroke(1.dp, if (isSearchFocused) ConsoleGamecube else BorderSubtle), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "✨", fontSize = 13.sp)
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    textStyle = BodyMd.copy(color = TextPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isSearchFocused = it.isFocused },
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty() && !isSearchFocused) {
                                Text(
                                    text = "Pergunta à IA: ex: 'jogos do género do monkey island' ou 'FPS táticos no PS3'...",
                                    style = BodyMd.copy(fontSize = 12.sp, color = StatusUnverifiedFg)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Button(
                onClick = { onSearchSubmit(searchQuery) },
                enabled = !isDiscovering,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                modifier = Modifier.height(38.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
            ) {
                if (isDiscovering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "RUN 🚀", style = LabelFilterStyle.copy(fontSize = 12.sp), color = Color.White)
                }
            }
        }

        // -------------------------------------------------------------
        // CHIPS DE GÉNERO
        // -------------------------------------------------------------
        GenreFilterBar(
            selectedGenre = selectedGenre,
            onGenreSelected = onGenreSelect
        )

        // -------------------------------------------------------------
        // FILTRO DE CONSOLAS
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val allConsoles = listOf<ConsolePlatform?>(null) + ConsolePlatform.entries
            allConsoles.forEach { platform ->
                val isSelected = selectedPlatform == platform
                val label = platform?.displayName ?: "Todas as Consolas"
                val tintColor = when (platform) {
                    ConsolePlatform.N64 -> ConsoleN64
                    ConsolePlatform.GAMECUBE -> ConsoleGamecube
                    ConsolePlatform.PS3 -> ConsolePS3
                    ConsolePlatform.SWITCH -> ConsoleSwitch
                    null -> StatusUnverifiedFg
                }

                Box(
                    modifier = Modifier
                        .background(if (isSelected) SurfaceElevated else SurfaceCard, RoundedCornerShape(3.dp))
                        .border(
                            BorderStroke(1.dp, if (isSelected) tintColor else BorderSubtle),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable { onPlatformSelect(platform) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        style = LabelFilterStyle.copy(fontSize = 11.sp),
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // LISTA DE JOGOS DESCOBERTOS
        // -------------------------------------------------------------
        if (isDiscovering) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(color = ConsoleGamecube, strokeWidth = 3.dp)
                    Text(
                        text = "A consultar o arquivo de retrogaming PAL do Gemini...",
                        style = BodyMd,
                        color = StatusUnverifiedFg
                    )
                }
            }
        } else if (discoveredGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🧭", fontSize = 32.sp)
                    Text(text = "Nenhum jogo encontrado para estes critérios.", style = HeadlineSm, color = TextPrimary)
                    Text(
                        text = "Tenta selecionar outro género ou pesquisar por termos diferentes.",
                        style = BodyMd,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(discoveredGames) { game ->
                    DiscoveredGameCard(
                        game = game,
                        onOpenDossier = onOpenDossier,
                        onAddToWishlist = onAddToWishlist
                    )
                }
            }
        }
    }
}
