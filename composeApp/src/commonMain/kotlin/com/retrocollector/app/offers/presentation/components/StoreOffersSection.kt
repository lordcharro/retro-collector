package com.retrocollector.app.offers.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.GameCondition
import com.retrocollector.app.core.domain.model.GameOffer
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.core.presentation.util.PriceFormatter

@Composable
fun StoreOffersSection(
    offers: List<GameOffer>,
    currency: String,
    onAddOfferClick: () -> Unit,
    onEditOfferClick: (GameOffer) -> Unit,
    onDeleteOfferClick: (GameOffer) -> Unit,
    onMarkAsBoughtClick: (GameOffer) -> Unit,
    modifier: Modifier = Modifier,
    isCompactLayout: Boolean = false
) {
    val activeOffers = offers.filter { !it.isArchived }
    val bestOfferId = activeOffers.minByOrNull { it.totalLandedPriceChf }?.id

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🏪", fontSize = 14.sp)
                    Text(
                        text = "STORES & LIVE OFFERS",
                        style = HeadlineSm.copy(fontSize = 13.sp),
                        color = TextPrimary
                    )
                    if (activeOffers.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(SurfaceElevated, RoundedCornerShape(4.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${activeOffers.size} AVAILABLE",
                                style = CodeSkuStyle.copy(fontSize = 10.sp),
                                color = StatusEnglishFg
                            )
                        }
                    }
                }

                Button(
                    onClick = onAddOfferClick,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConsoleGamecube,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "+ Add Offer",
                        style = LabelFilterStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (activeOffers.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBase, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🛒", fontSize = 20.sp)
                        Text(
                            text = "No active marketplace offers tracked yet",
                            style = BodySm,
                            color = TextSecondary
                        )
                        Text(
                            text = "Found this game on Ricardo, Tutti, Anibis or a Flohmarkt? Add a deal link.",
                            style = BodySm.copy(fontSize = 11.sp),
                            color = StatusUnverifiedFg
                        )
                    }
                }
            } else {
                if (isCompactLayout) {
                    // Mobile Vertical Stack
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeOffers.forEach { offer ->
                            StoreOfferCard(
                                offer = offer,
                                currency = currency,
                                isBestDeal = offer.id == bestOfferId,
                                onEditClick = { onEditOfferClick(offer) },
                                onDeleteClick = { onDeleteOfferClick(offer) },
                                onMarkAsBoughtClick = { onMarkAsBoughtClick(offer) }
                            )
                        }
                    }
                } else {
                    // Desktop Responsive Grid / Flow Layout
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeOffers.chunked(2).forEach { rowOffers ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowOffers.forEach { offer ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        StoreOfferCard(
                                            offer = offer,
                                            currency = currency,
                                            isBestDeal = offer.id == bestOfferId,
                                            onEditClick = { onEditOfferClick(offer) },
                                            onDeleteClick = { onDeleteOfferClick(offer) },
                                            onMarkAsBoughtClick = { onMarkAsBoughtClick(offer) }
                                        )
                                    }
                                }
                                if (rowOffers.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreOfferCard(
    offer: GameOffer,
    currency: String,
    isBestDeal: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMarkAsBoughtClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SurfaceElevated,
        border = BorderStroke(1.dp, if (isBestDeal) StatusEnglishFg else BorderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Source & Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                when {
                                    offer.source.contains("Ricardo", ignoreCase = true) -> StatusEditionFg.copy(alpha = 0.2f)
                                    offer.source.contains("Anibis", ignoreCase = true) -> StatusEnglishFg.copy(alpha = 0.2f)
                                    offer.source.contains("Tutti", ignoreCase = true) -> AccentBlue.copy(alpha = 0.2f)
                                    else -> SurfaceContainer
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                when {
                                    offer.source.contains("Ricardo", ignoreCase = true) -> StatusEditionFg
                                    offer.source.contains("Anibis", ignoreCase = true) -> StatusEnglishFg
                                    offer.source.contains("Tutti", ignoreCase = true) -> AccentBlue
                                    else -> BorderStrong
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = offer.source,
                            style = LabelBadgeStyle.copy(fontSize = 11.sp),
                            color = TextPrimary
                        )
                    }

                    if (isBestDeal) {
                        Box(
                            modifier = Modifier
                                .background(StatusEnglishFg, RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "TOP PICK",
                                style = LabelBadgeStyle.copy(fontSize = 9.sp),
                                color = Color.White
                            )
                        }
                    }

                    ConditionBadge(condition = offer.condition)
                }

                // Edit / Delete icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "✏️",
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { onEditClick() }
                            .padding(2.dp)
                    )
                    Text(
                        text = "🗑️",
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { onDeleteClick() }
                            .padding(2.dp)
                    )
                }
            }

            // Price & Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = PriceFormatter.format(offer.priceChf, currency),
                            style = HeadlineSm.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = StatusEnglishFg
                        )
                        if (offer.hasShipping) {
                            Text(
                                text = "+ ${PriceFormatter.format(offer.shippingChf ?: 0.0, currency)} post",
                                style = BodySm.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    if (offer.sellerOrLocation.isNotBlank()) {
                        Text(
                            text = "📍 ${offer.sellerOrLocation}",
                            style = BodySm.copy(fontSize = 11.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (offer.hasShipping) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "TOTAL LANDED", style = CodeSkuStyle.copy(fontSize = 9.sp), color = TextSecondary)
                        Text(
                            text = PriceFormatter.format(offer.totalLandedPriceChf, currency),
                            style = CodePriceStyle.copy(fontSize = 13.sp),
                            color = TextPrimary
                        )
                    }
                }
            }

            if (offer.notes.isNotBlank()) {
                Text(
                    text = offer.notes,
                    style = BodySm.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Bought Button
                Button(
                    onClick = onMarkAsBoughtClick,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusEnglishFg.copy(alpha = 0.2f),
                        contentColor = StatusEnglishFg
                    ),
                    border = BorderStroke(1.dp, StatusEnglishFg),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f).height(30.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Mark as Bought",
                            style = LabelFilterStyle.copy(fontSize = 11.sp, color = StatusEnglishFg)
                        )
                    }
                }

                // Open Link Button
                if (offer.listingUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            val url = offer.listingUrl.trim()
                            val fullUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                "https://$url"
                            } else {
                                url
                            }
                            try {
                                uriHandler.openUri(fullUrl)
                            } catch (e: Exception) {
                                println("Failed to open URI: ${e.message}")
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceContainer,
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, BorderSubtle),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(30.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🔗", fontSize = 11.sp)
                            Text(
                                text = "Open ${offer.source.substringBefore(".")} ↗",
                                style = LabelFilterStyle.copy(fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConditionBadge(condition: GameCondition, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                when (condition) {
                    GameCondition.CIB -> StatusEnglishFg.copy(alpha = 0.15f)
                    GameCondition.BOXED -> AccentBlue.copy(alpha = 0.15f)
                    GameCondition.LOOSE -> StatusRiskBg
                },
                RoundedCornerShape(3.dp)
            )
            .border(
                1.dp,
                when (condition) {
                    GameCondition.CIB -> StatusEnglishFg.copy(alpha = 0.5f)
                    GameCondition.BOXED -> AccentBlue.copy(alpha = 0.5f)
                    GameCondition.LOOSE -> StatusRiskFg.copy(alpha = 0.5f)
                },
                RoundedCornerShape(3.dp)
            )
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            text = "${condition.icon} ${condition.label}",
            style = LabelBadgeStyle.copy(fontSize = 10.sp),
            color = when (condition) {
                GameCondition.CIB -> StatusEnglishFg
                GameCondition.BOXED -> AccentBlue
                GameCondition.LOOSE -> StatusRiskFg
            }
        )
    }
}
