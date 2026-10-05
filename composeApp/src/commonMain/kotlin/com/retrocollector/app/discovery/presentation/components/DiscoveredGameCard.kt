package com.retrocollector.app.discovery.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
fun DiscoveredGameCard(
    game: DiscoveredGameItem,
    onOpenDossier: (DiscoveredGameItem) -> Unit,
    onAddToWishlist: (DiscoveredGameItem) -> Unit,
    modifier: Modifier = Modifier,
    currency: String = "CHF"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(6.dp))
            .clickable { onOpenDossier(game) }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Console, Year, and Language / Collection Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PlatformBadge(platform = game.platform)
                if (game.releaseYear.isNotBlank()) {
                    Text(
                        text = game.releaseYear,
                        style = CodeSkuStyle.copy(fontSize = 11.sp),
                        color = StatusUnverifiedFg
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (game.isAlreadyInCollection) {
                    Box(
                        modifier = Modifier
                            .background(StatusEnglishBg, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.discovery_badge_in_collection),
                            style = LabelBadgeStyle.copy(fontSize = 10.sp),
                            color = StatusEnglishFg
                        )
                    }
                } else if (game.isAlreadyInWishlist) {
                    Box(
                        modifier = Modifier
                            .background(StatusEditionBg, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.discovery_badge_in_wishlist),
                            style = LabelBadgeStyle.copy(fontSize = 10.sp),
                            color = StatusEditionFg
                        )
                    }
                }
                LanguageRiskBadge(status = game.languageStatus)
            }
        }

        // Row 2: Game Title
        Column {
            Text(
                text = game.title,
                style = HeadlineSm,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (game.franchiseName.isNotBlank() && game.franchiseName != game.title) {
                Text(
                    text = game.franchiseName,
                    style = BodySm,
                    color = TextSecondary
                )
            }
        }

        // Row 3: Modern Port / Remaster Tag
        if (game.hasModernPortOrRemaster || !game.modernPortDetails.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevated, RoundedCornerShape(4.dp))
                    .border(1.dp, BorderStrong, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(text = "✨", fontSize = 11.sp)
                    Text(
                        text = game.modernPortDetails ?: "Modern Port / Remaster Available",
                        style = CodeSkuStyle.copy(fontSize = 11.sp),
                        color = AccentBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Row 4: Rationale / Analytical Summary
        if (game.recommendationReason.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = game.recommendationReason,
                    style = BodyMd.copy(fontSize = 12.sp),
                    color = TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Row 5: Estimated Price and Tactical Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.discovery_est_market_price),
                    style = LabelBadgeStyle.copy(fontSize = 9.sp),
                    color = StatusUnverifiedFg
                )
                Text(
                    text = PriceFormatter.format(game.estimatedPriceChf, currency),
                    style = CodePriceStyle.copy(fontSize = 13.sp),
                    color = TextPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { onOpenDossier(game) },
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, BorderStrong),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(text = stringResource(Res.string.discovery_action_dossier), style = LabelFilterStyle.copy(fontSize = 11.sp), color = TextPrimary)
                }

                if (!game.isAlreadyInWishlist && !game.isAlreadyInCollection) {
                    Button(
                        onClick = { onAddToWishlist(game) },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(text = stringResource(Res.string.discovery_action_add_wishlist), style = LabelFilterStyle.copy(fontSize = 11.sp), color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DiscoveredGameCardPreview() {
    RetroTactileTheme {
        DiscoveredGameCard(
            game = DiscoveredGameItem(
                id = "disc_fzero_gx",
                title = "F-Zero GX",
                franchiseName = "F-Zero",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2003",
                genreDisplayName = "Futuristic Racing",
                genreTags = listOf("Racing", "Sci-Fi"),
                recommendationReason = "One of the fastest and most challenging arcade racing games ever made, developed by Sega.",
                languageStatus = LanguageStatus.FULL_ENGLISH,
                estimatedPriceChf = 65.0
            ),
            onOpenDossier = {},
            onAddToWishlist = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

