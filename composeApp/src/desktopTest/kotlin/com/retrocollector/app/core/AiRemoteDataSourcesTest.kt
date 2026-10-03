package com.retrocollector.app.core

import com.retrocollector.app.core.data.datasource.AnthropicRemoteDataSource
import com.retrocollector.app.core.data.datasource.OpenAiCompatibleRemoteDataSource
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.settings.domain.model.AiProvider
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AiRemoteDataSourcesTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `AnthropicRemoteDataSource testConnection returns success`() = runTest {
        var capturedUrl = ""
        var capturedApiKey = ""
        val mockEngine = MockEngine { request ->
            capturedUrl = request.url.toString()
            capturedApiKey = request.headers["x-api-key"] ?: ""
            respond(
                content = """{"id":"msg_123","content":[{"type":"text","text":"pong"}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val dataSource = AnthropicRemoteDataSource(client)
        val result = dataSource.testConnection(
            apiKey = "test-api-key",
            model = "claude-3-7-sonnet-20250219"
        )

        assertTrue(result.isSuccess, "Expected success but was: ${result.exceptionOrNull()?.message}")
        assertTrue(result.getOrNull()?.contains("established successfully") == true)
        assertTrue(capturedUrl.contains("api.anthropic.com/v1/messages"), "URL was: $capturedUrl")
        assertEquals("test-api-key", capturedApiKey)
    }

    @Test
    fun `AnthropicRemoteDataSource inspectGame parses structured verdict`() = runTest {
        val sampleResponse = """
            {
                "id": "msg_456",
                "content": [
                    {
                        "type": "text",
                        "text": "Analysis complete for Mario Sunshine PAL.\n```json\n{\n  \"title\": \"Super Mario Sunshine\",\n  \"franchise\": \"Super Mario\",\n  \"platform\": \"GAMECUBE\",\n  \"releaseYear\": \"2002\",\n  \"productCode\": \"DOL-P-GMSE\",\n  \"languageStatus\": \"FULL_ENGLISH\",\n  \"swissMarketMedianChf\": 45.0,\n  \"collectorVerdict\": \"Safe European buy\"\n}\n```"
                    }
                ]
            }
        """.trimIndent()

        val mockEngine = MockEngine { _ ->
            respond(
                content = sampleResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val dataSource = AnthropicRemoteDataSource(client)
        val result = dataSource.inspectGame(
            query = "Super Mario Sunshine DOL-P-GMSE",
            imageBase64 = null,
            apiKey = "test-key",
            model = "claude-3-7-sonnet-20250219"
        )

        assertTrue(result.isSuccess)
        val (text, verdict) = result.getOrThrow()
        assertTrue(text.contains("Analysis complete"))
        assertNotNull(verdict)
        assertEquals("Super Mario Sunshine", verdict?.title)
        assertEquals("GAMECUBE", verdict?.platform)
        assertEquals(LanguageStatus.FULL_ENGLISH.name, verdict?.languageStatus)
        assertEquals(45.0, verdict?.swissMarketMedianChf)
    }

    @Test
    fun `OpenAiCompatibleRemoteDataSource testConnection with local Ollama url`() = runTest {
        var capturedUrl = ""
        val mockEngine = MockEngine { request ->
            capturedUrl = request.url.toString()
            respond(
                content = """{"id":"chatcmpl-123","choices":[{"message":{"role":"assistant","content":"pong"}}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val dataSource = OpenAiCompatibleRemoteDataSource(
            provider = AiProvider.LOCAL_OLLAMA,
            client = client
        )

        val result = dataSource.testConnection(
            apiKey = "",
            model = "llama3.2-vision",
            baseUrl = "http://localhost:11434/v1"
        )

        assertTrue(result.isSuccess, "Expected success but was: ${result.exceptionOrNull()?.message}")
        assertTrue(result.getOrNull()?.contains("established successfully") == true)
        assertTrue(capturedUrl.contains("localhost:11434/v1/chat/completions"), "URL was: $capturedUrl")
    }

    @Test
    fun `OpenAiCompatibleRemoteDataSource discoverGames returns recommendations`() = runTest {
        val sampleResponse = """
            {
                "id": "chatcmpl-789",
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": "Recommended games:\n```json\n[\n  {\n    \"title\": \"Metroid Prime\",\n    \"franchise\": \"Metroid\",\n    \"platform\": \"GAMECUBE\",\n    \"releaseYear\": \"2003\",\n    \"genreTags\": [\"Adventure\", \"FPS\"],\n    \"recommendationReason\": \"Atmospheric masterpiece\",\n    \"languageStatus\": \"FULL_ENGLISH\",\n    \"estimatedPriceChf\": 40.0\n  }\n]\n```"
                        }
                    }
                ]
            }
        """.trimIndent()

        val mockEngine = MockEngine { _ ->
            respond(
                content = sampleResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val dataSource = OpenAiCompatibleRemoteDataSource(
            provider = AiProvider.OPENAI_COMPATIBLE,
            client = client
        )

        val result = dataSource.discoverGames(
            query = "Best GameCube games",
            apiKey = "sk-test",
            model = "gpt-4o",
            baseUrl = "https://api.openai.com/v1"
        )

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(1, items.size)
        assertEquals("Metroid Prime", items.first().title)
        assertEquals(ConsolePlatform.GAMECUBE, items.first().platform)
        assertEquals(LanguageStatus.FULL_ENGLISH, items.first().languageStatus)
        assertEquals(40.0, items.first().estimatedPriceChf)
    }
}
