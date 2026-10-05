package com.retrocollector.app.settings.presentation.ui

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.PlatformEcosystem
import com.retrocollector.app.core.presentation.components.TactileTextField
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.settings.domain.model.AiProvider
import com.retrocollector.app.settings.domain.model.AppLanguage
import com.retrocollector.app.settings.domain.model.ScraperProvider
import com.retrocollector.app.settings.domain.model.ThemeMode
import com.retrocollector.app.settings.domain.model.defaultPinnedPlatformIds
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsHeader(onDismiss: () -> Unit) {
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
                text = stringResource(Res.string.settings_title).uppercase(),
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
}

@Composable
fun SettingsFieldReadinessBanner() {
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
                    text = stringResource(Res.string.app_field_readiness_title),
                    style = LabelBadgeStyle.copy(fontSize = 10.sp),
                    color = StatusEnglishFg
                )
            }
            Text(
                text = stringResource(Res.string.app_field_readiness_subtitle),
                style = HeadlineSm.copy(fontSize = 14.sp),
                color = TextPrimary
            )
            Text(
                text = stringResource(Res.string.app_field_readiness_desc),
                style = BodySm.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SettingsThemeSection(
    selectedThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🎨", fontSize = 14.sp)
                Text(
                    text = stringResource(Res.string.settings_section_theme),
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg
                )
            }
            Box(
                modifier = Modifier
                    .background(if (selectedThemeMode == ThemeMode.LIGHT) StatusEditionBg else SurfaceBase, RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (selectedThemeMode == ThemeMode.LIGHT) stringResource(Res.string.settings_theme_light_badge) else stringResource(Res.string.settings_theme_dark_badge),
                    style = CodeSkuStyle.copy(fontSize = 10.sp),
                    color = if (selectedThemeMode == ThemeMode.LIGHT) StatusEditionFg else StatusEnglishFg
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
            Text(
                text = stringResource(Res.string.settings_theme_desc),
                style = BodySm.copy(fontSize = 12.sp),
                color = TextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(6.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = selectedThemeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) SurfaceElevated else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) BorderStrong else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onThemeSelected(mode) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = mode.icon, fontSize = 13.sp)
                            Text(
                                text = mode.displayName,
                                style = LabelFilterStyle.copy(fontSize = 12.sp),
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(ConsoleGamecube, RoundedCornerShape(2.5.dp))
                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsLanguageSection(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🌐", fontSize = 14.sp)
                Text(
                    text = stringResource(Res.string.settings_section_language),
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg
                )
            }
            Box(
                modifier = Modifier
                    .background(SurfaceBase, RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = selectedLanguage.displayName.uppercase(),
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
            Text(
                text = stringResource(Res.string.settings_language_desc),
                style = BodySm.copy(fontSize = 12.sp),
                color = TextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(6.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppLanguage.entries.forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) SurfaceElevated else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) BorderStrong else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onLanguageSelected(lang) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = lang.icon, fontSize = 13.sp)
                            Text(
                                text = lang.displayName,
                                style = LabelFilterStyle.copy(fontSize = 12.sp),
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(StatusEnglishFg, RoundedCornerShape(2.5.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
fun SettingsAiEngineSection(
    selectedAiProvider: AiProvider,
    onAiProviderChange: (AiProvider) -> Unit,
    geminiKey: String,
    onGeminiKeyChange: (String) -> Unit,
    selectedGeminiModel: String,
    onGeminiModelChange: (String) -> Unit,
    geminiModels: List<Pair<String, String>>,
    claudeKey: String,
    onClaudeKeyChange: (String) -> Unit,
    selectedClaudeModel: String,
    onClaudeModelChange: (String) -> Unit,
    claudeModels: List<Pair<String, String>>,
    openAiKey: String,
    onOpenAiKeyChange: (String) -> Unit,
    openAiBaseUrl: String,
    onOpenAiBaseUrlChange: (String) -> Unit,
    selectedOpenAiModel: String,
    onOpenAiModelChange: (String) -> Unit,
    openAiModels: List<Pair<String, String>>,
    localAiBaseUrl: String,
    onLocalAiBaseUrlChange: (String) -> Unit,
    selectedLocalAiModel: String,
    onLocalAiModelChange: (String) -> Unit,
    localAiModels: List<Pair<String, String>>,
    isAutoDiscoveryEnabled: Boolean,
    onAutoDiscoveryChange: (Boolean) -> Unit,
    isAutoSimilarGamesEnabled: Boolean,
    onAutoSimilarGamesChange: (Boolean) -> Unit,
    isTestingAi: Boolean,
    testStatusMessage: String?,
    onTestAi: () -> Unit
) {
    var isKeyVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🤖", fontSize = 14.sp)
                Text(
                    text = stringResource(Res.string.settings_ai_engine_title),
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = StatusUnverifiedFg
                )
            }
            Box(
                modifier = Modifier
                    .background(SurfaceBase, RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                val activeModelLabel = when (selectedAiProvider) {
                    AiProvider.GEMINI -> selectedGeminiModel.replace("gemini-", "").uppercase()
                    AiProvider.CLAUDE -> selectedClaudeModel.replace("claude-", "").uppercase()
                    AiProvider.OPENAI_COMPATIBLE -> selectedOpenAiModel.uppercase()
                    AiProvider.LOCAL_OLLAMA -> selectedLocalAiModel.uppercase()
                }
                Text(
                    text = "${selectedAiProvider.displayName.uppercase()} ($activeModelLabel)",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Provider Selector Tabs
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(Res.string.settings_ai_active_provider), style = LabelFilterStyle, color = TextPrimary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AiProvider.entries.forEach { provider ->
                        val isSelected = selectedAiProvider == provider
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) SurfaceElevated else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) StatusEnglishFg else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onAiProviderChange(provider) }
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(text = provider.icon, fontSize = 14.sp)
                                Text(
                                    text = when (provider) {
                                        AiProvider.GEMINI -> "Gemini"
                                        AiProvider.CLAUDE -> "Claude"
                                        AiProvider.OPENAI_COMPATIBLE -> "OpenAI"
                                        AiProvider.LOCAL_OLLAMA -> "Local"
                                    },
                                    style = LabelFilterStyle.copy(fontSize = 10.sp),
                                    color = if (isSelected) StatusEnglishFg else TextSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Provider Configurations
            when (selectedAiProvider) {
                AiProvider.GEMINI -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(Res.string.settings_ai_gemini_model), style = LabelFilterStyle, color = TextPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            geminiModels.forEach { (modelId, label) ->
                                val isSelected = selectedGeminiModel == modelId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) SurfaceCard else SurfaceBase, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isSelected) StatusEnglishFg else BorderSubtle, RoundedCornerShape(4.dp))
                                        .clickable { onGeminiModelChange(modelId) }
                                        .padding(vertical = 6.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = CodeSkuStyle.copy(
                                            fontSize = 9.sp,
                                            color = if (isSelected) StatusEnglishFg else TextSecondary
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(Res.string.settings_gemini_api_key_label), style = LabelFilterStyle, color = TextPrimary)
                        TactileTextField(
                            value = geminiKey,
                            onValueChange = onGeminiKeyChange,
                            placeholder = stringResource(Res.string.settings_gemini_api_key_hint),
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
                            text = stringResource(Res.string.settings_gemini_explainer),
                            style = BodySm.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                AiProvider.CLAUDE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(Res.string.settings_ai_claude_model), style = LabelFilterStyle, color = TextPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            claudeModels.forEach { (modelId, label) ->
                                val isSelected = selectedClaudeModel == modelId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) SurfaceCard else SurfaceBase, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isSelected) StatusEnglishFg else BorderSubtle, RoundedCornerShape(4.dp))
                                        .clickable { onClaudeModelChange(modelId) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
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

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(Res.string.settings_ai_anthropic_key), style = LabelFilterStyle, color = TextPrimary)
                        TactileTextField(
                            value = claudeKey,
                            onValueChange = onClaudeKeyChange,
                            placeholder = "sk-ant-api03-...",
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
                            text = stringResource(Res.string.settings_ai_anthropic_desc),
                            style = BodySm.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                AiProvider.OPENAI_COMPATIBLE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(Res.string.settings_ai_endpoint_url), style = LabelFilterStyle, color = TextPrimary)
                        TactileTextField(
                            value = openAiBaseUrl,
                            onValueChange = onOpenAiBaseUrlChange,
                            placeholder = "https://api.openai.com/v1 or https://openrouter.ai/api/v1",
                            textStyle = CodeSkuStyle.copy(color = TextPrimary),
                            placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg),
                            backgroundColor = SurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(Res.string.settings_ai_model_presets), style = LabelFilterStyle, color = TextPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            openAiModels.forEach { (modelId, label) ->
                                val isSelected = selectedOpenAiModel == modelId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) SurfaceCard else SurfaceBase, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isSelected) StatusEnglishFg else BorderSubtle, RoundedCornerShape(4.dp))
                                        .clickable { onOpenAiModelChange(modelId) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
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
                        TactileTextField(
                            value = selectedOpenAiModel,
                            onValueChange = onOpenAiModelChange,
                            placeholder = "Model ID (e.g. gpt-4o, deepseek-chat)",
                            textStyle = CodeSkuStyle.copy(color = TextPrimary),
                            placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg),
                            backgroundColor = SurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(Res.string.settings_ai_api_key_bearer), style = LabelFilterStyle, color = TextPrimary)
                        TactileTextField(
                            value = openAiKey,
                            onValueChange = onOpenAiKeyChange,
                            placeholder = "sk-...",
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
                            text = "Compatible with OpenAI, OpenRouter, DeepSeek, and standard chat completion endpoints.",
                            style = BodySm.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                AiProvider.LOCAL_OLLAMA -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "LOCAL OLLAMA / LM STUDIO SERVER URL", style = LabelFilterStyle, color = TextPrimary)
                        TactileTextField(
                            value = localAiBaseUrl,
                            onValueChange = onLocalAiBaseUrlChange,
                            placeholder = "http://localhost:11434/v1 or http://192.168.x.x:11434/v1",
                            textStyle = CodeSkuStyle.copy(color = TextPrimary),
                            placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg),
                            backgroundColor = SurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "LOCAL VISION MODEL PRESETS", style = LabelFilterStyle, color = TextPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            localAiModels.forEach { (modelId, label) ->
                                val isSelected = selectedLocalAiModel == modelId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) SurfaceCard else SurfaceBase, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isSelected) StatusEnglishFg else BorderSubtle, RoundedCornerShape(4.dp))
                                        .clickable { onLocalAiModelChange(modelId) }
                                        .padding(vertical = 6.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = CodeSkuStyle.copy(
                                            fontSize = 9.sp,
                                            color = if (isSelected) StatusEnglishFg else TextSecondary
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                        TactileTextField(
                            value = selectedLocalAiModel,
                            onValueChange = onLocalAiModelChange,
                            placeholder = "Model tag (e.g. llama3.2-vision, qwen2.5-vl:7b)",
                            textStyle = CodeSkuStyle.copy(color = TextPrimary),
                            placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg),
                            backgroundColor = SurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "100% private and offline inference. " + stringResource(Res.string.settings_ai_local_vision_hint),
                            style = BodySm.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
            }

            // Universal Test Connection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTestAi,
                    enabled = !isTestingAi,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isTestingAi) "⏳ Testing..." else "⚡ TEST ${selectedAiProvider.displayName.uppercase()}",
                        style = LabelFilterStyle,
                        color = TextPrimary
                    )
                }

                testStatusMessage?.let { msg ->
                    val statusConnectedStr = stringResource(Res.string.settings_status_connected)
                    val isTesting = isTestingAi || msg.startsWith("Testing")
                    val isSuccess = msg == statusConnectedStr ||
                        msg.startsWith("Connection") ||
                        msg.contains("established successfully", ignoreCase = true)
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

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

            // Auto-Discovery Quota Saver
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
                            text = stringResource(Res.string.settings_auto_discovery_title),
                            style = BodyMd.copy(fontSize = 13.sp),
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(SurfaceElevated, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.settings_auto_discovery_badge),
                                style = CodeSkuStyle.copy(fontSize = 9.sp),
                                color = ConsoleGamecube
                            )
                        }
                    }
                    Text(
                        text = stringResource(Res.string.settings_auto_discovery_subtitle),
                        style = BodySm.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isAutoDiscoveryEnabled,
                    onCheckedChange = onAutoDiscoveryChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ConsoleGamecube,
                        uncheckedThumbColor = StatusUnverifiedFg,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

            // Auto-Similar Games Quota Saver
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
                            text = stringResource(Res.string.settings_auto_similar_games_title),
                            style = BodyMd.copy(fontSize = 13.sp),
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(SurfaceElevated, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.settings_auto_similar_games_badge),
                                style = CodeSkuStyle.copy(fontSize = 9.sp),
                                color = ConsoleGamecube
                            )
                        }
                    }
                    Text(
                        text = stringResource(Res.string.settings_auto_similar_games_subtitle),
                        style = BodySm.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isAutoSimilarGamesEnabled,
                    onCheckedChange = onAutoSimilarGamesChange,
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
}

