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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
@Composable
fun QuickScanDialog(
    modifier: Modifier = Modifier,
    currency: String = "CHF",
    isAnalyzing: Boolean = false,
    statusMessage: String? = null,
    onAnalyze: (query: String, imageBase64: String?, spottedLocation: String, askingPriceChf: Double?) -> Unit = { _, _, _, _ -> },
    onDismiss: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Ricardo.ch") }
    var priceChfStr by remember { mutableStateOf("") }
    var isQueryFocused by remember { mutableStateOf(false) }
    var isImageFocused by remember { mutableStateOf(false) }
    var isPriceFocused by remember { mutableStateOf(false) }

    val isUrl = remember(query) {
        query.startsWith("http://") || query.startsWith("https://") || query.contains("ricardo.ch") || query.contains("tutti.ch")
    }

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

                Text(
                    text = TextKeys.App.TAGLINE,
                    style = BodySm,
                    color = TextSecondary
                )

                // Campo: Título, Código ou Link
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = TextKeys.Scanner.MANUAL_LABEL, style = LabelFilterStyle, color = TextPrimary)
                    androidx.compose.foundation.text.BasicTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            if (it.contains("ricardo.ch", ignoreCase = true)) {
                                location = "Ricardo.ch"
                            } else if (it.contains("tutti.ch", ignoreCase = true)) {
                                location = "Tutti.ch"
                            } else if (it.contains("anibis.ch", ignoreCase = true)) {
                                location = "Anibis.ch"
                            }
                        },
                        textStyle = BodyMd.copy(color = TextPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBase, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                            .onFocusChanged { isQueryFocused = it.isFocused },
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (query.isEmpty() && !isQueryFocused) {
                                    Text(
                                        text = TextKeys.Scanner.MANUAL_PLACEHOLDER,
                                        style = BodySm.copy(color = StatusUnverifiedFg)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (isUrl) {
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

                // Campo: Localização / Loja (Sourcing)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = TextKeys.Dossier.SPOTTED_LOCATION, style = LabelFilterStyle, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Ricardo.ch", "Tutti.ch", "Brocki Bern", "Flohmarkt").forEach { loc ->
                            Box(
                                modifier = Modifier
                                    .background(if (location == loc) SurfaceElevated else SurfaceBase, RoundedCornerShape(4.dp))
                                    .border(1.dp, if (location == loc) AccentBlue else BorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { location = loc }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = loc, style = LabelFilterStyle, color = if (location == loc) TextPrimary else TextSecondary)
                            }
                        }
                    }
                }

                // Campo Opcional: Imagem do Disco / Lombada
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "FOTO DO DISCO OU LOMBADA (OPCIONAL)", style = LabelFilterStyle, color = TextPrimary)
                    androidx.compose.foundation.text.BasicTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        textStyle = BodySm.copy(color = TextPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBase, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                            .onFocusChanged { isImageFocused = it.isFocused },
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (imageUrlInput.isEmpty() && !isImageFocused) {
                                    Text(
                                        text = "URL da foto do disco ou imagem Base64",
                                        style = BodySm.copy(color = StatusUnverifiedFg)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Campo: Preço Pedido
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val curr = currency.ifBlank { "CHF" }
                    Text(text = "${TextKeys.Radar.ASKING_PRICE} ($curr)", style = LabelFilterStyle, color = TextPrimary)
                    androidx.compose.foundation.text.BasicTextField(
                        value = priceChfStr,
                        onValueChange = { priceChfStr = it },
                        textStyle = CodePriceStyle.copy(color = TextPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBase, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                            .onFocusChanged { isPriceFocused = it.isFocused },
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (priceChfStr.isEmpty() && !isPriceFocused) {
                                    Text(text = "35.00", style = CodePriceStyle.copy(color = StatusUnverifiedFg))
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                if (statusMessage != null) {
                    Text(
                        text = statusMessage,
                        style = BodySm,
                        color = StatusEditionFg
                    )
                }

                // Botão de Execução
                Button(
                    onClick = {
                        val q = query.trim()
                        if (q.isNotEmpty()) {
                            val price = priceChfStr.toDoubleOrNull()
                            val img = imageUrlInput.trim().ifBlank { null }
                            onAnalyze(q, img, location, price)
                        }
                    },
                    enabled = !isAnalyzing && query.isNotBlank(),
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
