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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrocollector.app.core.domain.model.GameCondition
import com.retrocollector.app.core.domain.model.GameOffer
import com.retrocollector.app.core.presentation.components.TactileTextField
import com.retrocollector.app.core.presentation.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditOfferDialog(
    initialOffer: GameOffer? = null,
    currency: String = "CHF",
    onSave: (GameOffer) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = initialOffer != null
    val defaultSource = initialOffer?.source ?: "Ricardo.ch"
    val isDefaultPreset = defaultSource in GameOffer.PRESET_SOURCES.filter { it != "Other" }

    var selectedSourcePreset by remember {
        mutableStateOf(if (isDefaultPreset) defaultSource else "Other")
    }
    var customSourceText by remember {
        mutableStateOf(if (!isDefaultPreset) defaultSource else "")
    }

    var priceStr by remember { mutableStateOf(initialOffer?.priceChf?.toString() ?: "") }
    var shippingStr by remember { mutableStateOf(initialOffer?.shippingChf?.toString() ?: "") }
    var listingUrl by remember { mutableStateOf(initialOffer?.listingUrl ?: "") }
    var condition by remember { mutableStateOf(initialOffer?.condition ?: GameCondition.CIB) }
    var sellerOrLocation by remember { mutableStateOf(initialOffer?.sellerOrLocation ?: "") }
    var notes by remember { mutableStateOf(initialOffer?.notes ?: "") }

    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    val effectiveSource = if (selectedSourcePreset == "Other") {
        customSourceText.trim().ifBlank { "Other" }
    } else {
        selectedSourcePreset
    }

    val isValid = priceStr.toDoubleOrNull() != null && priceStr.toDouble() >= 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderStrong),
            modifier = modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Text(text = if (isEdit) "✏️" else "➕", fontSize = 16.sp)
                        Text(
                            text = if (isEdit) "EDIT STORE OFFER" else "ADD STORE OFFER",
                            style = HeadlineMd,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "✕",
                        color = StatusUnverifiedFg,
                        style = HeadlineSm,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                // Source Preset Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "MARKETPLACE / SOURCE", style = LabelFilterStyle, color = TextPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GameOffer.PRESET_SOURCES.forEach { preset ->
                            val isSelected = selectedSourcePreset == preset
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) SurfaceElevated else SurfaceBase, RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSelected) AccentBlue else BorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable {
                                        selectedSourcePreset = preset
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = preset,
                                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }

                    // Dynamic "Other" text input with max 20 chars counter
                    if (selectedSourcePreset == "Other") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "CUSTOM SOURCE NAME",
                                    style = LabelFilterStyle.copy(fontSize = 10.sp),
                                    color = AccentBlue
                                )
                                Text(
                                    text = "[ ${customSourceText.length} / 20 ]",
                                    style = CodeSkuStyle.copy(fontSize = 10.sp),
                                    color = if (customSourceText.length >= 20) StatusRiskFg else TextSecondary
                                )
                            }
                            TactileTextField(
                                value = customSourceText,
                                onValueChange = {
                                    if (it.length <= 20) {
                                        customSourceText = it
                                    }
                                },
                                placeholder = "e.g. Basel Flohmarkt, Friend",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Price & Shipping Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "ASKING PRICE ($currency) *", style = LabelFilterStyle, color = TextPrimary)
                        TactileTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            placeholder = "e.g. 28.00",
                            textStyle = CodePriceStyle.copy(color = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "SHIPPING ($currency) (OPTIONAL)", style = LabelFilterStyle, color = TextSecondary)
                        TactileTextField(
                            value = shippingStr,
                            onValueChange = { shippingStr = it },
                            placeholder = "e.g. 1.50",
                            textStyle = CodePriceStyle.copy(color = TextSecondary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
                                        if (isSelected) SurfaceElevated else SurfaceBase,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) StatusEnglishFg else BorderSubtle,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { condition = cond }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cond.icon} ${cond.label}",
                                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Listing URL Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "LISTING URL (OPTIONAL)", style = LabelFilterStyle, color = TextPrimary)
                        if (listingUrl.isNotBlank()) {
                            Text(
                                text = "🔗 Test Link ↗",
                                style = LabelFilterStyle.copy(fontSize = 11.sp, color = AccentBlue),
                                modifier = Modifier.clickable {
                                    val url = listingUrl.trim()
                                    val fullUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                        "https://$url"
                                    } else url
                                    try {
                                        uriHandler.openUri(fullUrl)
                                    } catch (e: Exception) {
                                        println("Error opening URI: ${e.message}")
                                    }
                                }
                            )
                        }
                    }
                    TactileTextField(
                        value = listingUrl,
                        onValueChange = { listingUrl = it },
                        placeholder = "https://www.ricardo.ch/de/a/... or tutti.ch/vi/...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Seller or Location
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "SELLER / LOCATION (OPTIONAL)", style = LabelFilterStyle, color = TextSecondary)
                    TactileTextField(
                        value = sellerOrLocation,
                        onValueChange = { sellerOrLocation = it },
                        placeholder = "e.g. Basel Gundeli, Seller GamerZH",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Notes
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "OFFER NOTES (OPTIONAL)", style = LabelFilterStyle, color = TextSecondary)
                    TactileTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "e.g. Auction ends Sunday 18:00, TWINT accepted",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceContainer,
                            contentColor = TextSecondary
                        ),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(text = "Cancel", style = LabelFilterStyle)
                    }

                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 0.0
                            val shipping = shippingStr.toDoubleOrNull()
                            val now = initialOffer?.createdAt ?: 0L
                            val offerId = initialOffer?.id?.ifBlank { null } ?: "offer_${now.takeIf { it > 0 } ?: (kotlinx.datetime.Clock.System.now().toEpochMilliseconds())}"
                            val offer = GameOffer(
                                id = offerId,
                                source = effectiveSource,
                                priceChf = price,
                                shippingChf = shipping,
                                listingUrl = listingUrl.trim(),
                                condition = condition,
                                sellerOrLocation = sellerOrLocation.trim(),
                                notes = notes.trim(),
                                isArchived = initialOffer?.isArchived ?: false,
                                isPurchased = initialOffer?.isPurchased ?: false,
                                createdAt = if (now > 0) now else kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                            )
                            onSave(offer)
                        },
                        enabled = isValid,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ConsoleGamecube,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(
                            text = if (isEdit) "Update Offer" else "Save Offer",
                            style = LabelFilterStyle.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
