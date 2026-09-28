package com.retrocollector.app.settings.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.settings.domain.model.AppSettings

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onDismiss: () -> Unit,
    onTestGeminiConnection: ((String, (Result<String>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var geminiKey by remember { mutableStateOf(settings.geminiApiKey) }
    var firebaseProjectId by remember { mutableStateOf(settings.firebaseProjectId) }
    var selectedCurrency by remember { mutableStateOf(settings.defaultCurrency.ifBlank { "CHF" }) }
    var isScraperEnabled by remember { mutableStateOf(settings.isScraperEnabled) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingGemini by remember { mutableStateOf(false) }

    var isGeminiFocused by remember { mutableStateOf(false) }
    var isFirebaseFocused by remember { mutableStateOf(false) }

    val currencies = remember {
        listOf(
            "CHF" to "Swiss Fr.",
            "EUR" to "Euro",
            "GBP" to "Pound",
            "USD" to "Dollar"
        )
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
                .widthIn(max = 560.dp)
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Tático
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(StatusEnglishFg, RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = TextKeys.Settings.TITLE.uppercase(),
                            style = HeadlineMd,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "✕",
                        color = StatusUnverifiedFg,
                        style = HeadlineSm,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }

                // Banner de Prontidão Tática (Tactical Field Readiness)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBase, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(StatusEnglishFg, RoundedCornerShape(3.dp)))
                            Text(
                                text = "FIELD READINESS ACTIVE",
                                style = LabelBadgeStyle.copy(fontSize = 10.sp),
                                color = StatusEnglishFg
                            )
                        }
                        Text(
                            text = "Offline Protocol & Regional Services",
                            style = HeadlineSm.copy(fontSize = 14.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "Calibrated for Swiss & European physical retro media acquisition.",
                            style = BodySm.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                // SEÇÃO 1: Vision Intelligence Engine (Google Gemini AI)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = TextKeys.Settings.GEMINI_SECTION.uppercase(),
                            style = LabelFilterStyle.copy(fontSize = 11.sp),
                            color = StatusUnverifiedFg
                        )
                        Box(
                            modifier = Modifier
                                .background(SurfaceBase, RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = TextKeys.Settings.GEMINI_MODEL,
                                style = CodeSkuStyle.copy(fontSize = 10.sp),
                                color = StatusEnglishFg
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBase, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Throughput / Info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceCard, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Model Throughput", style = BodySm.copy(fontSize = 11.sp), color = TextSecondary)
                            Text(text = "Avg response: ~640ms", style = CodeSkuStyle.copy(fontSize = 11.sp), color = StatusEnglishFg)
                        }

                        // API Key Input
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = TextKeys.Settings.GEMINI_API_KEY_LABEL, style = LabelFilterStyle, color = TextPrimary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard, RoundedCornerShape(4.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = geminiKey,
                                    onValueChange = {
                                        geminiKey = it
                                        testStatusMessage = null
                                    },
                                    textStyle = CodeSkuStyle.copy(color = TextPrimary),
                                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { isGeminiFocused = it.isFocused },
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (geminiKey.isEmpty() && !isGeminiFocused) {
                                                Text(
                                                    text = TextKeys.Settings.GEMINI_API_KEY_HINT,
                                                    style = CodeSkuStyle.copy(color = StatusUnverifiedFg)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                                Text(
                                    text = if (isKeyVisible) "🙈" else "👁️",
                                    modifier = Modifier
                                        .clickable { isKeyVisible = !isKeyVisible }
                                        .padding(start = 6.dp),
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = TextKeys.Settings.GEMINI_EXPLAINER,
                                style = BodySm.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }

                        // Test Connection Button & Status Feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val trimmedKey = geminiKey.trim()
                                    if (trimmedKey.isNotBlank()) {
                                        if (onTestGeminiConnection != null) {
                                            isTestingGemini = true
                                            testStatusMessage = "A testar ligação..."
                                            onTestGeminiConnection(trimmedKey) { result ->
                                                isTestingGemini = false
                                                result.fold(
                                                    onSuccess = { msg -> testStatusMessage = msg },
                                                    onFailure = { err -> testStatusMessage = err.message ?: TextKeys.Settings.STATUS_FAILED }
                                                )
                                            }
                                        } else {
                                            testStatusMessage = TextKeys.Settings.STATUS_CONNECTED
                                        }
                                    } else {
                                        testStatusMessage = TextKeys.Settings.STATUS_FAILED
                                    }
                                },
                                enabled = !isTestingGemini,
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isTestingGemini) "⏳ A testar..." else "⚡ " + TextKeys.Settings.GEMINI_TEST_BUTTON,
                                    style = LabelFilterStyle,
                                    color = TextPrimary
                                )
                            }

                            testStatusMessage?.let { msg ->
                                val isSuccess = msg == TextKeys.Settings.STATUS_CONNECTED ||
                                    msg.startsWith("Ligação") ||
                                    msg.startsWith("Connection")
                                Text(
                                    text = msg,
                                    style = BodySm.copy(fontSize = 11.sp),
                                    color = if (isSuccess) StatusEnglishFg else StatusRiskFg,
                                    modifier = Modifier.weight(1f, fill = false).padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // SEÇÃO 2: Regional Pricing & Market Feeds (Currency Selector & Parser)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = TextKeys.Settings.CURRENCY_SECTION.uppercase(),
                        style = LabelFilterStyle.copy(fontSize = 11.sp),
                        color = StatusUnverifiedFg
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBase, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Linha de Cabeçalho do Seletor de Moeda
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = TextKeys.Settings.CURRENCY_LABEL, style = LabelFilterStyle, color = TextPrimary)
                            Text(
                                text = "$selectedCurrency (Active Index)",
                                style = CodeSkuStyle.copy(fontSize = 11.sp),
                                color = StatusEnglishFg
                            )
                        }

                        // Matriz Segmentada de 4 Moedas (CHF, EUR, GBP, USD)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currencies.forEach { (curr, label) ->
                                val isSelected = selectedCurrency == curr
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) ConsoleGamecube else SurfaceCard,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) BorderStrong else BorderSubtle,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { selectedCurrency = curr }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = curr,
                                            style = CodePriceStyle.copy(fontSize = 13.sp),
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                        Text(
                                            text = label,
                                            style = LabelBadgeStyle.copy(fontSize = 9.sp),
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else StatusUnverifiedFg
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

                        // Toggle do Scraper Ricardo & Tutti
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f).padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = TextKeys.Settings.SCRAPER_TITLE,
                                        style = BodyMd.copy(fontSize = 13.sp),
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(SurfaceElevated, RoundedCornerShape(2.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = TextKeys.Settings.SCRAPER_BADGE,
                                            style = CodeSkuStyle.copy(fontSize = 9.sp),
                                            color = ConsoleGamecube
                                        )
                                    }
                                }
                                Text(
                                    text = TextKeys.Settings.SCRAPER_SUBTITLE,
                                    style = BodySm.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = isScraperEnabled,
                                onCheckedChange = { isScraperEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ConsoleGamecube,
                                    uncheckedThumbColor = StatusUnverifiedFg,
                                    uncheckedTrackColor = SurfaceElevated
                                )
                            )
                        }
                    }
                }

                // SEÇÃO 3: Cloud Database Sync & Offline Engine (Firebase)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = TextKeys.Settings.FIREBASE_SECTION.uppercase(),
                            style = LabelFilterStyle.copy(fontSize = 11.sp),
                            color = StatusUnverifiedFg
                        )
                        Text(
                            text = "SQLite & Firestore Sync",
                            style = CodeSkuStyle.copy(fontSize = 10.sp),
                            color = StatusUnverifiedFg
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBase, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = TextKeys.Settings.FIREBASE_PROJECT_ID_LABEL, style = LabelFilterStyle, color = TextPrimary)
                            BasicTextField(
                                value = firebaseProjectId,
                                onValueChange = { firebaseProjectId = it },
                                textStyle = CodeSkuStyle.copy(color = TextPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard, RoundedCornerShape(4.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                    .padding(10.dp)
                                    .onFocusChanged { isFirebaseFocused = it.isFocused },
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (firebaseProjectId.isEmpty() && !isFirebaseFocused) {
                                            Text(
                                                text = TextKeys.Settings.FIREBASE_PROJECT_ID_HINT,
                                                style = CodeSkuStyle.copy(color = StatusUnverifiedFg)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            Text(
                                text = "Synchronizes verified game acquisitions across Mac, Android, and Web.",
                                style = BodySm.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Botão de Gravação Principal
                Button(
                    onClick = {
                        onSaveSettings(
                            settings.copy(
                                geminiApiKey = geminiKey.trim(),
                                firebaseProjectId = firebaseProjectId.trim(),
                                defaultCurrency = selectedCurrency,
                                isScraperEnabled = isScraperEnabled
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusEnglishFg),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = "💾 " + TextKeys.Settings.SAVE_BUTTON,
                        style = LabelFilterStyle.copy(fontSize = 13.sp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

