package com.retrocollector.app.settings.data.datasource

import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.serialization.json.Json
import java.io.File

class DesktopSettingsLocalDataSource(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true }
) : SettingsLocalDataSource {
    private val settingsFile: File by lazy {
        val appDir = File(System.getProperty("user.home"), ".retrocollector").apply { mkdirs() }
        File(appDir, "settings.json")
    }

    override fun getSettings(): AppSettings {
        return try {
            if (settingsFile.exists()) {
                val content = settingsFile.readText()
                json.decodeFromString<AppSettings>(content)
            } else {
                AppSettings()
            }
        } catch (_: Exception) {
            AppSettings()
        }
    }

    override fun saveSettings(settings: AppSettings) {
        try {
            val content = json.encodeToString(AppSettings.serializer(), settings)
            settingsFile.writeText(content)
        } catch (_: Exception) {
            // Fallback
        }
    }
}

actual fun createSettingsLocalDataSource(): SettingsLocalDataSource = DesktopSettingsLocalDataSource()
