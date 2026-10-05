package com.retrocollector.app.core.data.datasource

import com.retrocollector.app.core.data.datasource.ai.AiPromptConstants
import com.retrocollector.app.core.data.datasource.ai.AiResponseParser
import com.retrocollector.app.core.data.datasource.ai.AiResponseParser.toDiscoveredGameItem
import com.retrocollector.app.core.data.openai.*
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
import kotlinx.serialization.json.*

class OpenAiCompatibleRemoteDataSource(
    override val provider: AiProvider = AiProvider.OPENAI_COMPATIBLE,
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

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun resolveEndpoint(baseUrl: String?): String {
        val base = baseUrl?.trim()?.ifBlank { null } ?: "https://api.openai.com/v1"
        val trimmed = base.trimEnd('/')
        return if (trimmed.endsWith("/chat/completions")) {
            trimmed
        } else {
            "$trimmed/chat/completions"
        }
    }

    override suspend fun testConnection(
        apiKey: String,
        model: String,
        baseUrl: String?
    ): Result<String> {
        val targetModel = model.ifBlank { "gpt-4o" }
        val endpoint = resolveEndpoint(baseUrl)

        return try {
            val requestBody = OpenAiChatRequest(
                model = targetModel,
                maxTokens = 10,
                messages = listOf(
                    OpenAiChatMessage(role = "user", content = JsonPrimitive("ping"))
                )
            )

            val response = client.post(endpoint) {
                timeout {
                    requestTimeoutMillis = 10_000
                    connectTimeoutMillis = 10_000
                    socketTimeoutMillis = 10_000
                }
                contentType(ContentType.Application.Json)
                if (apiKey.isNotBlank()) {
                    header("Authorization", "Bearer $apiKey")
                }
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                val errorMsg = parseErrorMessage(errorText)
                Result.failure(Exception(errorMsg ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            } else {
                val openAiResponse: OpenAiChatResponse = response.body()
                if (openAiResponse.error != null) {
                    Result.failure(Exception("AI API: ${openAiResponse.error.message}"))
                } else {
                    Result.success("Connection to $targetModel ($endpoint) established successfully!")
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            Result.failure(Exception("Connection timeout (10s). Check network connectivity & server URL."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "AI endpoint connection error"))
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
        val targetModel = model.ifBlank { "gpt-4o" }
        val endpoint = resolveEndpoint(baseUrl)
        val startTime = Clock.System.now().toEpochMilliseconds()

        val allImages = (listOfNotNull(imageBase64) + imagesBase64).distinct()

        return try {
            val userContent = buildUserContent(query, allImages)
            val messages = listOf(
                OpenAiChatMessage(role = "system", content = JsonPrimitive(AiPromptConstants.tacticalSystemPrompt)),
                OpenAiChatMessage(role = "user", content = userContent)
            )

            val requestBody = OpenAiChatRequest(
                model = targetModel,
                maxTokens = 4096,
                messages = messages,
                temperature = 0.2f
            )

            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                if (apiKey.isNotBlank()) {
                    header("Authorization", "Bearer $apiKey")
                }
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val openAiResponse: OpenAiChatResponse = response.body()
            if (openAiResponse.error != null) {
                return Result.failure(Exception("AI Error: ${openAiResponse.error.message}"))
            }

            val rawResponseText = openAiResponse.choices?.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from AI endpoint."))

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
        val targetModel = model.ifBlank { "gpt-4o" }
        val endpoint = resolveEndpoint(baseUrl)
        val startTime = Clock.System.now().toEpochMilliseconds()

        val allImages = (listOfNotNull(imageBase64) + imagesBase64).distinct()

        return try {
            val promptText = AiPromptConstants.buildFollowUpPrompt(game, history, userMessage)
            val userContent = buildUserContent(promptText, allImages)

            val messages = listOf(
                OpenAiChatMessage(role = "system", content = JsonPrimitive(AiPromptConstants.conversationalSystemPrompt)),
                OpenAiChatMessage(role = "user", content = userContent)
            )

            val requestBody = OpenAiChatRequest(
                model = targetModel,
                maxTokens = 4096,
                messages = messages,
                temperature = 0.2f
            )

            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                if (apiKey.isNotBlank()) {
                    header("Authorization", "Bearer $apiKey")
                }
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val openAiResponse: OpenAiChatResponse = response.body()
            if (openAiResponse.error != null) {
                return Result.failure(Exception("AI Error: ${openAiResponse.error.message}"))
            }

            val rawResponseText = openAiResponse.choices?.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from AI endpoint."))

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
        val targetModel = model.ifBlank { "gpt-4o" }
        val endpoint = resolveEndpoint(baseUrl)

        return try {
            val userPrompt = AiPromptConstants.buildDiscoveryUserPrompt(query, genre, platform)
            val messages = listOf(
                OpenAiChatMessage(role = "system", content = JsonPrimitive(AiPromptConstants.discoverySystemPrompt)),
                OpenAiChatMessage(role = "user", content = JsonPrimitive(userPrompt))
            )

            val requestBody = OpenAiChatRequest(
                model = targetModel,
                maxTokens = 4096,
                messages = messages,
                temperature = 0.3f
            )

            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                if (apiKey.isNotBlank()) {
                    header("Authorization", "Bearer $apiKey")
                }
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val openAiResponse: OpenAiChatResponse = response.body()
            if (openAiResponse.error != null) {
                return Result.failure(Exception("AI Error: ${openAiResponse.error.message}"))
            }

            val rawResponseText = openAiResponse.choices?.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from AI endpoint."))

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
        val targetModel = model.ifBlank { "gpt-4o" }
        val endpoint = resolveEndpoint(baseUrl)

        return try {
            val userPrompt = AiPromptConstants.buildSimilarGamesUserPrompt(gameTitle, platform, genre)
            val messages = listOf(
                OpenAiChatMessage(role = "system", content = JsonPrimitive(AiPromptConstants.similarGamesSystemPrompt)),
                OpenAiChatMessage(role = "user", content = JsonPrimitive(userPrompt))
            )

            val requestBody = OpenAiChatRequest(
                model = targetModel,
                maxTokens = 4096,
                messages = messages,
                temperature = 0.3f
            )

            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                if (apiKey.isNotBlank()) {
                    header("Authorization", "Bearer $apiKey")
                }
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                val parsedError = parseErrorMessage(errorBody)
                return Result.failure(Exception(parsedError ?: "HTTP Error ${response.status.value}: ${response.status.description}"))
            }

            val openAiResponse: OpenAiChatResponse = response.body()
            if (openAiResponse.error != null) {
                return Result.failure(Exception("AI Error: ${openAiResponse.error.message}"))
            }

            val rawResponseText = openAiResponse.choices?.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from AI endpoint."))

            val rawList = AiResponseParser.extractJsonDiscoveryList(rawResponseText)
            val items = rawList.map { it.toDiscoveredGameItem() }
            Result.success(items)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildUserContent(text: String, imagesBase64: List<String>): JsonElement {
        val sanitizedImages = imagesBase64.mapNotNull { AiResponseParser.sanitizeBase64(it) }
        if (sanitizedImages.isEmpty()) {
            return JsonPrimitive(text)
        }

        val jsonArray = buildJsonArray {
            add(buildJsonObject {
                put("type", "text")
                put("text", text)
            })
            sanitizedImages.forEach { sanitized ->
                add(buildJsonObject {
                    put("type", "image_url")
                    put("image_url", buildJsonObject {
                        put("url", "data:image/jpeg;base64,$sanitized")
                    })
                })
            }
        }
        return jsonArray
    }

    private fun parseErrorMessage(errorBody: String): String? {
        return try {
            jsonParser.decodeFromString<OpenAiChatResponse>(errorBody).error?.message
        } catch (_: Exception) {
            null
        }
    }
}
