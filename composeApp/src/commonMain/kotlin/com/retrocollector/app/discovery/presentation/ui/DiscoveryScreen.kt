package com.retrocollector.app.discovery.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.presentation.components.TacticalEmptyState
import com.retrocollector.app.core.presentation.components.TactileSearchField
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.discovery.presentation.components.DiscoveredGameCard
import org.jetbrains.compose.ui.tooling.preview.Preview

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
    modifier: Modifier = Modifier,
    currency: String = "CHF"
) {
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
                    Text(text = TextKeys.Discovery.TITLE, style = HeadlineMd, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .background(ConsoleGamecube, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = TextKeys.Discovery.BADGE_AI, style = LabelBadgeStyle.copy(fontSize = 10.sp), color = Color.White)
                    }
                }
                Text(
                    text = TextKeys.Discovery.SUBTITLE,
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
            TactileSearchField(
                query = searchQuery,
                onQueryChange = onQueryChange,
                placeholder = TextKeys.Discovery.SEARCH_PLACEHOLDER,
                onSearchSubmit = onSearchSubmit,
                searchIcon = "✨",
                minHeight = 38.dp,
                backgroundColor = SurfaceCard,
                focusedBorderColor = ConsoleGamecube,
                textStyle = BodyMd.copy(color = TextPrimary),
                placeholderStyle = BodyMd.copy(fontSize = 12.sp, color = StatusUnverifiedFg),
                modifier = Modifier.weight(1f)
            )

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
                    Text(text = TextKeys.Discovery.ACTION_RUN, style = LabelFilterStyle.copy(fontSize = 12.sp), color = Color.White)
                }
            }
        }

        // -------------------------------------------------------------
        // FILTROS DROPDOWN: GÉNERO & PLATAFORMA
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown de Género
            Box(modifier = Modifier.weight(1f)) {
                var expandedGenre by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(SurfaceCard, RoundedCornerShape(4.dp))
                        .border(
                            BorderStroke(1.dp, if (expandedGenre) ConsoleGamecube else BorderSubtle),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { expandedGenre = true }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(text = selectedGenre.icon, fontSize = 13.sp)
                        Text(
                            text = selectedGenre.displayName,
                            style = LabelFilterStyle.copy(fontSize = 12.sp),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(text = "▾", color = TextSecondary, fontSize = 12.sp)
                }

                DropdownMenu(
                    expanded = expandedGenre,
                    onDismissRequest = { expandedGenre = false },
                    modifier = Modifier
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                ) {
                    GameGenre.entries.forEach { genre ->
                        val isSelected = selectedGenre == genre
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = genre.icon, fontSize = 14.sp)
                                    Text(
                                        text = genre.displayName,
                                        style = BodyMd,
                                        color = if (isSelected) ConsoleGamecube else TextPrimary
                                    )
                                }
                            },
                            onClick = {
                                onGenreSelect(genre)
                                expandedGenre = false
                            }
                        )
                    }
                }
            }

            // Dropdown de Plataforma
            Box(modifier = Modifier.weight(1f)) {
                var expandedPlatform by remember { mutableStateOf(false) }
                val platformLabel = selectedPlatform?.displayName ?: "Todas as Consolas"
                val platformColor = when (selectedPlatform) {
                    ConsolePlatform.N64 -> ConsoleN64
                    ConsolePlatform.GAMECUBE -> ConsoleGamecube
                    ConsolePlatform.PS3 -> ConsolePS3
                    ConsolePlatform.SWITCH -> ConsoleSwitch
                    null -> StatusUnverifiedFg
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(SurfaceCard, RoundedCornerShape(4.dp))
                        .border(
                            BorderStroke(1.dp, if (expandedPlatform) ConsoleGamecube else BorderSubtle),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { expandedPlatform = true }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(modifier = Modifier.size(7.dp).background(platformColor, RoundedCornerShape(3.5.dp)))
                        Text(
                            text = platformLabel,
                            style = LabelFilterStyle.copy(fontSize = 12.sp),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(text = "▾", color = TextSecondary, fontSize = 12.sp)
                }

                DropdownMenu(
                    expanded = expandedPlatform,
                    onDismissRequest = { expandedPlatform = false },
                    modifier = Modifier
                        .background(SurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.size(7.dp).background(StatusUnverifiedFg, RoundedCornerShape(3.5.dp)))
                                Text(
                                    text = TextKeys.Discovery.ALL_PLATFORMS,
                                    style = BodyMd,
                                    color = if (selectedPlatform == null) ConsoleGamecube else TextPrimary
                                )
                            }
                        },
                        onClick = {
                            onPlatformSelect(null)
                            expandedPlatform = false
                        }
                    )

                    ConsolePlatform.entries.forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        val pColor = when (platform) {
                            ConsolePlatform.N64 -> ConsoleN64
                            ConsolePlatform.GAMECUBE -> ConsoleGamecube
                            ConsolePlatform.PS3 -> ConsolePS3
                            ConsolePlatform.SWITCH -> ConsoleSwitch
                        }
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(modifier = Modifier.size(7.dp).background(pColor, RoundedCornerShape(3.5.dp)))
                                    Text(
                                        text = platform.displayName,
                                        style = BodyMd,
                                        color = if (isSelected) ConsoleGamecube else TextPrimary
                                    )
                                }
                            },
                            onClick = {
                                onPlatformSelect(platform)
                                expandedPlatform = false
                            }
                        )
                    }
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
                        text = TextKeys.Discovery.LOADING_RADAR,
                        style = BodyMd,
                        color = StatusUnverifiedFg
                    )
                }
            }
        } else if (discoveredGames.isEmpty()) {
            TacticalEmptyState(
                icon = "🧭",
                title = TextKeys.Discovery.EMPTY_TITLE,
                subtitle = TextKeys.Discovery.EMPTY_SUBTITLE,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(discoveredGames, key = { it.id }) { game ->
                    DiscoveredGameCard(
                        game = game,
                        onOpenDossier = onOpenDossier,
                        onAddToWishlist = onAddToWishlist,
                        currency = currency
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DiscoveryScreenPreview() {
    RetroTactileTheme {
        val sampleDiscovered = listOf(
            DiscoveredGameItem(
                id = "disc_fzero_gx",
                title = "F-Zero GX",
                franchiseName = "F-Zero",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2003",
                genreDisplayName = "Corrida Futurista",
                genreTags = listOf("Racing", "Sci-Fi"),
                recommendationReason = "Um dos jogos de corrida arcade mais rápidos e difíceis de sempre, desenvolvido pela Amusement Vision (Sega).",
                languageStatus = LanguageStatus.FULL_ENGLISH,
                estimatedPriceChf = 65.0
            ),
            DiscoveredGameItem(
                id = "disc_perfect_dark",
                title = "Perfect Dark",
                franchiseName = "Perfect Dark",
                platform = ConsolePlatform.N64,
                releaseYear = "2000",
                genreDisplayName = "FPS de Espionagem",
                genreTags = listOf("FPS", "Action"),
                recommendationReason = "Sucessor espiritual de GoldenEye 007 com bots avançados e iluminação dinâmica impressionante no N64 Expansion Pak.",
                languageStatus = LanguageStatus.FULL_ENGLISH,
                estimatedPriceChf = 35.0
            )
        )
        DiscoveryScreen(
            discoveredGames = sampleDiscovered,
            selectedGenre = GameGenre.ALL,
            selectedPlatform = null,
            isDiscovering = false,
            searchQuery = "",
            onQueryChange = {},
            onSearchSubmit = {},
            onGenreSelect = {},
            onPlatformSelect = {},
            onOpenDossier = {},
            onAddToWishlist = {}
        )
    }
}
