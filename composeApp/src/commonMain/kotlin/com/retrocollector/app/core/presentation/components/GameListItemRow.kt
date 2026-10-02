package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.core.presentation.util.PriceFormatter
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GameListItemRow(
    game: GameItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currency: String = "CHF",
    trailingContent: (@Composable () -> Unit)? = null
) {
    val platformColor = androidx.compose.ui.graphics.Color(game.platform.brandColorHex)

    val rowBg = if (isSelected) SurfaceElevated else SurfaceCard

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .background(rowBg)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "${game.title}, Platform ${game.platform.displayName}, ${game.languageStatus.label}"
                stateDescription = if (isSelected) "Selected" else "Not selected"
                onClick(label = "Open dossier for ${game.title}") {
                    onClick()
                    true
                }
            }
            .clickable(onClick = onClick)
    ) {
        // Lateral active selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .align(Alignment.CenterStart)
                    .background(platformColor)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Title, SKU, and Location
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = game.title,
                        style = HeadlineSm,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (!game.productCode.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(SurfaceContainer, RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = game.productCode,
                                style = CodeSkuStyle.copy(fontSize = 11.sp),
                                color = AccentBlue,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val locationText = if (game.activeOffers.isNotEmpty() && game.collectionStatus == com.retrocollector.app.core.domain.model.CollectionStatus.WISHLIST) {
                        val count = game.activeOffers.size
                        if (count > 1) {
                            "• $count deals (Top: ${game.bestOffer?.source})"
                        } else {
                            "• ${game.bestOffer?.source}"
                        }
                    } else if (game.spottedLocation.isNotBlank()) {
                        "• ${game.spottedLocation}"
                    } else null

                    if (locationText != null) {
                        Text(
                            text = locationText,
                            style = BodySm,
                            color = if (game.activeOffers.isNotEmpty() && game.collectionStatus == com.retrocollector.app.core.domain.model.CollectionStatus.WISHLIST) StatusEnglishFg else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (game.releaseYear.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(SurfaceContainerHigh, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = game.releaseYear,
                                style = CodeSkuStyle.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Right Column: Custom slot content or standard price and Language Risk Badge
            if (trailingContent != null) {
                trailingContent()
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val displayPrice = if (game.collectionStatus == com.retrocollector.app.core.domain.model.CollectionStatus.WISHLIST && game.activeOffers.isNotEmpty()) {
                        game.bestOffer?.totalLandedPriceChf
                    } else {
                        game.paidPriceChf ?: game.askingPriceChf ?: game.targetPriceChf
                    }

                    Text(
                        text = PriceFormatter.format(displayPrice, currency),
                        style = CodePriceStyle,
                        color = if (game.activeOffers.isNotEmpty() && game.collectionStatus == com.retrocollector.app.core.domain.model.CollectionStatus.WISHLIST) StatusEnglishFg else TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )

                    LanguageRiskBadge(status = game.languageStatus)
                }
            }
        }
    }
}

@Preview
@Composable
fun GameListItemRowPreview() {
    RetroTactileTheme {
        GameListItemRow(
            game = GameItem(
                id = "gc_re4",
                title = "Resident Evil 4",
                franchiseName = "Resident Evil",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2005",
                productCode = "DOL-P-G4BE",
                spottedLocation = "Brockenhaus Bern",
                askingPriceChf = 35.0,
                languageStatus = LanguageStatus.SUBS_ONLY
            ),
            isSelected = true,
            onClick = {}
        )
    }
}

