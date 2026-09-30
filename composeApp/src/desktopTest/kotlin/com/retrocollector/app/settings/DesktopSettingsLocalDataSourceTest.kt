package com.retrocollector.app.settings

import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.settings.data.datasource.DesktopSettingsLocalDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DesktopSettingsLocalDataSourceTest {

    @Test
    fun `saves and retrieves settings correctly`() {
        val dataSource = DesktopSettingsLocalDataSource()
        val original = dataSource.getSettings()

        val testSettings = original.copy(
            geminiApiKey = "test_key_12345",
            defaultCurrency = "EUR",
            selectedPlatformFilter = ConsolePlatform.GAMECUBE
        )

        dataSource.saveSettings(testSettings)
        val loaded = dataSource.getSettings()

        assertEquals("test_key_12345", loaded.geminiApiKey)
        assertEquals("EUR", loaded.defaultCurrency)
        assertEquals(ConsolePlatform.GAMECUBE, loaded.selectedPlatformFilter)

        // Restaurar estado
        dataSource.saveSettings(original)
    }
}