@Suppress("LongParameterList")
@Composable
fun SettingsMarketplaceSection(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    currencies: List<Pair<String, String>>,
    isScraperEnabled: Boolean,
    onScraperEnabledChange: (Boolean) -> Unit,
    scraperProvider: ScraperProvider,
    onScraperProviderChange: (ScraperProvider) -> Unit,
    scrapeDoKey: String,
    onScrapeDoKeyChange: (String) -> Unit,
    scrapeDoSuperProxy: Boolean,
    onScrapeDoSuperProxyChange: (Boolean) -> Unit,
    customProxyUrl: String,
    onCustomProxyUrlChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.settings_currency_section).uppercase(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(Res.string.settings_currency_label), style = LabelFilterStyle, color = TextPrimary)
                Text(
                    text = "$selectedCurrency (Active Index)",
                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                    color = StatusEnglishFg
                )
            }

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
                            .clickable { onCurrencyChange(curr) }
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

            // Scraper Proxy
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
                            text = stringResource(Res.string.settings_scraper_proxy_title),
                            style = BodyMd.copy(fontSize = 13.sp),
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(SurfaceElevated, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.settings_scraper_cloudflare_bypass),
                                style = CodeSkuStyle.copy(fontSize = 9.sp),
                                color = ConsoleGamecube
                            )
                        }
                    }
                    Text(
                        text = stringResource(Res.string.settings_scraper_proxy_desc),
                        style = BodySm.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isScraperEnabled,
                    onCheckedChange = onScraperEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ConsoleGamecube,
                        uncheckedThumbColor = StatusUnverifiedFg,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }

            if (isScraperEnabled) {
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_scraper_provider_label),
                        style = LabelFilterStyle,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ScraperProvider.entries.forEach { provider ->
                            val isSelected = scraperProvider == provider
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) SurfaceElevated else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) BorderStrong else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { onScraperProviderChange(provider) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provider.displayName,
                                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                when (scraperProvider) {
                    ScraperProvider.SCRAPE_DO -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(Res.string.settings_scraper_token_label),
                                        style = LabelFilterStyle,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = stringResource(Res.string.settings_scraper_free_req),
                                        style = CodeSkuStyle.copy(fontSize = 10.sp),
                                        color = StatusEnglishFg
                                    )
                                }
                                TactileTextField(
                                    value = scrapeDoKey,
                                    onValueChange = onScrapeDoKeyChange,
                                    placeholder = "Paste your Scrape.do API Token here...",
                                    textStyle = CodeSkuStyle.copy(color = TextPrimary, fontSize = 11.sp),
                                    placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg, fontSize = 11.sp),
                                    backgroundColor = SurfaceCard,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = stringResource(Res.string.settings_scraper_token_hint),
                                    style = BodySm.copy(fontSize = 10.sp),
                                    color = TextSecondary
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard, RoundedCornerShape(4.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.settings_scraper_residential_proxy),
                                            style = LabelFilterStyle.copy(fontSize = 10.sp),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (scrapeDoSuperProxy) "25 credits/req" else "5 credits/req",
                                            style = CodeSkuStyle.copy(fontSize = 9.sp),
                                            color = if (scrapeDoSuperProxy) StatusEditionFg else StatusEnglishFg
                                        )
                                    }
                                    Text(
                                        text = stringResource(Res.string.settings_scraper_residential_hint),
                                        style = BodySm.copy(fontSize = 10.sp),
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = scrapeDoSuperProxy,
                                    onCheckedChange = onScrapeDoSuperProxyChange,
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
                    ScraperProvider.CUSTOM_PROXY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Res.string.settings_scraper_custom_url),
                                    style = LabelFilterStyle,
                                    color = TextPrimary
                                )
                                Text(
                                    text = stringResource(Res.string.settings_scraper_unlimited_free),
                                    style = CodeSkuStyle.copy(fontSize = 10.sp),
                                    color = StatusEnglishFg
                                )
                            }
                            TactileTextField(
                                value = customProxyUrl,
                                onValueChange = onCustomProxyUrlChange,
                                placeholder = "e.g. https://my-scraper.fly.dev/scrape?url=",
                                textStyle = CodeSkuStyle.copy(color = TextPrimary, fontSize = 11.sp),
                                placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg, fontSize = 11.sp),
                                backgroundColor = SurfaceCard,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = stringResource(Res.string.settings_scraper_custom_hint),
                                style = BodySm.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsFirebaseSection(
    firebaseProjectId: String,
    onFirebaseProjectIdChange: (String) -> Unit,
    isTestingFirestore: Boolean,
    firestoreStatusMessage: String?,
    onTestFirestore: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.settings_firebase_section).uppercase(),
                style = LabelFilterStyle.copy(fontSize = 11.sp),
                color = StatusUnverifiedFg
            )
            Text(
                text = stringResource(Res.string.settings_firebase_subtitle),
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
                Text(text = stringResource(Res.string.settings_firebase_project_id_label), style = LabelFilterStyle, color = TextPrimary)
                TactileTextField(
                    value = firebaseProjectId,
                    onValueChange = onFirebaseProjectIdChange,
                    placeholder = stringResource(Res.string.settings_firebase_project_id_hint),
                    textStyle = CodeSkuStyle.copy(color = TextPrimary),
                    placeholderStyle = CodeSkuStyle.copy(color = StatusUnverifiedFg),
                    backgroundColor = SurfaceCard,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(Res.string.settings_firebase_desc),
                    style = BodySm.copy(fontSize = 11.sp),
                    color = TextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTestFirestore,
                    enabled = !isTestingFirestore,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isTestingFirestore) "⏳ Testing..." else "⚡ " + stringResource(Res.string.settings_firebase_test_button).uppercase(),
                        style = LabelFilterStyle,
                        color = TextPrimary
                    )
                }

                firestoreStatusMessage?.let { msg ->
                    val statusConnectedStr = stringResource(Res.string.settings_firebase_status_connected)
                    val isTesting = isTestingFirestore || msg.startsWith("Connecting")
                    val isSuccess = msg == statusConnectedStr || msg.contains("successfully", ignoreCase = true)
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsPlatformsSection(
    pinnedPlatformIds: Set<String>,
    onPinnedPlatformIdsChange: (Set<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.settings_platforms_section).uppercase(),
                style = LabelFilterStyle.copy(fontSize = 11.sp),
                color = StatusUnverifiedFg,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(1f, fill = false)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(SurfaceBase, RoundedCornerShape(4.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                        .clickable { onPinnedPlatformIdsChange(defaultPinnedPlatformIds.toSet()) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.settings_platforms_select_defaults),
                        style = CodeSkuStyle.copy(fontSize = 10.sp),
                        color = StatusEnglishFg,
                        softWrap = false,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .background(SurfaceBase, RoundedCornerShape(4.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                        .clickable { onPinnedPlatformIdsChange(ConsolePlatform.entries.map { it.id }.toSet()) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.settings_platforms_pin_all),
                        style = CodeSkuStyle.copy(fontSize = 10.sp),
                        color = ConsoleGamecube,
                        softWrap = false,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .background(SurfaceBase, RoundedCornerShape(4.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                        .clickable { onPinnedPlatformIdsChange(emptySet()) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.settings_platforms_clear_all),
                        style = CodeSkuStyle.copy(fontSize = 10.sp),
                        color = StatusRiskFg,
                        softWrap = false,
                        maxLines = 1
                    )
                }
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
            Text(
                text = stringResource(Res.string.settings_platforms_subtitle),
                style = BodySm.copy(fontSize = 11.sp),
                color = TextSecondary
            )

            PlatformEcosystem.entries.forEach { eco ->
                val ecoPlatforms = remember(eco) {
                    ConsolePlatform.entries.filter { it.ecosystem == eco }
                }
                val ecoColor = Color(eco.brandColorHex)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = eco.icon, fontSize = 13.sp)
                        Text(
                            text = eco.displayName.uppercase(),
                            style = LabelFilterStyle.copy(fontSize = 11.sp),
                            color = ecoColor
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ecoPlatforms.forEach { platform ->
                            val isPinned = pinnedPlatformIds.contains(platform.id)
                            val pColor = Color(platform.brandColorHex)

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isPinned) SurfaceElevated else SurfaceBase,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isPinned) pColor else BorderSubtle,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        onPinnedPlatformIdsChange(
                                            if (isPinned) pinnedPlatformIds - platform.id else pinnedPlatformIds + platform.id
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(if (isPinned) pColor else StatusUnverifiedFg, RoundedCornerShape(3.dp))
                                    )
                                    Text(
                                        text = platform.shortName,
                                        style = CodeSkuStyle.copy(fontSize = 11.sp),
                                        color = if (isPinned) TextPrimary else TextSecondary
                                    )
                                    if (isPinned) {
                                        Text(text = "📌", fontSize = 10.sp)
                                    }
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
fun SettingsSaveButton(onSave: () -> Unit) {
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = StatusEnglishFg),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        Text(
            text = "💾 " + stringResource(Res.string.settings_save_button),
            style = LabelFilterStyle.copy(fontSize = 13.sp),
            color = Color.White
        )
    }
}
