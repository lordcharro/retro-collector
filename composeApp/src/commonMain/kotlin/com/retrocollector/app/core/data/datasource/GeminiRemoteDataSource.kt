package com.retrocollector.app.core.data.datasource

import com.retrocollector.app.core.data.gemini.*
import com.retrocollector.app.core.domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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

class GeminiRemoteDataSource(
    private val client: HttpClient = HttpClient {
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
        1. Identificar o jogo, plataforma, ano e código serial na lombada (DOL-P-xxxx, BLES-xxxxx, NUS-xxxx, etc.).
        2. Determinar o risco de idioma: Full English (Áudio+Legendas), Subs Only (Legendas em EN), German Only (Bloqueado a alemão sem inglês) ou NOE Edition (Avisos de manual/caixa).
        3. Identificar os SKUs Seguros (ex: UKV, EUR) e SKUs Arriscados (ex: NOE alemão cortado).
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
          "productCode": "DOL-P-G4BE",
          "barcode": "045496392345",
          "languageStatus": "FULL_ENGLISH" | "SUBS_ONLY" | "GERMAN_ONLY" | "EDITION_NOTICE",
          "audioLanguages": ["English"],
          "subtitleLanguages": ["English", "French"],
          "safeSkus": [
            {"code": "DOL-P-G4BE", "region": "UK", "editionNote": "Uncut English Audio and Menus", "isSafe": true}
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

    suspend fun inspectGame(
        query: String,
        imageBase64: String? = null,
        apiKey: String
    ): Result<Pair<String, StitchGeminiStructuredVerdict?>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Chave da API do Gemini não configurada. Acede às Definições para inserir a chave."))
        }

        val startTime = Clock.System.now().toEpochMilliseconds()

        return try {
            val parts = mutableListOf<GeminiPart>()
            parts.add(GeminiPart(text = query))

            if (!imageBase64.isNullOrBlank()) {
                val cleanBase64 = imageBase64.substringAfter("base64,")
                parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = cleanBase64)))
            }

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = parts)),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = tacticalSystemPrompt)))
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            if (response.error != null) {
                return Result.failure(Exception("Erro Gemini (${response.error.code}): ${response.error.message}"))
            }

            val candidate = response.candidates?.firstOrNull()
            val responseText = candidate?.content?.parts?.joinToString("\n") { it.text ?: "" }
                ?: return Result.failure(Exception("Resposta vazia da API do Gemini."))

            val durationSeconds = ((Clock.System.now().toEpochMilliseconds() - startTime) / 1000.0)
            val verdict = extractJsonVerdict(responseText)?.copy(latencySeconds = durationSeconds)

            Result.success(Pair(responseText, verdict))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
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
}
