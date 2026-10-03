package com.retrocollector.app.core.data.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiChatMessage>,
    val temperature: Float? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null
)

@Serializable
data class OpenAiChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: JsonElement
)

@Serializable
data class OpenAiContentPart(
    val type: String, // "text" or "image_url"
    val text: String? = null,
    @SerialName("image_url") val imageUrl: OpenAiImageUrl? = null
)

@Serializable
data class OpenAiImageUrl(
    val url: String // "data:image/jpeg;base64,..." or URL
)

@Serializable
data class OpenAiChatResponse(
    val id: String? = null,
    val choices: List<OpenAiChoice>? = null,
    val error: OpenAiError? = null
)

@Serializable
data class OpenAiChoice(
    val index: Int? = null,
    val message: OpenAiResponseMessage? = null
)

@Serializable
data class OpenAiResponseMessage(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class OpenAiError(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null
)
