package com.retrocollector.app.core.data.datasource.ai

import com.retrocollector.app.core.data.datasource.StitchGeminiDiscoveryContainer
import com.retrocollector.app.core.data.datasource.StitchGeminiDiscoveryGame
import com.retrocollector.app.core.data.datasource.StitchGeminiStructuredVerdict
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.LanguageStatus
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.Json

object AiResponseParser {

    val jsonParser: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun cleanResponseText(fullText: String): String {
        return fullText.replace(Regex("```json\\s*[\\s\\S]*?\\s*```"), "").trim()
    }

    fun extractJsonVerdict(fullText: String): StitchGeminiStructuredVerdict? {
        return try {
            val jsonPattern = Regex("```json\\s*([\\s\\S]*?)\\s*```")
            val match = jsonPattern.find(fullText)
            val jsonString = match?.groupValues?.get(1)?.trim() ?: return null
            jsonParser.decodeFromString<StitchGeminiStructuredVerdict>(jsonString)
        } catch (e: Exception) {
            println("Verdict extraction skipped: ${e.message}")
            null
        }
    }

    fun extractJsonDiscoveryList(fullText: String): List<StitchGeminiDiscoveryGame> {
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

    fun StitchGeminiDiscoveryGame.toDiscoveredGameItem(): DiscoveredGameItem {
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

    fun sanitizeBase64(raw: String?): String? {
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
            @OptIn(ExperimentalEncodingApi::class)
            val bytes = Base64.Default.decode(clean)
            if (bytes.isNotEmpty()) clean else null
        } catch (_: Exception) {
            null
        }
    }
}
