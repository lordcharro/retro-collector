package com.retrocollector.app.core.data.gemini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("system_instruction") val systemInstruction: GeminiContent? = null,
    @SerialName("generation_config") val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    val thought: Boolean? = null,
    @SerialName("inline_data") val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String // Base64
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float = 0.2f
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null
)

@Serializable
data class GeminiStructuredVerdict(
    val title: String = "",
    val franchise: String = "",
    val platform: String = "PS3",
    val productCode: String? = null,
    val languageStatus: String = "UNKNOWN", // FULL_ENGLISH, SUBTITLES_ONLY, GERMAN_ONLY, DEPENDS_ON_EDITION
    val audioLanguages: List<String> = emptyList(),
    val subtitleLanguages: List<String> = emptyList(),
    val languageNotes: String = "",
    val recommendedEdition: String = "",
    val swissMarketWarning: String = "",
    val collectionAdvice: String = ""
)
