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
        És o assistente tático de verificação de retrogaming europeu (PAL) do RetroCollector, otimizado para o mercado da Suíça (Ricardo.ch, Tutti.ch, Brockenhaus, Flohmarkt).
        Foco de consolas: Nintendo 64 (NUS), GameCube (DOL), PlayStation 3 (BLES/BCES) e Nintendo Switch (HAC).
        
        Missão Crítica:
        1. Identificar o jogo, plataforma, ano e código serial na lombada ou disco (DOL-P-xxxx, BLES-xxxxx, NUS-xxxx, etc.).
           ATENÇÃO ESPECIAL A CÓDIGOS PS3 E NINTENDO NA SUÍÇA: É muito comum na Suíça encontrar caixas com código francês/europeu (ex: BLES-01811) com discos com selo duplo USK/PEGI e código DACH (ex: BLES-01780). Discos BLES-01780 são 100% autênticos, incluem áudio e legendas em inglês e devem ser catalogados como Safe SKUs quando não censurados!
        2. Determinar o risco de idioma: Full English (Áudio+Legendas), Subs Only (Legendas em EN), German Only (Bloqueado a alemão sem inglês) ou NOE Edition (Avisos de manual/caixa).
        3. Identificar os SKUs Seguros (ex: UKV, EUR, DACH multilingue) e SKUs Arriscados (ex: NOE alemão cortado).
        4. Avaliar o preço de mercado suíço (Mediana de 90 dias em CHF em vendas no Ricardo.ch).
        5. Fornecer um "Field Collector Verdict" pragmático (Comprar ou Ignorar e preço alvo).
        
        Responde em português direto, claro e analítico.
        No final da resposta, inclui OBRIGATORIAMENTE um bloco JSON estrito:
        ```json
        {
          "title": "Nome do Jogo",
          "franchise": "Franquia",
          "platform": "GAMECUBE" | "PS3" | "N64" | "SWITCH",
          "releaseYear": "2005",
          "productCode": "BLES-01780",
          "barcode": "045496392345",
          "languageStatus": "FULL_ENGLISH" | "SUBS_ONLY" | "GERMAN_ONLY" | "EDITION_NOTICE",
          "audioLanguages": ["English"],
          "subtitleLanguages": ["English", "French"],
          "safeSkus": [
            {"code": "BLES-01780", "region": "DACH / CH", "editionNote": "Disco multilingue PEGI 18 + USK 18 com inglês integral", "isSafe": true}
          ],
          "riskySkus": [
            {"code": "DOL-P-G4BP", "region": "NOE", "editionNote": "German BPjM Cut Edition, Missing Mini-games", "isSafe": false}
          ],
          "swissMarketMedianChf": 31.50,
          "historicalMinChf": 28.00,
          "historicalMaxChf": 36.00,
          "censorshipWarning": "Aviso sobre censura ou dublagem alemã forçada se aplicável",
          "collectorVerdict": "Veredito claro de compra ou rejeição com preço alvo sugerido"
        }
        ```
    """.trimIndent()

    private val conversationalSystemPrompt = """
        És o assistente tático de verificação de retrogaming europeu (PAL) do RetroCollector, focado no mercado da Suíça (Ricardo.ch, Tutti.ch, Brockenhaus, Flohmarkt).
        Foco de consolas: Nintendo 64 (NUS), GameCube (DOL), PlayStation 3 (BLES/BCES) e Nintendo Switch (HAC).
        
        Diretrizes de Conversação:
        1. Responde de forma direta, pragmática e especializada em português a qualquer pergunta do colecionador sobre o jogo, edições especiais, idiomas, censura ou códigos BLES/DOL/NUS.
        2. Esclarece códigos no disco versus na caixa: por exemplo, na Suíça/Alemanha, é comum o disco ter BLES-01780 (bilíngue alemão/inglês com USK 18 e PEGI 18), enquanto a caixa tem BLES-01811 ou BLES-01800. Confirma ao utilizador que BLES-01780 é 100% autêntico e multilingue com inglês.
        3. Se a conversa ou o utilizador confirmar novos dados relevantes sobre a cópia (como código serial no disco, confirmação de idiomas ou novas notas de colecionador), podes incluir no final da resposta um bloco JSON estrito com os dados atualizados:
        ```json
        {
          "productCode": "BLES-01780",
          "languageStatus": "FULL_ENGLISH" | "SUBS_ONLY" | "GERMAN_ONLY" | "EDITION_NOTICE",
          "safeSkus": [
            {"code": "BLES-01780", "region": "DACH / CH", "editionNote": "Disco autêntico com áudio e menus em inglês", "isSafe": true}
          ],
          "riskySkus": [],
          "collectorVerdict": "Veredito atualizado"
        }
        ```
        Caso contrário, responde apenas em texto natural, analítico e bem estruturado em markdown.
    """.trimIndent()

    private val discoverySystemPrompt = """
        És o motor tático de descoberta e recomendação de videojogos europeus (PAL) do RetroCollector, otimizado para colecionadores na Suíça e no mercado europeu.
        Foco de consolas centrais: Nintendo 64 (N64), Nintendo GameCube (GAMECUBE), PlayStation 3 (PS3) e Nintendo Switch (SWITCH).
        
        Missão de Descoberta:
        1. Sugerir entre 4 a 8 jogos com base no género selecionado, pesquisa de texto ou termos retro (ex: Point & Click, Survival Horror, RTS, Hidden Gems, jogos tipo Monkey Island).
        2. Focar em lançamentos europeus (PAL) que possuam áudio e/ou legendas em inglês garantidos (evitar cópias USK apenas em alemão).
        3. Identificar se o jogo clássico possui algum PORT MODERNO ou REMASTER na Nintendo Switch ou PlayStation 3 (ex: "Disponível na Switch via eShop e físico Limited Run", "Remaster HD no PS3").
        4. Fornecer os Safe SKUs conhecidos (ex: DOL-P-G4BE, BLES-01124, NUS-NPWE, etc.).
        5. Estimar o preço médio de mercado suíço (Ricardo.ch / Tutti.ch em CHF).
        6. Sugerir 2 a 3 jogos semelhantes de estilo idêntico.
        
        Responde SEMPRE com um bloco JSON estrito no final:
        ```json
        [
          {
            "title": "Nome do Jogo",
            "franchise": "Nome da Franquia",
            "platform": "GAMECUBE" | "PS3" | "N64" | "SWITCH",
            "releaseYear": "2002",
            "genreTags": ["Point & Click", "Aventura", "Humor"],
            "recommendationReason": "Clássico imperdível com escrita inteligente, quebra-cabeças icónicos e lançamento PAL multilingue com inglês integral.",
            "languageStatus": "FULL_ENGLISH" | "SUBS_ONLY" | "EDITION_NOTICE",
            "safeSkus": [
              {"code": "DOL-P-G4BE", "region": "UKV / EUR", "editionNote": "Edição europeia com áudio em inglês", "isSafe": true}
            ],
            "hasModernPortOrRemaster": true,
            "modernPortDetails": "Disponível na Nintendo Switch (eShop e edição física)",
            "estimatedPriceChf": 35.0,
            "similarTitles": ["Grim Fandango", "Broken Sword", "Sam & Max"]
          }
        ]
        ```
    """.trimIndent()

    private val similarGamesSystemPrompt = """
        És o motor de correlação e recomendação de jogos semelhantes do RetroCollector.
        Gera 3 a 5 jogos do mesmo estilo, atmosfera, jogabilidade e género, focando em lançamentos nas consolas Nintendo 64, GameCube, PS3 e Nintendo Switch.
        Verifica a compatibilidade de idioma inglês em edições europeias (PAL) e indica se existe remaster ou port na Switch/PS3.
        
        Responde OBRIGATORIAMENTE no final com um bloco JSON estrito:
        ```json
        [
          {
            "title": "Nome do Jogo",
            "franchise": "Nome da Franquia",
            "platform": "GAMECUBE" | "PS3" | "N64" | "SWITCH",
            "releaseYear": "2004",
            "genreTags": ["Survival Horror", "Psicológico"],
            "recommendationReason": "Mesma atmosfera opressiva, quebra-cabeças e foco em exploração.",
            "languageStatus": "FULL_ENGLISH",
            "safeSkus": [
              {"code": "BLES-00561", "region": "EUR", "editionNote": "Edição com inglês integral", "isSafe": true}
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
            return Result.failure(IllegalArgumentException("Chave da API do Gemini não configurada."))
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
                    Result.failure(Exception(errorMsg ?: "Erro HTTP ${response.status.value}: ${response.status.description}"))
                } else {
                    val geminiResponse: GeminiResponse = response.body()
                    if (geminiResponse.error != null) {
                        Result.failure(Exception("Google API: ${geminiResponse.error.message ?: "Erro (${geminiResponse.error.code})" }"))
                    } else {
                        Result.success("Ligação com $targetModel estabelecida com sucesso!")
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "Erro de ligação à API"))
            }
        } ?: Result.failure(Exception("Tempo limite esgotado (10s). Verifica a tua ligação à Internet."))
    }

    suspend fun inspectGame(
        query: String,
        imageBase64: String? = null,
        apiKey: String,
        model: String = "gemini-3.7-flash"
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Chave da API do Gemini não configurada. Acede às Definições para inserir a chave."))
        }

        val primaryModel = model.ifBlank { "gemini-3.7-flash" }
        val result = executeInspect(query, imageBase64, apiKey, primaryModel)

        // Se falhar devido a pico de procura temporário no modelo solicitado, tenta fallback automático
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
            return Result.failure(IllegalArgumentException("Chave da API do Gemini não configurada. Acede às Definições para inserir a chave."))
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
            return Result.failure(IllegalArgumentException("Chave da API do Gemini não configurada. Acede às Definições para inserir a chave."))
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
            return Result.failure(IllegalArgumentException("Chave da API do Gemini não configurada. Acede às Definições para inserir a chave."))
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
                return Result.failure(Exception(parsedError ?: "Erro HTTP ${response.status.value}: ${response.status.description}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            if (geminiResponse.error != null) {
                return Result.failure(Exception("Erro Gemini (${geminiResponse.error.code}): ${geminiResponse.error.message}"))
            }

            val candidate = geminiResponse.candidates?.firstOrNull()
            val rawResponseText = candidate?.content?.parts
                ?.filter { it.thought != true }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Resposta vazia da API do Gemini."))

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
                return Result.failure(Exception(parsedError ?: "Erro HTTP ${response.status.value}: ${response.status.description}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            if (geminiResponse.error != null) {
                return Result.failure(Exception("Erro Gemini (${geminiResponse.error.code}): ${geminiResponse.error.message}"))
            }

            val candidate = geminiResponse.candidates?.firstOrNull()
            val rawResponseText = candidate?.content?.parts
                ?.filter { it.thought != true }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Resposta vazia da API do Gemini."))

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
            promptBuilder.appendLine("Contexto do Jogo Analisado:")
            promptBuilder.appendLine("- Título: ${game.title} (${game.platform.displayName})")
            promptBuilder.appendLine("- Código Serial Registado: ${game.productCode ?: "N/D"}")
            promptBuilder.appendLine("- Preço Pedido: CHF ${game.askingPriceChf ?: "N/D"}")
            promptBuilder.appendLine("- Local de Aquisição: ${game.spottedLocation}")
            promptBuilder.appendLine("- Status de Idioma: ${game.languageStatus.label}")
            if (game.safeSkus.isNotEmpty()) {
                promptBuilder.appendLine("- SKUs Seguros: ${game.safeSkus.joinToString { "${it.code} (${it.region})" }}")
            }
            if (game.riskySkus.isNotEmpty()) {
                promptBuilder.appendLine("- SKUs de Risco: ${game.riskySkus.joinToString { "${it.code} (${it.region})" }}")
            }
            promptBuilder.appendLine()
        }

        val recentHistory = history.takeLast(4)
        if (recentHistory.isNotEmpty()) {
            promptBuilder.appendLine("Histórico da Conversa:")
            recentHistory.forEach { msg ->
                val senderLabel = if (msg.sender == MessageSender.USER) "Utilizador" else "Gemini"
                promptBuilder.appendLine("$senderLabel: ${msg.text.take(180)}")
            }
            promptBuilder.appendLine()
        }

        promptBuilder.append("Pergunta ou Observação do Colecionador: $userMessage")
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
                return Result.failure(Exception(parsedError ?: "Erro HTTP ${response.status.value}: ${response.status.description}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            if (geminiResponse.error != null) {
                return Result.failure(Exception("Erro Gemini (${geminiResponse.error.code}): ${geminiResponse.error.message}"))
            }

            val candidate = geminiResponse.candidates?.firstOrNull()
            val rawResponseText = candidate?.content?.parts
                ?.filter { it.thought != true }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Resposta vazia da API do Gemini."))

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
                appendLine("Jogo de Origem:")
                appendLine("- Título: $gameTitle")
                appendLine("- Plataforma: ${platform.displayName}")
                if (!genre.isNullOrBlank()) appendLine("- Género / Estilo: $genre")
                appendLine()
                append("Gera 3 a 5 recomendações de jogos semelhantes lançados para Nintendo 64, GameCube, PS3 ou Switch com edições PAL com inglês garantido.")
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
                return Result.failure(Exception(parsedError ?: "Erro HTTP ${response.status.value}: ${response.status.description}"))
            }

            val geminiResponse: GeminiResponse = response.body()
            if (geminiResponse.error != null) {
                return Result.failure(Exception("Erro Gemini (${geminiResponse.error.code}): ${geminiResponse.error.message}"))
            }

            val candidate = geminiResponse.candidates?.firstOrNull()
            val rawResponseText = candidate?.content?.parts
                ?.filter { it.thought != true }
                ?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Resposta vazia da API do Gemini."))

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
        builder.appendLine("Parâmetros de Descoberta de Jogos:")
        if (platform != null) {
            builder.appendLine("- Consola Alvo: ${platform.displayName}")
        } else {
            builder.appendLine("- Consolas Alvo: Nintendo 64, GameCube, PlayStation 3 e Nintendo Switch")
        }

        if (genre != null && genre != GameGenre.ALL) {
            builder.appendLine("- Género Selecionado: ${genre.displayName} (${genre.promptDescription})")
        }

        if (!query.isNullOrBlank()) {
            builder.appendLine("- Pedido / Termos de Pesquisa do Utilizador: $query")
        } else if (genre != null && genre != GameGenre.ALL) {
            builder.appendLine("- Foco: Melhores clássicos essenciais, jogos de culto e pérolas PAL do género ${genre.displayName}")
        } else {
            builder.appendLine("- Foco: Recomendações gerais de grandes jogos e pérolas retro PAL equilibradas entre as consolas")
        }

        builder.appendLine()
        builder.append("Gera a lista de sugestões no formato JSON estrito especificado.")
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
        val platformEnum = when (platform.uppercase().trim()) {
            "N64", "NINTENDO 64", "NUS" -> ConsolePlatform.N64
            "GAMECUBE", "GC", "DOL", "NINTENDO GAMECUBE" -> ConsolePlatform.GAMECUBE
            "PS3", "PLAYSTATION 3", "BLES", "BCES" -> ConsolePlatform.PS3
            "SWITCH", "NINTENDO SWITCH", "HAC" -> ConsolePlatform.SWITCH
            else -> ConsolePlatform.GAMECUBE
        }
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
