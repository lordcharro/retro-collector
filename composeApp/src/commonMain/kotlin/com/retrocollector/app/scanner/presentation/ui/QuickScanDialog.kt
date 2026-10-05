package com.retrocollector.app.scanner.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.presentation.components.TactileTextField
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.resources.stringResource

private enum class QuickScanTab(val icon: String) {
    AUCTION_URL("🔗"),
    MANUAL_SEARCH("🔍")
}

@Composable
fun QuickScanDialog(
    modifier: Modifier = Modifier,
    currency: String = "CHF",
    isAnalyzing: Boolean = false,
    statusMessage: String? = null,
    onAnalyze: (query: String, imageBase64: String?, spottedLocation: String, askingPriceChf: Double?) -> Unit = { _, _, _, _ -> },
    onDismiss: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(QuickScanTab.AUCTION_URL) }

    // Auction URL Tab State
    var urlInput by remember { mutableStateOf("") }
    var urlLocation by remember { mutableStateOf("Ricardo.ch") }
    var urlPriceStr by remember { mutableStateOf("") }

    // Manual Search Tab State
    var manualQuery by remember { mutableStateOf("") }
    var manualLocation by remember { mutableStateOf("Ricardo.ch") }
    var manualPriceStr by remember { mutableStateOf("") }
    var imageInput by remember { mutableStateOf("") }

    val isApiKeyInImage = remember(imageInput) {
        val trimmed = imageInput.trim()
        trimmed.startsWith("AQ.") || trimmed.startsWith("AIzaSy")
    }

    val isListingUrlInImage = remember(imageInput) {
        val trimmed = imageInput.trim().lowercase()
        trimmed.contains("ricardo.ch/de/a/") || trimmed.contains("tutti.ch/vi/")
    }

    val isManualQueryUrl = remember(manualQuery) {
        val trimmed = manualQuery.trim().lowercase()
        trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.contains("ricardo.ch") || trimmed.contains("tutti.ch")
    }

    val curr = currency.ifBlank { "CHF" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderStrong),
            modifier = modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
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
                        Text(text = "⚡", fontSize = 18.sp)
                        Text(text = stringResource(Res.string.scanner_title), style = HeadlineMd, color = TextPrimary)
                    }
                    Text(
                        text = "✕",
                        color = StatusUnverifiedFg,
                        style = HeadlineSm,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                // Tab Selector (Segmented Control)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBase, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickScanTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val tabLabel = when (tab) {
                            QuickScanTab.AUCTION_URL -> stringResource(Res.string.scanner_tab_url)
                            QuickScanTab.MANUAL_SEARCH -> stringResource(Res.string.scanner_tab_manual)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) SurfaceElevated else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) AccentBlue else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = tab.icon, fontSize = 13.sp)
                                Text(
                                    text = tabLabel,
                                    style = LabelFilterStyle,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    QuickScanTab.AUCTION_URL -> {
                        // --- AUCTION URL TAB ---
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(Res.string.scanner_url_label),
                                style = LabelFilterStyle,
                                color = TextPrimary
                            )
                            TactileTextField(
                                value = urlInput,
                                onValueChange = {
                                    urlInput = it
                                    if (it.contains("ricardo.ch", ignoreCase = true)) {
                                        urlLocation = "Ricardo.ch"
                                    } else if (it.contains("tutti.ch", ignoreCase = true)) {
                                        urlLocation = "Tutti.ch"
                                    }
                                },
                                placeholder = stringResource(Res.string.scanner_url_placeholder),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Informational feedback
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceElevated, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "🌐", fontSize = 12.sp)
                                Text(
                                    text = stringResource(Res.string.scanner_desc_url_banner),
                                    style = BodySm.copy(fontSize = 11.sp),
                                    color = StatusEnglishFg
                                )
                            }
                        }

                        // Platform / Source
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = stringResource(Res.string.scanner_platform_source_label), style = LabelFilterStyle, color = TextPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Ricardo.ch", "Tutti.ch", "Anibis.ch").forEach { loc ->
                                    val isLocSelected = urlLocation == loc
                                    Box(
                                        modifier = Modifier
                                            .background(if (isLocSelected) SurfaceElevated else SurfaceBase, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isLocSelected) AccentBlue else BorderSubtle, RoundedCornerShape(4.dp))
                                            .clickable { urlLocation = loc }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = loc,
                                            style = LabelFilterStyle,
                                            color = if (isLocSelected) TextPrimary else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Optional Price Override
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = stringResource(Res.string.scanner_asking_price_optional, curr), style = LabelFilterStyle, color = TextPrimary)
                            TactileTextField(
                                value = urlPriceStr,
                                onValueChange = { urlPriceStr = it },
                                placeholder = stringResource(Res.string.scanner_price_placeholder),
                                textStyle = CodePriceStyle.copy(color = TextPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (statusMessage != null) {
                            Text(text = statusMessage, style = BodySm, color = StatusEditionFg)
                        }

                        // Extract & Analyze Button
                        Button(
                            onClick = {
                                val q = urlInput.trim()
                                if (q.isNotEmpty()) {
                                    val price = urlPriceStr.toDoubleOrNull()
                                    onAnalyze(q, null, urlLocation, price)
                                }
                            },
                            enabled = !isAnalyzing && urlInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(Res.string.scanner_action_extracting), style = LabelFilterStyle)
                            } else {
                                Text(text = stringResource(Res.string.scanner_action_extract), style = LabelFilterStyle, color = Color.White)
                            }
                        }
                    }

                    QuickScanTab.MANUAL_SEARCH -> {
                        // --- MANUAL SEARCH / SKU TAB ---
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = stringResource(Res.string.scanner_query_label), style = LabelFilterStyle, color = TextPrimary)
                            TactileTextField(
                                value = manualQuery,
                                onValueChange = {
                                    manualQuery = it
                                    if (it.contains("ricardo.ch", ignoreCase = true)) {
                                        manualLocation = "Ricardo.ch"
                                    } else if (it.contains("tutti.ch", ignoreCase = true)) {
                                        manualLocation = "Tutti.ch"
                                    }
                                },
                                placeholder = stringResource(Res.string.scanner_manual_placeholder_full),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (isManualQueryUrl) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceElevated, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "🌐", fontSize = 12.sp)
                                    Text(
                                        text = stringResource(Res.string.scanner_link_detected_hint),
                                        style = BodySm.copy(fontSize = 11.sp),
                                        color = StatusEnglishFg
                                    )
                                }
                            }
                        }

                        // Spotted Location
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = stringResource(Res.string.dossier_spotted_location), style = LabelFilterStyle, color = TextPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Ricardo.ch", "Tutti.ch", "Brocki Bern", "Flohmarkt").forEach { loc ->
                                    val isLocSelected = manualLocation == loc
                                    Box(
                                        modifier = Modifier
                                            .background(if (isLocSelected) SurfaceElevated else SurfaceBase, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isLocSelected) AccentBlue else BorderSubtle, RoundedCornerShape(4.dp))
                                            .clickable { manualLocation = loc }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = loc,
                                            style = LabelFilterStyle,
                                            color = if (isLocSelected) TextPrimary else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Optional Photo
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = stringResource(Res.string.scanner_photo_optional_label), style = LabelFilterStyle, color = TextPrimary)
                            TactileTextField(
                                value = imageInput,
                                onValueChange = { imageInput = it },
                                placeholder = stringResource(Res.string.scanner_photo_placeholder),
                                textStyle = BodySm.copy(color = TextPrimary),
                                focusedBorderColor = if (isApiKeyInImage) StatusEditionFg else AccentBlue,
                                unfocusedBorderColor = if (isApiKeyInImage) StatusEditionFg else BorderSubtle,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Intelligent validation feedback
                            if (isApiKeyInImage) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceElevated, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "⚠️", fontSize = 12.sp)
                                    Text(
                                        text = stringResource(Res.string.scanner_warn_api_key_in_photo),
                                        style = BodySm.copy(fontSize = 11.sp),
                                        color = StatusEditionFg
                                    )
                                }
                            } else if (isListingUrlInImage) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceElevated, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "💡", fontSize = 12.sp)
                                    Text(
                                        text = stringResource(Res.string.scanner_hint_url_in_photo),
                                        style = BodySm.copy(fontSize = 11.sp),
                                        color = AccentBlue
                                    )
                                }
                            }
                        }

                        // Asking Price
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "${stringResource(Res.string.radar_asking_price)} ($curr)", style = LabelFilterStyle, color = TextPrimary)
                            TactileTextField(
                                value = manualPriceStr,
                                onValueChange = { manualPriceStr = it },
                                placeholder = "35.00",
                                textStyle = CodePriceStyle.copy(color = TextPrimary),
                                placeholderStyle = CodePriceStyle.copy(color = StatusUnverifiedFg),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (statusMessage != null) {
                            Text(text = statusMessage, style = BodySm, color = StatusEditionFg)
                        }

                        // Manual Run Button
                        Button(
                            onClick = {
                                val q = manualQuery.trim()
                                if (q.isNotEmpty()) {
                                    val price = manualPriceStr.toDoubleOrNull()
                                    val img = if (isApiKeyInImage) null else imageInput.trim().ifBlank { null }
                                    onAnalyze(q, img, manualLocation, price)
                                }
                            },
                            enabled = !isAnalyzing && manualQuery.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(Res.string.scanner_analyzing), style = LabelFilterStyle)
                            } else {
                                Text(text = stringResource(Res.string.scanner_action_extract), style = LabelFilterStyle, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
fun QuickScanDialogPreview() {
    RetroTactileTheme {
        QuickScanDialog()
    }
}
