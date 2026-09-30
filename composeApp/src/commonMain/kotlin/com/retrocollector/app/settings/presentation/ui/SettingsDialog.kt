package com.retrocollector.app.settings.presentation.ui

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrocollector.app.core.presentation.components.TactileTextField
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.settings.domain.model.AppSettings
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onDismiss: () -> Unit,
    onTestGeminiConnection: ((String, String, (Result<String>) -> Unit) -> Unit)? = null,
    onTestFirestoreConnection: ((String, (Result<String>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var geminiKey by remember { mutableStateOf(settings.geminiApiKey) }
    var selectedModel by remember { mutableStateOf(settings.geminiModel.ifBlank { "gemini-3.7-flash" }) }
    var firebaseProjectId by remember { mutableStateOf(settings.firebaseProjectId) }
    var selectedCurrency by remember { mutableStateOf(settings.defaultCurrency.ifBlank { "CHF" }) }
    var isScraperEnabled by remember { mutableStateOf(settings.isScraperEnabled) }
    var ricardoCookie by remember { mutableStateOf(settings.ricardoSessionCookie) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingGemini by remember { mutableStateOf(false) }
    var firestoreStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingFirestore by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val geminiModels = remember {
        listOf(
            "gemini-3.7-flash" to "3.7 Flash (Estável)",
            "gemini-3.6-flash" to "3.6 Flash (Rápido)",
            "gemini-3.5-flash" to "3.5 Flash (Base)",
            "gemini-3.8-flash" to "3.8 Flash (Preview)"
        )
    }

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
                                text = selectedModel.replace("gemini-", "").uppercase(),
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
                            Text(
                                text = "Model Throughput",
                                style = BodySm.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                            Text(
                                text = if (selectedModel.contains("lite")) {
                                    "Ultra-low latency (~280ms)"
                                } else {
                                    "Avg response: ~550ms"
                                },
                                style = CodeSkuStyle.copy(fontSize = 11.sp),
                                color = StatusEnglishFg
                            )
                        }

                        // Model Selector Chips
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "ACTIVE MODEL", style = LabelFilterStyle, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                geminiModels.forEach { (modelId, label) ->
                                    val isSelected = selectedModel == modelId
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) SurfaceCard else SurfaceBase,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) StatusEnglishFg else BorderSubtle,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                selectedModel = modelId
                                                testStatusMessage = null
                                            }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = CodeSkuStyle.copy(
                                                fontSize = 10.sp,
                                                color = if (isSelected) StatusEnglishFg else TextSecondary
                                            ),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        // API Key Input
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = TextKeys.Settings.GEMINI_API_KEY_LABEL, style = LabelFilterStyle, color = TextPrimary)
                            TactileTextField(
                                value = geminiKey,
                                onValueChange = {
                                    geminiKey = it
                                    testStatusMessage = null
                                },
                                placeholder = TextKeys.Settings.GEMINI_API_KEY_HINT,
                                textStyle = CodeSkuStyle.copy(color = TextPrimary),
                                placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg),
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                backgroundColor = SurfaceCard,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Text(
                                        text = if (isKeyVisible) "🙈" else "👁️",
                                        modifier = Modifier
                                            .clickable { isKeyVisible = !isKeyVisible }
                                            .padding(start = 4.dp),
                                        fontSize = 14.sp
                                    )
                                }
                            )
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
                                            coroutineScope.launch {
                                                isTestingGemini = true
                                                testStatusMessage = "A testar ligação..."
                                                try {
                                                    withTimeoutOrNull(12_000) {
                                                        onTestGeminiConnection(trimmedKey, selectedModel) { result ->
                                                            result.fold(
                                                                onSuccess = { msg -> testStatusMessage = msg },
                                                                onFailure = { err ->
                                                                    testStatusMessage = err.message ?: TextKeys.Settings.STATUS_FAILED
                                                                }
                                                            )
                                                        }
                                                    } ?: run {
                                                        testStatusMessage = "Tempo limite esgotado (12s). Verifica a ligação."
                                                    }
                                                } catch (e: Exception) {
                                                    testStatusMessage = e.message ?: TextKeys.Settings.STATUS_FAILED
                                                } finally {
                                                    isTestingGemini = false
                                                }
                                            }
                                        } else {
                                            testStatusMessage = TextKeys.Settings.STATUS_CONNECTED
                                        }
                                    } else {
                                        testStatusMessage = "Por favor, introduz uma chave de API válida."
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
                                val isTesting = isTestingGemini || msg.startsWith("A testar")
                                val isSuccess = msg == TextKeys.Settings.STATUS_CONNECTED ||
                                    msg.startsWith("Ligação") ||
                                    msg.startsWith("Connection")
                                val textColor = when {
                                    isTesting -> StatusEditionFg
                                    isSuccess -> StatusEnglishFg
                                    else -> StatusRiskFg
                                }
                                Text(
                                    text = msg,
                                    style = BodySm.copy(fontSize = 11.sp),
                                    color = textColor,
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

                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

                        // Cookie de Sessão Ricardo.ch (Opcional anti-captcha)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COOKIE DE SESSÃO RICARDO.CH (OPCIONAL)",
                                    style = LabelFilterStyle,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Anti-Captcha",
                                    style = CodeSkuStyle.copy(fontSize = 10.sp),
                                    color = StatusEnglishFg
                                )
                            }
                            TactileTextField(
                                value = ricardoCookie,
                                onValueChange = { ricardoCookie = it },
                                placeholder = "Ex: ricardo_session=... ou token da tua conta",
                                textStyle = CodeSkuStyle.copy(color = TextPrimary, fontSize = 11.sp),
                                placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg, fontSize = 11.sp),
                                backgroundColor = SurfaceCard,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Permite autenticar os pedidos ao Ricardo.ch com a tua conta para evitar desafios de Captcha.",
                                style = BodySm.copy(fontSize = 10.sp),
                                color = TextSecondary
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
                            TactileTextField(
                                value = firebaseProjectId,
                                onValueChange = { firebaseProjectId = it },
                                placeholder = TextKeys.Settings.FIREBASE_PROJECT_ID_HINT,
                                textStyle = CodeSkuStyle.copy(color = TextPrimary),
                                placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg),
                                backgroundColor = SurfaceCard,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Synchronizes verified game acquisitions across Mac, Android, and Web.",
                                style = BodySm.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }

                        // Test Firestore Connection Button & Status Feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val trimmed = firebaseProjectId.trim()
                                    if (trimmed.isNotBlank()) {
                                        if (onTestFirestoreConnection != null) {
                                            coroutineScope.launch {
                                                isTestingFirestore = true
                                                firestoreStatusMessage = TextKeys.Settings.FIREBASE_TESTING
                                                try {
                                                    withTimeoutOrNull(12_000) {
                                                        onTestFirestoreConnection(trimmed) { result ->
                                                            result.fold(
                                                                onSuccess = { msg -> firestoreStatusMessage = msg },
                                                                onFailure = { err ->
                                                                    firestoreStatusMessage = err.message ?: TextKeys.Settings.FIREBASE_STATUS_FAILED
                                                                }
                                                            )
                                                        }
                                                    } ?: run {
                                                        firestoreStatusMessage = "Tempo limite esgotado (12s). Verifica o Project ID."
                                                    }
                                                } catch (e: Exception) {
                                                    firestoreStatusMessage = e.message ?: TextKeys.Settings.FIREBASE_STATUS_FAILED
                                                } finally {
                                                    isTestingFirestore = false
                                                }
                                            }
                                        } else {
                                            firestoreStatusMessage = TextKeys.Settings.FIREBASE_STATUS_CONNECTED
                                        }
                                    } else {
                                        firestoreStatusMessage = TextKeys.Settings.FIREBASE_STATUS_NO_PROJECT
                                    }
                                },
                                enabled = !isTestingFirestore,
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isTestingFirestore) "⏳ " + TextKeys.Settings.FIREBASE_TESTING else "⚡ " + TextKeys.Settings.FIREBASE_TEST_BUTTON,
                                    style = LabelFilterStyle,
                                    color = TextPrimary
                                )
                            }

                            firestoreStatusMessage?.let { msg ->
                                val isTesting = isTestingFirestore || msg == TextKeys.Settings.FIREBASE_TESTING
                                val isSuccess = msg == TextKeys.Settings.FIREBASE_STATUS_CONNECTED ||
                                    msg.startsWith("Firestore connected") ||
                                    msg.startsWith("Ligação")
                                val textColor = when {
                                    isTesting -> StatusEditionFg
                                    isSuccess -> StatusEnglishFg
                                    else -> StatusRiskFg
                                }
                                Text(
                                    text = msg,
                                    style = BodySm.copy(fontSize = 11.sp),
                                    color = textColor,
                                    modifier = Modifier.weight(1f, fill = false).padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Botão de Gravação Principal
                Button(
                    onClick = {
                        onSaveSettings(
                            settings.copy(
                                geminiApiKey = geminiKey.trim(),
                                geminiModel = selectedModel,
                                firebaseProjectId = firebaseProjectId.trim(),
                                defaultCurrency = selectedCurrency,
                                isScraperEnabled = isScraperEnabled,
                                ricardoSessionCookie = ricardoCookie.trim()
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

@Preview
@Composable
fun SettingsDialogPreview() {
    RetroTactileTheme {
        SettingsDialog(
            settings = AppSettings(
                geminiApiKey = "AIzaSyPreviewKeyExample",
                geminiModel = "gemini-3.7-flash",
                firebaseProjectId = "retro-collector-swiss",
                defaultCurrency = "CHF",
                isScraperEnabled = true
            ),
            onSaveSettings = {},
            onDismiss = {}
        )
    }
}

