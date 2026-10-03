package com.retrocollector.app.core.data.anthropic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    val system: String? = null,
    val messages: List<AnthropicMessage>,
    val temperature: Float? = null
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: List<AnthropicContentBlock>
)

@Serializable
data class AnthropicContentBlock(
    val type: String, // "text" or "image"
    val text: String? = null,
    val source: AnthropicImageSource? = null
)

@Serializable
data class AnthropicImageSource(
    val type: String = "base64",
    @SerialName("media_type") val mediaType: String = "image/jpeg",
    val data: String
)

@Serializable
data class AnthropicResponse(
    val id: String? = null,
    val content: List<AnthropicResponseContent>? = null,
    val error: AnthropicError? = null
)

@Serializable
data class AnthropicResponseContent(
    val type: String? = null,
    val text: String? = null
)

@Serializable
data class AnthropicError(
    val type: String? = null,
    val message: String? = null
)
