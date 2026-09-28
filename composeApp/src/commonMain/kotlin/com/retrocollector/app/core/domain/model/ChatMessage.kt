package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageSender {
    USER,
    GEMINI
}

@Serializable
data class ChatMessage(
    val id: String,
    val contextId: String, // Game ID or Franchise ID
    val sender: MessageSender,
    val text: String,
    val imageBase64: String? = null,
    val timestamp: Long = 0L,
    val suggestedGameUpdate: GameItem? = null
)
