package com.retrocollector.app.core.data.datasource

import com.retrocollector.app.core.data.datasource.ai.AiPromptConstants
import com.retrocollector.app.core.data.datasource.ai.AiResponseParser
import com.retrocollector.app.core.data.datasource.ai.AiResponseParser.toDiscoveredGameItem
import com.retrocollector.app.core.data.gemini.*
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.settings.domain.model.AiProvider
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StitchGeminiStructuredVerdict(
    val title: String = "",
    val franchise: String = "",
    val platform: String = "GAMECUBE",
    val releaseYear: String = "",
    val productCode: String? = null,
    val barcode: String? = null,
    val languageStatus: String = "UNVERIFIED", // FULL_ENGLISH, SUBS_ONLY, GERMAN_ONLY, EDITION_NOTICE
    val audioLanguages: List<String> = emptyList(),
    val subtitleLanguages: List<String> = emptyList(),
    val safeSkus: List<SkuInfo> = emptyList(),
    val riskySkus: List<SkuInfo> = emptyList(),
    val swissMarketMedianChf: Double? = null,
    val historicalMinChf: Double? = null,
    val historicalMaxChf: Double? = null,
    val censorshipWarning: String? = null,
    val collectorVerdict: String = "",
    val latencySeconds: Double = 0.45
)

@Serializable
data class StitchGeminiDiscoveryGame(
    val title: String = "",
    val franchise: String = "",
    val platform: String = "GAMECUBE",
    val releaseYear: String = "",
    val genreTags: List<String> = emptyList(),
    val recommendationReason: String = "",
    val languageStatus: String = "FULL_ENGLISH",
    val safeSkus: List<SkuInfo> = emptyList(),
    val hasModernPortOrRemaster: Boolean = false,
    val modernPortDetails: String? = null,
    val estimatedPriceChf: Double? = null,
    val similarTitles: List<String> = emptyList()
)

@Serializable
data class StitchGeminiDiscoveryContainer(
    val games: List<StitchGeminiDiscoveryGame> = emptyList()
)

