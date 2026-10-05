package com.retrocollector.app.settings

import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.settings.data.datasource.DesktopSettingsLocalDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import com.retrocollector.app.settings.domain.model.AiProvider
import com.retrocollector.app.settings.domain.model.AppLanguage
import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.serialization.json.Json

class DesktopSettingsLocalDataSourceTest {

    @Test
    fun `saves and retrieves settings correctly`() {
        val dataSource = DesktopSettingsLocalDataSource()
        val original = dataSource.getSettings()

        val testSettings = original.copy(
            aiProvider = AiProvider.CLAUDE,
            geminiApiKey = "test_key_12345",
            claudeApiKey = "sk-ant-test-999",
            claudeModel = "claude-3-7-sonnet-20250219",
            openAiApiKey = "sk-test-openai",
            openAiBaseUrl = "https://openrouter.ai/api/v1",
            localAiBaseUrl = "http://localhost:11434/v1",
            localAiModel = "llama3.2-vision",
            defaultCurrency = "EUR",
            selectedPlatformFilter = ConsolePlatform.GAMECUBE,
            appLanguage = AppLanguage.PORTUGUESE
        )

        dataSource.saveSettings(testSettings)
        val loaded = dataSource.getSettings()

        assertEquals(AiProvider.CLAUDE, loaded.aiProvider)
        assertEquals("test_key_12345", loaded.geminiApiKey)
        assertEquals("sk-ant-test-999", loaded.claudeApiKey)
        assertEquals("claude-3-7-sonnet-20250219", loaded.claudeModel)
        assertEquals("sk-test-openai", loaded.openAiApiKey)
        assertEquals("https://openrouter.ai/api/v1", loaded.openAiBaseUrl)
        assertEquals("http://localhost:11434/v1", loaded.localAiBaseUrl)
        assertEquals("llama3.2-vision", loaded.localAiModel)
        assertEquals("EUR", loaded.defaultCurrency)
        assertEquals(ConsolePlatform.GAMECUBE, loaded.selectedPlatformFilter)
        assertEquals(AppLanguage.PORTUGUESE, loaded.appLanguage)

        // Restaurar estado
        dataSource.saveSettings(original)
    }

    @Test
    fun `deserializing legacy settings json without appLanguage defaults to English`() {
        val legacyJson = """
            {
                "geminiApiKey": "old_key",
                "defaultCurrency": "CHF"
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }
        val settings = json.decodeFromString<AppSettings>(legacyJson)
        assertEquals(AppLanguage.ENGLISH, settings.appLanguage)
        assertEquals("old_key", settings.geminiApiKey)
        assertEquals("CHF", settings.defaultCurrency)
    }

    @Test
    fun `deserializing settings with explicit language preserves language`() {
        val jsonWithLang = """
            {
                "appLanguage": "PORTUGUESE",
                "defaultCurrency": "EUR"
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }
        val settings = json.decodeFromString<AppSettings>(jsonWithLang)
        assertEquals(AppLanguage.PORTUGUESE, settings.appLanguage)
        assertEquals("EUR", settings.defaultCurrency)
    }
}
