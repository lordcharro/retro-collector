package com.retrocollector.app.core.domain.repository

import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface IGameRepository {
    val games: StateFlow<List<GameItem>>
    val chatMessages: StateFlow<List<ChatMessage>>
    val settings: StateFlow<AppSettings>

    fun getGameById(id: String): GameItem?
    fun upsertGame(game: GameItem)
    fun deleteGame(id: String)
    fun getChatMessagesForGame(gameId: String): List<ChatMessage>
    fun addChatMessage(message: ChatMessage)
    fun updateSettings(settings: AppSettings)
    suspend fun syncFromFirestore(): Result<Unit>
    suspend fun testGeminiConnection(apiKey: String): Result<String>

    suspend fun inspectGameWithAi(
        query: String,
        imageBase64: String?,
        spottedLocation: String,
        askingPriceChf: Double?
    ): Result<Pair<ChatMessage, GameItem?>>

    suspend fun sendFollowUpChat(
        contextId: String,
        userMessage: String,
        imageBase64: String?
    ): Result<ChatMessage>
}
