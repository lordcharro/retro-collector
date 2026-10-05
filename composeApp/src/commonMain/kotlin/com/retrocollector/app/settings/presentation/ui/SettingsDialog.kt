package com.retrocollector.app.settings.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.settings.domain.model.AiProvider
import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onDismiss: () -> Unit,
    onTestAiConnection: ((AiProvider, String, String, String?, (Result<String>) -> Unit) -> Unit)? = null,
    onTestGeminiConnection: ((String, String, (Result<String>) -> Unit) -> Unit)? = null,
    onTestFirestoreConnection: ((String, (Result<String>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedThemeMode by remember { mutableStateOf(settings.themeMode) }
    var selectedLanguage by remember { mutableStateOf(settings.appLanguage) }
    var selectedAiProvider by remember { mutableStateOf(settings.aiProvider) }
    var geminiKey by remember { mutableStateOf(settings.geminiApiKey) }
    var selectedGeminiModel by remember { mutableStateOf(settings.geminiModel.ifBlank { "gemini-3.7-flash" }) }
    var claudeKey by remember { mutableStateOf(settings.claudeApiKey) }
    var selectedClaudeModel by remember { mutableStateOf(settings.claudeModel.ifBlank { "claude-3-7-sonnet-20250219" }) }
    var openAiKey by remember { mutableStateOf(settings.openAiApiKey) }
    var openAiBaseUrl by remember { mutableStateOf(settings.openAiBaseUrl.ifBlank { "https://api.openai.com/v1" }) }
    var selectedOpenAiModel by remember { mutableStateOf(settings.openAiModel.ifBlank { "gpt-4o" }) }
    var localAiBaseUrl by remember { mutableStateOf(settings.localAiBaseUrl.ifBlank { "http://localhost:11434/v1" }) }
    var selectedLocalAiModel by remember { mutableStateOf(settings.localAiModel.ifBlank { "llama3.2-vision" }) }
    var firebaseProjectId by remember { mutableStateOf(settings.firebaseProjectId) }
    var selectedCurrency by remember { mutableStateOf(settings.defaultCurrency.ifBlank { "CHF" }) }
    var isScraperEnabled by remember { mutableStateOf(settings.isScraperEnabled) }
    var scraperProvider by remember { mutableStateOf(settings.scraperProvider) }
    var scrapeDoKey by remember { mutableStateOf(settings.scrapeDoApiKey) }
    var scrapeDoSuperProxy by remember { mutableStateOf(settings.scrapeDoSuperProxy) }
    var customProxyUrl by remember { mutableStateOf(settings.customScraperProxyUrl) }
    var isAutoDiscoveryEnabled by remember { mutableStateOf(settings.isAutoDiscoveryEnabled) }
    var isAutoSimilarGamesEnabled by remember { mutableStateOf(settings.isAutoSimilarGamesEnabled) }
    var pinnedPlatformIds by remember { mutableStateOf(settings.pinnedPlatformIds.toSet()) }

    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingAi by remember { mutableStateOf(false) }
    var firestoreStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingFirestore by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val geminiModels = remember {
        listOf(
            "gemini-3.7-flash" to "3.7 Flash",
            "gemini-3.6-flash" to "3.6 Flash",
            "gemini-3.8-flash" to "3.8 Flash",
            "gemini-3.5-flash" to "3.5 Flash",
            "gemini-2.5-flash" to "2.5 Flash",
            "gemini-2.5-flash-lite" to "2.5 Flash-Lite"
        )
    }

    val claudeModels = remember {
        listOf(
            "claude-3-7-sonnet-20250219" to "3.7 Sonnet",
            "claude-3-5-sonnet-20241022" to "3.5 Sonnet",
            "claude-3-5-haiku-20241022" to "3.5 Haiku"
        )
    }

    val openAiModels = remember {
        listOf(
            "gpt-4o" to "GPT-4o",
            "gpt-4o-mini" to "4o Mini",
            "deepseek-chat" to "DeepSeek V3",
            "claude-3-7-sonnet" to "Claude via OR"
        )
    }

    val localAiModels = remember {
        listOf(
            "llama3.2-vision" to "Llama 3.2 Vision",
            "qwen2.5-vl:7b" to "Qwen 2.5 VL",
            "llava:13b" to "LLaVA 13B",
            "minicpm-v" to "MiniCPM-V"
        )
    }

    val currencies = remember {
        listOf(
            "CHF" to "Swiss Franc",
            "EUR" to "Euro",
            "GBP" to "British Pound",
            "USD" to "US Dollar"
        )
    }

    fun buildCurrentSettings(): AppSettings = settings.copy(
        aiProvider = selectedAiProvider,
        geminiApiKey = geminiKey.trim(),
        geminiModel = selectedGeminiModel,
        claudeApiKey = claudeKey.trim(),
        claudeModel = selectedClaudeModel,
        openAiApiKey = openAiKey.trim(),
        openAiBaseUrl = openAiBaseUrl.trim(),
        openAiModel = selectedOpenAiModel.trim(),
        localAiBaseUrl = localAiBaseUrl.trim(),
        localAiModel = selectedLocalAiModel.trim(),
        firebaseProjectId = firebaseProjectId.trim(),
        defaultCurrency = selectedCurrency,
        isScraperEnabled = isScraperEnabled,
        scraperProvider = scraperProvider,
        scrapeDoApiKey = scrapeDoKey.trim(),
        scrapeDoSuperProxy = scrapeDoSuperProxy,
        customScraperProxyUrl = customProxyUrl.trim(),
        isAutoDiscoveryEnabled = isAutoDiscoveryEnabled,
        isAutoSimilarGamesEnabled = isAutoSimilarGamesEnabled,
        pinnedPlatformIds = pinnedPlatformIds.toList(),
        themeMode = selectedThemeMode,
        appLanguage = selectedLanguage
    )

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
                SettingsHeader(onDismiss = onDismiss)

                SettingsFieldReadinessBanner()

                SettingsThemeSection(
                    selectedThemeMode = selectedThemeMode,
                    onThemeSelected = { mode ->
                        selectedThemeMode = mode
                        onSaveSettings(buildCurrentSettings().copy(themeMode = mode))
                    }
                )

                SettingsLanguageSection(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { lang ->
                        selectedLanguage = lang
                        onSaveSettings(buildCurrentSettings().copy(appLanguage = lang))
                    }
                )

                SettingsAiEngineSection(
                    selectedAiProvider = selectedAiProvider,
                    onAiProviderChange = {
                        selectedAiProvider = it
                        testStatusMessage = null
                    },
                    geminiKey = geminiKey,
                    onGeminiKeyChange = {
                        geminiKey = it
                        testStatusMessage = null
                    },
                    selectedGeminiModel = selectedGeminiModel,
                    onGeminiModelChange = {
                        selectedGeminiModel = it
                        testStatusMessage = null
                    },
                    geminiModels = geminiModels,
                    claudeKey = claudeKey,
                    onClaudeKeyChange = {
                        claudeKey = it
                        testStatusMessage = null
                    },
                    selectedClaudeModel = selectedClaudeModel,
                    onClaudeModelChange = {
                        selectedClaudeModel = it
                        testStatusMessage = null
                    },
                    claudeModels = claudeModels,
                    openAiKey = openAiKey,
                    onOpenAiKeyChange = {
                        openAiKey = it
                        testStatusMessage = null
                    },
                    openAiBaseUrl = openAiBaseUrl,
                    onOpenAiBaseUrlChange = {
                        openAiBaseUrl = it
                        testStatusMessage = null
                    },
                    selectedOpenAiModel = selectedOpenAiModel,
                    onOpenAiModelChange = {
                        selectedOpenAiModel = it
                        testStatusMessage = null
                    },
                    openAiModels = openAiModels,
                    localAiBaseUrl = localAiBaseUrl,
                    onLocalAiBaseUrlChange = {
                        localAiBaseUrl = it
                        testStatusMessage = null
                    },
                    selectedLocalAiModel = selectedLocalAiModel,
                    onLocalAiModelChange = {
                        selectedLocalAiModel = it
                        testStatusMessage = null
                    },
                    localAiModels = localAiModels,
                    isAutoDiscoveryEnabled = isAutoDiscoveryEnabled,
                    onAutoDiscoveryChange = { isAutoDiscoveryEnabled = it },
                    isAutoSimilarGamesEnabled = isAutoSimilarGamesEnabled,
                    onAutoSimilarGamesChange = { isAutoSimilarGamesEnabled = it },
                    isTestingAi = isTestingAi,
                    testStatusMessage = testStatusMessage,
                    onTestAi = {
                        val currentProvider = selectedAiProvider
                        val currentKey = when (currentProvider) {
                            AiProvider.GEMINI -> geminiKey.trim()
                            AiProvider.CLAUDE -> claudeKey.trim()
                            AiProvider.OPENAI_COMPATIBLE -> openAiKey.trim()
                            AiProvider.LOCAL_OLLAMA -> ""
                        }
                        val currentModel = when (currentProvider) {
                            AiProvider.GEMINI -> selectedGeminiModel
                            AiProvider.CLAUDE -> selectedClaudeModel
                            AiProvider.OPENAI_COMPATIBLE -> selectedOpenAiModel.trim()
                            AiProvider.LOCAL_OLLAMA -> selectedLocalAiModel.trim()
                        }
                        val currentBaseUrl = when (currentProvider) {
                            AiProvider.GEMINI, AiProvider.CLAUDE -> null
                            AiProvider.OPENAI_COMPATIBLE -> openAiBaseUrl.trim()
                            AiProvider.LOCAL_OLLAMA -> localAiBaseUrl.trim()
                        }

                        if (currentProvider != AiProvider.LOCAL_OLLAMA && currentKey.isBlank()) {
                            coroutineScope.launch {
                                testStatusMessage = getString(Res.string.settings_enter_api_key, currentProvider.displayName)
                            }
                            return@SettingsAiEngineSection
                        }

                        coroutineScope.launch {
                            isTestingAi = true
                            testStatusMessage = getString(Res.string.settings_testing_provider, currentProvider.displayName)
                            try {
                                withTimeoutOrNull(12_000) {
                                    if (onTestAiConnection != null) {
                                        onTestAiConnection(currentProvider, currentKey, currentModel, currentBaseUrl) { result ->
                                            result.fold(
                                                onSuccess = { msg -> testStatusMessage = msg },
                                                onFailure = { err ->
                                                    coroutineScope.launch {
                                                        testStatusMessage = err.message ?: getString(Res.string.settings_status_failed)
                                                    }
                                                }
                                            )
                                        }
                                    } else if (onTestGeminiConnection != null) {
                                        onTestGeminiConnection(currentKey, currentModel) { result ->
                                            result.fold(
                                                onSuccess = { msg -> testStatusMessage = msg },
                                                onFailure = { err ->
                                                    coroutineScope.launch {
                                                        testStatusMessage = err.message ?: getString(Res.string.settings_status_failed)
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        testStatusMessage = getString(Res.string.settings_status_connected)
                                    }
                                } ?: run {
                                    testStatusMessage = getString(Res.string.settings_timeout_ai)
                                }
                            } catch (e: Exception) {
                                testStatusMessage = e.message ?: getString(Res.string.settings_status_failed)
                            } finally {
                                isTestingAi = false
                            }
                        }
                    }
                )

                SettingsMarketplaceSection(
                    selectedCurrency = selectedCurrency,
                    onCurrencyChange = { selectedCurrency = it },
                    currencies = currencies,
                    isScraperEnabled = isScraperEnabled,
                    onScraperEnabledChange = { isScraperEnabled = it },
                    scraperProvider = scraperProvider,
                    onScraperProviderChange = { scraperProvider = it },
                    scrapeDoKey = scrapeDoKey,
                    onScrapeDoKeyChange = { scrapeDoKey = it },
                    scrapeDoSuperProxy = scrapeDoSuperProxy,
                    onScrapeDoSuperProxyChange = { scrapeDoSuperProxy = it },
                    customProxyUrl = customProxyUrl,
                    onCustomProxyUrlChange = { customProxyUrl = it }
                )

                SettingsFirebaseSection(
                    firebaseProjectId = firebaseProjectId,
                    onFirebaseProjectIdChange = { firebaseProjectId = it },
                    isTestingFirestore = isTestingFirestore,
                    firestoreStatusMessage = firestoreStatusMessage,
                    onTestFirestore = {
                        val trimmed = firebaseProjectId.trim()
                        if (trimmed.isNotBlank()) {
                            if (onTestFirestoreConnection != null) {
                                coroutineScope.launch {
                                    isTestingFirestore = true
                                    firestoreStatusMessage = getString(Res.string.settings_firebase_testing)
                                    try {
                                        withTimeoutOrNull(12_000) {
                                            onTestFirestoreConnection(trimmed) { result ->
                                                result.fold(
                                                    onSuccess = { msg -> firestoreStatusMessage = msg },
                                                    onFailure = { err ->
                                                        coroutineScope.launch {
                                                            firestoreStatusMessage = err.message ?: getString(Res.string.settings_firebase_status_failed)
                                                        }
                                                    }
                                                )
                                            }
                                        } ?: run {
                                            firestoreStatusMessage = getString(Res.string.settings_timeout_firebase)
                                        }
                                    } catch (e: Exception) {
                                        firestoreStatusMessage = e.message ?: getString(Res.string.settings_firebase_status_failed)
                                    } finally {
                                        isTestingFirestore = false
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    firestoreStatusMessage = getString(Res.string.settings_firebase_status_connected)
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                firestoreStatusMessage = getString(Res.string.settings_firebase_status_no_project)
                            }
                        }
                    }
                )

                SettingsPlatformsSection(
                    pinnedPlatformIds = pinnedPlatformIds,
                    onPinnedPlatformIdsChange = { pinnedPlatformIds = it }
                )

                SettingsSaveButton(
                    onSave = { onSaveSettings(buildCurrentSettings()) }
                )
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
