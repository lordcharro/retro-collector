package com.retrocollector.app.offers.presentation.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrocollector.app.core.domain.model.GameCondition
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.model.GameOffer
import com.retrocollector.app.core.presentation.components.PlatformBadge
import com.retrocollector.app.core.presentation.components.TactileTextField
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.core.presentation.util.PriceFormatter

@Composable
fun AcquisitionModal(
    game: GameItem,
    selectedOffer: GameOffer? = null,
    currency: String = "CHF",
    onConfirm: (finalPriceChf: Double, condition: GameCondition, offerId: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialPrice = selectedOffer?.totalLandedPriceChf
        ?: game.bestOffer?.totalLandedPriceChf
        ?: game.askingPriceChf
        ?: game.targetPriceChf
        ?: 0.0

    var priceStr by remember { mutableStateOf(if (initialPrice > 0.0) initialPrice.toString() else "") }
    var condition by remember {
        mutableStateOf(selectedOffer?.condition ?: game.acquiredCondition ?: GameCondition.CIB)
    }

    val sourceLabel = selectedOffer?.source ?: game.spottedLocation.ifBlank { "Direct Acquisition" }
    val targetPrice = game.targetPriceChf ?: game.marketRadar?.medianPriceChf
    val currentPriceNum = priceStr.toDoubleOrNull() ?: 0.0
    val savings = if (targetPrice != null && targetPrice > currentPriceNum && currentPriceNum > 0.0) {
        targetPrice - currentPriceNum
    } else null

    val competingOffersCount = game.activeOffers.count { it.id != selectedOffer?.id }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderStrong),
            modifier = modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.94f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Icon & Title
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(StatusEnglishFg.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                            .border(1.dp, StatusEnglishFg, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✓", fontSize = 20.sp, color = StatusEnglishFg, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Add to Collection",
                        style = HeadlineMd.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Convert active deal into an owned collection item",
                        style = BodySm.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }

                // Game Card Summary
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                PlatformBadge(platform = game.platform)
                                if (!game.productCode.isNullOrBlank()) {
                                    Text(
                                        text = game.productCode,
                                        style = CodeSkuStyle.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(SurfaceBase, RoundedCornerShape(4.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sourceLabel,
                                    style = LabelBadgeStyle.copy(fontSize = 10.sp),
                                    color = StatusEnglishFg
                                )
                            }
                        }

                        Text(
                            text = game.title,
                            style = HeadlineSm.copy(fontSize = 14.sp),
                            color = TextPrimary
                        )

                        if (savings != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StatusEnglishFg.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, StatusEnglishFg.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "💵", fontSize = 12.sp)
                                    Text(
                                        text = "ADVANTAGE UNLOCKED: Saved ${PriceFormatter.format(savings, currency)} under target price",
                                        style = CodeSkuStyle.copy(fontSize = 10.sp),
                                        color = StatusEnglishFg
                                    )
                                }
                            }
                        }
                    }
                }

                // Final Purchase Price Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FINAL PURCHASE PRICE ($currency) *",
                            style = LabelFilterStyle,
                            color = TextPrimary
                        )
                        Text(
                            text = "Negotiated?",
                            style = LabelFilterStyle.copy(fontSize = 10.sp, color = AccentBlue)
                        )
                    }
                    TactileTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        placeholder = "e.g. 28.00",
                        textStyle = CodePriceStyle.copy(fontSize = 16.sp, color = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Pre-filled from $sourceLabel. Edit if actual cash outlay differed.",
                        style = BodySm.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }

                // Condition & Completeness Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "CONDITION & COMPLETENESS", style = LabelFilterStyle, color = TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GameCondition.entries.forEach { cond ->
                            val isSelected = condition == cond
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) ConsoleGamecube.copy(alpha = 0.2f) else SurfaceElevated,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) ConsoleGamecube else BorderSubtle,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { condition = cond }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cond.icon} ${cond.label}",
                                    style = LabelFilterStyle.copy(
                                        fontSize = 11.sp,
                                        color = if (isSelected) TextPrimary else TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                // Competing offers notice
                if (competingOffersCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBase, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🗄️", fontSize = 12.sp)
                        Text(
                            text = "Other $competingOffersCount competing listings will be automatically archived into your deal history.",
                            style = BodySm.copy(fontSize = 11.sp),
                            color = StatusUnverifiedFg
                        )
                    }
                }

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            val finalPrice = priceStr.toDoubleOrNull() ?: 0.0
                            onConfirm(finalPrice, condition, selectedOffer?.id)
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusEnglishFg,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "✓", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Confirm & Move to Owned",
                                style = HeadlineSm.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevated,
                            contentColor = TextSecondary
                        ),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text(
                            text = "Cancel & Keep as Active Deal",
                            style = LabelFilterStyle.copy(fontSize = 12.sp, color = TextSecondary)
                        )
                    }
                }
            }
        }
    }
}
