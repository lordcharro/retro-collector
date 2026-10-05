package com.retrocollector.app.discovery.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.presentation.components.LanguageRiskBadge
import com.retrocollector.app.core.presentation.components.PlatformBadge
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.core.presentation.util.PriceFormatter
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SimilarGamesShelf(
    similarGames: List<DiscoveredGameItem>,
    isLoading: Boolean,
    onSelectGame: (DiscoveredGameItem) -> Unit,
    onAddToWishlist: (DiscoveredGameItem) -> Unit,
    modifier: Modifier = Modifier,
    currency: String = "CHF",
    onRefreshSimilarGames: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                Text(text = "🎯", fontSize = 13.sp)
                Text(
                    text = stringResource(Res.string.discovery_shelf_title),
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = ConsoleGamecube,
                        strokeWidth = 2.dp
                    )
                } else {
                    if (onRefreshSimilarGames != null) {
                        Box(
                            modifier = Modifier
                                .background(SurfaceElevated, RoundedCornerShape(3.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(3.dp))
                                .clickable { onRefreshSimilarGames() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.discovery_shelf_ai_scan),
                                style = LabelBadgeStyle.copy(fontSize = 10.sp),
                                color = ConsoleGamecube
                            )
                        }
                    }
                    Text(
                        text = "${similarGames.size} ${stringResource(Res.string.discovery_shelf_suggestions)}",
                        style = CodeSkuStyle.copy(fontSize = 10.sp),
                        color = StatusUnverifiedFg
                    )
                }
            }
        }

        if (similarGames.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.discovery_shelf_empty),
                    style = BodySm,
                    color = TextSecondary
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(similarGames, key = { it.id }) { simGame ->
                    Column(
                        modifier = Modifier
                            .width(180.dp)
                            .background(SurfaceContainer, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderStrong, RoundedCornerShape(4.dp))
                            .clickable { onSelectGame(simGame) }
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlatformBadge(platform = simGame.platform)
                            LanguageRiskBadge(status = simGame.languageStatus)
                        }

                        Text(
                            text = simGame.title,
                            style = HeadlineSm.copy(fontSize = 13.sp),
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!simGame.modernPortDetails.isNullOrBlank()) {
                            Text(
                                text = "✨ " + stringResource(Res.string.discovery_switch_port),
                                style = CodeSkuStyle.copy(fontSize = 9.sp),
                                color = AccentBlue
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = PriceFormatter.format(simGame.estimatedPriceChf, currency),
                                style = CodePriceStyle.copy(fontSize = 11.sp),
                                color = TextPrimary
                            )

                            if (!simGame.isAlreadyInWishlist && !simGame.isAlreadyInCollection) {
                                Box(
                                    modifier = Modifier
                                        .background(ConsoleGamecube, RoundedCornerShape(3.dp))
                                        .clickable { onAddToWishlist(simGame) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.discovery_action_add_wishlist),
                                        style = LabelBadgeStyle.copy(fontSize = 9.sp),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SimilarGamesShelfPreview() {
    RetroTactileTheme {
        SimilarGamesShelf(
            similarGames = listOf(
                DiscoveredGameItem(
                    id = "sim_1",
                    title = "Silent Hill 2",
                    platform = ConsolePlatform.PS3,
                    estimatedPriceChf = 40.0,
                    languageStatus = LanguageStatus.FULL_ENGLISH
                ),
                DiscoveredGameItem(
                    id = "sim_2",
                    title = "Dead Space",
                    platform = ConsolePlatform.PS3,
                    estimatedPriceChf = 25.0,
                    languageStatus = LanguageStatus.FULL_ENGLISH
                )
            ),
            isLoading = false,
            onSelectGame = {},
            onAddToWishlist = {}
        )
    }
}

