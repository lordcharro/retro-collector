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
    fun addOrUpdateOffer(gameId: String, offer: com.retrocollector.app.core.domain.model.GameOffer)
    fun deleteOffer(gameId: String, offerId: String)
    fun convertOfferToOwned(
        gameId: String,
        offerId: String?,
        finalPriceChf: Double,
        condition: com.retrocollector.app.core.domain.model.GameCondition
    )
    fun getChatMessagesForGame(gameId: String): List<ChatMessage>
    fun addChatMessage(message: ChatMessage)
    fun deleteChatMessage(messageId: String) {}
    fun updateSettings(settings: AppSettings)
    suspend fun syncFromFirestore(): Result<Unit>
    suspend fun testAiConnection(
        provider: com.retrocollector.app.settings.domain.model.AiProvider,
        apiKey: String,
        model: String,
        baseUrl: String? = null
    ): Result<String> = Result.success("OK")

    suspend fun testGeminiConnection(apiKey: String, model: String = "gemini-3.7-flash"): Result<String> =
        testAiConnection(com.retrocollector.app.settings.domain.model.AiProvider.GEMINI, apiKey, model, null)

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

    fun getGamesByStatus(status: com.retrocollector.app.core.domain.model.CollectionStatus): List<GameItem>

    suspend fun discoverGames(
        query: String? = null,
        genre: com.retrocollector.app.core.domain.model.GameGenre? = null,
        platform: com.retrocollector.app.core.domain.model.ConsolePlatform? = null,
        forceRefresh: Boolean = false
    ): Result<List<com.retrocollector.app.core.domain.model.DiscoveredGameItem>>

    suspend fun getSimilarGames(
        game: GameItem,
        forceRefresh: Boolean = false
    ): Result<List<com.retrocollector.app.core.domain.model.DiscoveredGameItem>>

    fun getCuratedGames(
        genre: com.retrocollector.app.core.domain.model.GameGenre? = null,
        platform: com.retrocollector.app.core.domain.model.ConsolePlatform? = null,
        query: String? = null
    ): List<com.retrocollector.app.core.domain.model.DiscoveredGameItem> = emptyList()

    fun getCuratedSimilarGames(
        game: GameItem
    ): List<com.retrocollector.app.core.domain.model.DiscoveredGameItem> = emptyList()
}
