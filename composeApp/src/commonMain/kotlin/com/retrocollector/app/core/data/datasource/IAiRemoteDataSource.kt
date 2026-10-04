package com.retrocollector.app.core.data.datasource

import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.settings.domain.model.AiProvider

interface IAiRemoteDataSource {
    val provider: AiProvider

    suspend fun testConnection(
        apiKey: String,
        model: String,
        baseUrl: String? = null
    ): Result<String>

    suspend fun inspectGame(
        query: String,
        imageBase64: String? = null,
        apiKey: String,
        model: String,
        baseUrl: String? = null,
        imagesBase64: List<String> = emptyList()
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>>

    suspend fun sendFollowUpChat(
        history: List<ChatMessage>,
        game: GameItem?,
        userMessage: String,
        imageBase64: String?,
        apiKey: String,
        model: String,
        baseUrl: String? = null,
        imagesBase64: List<String> = emptyList()
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>>

    suspend fun discoverGames(
        query: String? = null,
        genre: GameGenre? = null,
        platform: ConsolePlatform? = null,
        apiKey: String,
        model: String,
        baseUrl: String? = null
    ): Result<List<DiscoveredGameItem>>

    suspend fun fetchSimilarGames(
        gameTitle: String,
        platform: ConsolePlatform,
        genre: String? = null,
        apiKey: String,
        model: String,
        baseUrl: String? = null
    ): Result<List<DiscoveredGameItem>>
}
