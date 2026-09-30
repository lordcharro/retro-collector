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
import com.retrocollector.app.core.presentation.components.TactileTextField
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*

private enum class QuickScanTab(val label: String, val icon: String) {
    AUCTION_URL("Link de Leilão", "🔗"),
    MANUAL_SEARCH("Pesquisa Manual / SKU", "🔍")
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

    // Estado da Aba URL
    var urlInput by remember { mutableStateOf("") }
    var urlLocation by remember { mutableStateOf("Ricardo.ch") }
    var urlPriceStr by remember { mutableStateOf("") }

    // Estado da Aba Manual
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
                        Text(text = TextKeys.Scanner.TITLE, style = HeadlineMd, color = TextPrimary)
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
                                    text = tab.label,
                                    style = LabelFilterStyle,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    QuickScanTab.AUCTION_URL -> {
                        // --- ABA LINK DE LEILÃO ---
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "LINK DO ANÚNCIO (RICARDO.CH OU TUTTI.CH)",
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
                                placeholder = "https://www.ricardo.ch/de/a/... ou Tutti.ch",
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Feedback informativo sobre o link
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
                                    text = "O RetroCollector extrai automaticamente o título, descrição, fotos do anúncio e preço em CHF.",
                                    style = BodySm.copy(fontSize = 11.sp),
                                    color = StatusEnglishFg
                                )
                            }
                        }

                        // Plataforma detetada
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "PLATAFORMA / ORIGEM", style = LabelFilterStyle, color = TextPrimary)
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

                        // Preço Opcional para sobrescrever
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "PREÇO ESTIMADO ($curr) (OPCIONAL)", style = LabelFilterStyle, color = TextPrimary)
                            TactileTextField(
                                value = urlPriceStr,
                                onValueChange = { urlPriceStr = it },
                                placeholder = "Deixar vazio para extrair automaticamente do anúncio",
                                textStyle = CodePriceStyle.copy(color = TextPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (statusMessage != null) {
                            Text(text = statusMessage, style = BodySm, color = StatusEditionFg)
                        }

                        // Botão de Extração & Análise
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
                                Text(text = "A extrair e analisar anúncio...", style = LabelFilterStyle)
                            } else {
                                Text(text = "Extrair & Analisar Anúncio", style = LabelFilterStyle, color = Color.White)
                            }
                        }
                    }

                    QuickScanTab.MANUAL_SEARCH -> {
                        // --- ABA PESQUISA MANUAL / SKU ---
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "TÍTULO DO JOGO, CÓDIGO SERIAL OU CÓDIGO DE BARRAS", style = LabelFilterStyle, color = TextPrimary)
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
                                placeholder = "ex: Tomb Raider PS3, BLES-01780 ou 0045496351052",
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
                                        text = "Link detetado. O RetroCollector descarregará a foto e detalhes do anúncio.",
                                        style = BodySm.copy(fontSize = 11.sp),
                                        color = StatusEnglishFg
                                    )
                                }
                            }
                        }

                        // Localização
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = TextKeys.Dossier.SPOTTED_LOCATION, style = LabelFilterStyle, color = TextPrimary)
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

                        // Foto Opcional
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "FOTO DO DISCO OU LOMBADA (OPCIONAL)", style = LabelFilterStyle, color = TextPrimary)
                            TactileTextField(
                                value = imageInput,
                                onValueChange = { imageInput = it },
                                placeholder = "URL da foto do disco (https://...) ou imagem Base64",
                                textStyle = BodySm.copy(color = TextPrimary),
                                focusedBorderColor = if (isApiKeyInImage) StatusEditionFg else AccentBlue,
                                unfocusedBorderColor = if (isApiKeyInImage) StatusEditionFg else BorderSubtle,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Alertas de validação inteligente
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
                                        text = "Parece ter colado a sua chave de API aqui. Este campo é exclusivo para fotos do disco.",
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
                                        text = "Isto é o link do anúncio. Mude para a aba 'Link de Leilão' para extrair fotos e dados completos.",
                                        style = BodySm.copy(fontSize = 11.sp),
                                        color = AccentBlue
                                    )
                                }
                            }
                        }

                        // Preço Pedido
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "${TextKeys.Radar.ASKING_PRICE} ($curr)", style = LabelFilterStyle, color = TextPrimary)
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

                        // Botão de Execução Manual
                        Button(
                            onClick = {
                                val q = manualQuery.trim()
                                if (q.isNotEmpty()) {
                                    val price = manualPriceStr.toDoubleOrNull()
                                    // Se for uma chave de API colada por engano, não a envia como imagem
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
                                Text(text = TextKeys.Scanner.ANALYZING, style = LabelFilterStyle)
                            } else {
                                Text(text = TextKeys.Scanner.MANUAL_BUTTON, style = LabelFilterStyle, color = Color.White)
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
