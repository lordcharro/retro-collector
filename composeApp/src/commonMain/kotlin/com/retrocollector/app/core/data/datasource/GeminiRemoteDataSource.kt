package com.retrocollector.app.core.data.datasource

import com.retrocollector.app.core.data.gemini.*
import com.retrocollector.app.core.domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.decodeBase64Bytes
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
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val tacticalSystemPrompt = """
        You are RetroCollector's tactical European (PAL) retrogaming verification assistant, optimized for the Swiss market (Ricardo.ch, Tutti.ch, Brockenhaus, Flohmarkt).
        Supported ecosystems and consoles:
        - Nintendo: NES, SNES, Nintendo 64 (NUS), GameCube (DOL), Wii (RVL), Wii U (WUP), Switch (HAC), Switch 2, Game Boy / Color (DMG/CGB), Game Boy Advance (AGB), Nintendo DS (NTR), Nintendo 3DS (CTR).
        - PlayStation: PS1 (SLES/SCES), PS2 (SLES/SCES), PS3 (BLES/BCES), PS4 (CUSA), PS5 (PPSA), PSP (ULES/UCES), PS Vita (PCSF/PCSB).
        - Xbox: Xbox Original (MS), Xbox 360 (X360), Xbox One (XONE), Xbox Series X|S (XSX).
        - Sega: Master System (MK), Mega Drive (MK), Sega Saturn (MK/T), Dreamcast (MK/HDR), Game Gear (MK).
        - Retro Vintage: Atari, ColecoVision, Intellivision, Commodore, Neo Geo, PC Engine.
        
        Critical Mission:
        1. Identify the game, platform, release year, and serial SKU on spine/disc (DOL-P-xxxx, BLES-xxxxx, NUS-xxxx, SLES-xxxxx, CUSA-xxxxx, etc.).
           SPECIAL ATTENTION TO PS3 AND NINTENDO CODES IN SWITZERLAND: In Switzerland it is very common to find French/European box codes (e.g. BLES-01811) with dual-branded USK/PEGI discs marked with DACH codes (e.g. BLES-01780). BLES-01780 discs are 100% authentic, include English audio & subtitles, and should be cataloged as Safe SKUs when uncut!
        2. Determine language risk: Full English (Audio+Subtitles), Subs Only (EN Subtitles), German Only (German audio/text only), or Edition Notice (box/manual notes).
        3. Identify Safe SKUs (e.g. UKV, EUR, multilingual DACH) and Risky SKUs (e.g. censored German NOE).
        4. Evaluate Swiss market valuation (90-day median CHF on Ricardo.ch sales).
        5. Provide a pragmatic "Field Collector Verdict" (Buy or Pass and target price).
        
        Respond in direct, clear, and analytical English.
        At the end of your response, you MUST include a strict JSON block:
        ```json
        {
          "title": "Game Title",
          "franchise": "Franchise Name",
          "platform": "NES" | "SNES" | "N64" | "GAMECUBE" | "WII" | "WII_U" | "SWITCH" | "SWITCH_2" | "GAME_BOY" | "GBA" | "NDS" | "N3DS" | "PS1" | "PS2" | "PS3" | "PS4" | "PS5" | "PSP" | "PS_VITA" | "XBOX_OG" | "XBOX_360" | "XBOX_ONE" | "XBOX_SERIES" | "MASTER_SYSTEM" | "MEGADRIVE" | "SEGA_SATURN" | "DREAMCAST" | "GAME_GEAR" | "RETRO_VINTAGE",
          "releaseYear": "2005",
          "productCode": "BLES-01780",
          "barcode": "045496392345",
          "languageStatus": "FULL_ENGLISH" | "SUBS_ONLY" | "GERMAN_ONLY" | "EDITION_NOTICE",
          "audioLanguages": ["English"],
          "subtitleLanguages": ["English", "French"],
          "safeSkus": [
            {"code": "BLES-01780", "region": "DACH / CH", "editionNote": "Multilingual PEGI 18 + USK 18 disc with full English support", "isSafe": true}
          ],
          "riskySkus": [
            {"code": "DOL-P-G4BP", "region": "NOE", "editionNote": "German BPjM Cut Edition, Missing Mini-games", "isSafe": false}
          ],
          "swissMarketMedianChf": 31.50,
          "historicalMinChf": 28.00,
          "historicalMaxChf": 36.00,
          "censorshipWarning": "Warning about censorship or forced German audio if applicable",
          "collectorVerdict": "Clear buy/pass verdict with suggested target price"
        }
        ```
    """.trimIndent()

    private val conversationalSystemPrompt = """
        You are RetroCollector's tactical European (PAL) retrogaming verification assistant, focused on the Swiss market (Ricardo.ch, Tutti.ch, Brockenhaus, Flohmarkt).
        Supports all 5 major gaming ecosystems: Nintendo, PlayStation, Xbox, Sega, and Retro Vintage.
        
        Conversation Guidelines:
        1. Respond in a direct, pragmatic, and specialized manner in English to any collector question regarding games, special editions, languages, censorship, or serial codes.
        2. Clarify disc versus box codes: for instance, in Switzerland/Germany, discs often carry BLES-01780 (bilingual German/English with USK 18 and PEGI 18), while the outer case has BLES-01811 or BLES-01800. Confirm to the user that BLES-01780 is 100% authentic and multilingual with full English.
        3. If the conversation or user confirms new relevant data about the copy (such as disc serial SKU, confirmed languages, or new collector notes), you may append a strict JSON block at the end with updated fields:
        ```json
        {
          "productCode": "BLES-01780",
          "languageStatus": "FULL_ENGLISH" | "SUBS_ONLY" | "GERMAN_ONLY" | "EDITION_NOTICE",
          "safeSkus": [
            {"code": "BLES-01780", "region": "DACH / CH", "editionNote": "Authentic disc with English audio and menus", "isSafe": true}
          ],
          "riskySkus": [],
          "collectorVerdict": "Updated collector verdict"
        }
        ```
        Otherwise, respond only in natural, analytical, and well-structured Markdown prose.
    """.trimIndent()

    private val discoverySystemPrompt = """
        You are RetroCollector's tactical discovery and recommendation engine for European (PAL) video games, optimized for collectors in Switzerland and Europe.
        Supports Nintendo, PlayStation, Xbox, Sega, and Retro Vintage ecosystems.
        
        Discovery Mission:
        1. Suggest between 4 to 8 games based on selected genre, text search query, or retro terms (e.g. Point & Click, Survival Horror, RTS, Hidden Gems, Monkey Island-like games).
        2. Focus on European (PAL) releases with guaranteed English audio and/or subtitles (avoid German-only USK copies).
        3. Identify whether the classic game has any MODERN PORT or REMASTER (e.g. "Available on Switch via eShop and physical Limited Run", "HD Remaster on PS4/PS5").
        4. Provide known Safe SKUs (e.g. DOL-P-G4BE, BLES-01124, NUS-NPWE, SLES-50382, etc.).
        5. Estimate average Swiss market price (Ricardo.ch / Tutti.ch in CHF).
        6. Suggest 2 to 3 similar games in the same genre/style.
        
        ALWAYS respond with a strict JSON block at the end:
        ```json
        [
          {
            "title": "Game Title",
            "franchise": "Franchise Name",
            "platform": "NES" | "SNES" | "N64" | "GAMECUBE" | "WII" | "WII_U" | "SWITCH" | "SWITCH_2" | "GAME_BOY" | "GBA" | "NDS" | "N3DS" | "PS1" | "PS2" | "PS3" | "PS4" | "PS5" | "PSP" | "PS_VITA" | "XBOX_OG" | "XBOX_360" | "XBOX_ONE" | "XBOX_SERIES" | "MASTER_SYSTEM" | "MEGADRIVE" | "SEGA_SATURN" | "DREAMCAST" | "GAME_GEAR" | "RETRO_VINTAGE",
            "releaseYear": "2002",
            "genreTags": ["Point & Click", "Adventure", "Humor"],
            "recommendationReason": "Unmissable classic with witty writing, iconic puzzles, and multilingual PAL release featuring full English.",
            "languageStatus": "FULL_ENGLISH" | "SUBS_ONLY" | "EDITION_NOTICE",
            "safeSkus": [
              {"code": "DOL-P-G4BE", "region": "UKV / EUR", "editionNote": "European release with English audio", "isSafe": true}
            ],
            "hasModernPortOrRemaster": true,
            "modernPortDetails": "Available on Nintendo Switch (eShop and physical release)",
            "estimatedPriceChf": 35.0,
            "similarTitles": ["Grim Fandango", "Broken Sword", "Sam & Max"]
          }
        ]
        ```
    """.trimIndent()

    private val similarGamesSystemPrompt = """
        You are RetroCollector's game correlation and similar titles recommendation engine.
        Generate 3 to 5 games of matching style, atmosphere, gameplay, and genre across Nintendo, PlayStation, Xbox, Sega, and Retro Vintage platforms.
        Verify English language compatibility for European (PAL) releases and indicate if any remaster or modern port exists.
        
        You MUST respond at the end with a strict JSON block:
        ```json
        [
          {
            "title": "Game Title",
            "franchise": "Franchise Name",
            "platform": "NES" | "SNES" | "N64" | "GAMECUBE" | "WII" | "WII_U" | "SWITCH" | "SWITCH_2" | "GAME_BOY" | "GBA" | "NDS" | "N3DS" | "PS1" | "PS2" | "PS3" | "PS4" | "PS5" | "PSP" | "PS_VITA" | "XBOX_OG" | "XBOX_360" | "XBOX_ONE" | "XBOX_SERIES" | "MASTER_SYSTEM" | "MEGADRIVE" | "SEGA_SATURN" | "DREAMCAST" | "GAME_GEAR" | "RETRO_VINTAGE",
            "releaseYear": "2004",
            "genreTags": ["Survival Horror", "Psychological"],
            "recommendationReason": "Similar oppressive atmosphere, puzzles, and exploration focus.",
            "languageStatus": "FULL_ENGLISH",
            "safeSkus": [
              {"code": "BLES-00561", "region": "EUR", "editionNote": "Edition with full English", "isSafe": true}
            ],
            "hasModernPortOrRemaster": false,
            "modernPortDetails": null,
            "estimatedPriceChf": 45.0,
            "similarTitles": ["Silent Hill 2", "Forbidden Siren"]
          }
        ]
        ```
    """.trimIndent()

    suspend fun testConnection(apiKey: String, model: String = "gemini-3.7-flash"): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Gemini API key is not configured."))
        }

        val targetModel = model.ifBlank { "gemini-3.7-flash" }

        return withTimeoutOrNull(10_000) {
            try {
                val requestBody = GeminiRequest(
                    contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = "ping"))))
                )
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"
                val response = client.post(url) {
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
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "API connection error"))
            }
        } ?: Result.failure(Exception("Connection timeout (10s). Check your internet connection."))
    }

    suspend fun inspectGame(
        query: String,
        imageBase64: String? = null,
        apiKey: String,
        model: String = "gemini-3.7-flash"
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

    suspend fun sendFollowUpChat(
        history: List<ChatMessage>,
        game: GameItem?,
        userMessage: String,
        imageBase64: String?,
        apiKey: String,
        model: String = "gemini-3.7-flash"
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

    suspend fun discoverGames(
        query: String? = null,
        genre: GameGenre? = null,
        platform: ConsolePlatform? = null,
        apiKey: String,
        model: String = "gemini-3.7-flash"
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

    suspend fun fetchSimilarGames(
        gameTitle: String,
        platform: ConsolePlatform,
        genre: String? = null,
        apiKey: String,
        model: String = "gemini-3.7-flash"
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

            val sanitizedBase64 = sanitizeBase64(imageBase64)
            if (sanitizedBase64 != null) {
                parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = sanitizedBase64)))
            }

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = parts)),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = tacticalSystemPrompt)))
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
            val verdict = extractJsonVerdict(rawResponseText)?.copy(latencySeconds = durationSeconds)
            val cleanText = cleanResponseText(rawResponseText).ifBlank { rawResponseText }

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
            val promptText = buildFollowUpPrompt(game, history, userMessage)
            val parts = mutableListOf(GeminiPart(text = promptText))

            val sanitizedBase64 = sanitizeBase64(imageBase64)
            if (sanitizedBase64 != null) {
                parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = sanitizedBase64)))
            }

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = parts)),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = conversationalSystemPrompt)))
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
            val verdict = extractJsonVerdict(rawResponseText)?.copy(latencySeconds = durationSeconds)
            val cleanText = cleanResponseText(rawResponseText).ifBlank { rawResponseText }

            Result.success(Pair(cleanText, verdict))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildFollowUpPrompt(
        game: GameItem?,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val promptBuilder = StringBuilder()
        if (game != null) {
            promptBuilder.appendLine("Analyzed Game Context:")
            promptBuilder.appendLine("- Title: ${game.title} (${game.platform.displayName})")
            promptBuilder.appendLine("- Registered Product Code: ${game.productCode ?: "N/A"}")
            promptBuilder.appendLine("- Asking Price: CHF ${game.askingPriceChf ?: "N/A"}")
            promptBuilder.appendLine("- Sighting Location: ${game.spottedLocation}")
            promptBuilder.appendLine("- Language Status: ${game.languageStatus.label}")
            if (game.safeSkus.isNotEmpty()) {
                promptBuilder.appendLine("- Safe SKUs: ${game.safeSkus.joinToString { "${it.code} (${it.region})" }}")
            }
            if (game.riskySkus.isNotEmpty()) {
                promptBuilder.appendLine("- Risky SKUs: ${game.riskySkus.joinToString { "${it.code} (${it.region})" }}")
            }
            promptBuilder.appendLine()
        }

        val recentHistory = history.takeLast(4)
        if (recentHistory.isNotEmpty()) {
            promptBuilder.appendLine("Conversation History:")
            recentHistory.forEach { msg ->
                val senderLabel = if (msg.sender == MessageSender.USER) "User" else "Gemini"
                promptBuilder.appendLine("$senderLabel: ${msg.text.take(180)}")
            }
            promptBuilder.appendLine()
        }

        promptBuilder.append("Collector Question / Note: $userMessage")
        return promptBuilder.toString()
    }

    private suspend fun executeDiscoverGames(
        query: String?,
        genre: GameGenre?,
        platform: ConsolePlatform?,
        apiKey: String,
        model: String
    ): Result<List<DiscoveredGameItem>> {
        return try {
            val userPrompt = buildDiscoveryUserPrompt(query, genre, platform)
            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = discoverySystemPrompt)))
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

            val rawList = extractJsonDiscoveryList(rawResponseText)
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
            val userPrompt = buildString {
                appendLine("Source Game:")
                appendLine("- Title: $gameTitle")
                appendLine("- Platform: ${platform.displayName}")
                if (!genre.isNullOrBlank()) appendLine("- Genre / Style: $genre")
                appendLine()
                append("Generate 3 to 5 recommendations of similar games released for Nintendo 64, GameCube, PS3 or Switch with PAL editions having guaranteed English.")
            }

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = similarGamesSystemPrompt)))
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

            val rawList = extractJsonDiscoveryList(rawResponseText)
            val items = rawList.map { it.toDiscoveredGameItem() }
            Result.success(items)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildDiscoveryUserPrompt(
        query: String?,
        genre: GameGenre?,
        platform: ConsolePlatform?
    ): String {
        val builder = StringBuilder()
        builder.appendLine("Game Discovery Parameters:")
        if (platform != null) {
            builder.appendLine("- Target Console: ${platform.displayName}")
        } else {
            builder.appendLine("- Target Consoles: Nintendo 64, GameCube, PlayStation 3, and Nintendo Switch")
        }

        if (genre != null && genre != GameGenre.ALL) {
            builder.appendLine("- Selected Genre: ${genre.displayName} (${genre.promptDescription})")
        }

        if (!query.isNullOrBlank()) {
            builder.appendLine("- User Search Query: $query")
        } else if (genre != null && genre != GameGenre.ALL) {
            builder.appendLine("- Focus: Best essential classics, cult games, and PAL gems of genre ${genre.displayName}")
        } else {
            builder.appendLine("- Focus: General recommendations of great retro PAL games and gems balanced across consoles")
        }

        builder.appendLine()
        builder.append("Generate the list of suggestions in the strict JSON format specified.")
        return builder.toString()
    }

    private fun extractJsonDiscoveryList(fullText: String): List<StitchGeminiDiscoveryGame> {
        return try {
            val jsonPattern = Regex("```json\\s*([\\s\\S]*?)\\s*```")
            val match = jsonPattern.find(fullText)
            val jsonString = match?.groupValues?.get(1)?.trim() ?: fullText.trim()
            if (jsonString.startsWith("[")) {
                jsonParser.decodeFromString<List<StitchGeminiDiscoveryGame>>(jsonString)
            } else if (jsonString.startsWith("{")) {
                jsonParser.decodeFromString<StitchGeminiDiscoveryContainer>(jsonString).games
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Discovery extraction error: ${e.message}")
            emptyList()
        }
    }

    private fun StitchGeminiDiscoveryGame.toDiscoveredGameItem(): DiscoveredGameItem {
        val platformEnum = ConsolePlatform.fromPlatformString(platform) ?: ConsolePlatform.GAMECUBE
        val langStatus = when (languageStatus.uppercase().trim()) {
            "FULL_ENGLISH" -> LanguageStatus.FULL_ENGLISH
            "SUBS_ONLY" -> LanguageStatus.SUBS_ONLY
            "GERMAN_ONLY" -> LanguageStatus.GERMAN_ONLY
            "EDITION_NOTICE" -> LanguageStatus.EDITION_NOTICE
            else -> LanguageStatus.FULL_ENGLISH
        }
        val safeId = title.lowercase()
            .replace(" ", "_")
            .filter { it.isLetterOrDigit() || it == '_' }
            .take(30)
        return DiscoveredGameItem(
            id = "disc_${safeId}_${platformEnum.name}",
            title = title,
            franchiseName = franchise,
            platform = platformEnum,
            releaseYear = releaseYear,
            genreDisplayName = genreTags.firstOrNull() ?: "",
            genreTags = genreTags,
            recommendationReason = recommendationReason,
            languageStatus = langStatus,
            safeSkus = safeSkus,
            hasModernPortOrRemaster = hasModernPortOrRemaster,
            modernPortDetails = modernPortDetails,
            estimatedPriceChf = estimatedPriceChf,
            similarTitles = similarTitles
        )
    }

    private fun parseErrorMessage(errorBody: String): String? {
        return try {
            jsonParser.decodeFromString<GeminiResponse>(errorBody).error?.message
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanResponseText(fullText: String): String {
        return fullText.replace(Regex("```json\\s*[\\s\\S]*?\\s*```"), "").trim()
    }

    private fun extractJsonVerdict(fullText: String): StitchGeminiStructuredVerdict? {
        return try {
            val jsonPattern = Regex("```json\\s*([\\s\\S]*?)\\s*```")
            val match = jsonPattern.find(fullText)
            val jsonString = match?.groupValues?.get(1) ?: return null
            jsonParser.decodeFromString<StitchGeminiStructuredVerdict>(jsonString)
        } catch (e: Exception) {
            println("Verdict extraction skipped: ${e.message}")
            null
        }
    }

    private fun sanitizeBase64(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.substringAfter("base64,").trim()
        if (clean.length < 100) return null
        if (clean.startsWith("AQ.") || clean.startsWith("AIzaSy") || clean.startsWith("http://") || clean.startsWith("https://")) {
            return null
        }
        val base64Regex = Regex("^[A-Za-z0-9+/=\r\n]+$")
        if (!base64Regex.matches(clean)) {
            return null
        }
        return try {
            val bytes = clean.decodeBase64Bytes()
            if (bytes.isNotEmpty()) clean else null
        } catch (_: Exception) {
            null
        }
    }
}
