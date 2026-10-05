package com.retrocollector.app.wishlist.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.presentation.components.EnrichmentBadge
import com.retrocollector.app.core.presentation.components.GameListItemRow
import com.retrocollector.app.core.presentation.components.TacticalEmptyState
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.resources.stringResource
import com.retrocollector.app.wishlist.presentation.viewmodel.WishlistUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Wishlist Screen — tactical list of desired games with support
 * for CSV import and background AI enrichment.
 */
@Composable
fun WishlistScreen(
    state: WishlistUiState,
    selectedGameId: String? = null,
    currency: String = "CHF",
    onGameSelected: (GameItem) -> Unit = {},
    onOpenImportDialog: () -> Unit = {},
    onRetryEnrichment: (GameItem) -> Unit = {},
    onNavigateToDetail: (GameItem) -> Unit = {},
    onClearSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        // Wishlist Actions Bar: Item Count, Enrichment Progress, and Import Button
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
                val countText = if (state.searchQuery.isNotBlank()) {
                    "${state.wishlistGames.size}/${state.allWishlistCount} ${stringResource(Res.string.collection_stats_games)}"
                } else {
                    "${state.allWishlistCount} ${stringResource(Res.string.collection_stats_games)}"
                }
                Text(
                    text = countText,
                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg
                )

                // Global enrichment progress
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
                            text = "${stringResource(Res.string.wishlist_enrichment_banner)}: $completed/$total",
                            style = CodeSkuStyle.copy(fontSize = 11.sp),
                            color = ConsoleGamecube
                        )
                    }
                }
            }

            // CSV Import Button
            Button(
                onClick = onOpenImportDialog,
                colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(30.dp).defaultMinSize(minHeight = 30.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "${stringResource(Res.string.wishlist_import_button)} 📥",
                    style = LabelFilterStyle.copy(fontSize = 12.sp),
                    color = Color.White
                )
            }
        }

        // Wishlist Games List
        if (state.allWishlistCount == 0) {
            TacticalEmptyState(
                icon = "💝",
                title = stringResource(Res.string.wishlist_empty_state),
                actionLabel = stringResource(Res.string.wishlist_import_button),
                onActionClick = onOpenImportDialog,
                modifier = Modifier.fillMaxSize()
            )
        } else if (state.wishlistGames.isEmpty()) {
            TacticalEmptyState(
                icon = "🔍",
                title = stringResource(Res.string.wishlist_empty_search_title),
                subtitle = stringResource(Res.string.wishlist_empty_search_subtitle),
                actionLabel = stringResource(Res.string.wishlist_clear_search),
                onActionClick = onClearSearch,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.wishlistGames, key = { it.id }) { game ->
                    GameListItemRow(
                        game = game,
                        isSelected = selectedGameId == game.id,
                        currency = currency,
                        onClick = {
                            onGameSelected(game)
                            onNavigateToDetail(game)
                        },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                EnrichmentBadge(status = game.enrichmentStatus)
                                if (game.enrichmentStatus == EnrichmentStatus.FAILED) {
                                    Box(
                                        modifier = Modifier
                                            .background(StatusRiskBg, RoundedCornerShape(4.dp))
                                            .clickable { onRetryEnrichment(game) }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "🔄 " + stringResource(Res.string.common_retry),
                                            style = LabelBadgeStyle.copy(fontSize = 10.sp),
                                            color = StatusRiskFg
                                        )
                                    }
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                }
            }
        }
    }
}

@Preview
@Composable
fun WishlistScreenPreview() {
    RetroTactileTheme {
        val sampleWishlist = listOf(
            GameItem(
                id = "n64_zelda_oot",
                title = "The Legend of Zelda: Ocarina of Time",
                franchiseName = "Zelda",
                platform = ConsolePlatform.N64,
                releaseYear = "1998",
                productCode = "NUS-CZLE-EUR",
                collectionStatus = CollectionStatus.WISHLIST,
                enrichmentStatus = EnrichmentStatus.COMPLETE
            ),
            GameItem(
                id = "gc_metroid_prime",
                title = "Metroid Prime",
                franchiseName = "Metroid",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2002",
                productCode = "DOL-GM8E-USA",
                collectionStatus = CollectionStatus.WISHLIST,
                enrichmentStatus = EnrichmentStatus.PENDING
            ),
            GameItem(
                id = "ps3_demons_souls",
                title = "Demon's Souls",
                franchiseName = "Souls",
                platform = ConsolePlatform.PS3,
                releaseYear = "2009",
                productCode = "BLES-00932",
                collectionStatus = CollectionStatus.WISHLIST,
                enrichmentStatus = EnrichmentStatus.FAILED
            )
        )
        WishlistScreen(
            state = WishlistUiState(
                wishlistGames = sampleWishlist,
                enrichmentProgress = Pair(1, 3)
            ),
            selectedGameId = sampleWishlist.first().id
        )
    }
}
