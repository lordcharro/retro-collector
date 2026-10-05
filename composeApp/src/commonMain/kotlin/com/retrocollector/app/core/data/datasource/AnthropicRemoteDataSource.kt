package com.retrocollector.app.core.data.datasource

import com.retrocollector.app.core.data.anthropic.*
import com.retrocollector.app.core.data.datasource.ai.AiPromptConstants
import com.retrocollector.app.core.data.datasource.ai.AiResponseParser
import com.retrocollector.app.core.data.datasource.ai.AiResponseParser.toDiscoveredGameItem
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
import kotlin.time.Clock
import kotlinx.serialization.json.Json

class AnthropicRemoteDataSource(
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

    override val provider: AiProvider = AiProvider.CLAUDE

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
            return Result.failure(IllegalArgumentException("Anthropic API key is not configured."))
        }

        val targetModel = model.ifBlank { "claude-3-7-sonnet-20250219" }

        return try {
            val requestBody = AnthropicRequest(
                model = targetModel,
                maxTokens = 10,
                messages = listOf(
                    AnthropicMessage(
                        role = "user",
                        content = listOf(AnthropicContentBlock(type = "text", text = "ping"))
                    )
                )
            )

            val endpoint = baseUrl?.ifBlank { null } ?: "https://api.anthropic.com/v1/messages"
            val response = client.post(endpoint) {
                timeout {
                    requestTimeoutMillis = 10_000
                    connectTimeoutMillis = 10_000
                    socketTimeoutMillis = 10_000
                }
                contentType(ContentType.Application.Json)
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorText)
                Result.failure(Exception(errorMsg ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            } else {
                val anthropicResponse: AnthropicResponse = response.body()
                if (anthropicResponse.error != null) {
                    Result.failure(Exception("Anthropic API: ${anthropicResponse.error.message ?: anthropicResponse.error.type}"))
                } else {
                    Result.success("Connection to Claude ($targetModel) established successfully!")
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            Result.failure(Exception("Connection timeout (10s). Check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Anthropic connection error"))
        }
    }

    override suspend fun inspectGame(
        query: String,
        imageBase64: String?,
        apiKey: String,
        model: String,
        baseUrl: String?,
        imagesBase64: List<String>
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Anthropic API key is not configured. Access Settings to set up the key."))
        }

        val allImages = (listOfNotNull(imageBase64) + imagesBase64).distinct()
        val targetModel = model.ifBlank { "claude-3-7-sonnet-20250219" }
        val startTime = Clock.System.now().toEpochMilliseconds()

        return try {
            val contentBlocks = mutableListOf<AnthropicContentBlock>()
            allImages.forEach { rawImg ->
                val sanitizedBase64 = AiResponseParser.sanitizeBase64(rawImg)
                if (sanitizedBase64 != null) {
                    contentBlocks.add(
                        AnthropicContentBlock(
                            type = "image",
                            source = AnthropicImageSource(type = "base64", mediaType = "image/jpeg", data = sanitizedBase64)
                        )
                    )
                }
            }
            contentBlocks.add(AnthropicContentBlock(type = "text", text = query))

            val requestBody = AnthropicRequest(
                model = targetModel,
                maxTokens = 4096,
                system = AiPromptConstants.tacticalSystemPrompt,
                messages = listOf(AnthropicMessage(role = "user", content = contentBlocks)),
                temperature = 0.2f
            )

            val endpoint = baseUrl?.ifBlank { null } ?: "https://api.anthropic.com/v1/messages"
            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val anthropicResponse: AnthropicResponse = response.body()
            if (anthropicResponse.error != null) {
                return Result.failure(Exception("Claude Error: ${anthropicResponse.error.message}"))
            }

            val rawResponseText = anthropicResponse.content
                ?.filter { it.type == "text" }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Empty response from Claude API."))

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

    override suspend fun sendFollowUpChat(
        history: List<ChatMessage>,
        game: GameItem?,
        userMessage: String,
        imageBase64: String?,
        apiKey: String,
        model: String,
        baseUrl: String?,
        imagesBase64: List<String>
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Anthropic API key is not configured. Access Settings to set up the key."))
        }

        val allImages = (listOfNotNull(imageBase64) + imagesBase64).distinct()
        val targetModel = model.ifBlank { "claude-3-7-sonnet-20250219" }
        val startTime = Clock.System.now().toEpochMilliseconds()

        return try {
            val promptText = AiPromptConstants.buildFollowUpPrompt(game, history, userMessage)
            val contentBlocks = mutableListOf<AnthropicContentBlock>()

            allImages.forEach { rawImg ->
                val sanitizedBase64 = AiResponseParser.sanitizeBase64(rawImg)
                if (sanitizedBase64 != null) {
                    contentBlocks.add(
                        AnthropicContentBlock(
                            type = "image",
                            source = AnthropicImageSource(type = "base64", mediaType = "image/jpeg", data = sanitizedBase64)
                        )
                    )
                }
            }
            contentBlocks.add(AnthropicContentBlock(type = "text", text = promptText))

            val requestBody = AnthropicRequest(
                model = targetModel,
                maxTokens = 4096,
                system = AiPromptConstants.conversationalSystemPrompt,
                messages = listOf(AnthropicMessage(role = "user", content = contentBlocks)),
                temperature = 0.2f
            )

            val endpoint = baseUrl?.ifBlank { null } ?: "https://api.anthropic.com/v1/messages"
            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val anthropicResponse: AnthropicResponse = response.body()
            if (anthropicResponse.error != null) {
                return Result.failure(Exception("Claude Error: ${anthropicResponse.error.message}"))
            }

            val rawResponseText = anthropicResponse.content
                ?.filter { it.type == "text" }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Empty response from Claude API."))

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

    override suspend fun discoverGames(
        query: String?,
        genre: GameGenre?,
        platform: ConsolePlatform?,
        apiKey: String,
        model: String,
        baseUrl: String?
    ): Result<List<DiscoveredGameItem>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Anthropic API key is not configured. Access Settings to set up the key."))
        }

        val targetModel = model.ifBlank { "claude-3-7-sonnet-20250219" }

        return try {
            val userPrompt = AiPromptConstants.buildDiscoveryUserPrompt(query, genre, platform)
            val requestBody = AnthropicRequest(
                model = targetModel,
                maxTokens = 4096,
                system = AiPromptConstants.discoverySystemPrompt,
                messages = listOf(
                    AnthropicMessage(
                        role = "user",
                        content = listOf(AnthropicContentBlock(type = "text", text = userPrompt))
                    )
                ),
                temperature = 0.3f
            )

            val endpoint = baseUrl?.ifBlank { null } ?: "https://api.anthropic.com/v1/messages"
            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val anthropicResponse: AnthropicResponse = response.body()
            if (anthropicResponse.error != null) {
                return Result.failure(Exception("Claude Error: ${anthropicResponse.error.message}"))
            }

            val rawResponseText = anthropicResponse.content
                ?.filter { it.type == "text" }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Empty response from Claude API."))

            val rawList = AiResponseParser.extractJsonDiscoveryList(rawResponseText)
            val items = rawList.map { it.toDiscoveredGameItem() }
            Result.success(items)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
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
            return Result.failure(IllegalArgumentException("Anthropic API key is not configured. Access Settings to set up the key."))
        }

        val targetModel = model.ifBlank { "claude-3-7-sonnet-20250219" }

        return try {
            val userPrompt = AiPromptConstants.buildSimilarGamesUserPrompt(gameTitle, platform, genre)
            val requestBody = AnthropicRequest(
                model = targetModel,
                maxTokens = 4096,
                system = AiPromptConstants.similarGamesSystemPrompt,
                messages = listOf(
                    AnthropicMessage(
                        role = "user",
                        content = listOf(AnthropicContentBlock(type = "text", text = userPrompt))
                    )
                ),
                temperature = 0.3f
            )

            val endpoint = baseUrl?.ifBlank { null } ?: "https://api.anthropic.com/v1/messages"
            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val anthropicResponse: AnthropicResponse = response.body()
            if (anthropicResponse.error != null) {
                return Result.failure(Exception("Claude Error: ${anthropicResponse.error.message}"))
            }

            val rawResponseText = anthropicResponse.content
                ?.filter { it.type == "text" }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Empty response from Claude API."))

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
            jsonParser.decodeFromString<AnthropicResponse>(errorBody).error?.message
        } catch (_: Exception) {
            null
        }
    }
}
