package com.retrocollector.app.settings

import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.settings.data.datasource.DesktopSettingsLocalDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import com.retrocollector.app.settings.domain.model.AiProvider

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
            selectedPlatformFilter = ConsolePlatform.GAMECUBE
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

        // Restaurar estado
        dataSource.saveSettings(original)
    }
}
