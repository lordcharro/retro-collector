package com.retrocollector.app.settings.domain.model

import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.ConsolePlatform
import kotlinx.serialization.Serializable

val defaultPinnedPlatformIds = listOf("n64", "gamecube", "ps3", "switch")

@Serializable
enum class ThemeMode(val displayName: String, val icon: String) {
    DARK("Dark", "🌙"),
    LIGHT("Light", "☀️"),
    SYSTEM("System", "🖥️")
}

@Serializable
enum class AiProvider(val displayName: String, val icon: String) {
    GEMINI("Google Gemini", "✨"),
    CLAUDE("Anthropic Claude", "🟣"),
    OPENAI_COMPATIBLE("OpenAI / OpenRouter", "🔵"),
    LOCAL_OLLAMA("Local AI (Ollama / LM Studio)", "🟢")
}

@Serializable
enum class ScraperProvider(val displayName: String) {
    SCRAPE_DO("Scrape.do API"),
    CUSTOM_PROXY("Custom Proxy")
}

@Serializable
data class AppSettings(
    val aiProvider: AiProvider = AiProvider.GEMINI,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.7-flash",
    val claudeApiKey: String = "",
    val claudeModel: String = "claude-3-7-sonnet-20250219",
    val openAiApiKey: String = "",
    val openAiBaseUrl: String = "https://api.openai.com/v1",
    val openAiModel: String = "gpt-4o",
    val localAiBaseUrl: String = "http://localhost:11434/v1",
    val localAiModel: String = "llama3.2-vision",
    val firebaseProjectId: String = "",
    val firebaseApiKey: String = "",
    val defaultCurrency: String = "CHF",
    val isScraperEnabled: Boolean = true,
    val scraperProvider: ScraperProvider = ScraperProvider.SCRAPE_DO,
    val scrapeDoApiKey: String = "",
    val scrapeDoSuperProxy: Boolean = false,
    val customScraperProxyUrl: String = "",
    val isAutoDiscoveryEnabled: Boolean = false,
    val isAutoSimilarGamesEnabled: Boolean = false,
    val selectedPlatformFilter: ConsolePlatform? = null,
    val onlyEnglishFilter: Boolean = false,
    val statusFilter: CollectionStatus? = null,
    val pinnedPlatformIds: List<String> = defaultPinnedPlatformIds,
    val themeMode: ThemeMode = ThemeMode.DARK
) {
    fun isPlatformPinned(platform: ConsolePlatform): Boolean =
        pinnedPlatformIds.any { it.equals(platform.id, ignoreCase = true) }

    fun togglePlatformPin(platform: ConsolePlatform): AppSettings {
        val current = pinnedPlatformIds.toMutableList()
        val matchIndex = current.indexOfFirst { it.equals(platform.id, ignoreCase = true) }
        if (matchIndex >= 0) {
            current.removeAt(matchIndex)
        } else {
            current.add(platform.id)
        }
        return copy(pinnedPlatformIds = current)
    }
}
