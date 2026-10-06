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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.domain.model.PlatformEcosystem
import com.retrocollector.app.core.presentation.components.TacticalEmptyState
import com.retrocollector.app.core.presentation.components.TactileSearchField
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.discovery.presentation.components.DiscoveredGameCard
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

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
    currency: String = "CHF",
    isAutoDiscoveryEnabled: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // -------------------------------------------------------------
        // HEADER & AI PROMPT BAR
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
                    Text(text = stringResource(Res.string.discovery_title), style = HeadlineMd, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .background(if (isAutoDiscoveryEnabled) ConsoleGamecube else SurfaceElevated, RoundedCornerShape(3.dp))
                            .border(1.dp, if (isAutoDiscoveryEnabled) BorderStrong else BorderSubtle, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAutoDiscoveryEnabled) stringResource(Res.string.discovery_badge_ai) else stringResource(Res.string.discovery_badge_manual_ai),
                            style = LabelBadgeStyle.copy(fontSize = 10.sp),
                            color = if (isAutoDiscoveryEnabled) Color.White else StatusEnglishFg
                        )
                    }
                }
                Text(
                    text = stringResource(Res.string.discovery_subtitle),
                    style = BodySm,
                    color = TextSecondary
                )
            }
        }

        // AI Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileSearchField(
                query = searchQuery,
                onQueryChange = onQueryChange,
                placeholder = stringResource(Res.string.discovery_search_placeholder),
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
                    Text(text = stringResource(Res.string.discovery_action_run), style = LabelFilterStyle.copy(fontSize = 12.sp), color = Color.White)
                }
            }
        }

        // -------------------------------------------------------------
        // DROPDOWN FILTERS: GENRE & PLATFORM
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Genre Dropdown
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

            // Platform Dropdown
            Box(modifier = Modifier.weight(1f)) {
                var expandedPlatform by remember { mutableStateOf(false) }
                val defaultPlatformLabel = stringResource(Res.string.discovery_all_consoles)
                val platformLabel = selectedPlatform?.displayName ?: defaultPlatformLabel
                val platformColor = selectedPlatform?.let { Color(it.brandColorHex) } ?: StatusUnverifiedFg

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
                        .widthIn(min = 240.dp)
                        .heightIn(max = 420.dp)
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
                                    text = stringResource(Res.string.discovery_all_platforms),
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

                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

                    PlatformEcosystem.entries.forEach { eco ->
                        val ecoPlatforms = remember(eco) {
                            ConsolePlatform.entries.filter { it.ecosystem == eco }
                        }
                        val ecoColor = Color(eco.brandColorHex)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceCard)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = eco.icon, fontSize = 11.sp)
                            Text(
                                text = eco.displayName.uppercase(),
                                style = LabelFilterStyle.copy(fontSize = 10.sp),
                                color = ecoColor
                            )
                        }

                        ecoPlatforms.forEach { platform ->
                            val isSelected = selectedPlatform == platform
                            val pColor = Color(platform.brandColorHex)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(modifier = Modifier.size(6.dp).background(pColor, RoundedCornerShape(3.dp)))
                                        Text(
                                            text = platform.displayName,
                                            style = BodyMd.copy(fontSize = 12.sp),
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
        }

        // -------------------------------------------------------------
        // DISCOVERED GAMES LIST
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
                        text = stringResource(Res.string.discovery_loading_radar),
                        style = BodyMd,
                        color = StatusUnverifiedFg
                    )
                }
            }
        } else if (discoveredGames.isEmpty()) {
            TacticalEmptyState(
                icon = "🧭",
                title = stringResource(Res.string.discovery_empty_title),
                subtitle = stringResource(Res.string.discovery_empty_subtitle),
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
                genreDisplayName = "Futuristic Racing",
                genreTags = listOf("Racing", "Sci-Fi"),
                recommendationReason = "One of the fastest and most challenging arcade racers of all time, developed by Amusement Vision (Sega).",
                languageStatus = LanguageStatus.FULL_ENGLISH,
                estimatedPriceChf = 65.0
            ),
            DiscoveredGameItem(
                id = "disc_perfect_dark",
                title = "Perfect Dark",
                franchiseName = "Perfect Dark",
                platform = ConsolePlatform.N64,
                releaseYear = "2000",
                genreDisplayName = "Tactical Espionage FPS",
                genreTags = listOf("FPS", "Action"),
                recommendationReason = "Spiritual successor to GoldenEye 007 with advanced combat bots and impressive lighting on the N64 Expansion Pak.",
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