class GeminiRemoteDataSource(
    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 60000
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) : IAiRemoteDataSource {

    override val provider: AiProvider = AiProvider.GEMINI

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun testConnection(
        apiKey: String,
        model: String,
        baseUrl: String?
    ): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Gemini API key is not configured."))
        }

        val targetModel = model.ifBlank { "gemini-3.7-flash" }

        return try {
            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = "ping"))))
            )
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"
            val response = client.post(url) {
                timeout {
                    requestTimeoutMillis = 10_000
                    connectTimeoutMillis = 10_000
                    socketTimeoutMillis = 10_000
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                val errorMsg = try {
                    jsonParser.decodeFromString<GeminiResponse>(errorText).error?.message
                } catch (_: Exception) { null }
                Result.failure(Exception(errorMsg ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            } else {
                val geminiResponse: GeminiResponse = response.body()
                if (geminiResponse.error != null) {
                    Result.failure(Exception("Google API: ${geminiResponse.error.message ?: "Error (${geminiResponse.error.code})" }"))
                } else {
                    Result.success("Connection to $targetModel established successfully!")
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            Result.failure(Exception("Connection timeout (10s). Check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "API connection error"))
        }
    }

    override suspend fun inspectGame(
        query: String,
        imageBase64: String?,
        apiKey: String,
        model: String,
        baseUrl: String?
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Gemini API key is not configured. Access Settings to set up the key."))
        }

        val primaryModel = model.ifBlank { "gemini-3.7-flash" }
        val result = executeInspect(query, imageBase64, apiKey, primaryModel)

        // If it fails due to temporary high demand on the requested model, attempt automatic fallback
        if (result.isFailure) {
            val errMsg = result.exceptionOrNull()?.message.orEmpty()
            if (errMsg.contains("demand", ignoreCase = true) || errMsg.contains("503") || errMsg.contains("unavailable", ignoreCase = true)) {
                val fallbackModel = if (primaryModel == "gemini-3.7-flash") "gemini-3.6-flash" else "gemini-3.7-flash"
                return executeInspect(query, imageBase64, apiKey, fallbackModel)
            }
        }
        return result
    }

    override suspend fun sendFollowUpChat(
        history: List<ChatMessage>,
        game: GameItem?,
        userMessage: String,
        imageBase64: String?,
        apiKey: String,
        model: String,
        baseUrl: String?
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Gemini API key is not configured. Access Settings to set up the key."))
        }

        val primaryModel = model.ifBlank { "gemini-3.7-flash" }
        val result = executeFollowUpChat(history, game, userMessage, imageBase64, apiKey, primaryModel)

        if (result.isFailure) {
            val errMsg = result.exceptionOrNull()?.message.orEmpty()
            if (errMsg.contains("demand", ignoreCase = true) || errMsg.contains("503") || errMsg.contains("unavailable", ignoreCase = true)) {
                val fallbackModel = if (primaryModel == "gemini-3.7-flash") "gemini-3.6-flash" else "gemini-3.7-flash"
                return executeFollowUpChat(history, game, userMessage, imageBase64, apiKey, fallbackModel)
            }
        }
        return result
    }

    override suspend fun discoverGames(
        query: String?,
        genre: GameGenre?,
        platform: ConsolePlatform?,
        apiKey: String,
        model: String,
        baseUrl: String?
    ): Result<List<DiscoveredGameItem>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Gemini API key is not configured. Access Settings to set up the key."))
        }

        val primaryModel = model.ifBlank { "gemini-3.7-flash" }
        val result = executeDiscoverGames(query, genre, platform, apiKey, primaryModel)

        if (result.isFailure) {
            val errMsg = result.exceptionOrNull()?.message.orEmpty()
            if (errMsg.contains("demand", ignoreCase = true) || errMsg.contains("503") || errMsg.contains("unavailable", ignoreCase = true)) {
                val fallbackModel = if (primaryModel == "gemini-3.7-flash") "gemini-3.6-flash" else "gemini-3.7-flash"
                return executeDiscoverGames(query, genre, platform, apiKey, fallbackModel)
            }
        }
        return result
    }

    override suspend fun fetchSimilarGames(
        gameTitle: String,
        platform: ConsolePlatform,
        genre: String?,
        apiKey: String,
        model: String,
        baseUrl: String?
    ): Result<List<DiscoveredGameItem>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Gemini API key is not configured. Access Settings to set up the key."))
        }

        val primaryModel = model.ifBlank { "gemini-3.7-flash" }
        val result = executeFetchSimilarGames(gameTitle, platform, genre, apiKey, primaryModel)

        if (result.isFailure) {
            val errMsg = result.exceptionOrNull()?.message.orEmpty()
            if (errMsg.contains("demand", ignoreCase = true) || errMsg.contains("503") || errMsg.contains("unavailable", ignoreCase = true)) {
                val fallbackModel = if (primaryModel == "gemini-3.7-flash") "gemini-3.6-flash" else "gemini-3.7-flash"
                return executeFetchSimilarGames(gameTitle, platform, genre, apiKey, fallbackModel)
            }
        }
        return result
    }

    private suspend fun executeInspect(
        query: String,
        imageBase64: String?,
        apiKey: String,
        model: String
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>> {
        val startTime = Clock.System.now().toEpochMilliseconds()

        return try {
            val parts = mutableListOf<GeminiPart>()
            parts.add(GeminiPart(text = query))

            val sanitizedBase64 = AiResponseParser.sanitizeBase64(imageBase64)
            if (sanitizedBase64 != null) {
                parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = sanitizedBase64)))
            }

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = parts)),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = AiPromptConstants.tacticalSystemPrompt)))
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            if (geminiResponse.error != null) {
                return Result.failure(Exception("Gemini Error (${geminiResponse.error.code}): ${geminiResponse.error.message}"))
            }

            val candidate = geminiResponse.candidates?.firstOrNull()
            val rawResponseText = candidate?.content?.parts
                ?.filter { it.thought != true }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Empty response from Gemini API."))

            val durationSeconds = ((Clock.System.now().toEpochMilliseconds() - startTime) / 1000.0)
            val verdict = AiResponseParser.extractJsonVerdict(rawResponseText)?.copy(latencySeconds = durationSeconds)
            val cleanText = AiResponseParser.cleanResponseText(rawResponseText).ifBlank { rawResponseText }

            Result.success(Pair(cleanText, verdict))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun executeFollowUpChat(
        history: List<ChatMessage>,
        game: GameItem?,
        userMessage: String,
        imageBase64: String?,
        apiKey: String,
        model: String
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>> {
        val startTime = Clock.System.now().toEpochMilliseconds()

        return try {
            val promptText = AiPromptConstants.buildFollowUpPrompt(game, history, userMessage)
            val parts = mutableListOf(GeminiPart(text = promptText))

            val sanitizedBase64 = AiResponseParser.sanitizeBase64(imageBase64)
            if (sanitizedBase64 != null) {
                parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = sanitizedBase64)))
            }

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = parts)),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = AiPromptConstants.conversationalSystemPrompt)))
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            if (geminiResponse.error != null) {
                return Result.failure(Exception("Gemini Error (${geminiResponse.error.code}): ${geminiResponse.error.message}"))
            }

            val candidate = geminiResponse.candidates?.firstOrNull()
            val rawResponseText = candidate?.content?.parts
                ?.filter { it.thought != true }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Empty response from Gemini API."))

            val durationSeconds = ((Clock.System.now().toEpochMilliseconds() - startTime) / 1000.0)
            val verdict = AiResponseParser.extractJsonVerdict(rawResponseText)?.copy(latencySeconds = durationSeconds)
            val cleanText = AiResponseParser.cleanResponseText(rawResponseText).ifBlank { rawResponseText }

            Result.success(Pair(cleanText, verdict))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun executeDiscoverGames(
        query: String?,
        genre: GameGenre?,
        platform: ConsolePlatform?,
        apiKey: String,
        model: String
    ): Result<List<DiscoveredGameItem>> {
        return try {
            val userPrompt = AiPromptConstants.buildDiscoveryUserPrompt(query, genre, platform)
            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = AiPromptConstants.discoverySystemPrompt)))
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            if (geminiResponse.error != null) {
                return Result.failure(Exception("Gemini Error (${geminiResponse.error.code}): ${geminiResponse.error.message}"))
            }

            val candidate = geminiResponse.candidates?.firstOrNull()
            val rawResponseText = candidate?.content?.parts
                ?.filter { it.thought != true }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Empty response from Gemini API."))

            val rawList = AiResponseParser.extractJsonDiscoveryList(rawResponseText)
            val items = rawList.map { it.toDiscoveredGameItem() }
            Result.success(items)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun executeFetchSimilarGames(
        gameTitle: String,
        platform: ConsolePlatform,
        genre: String?,
        apiKey: String,
        model: String
    ): Result<List<DiscoveredGameItem>> {
        return try {
            val userPrompt = AiPromptConstants.buildSimilarGamesUserPrompt(gameTitle, platform, genre)

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = AiPromptConstants.similarGamesSystemPrompt)))
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            if (geminiResponse.error != null) {
                return Result.failure(Exception("Gemini Error (${geminiResponse.error.code}): ${geminiResponse.error.message}"))
            }

            val candidate = geminiResponse.candidates?.firstOrNull()
            val rawResponseText = candidate?.content?.parts
                ?.filter { it.thought != true }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Empty response from Gemini API."))

            val rawList = AiResponseParser.extractJsonDiscoveryList(rawResponseText)
            val items = rawList.map { it.toDiscoveredGameItem() }
            Result.success(items)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String): String? {
        return try {
            jsonParser.decodeFromString<GeminiResponse>(errorBody).error?.message
        } catch (_: Exception) {
            null
        }
    }
}
