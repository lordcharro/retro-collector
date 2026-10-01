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
data class AppSettings(
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.7-flash",
    val firebaseProjectId: String = "",
    val firebaseApiKey: String = "",
    val defaultCurrency: String = "CHF",
    val isScraperEnabled: Boolean = true,
    val isAutoDiscoveryEnabled: Boolean = false,
    val ricardoSessionCookie: String = "",
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
