package com.retrocollector.app.settings.data.datasource

import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.serialization.json.Json
import java.io.File

class AndroidSettingsLocalDataSource(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true },
    private val filesDirProvider: () -> File? = { null }
) : SettingsLocalDataSource {
    private var inMemorySettings = AppSettings()

    private val settingsFile: File?
        get() = filesDirProvider()?.let { File(it, "settings.json") }

    override fun getSettings(): AppSettings {
        val file = settingsFile
        if (file != null && file.exists()) {
            return try {
                json.decodeFromString<AppSettings>(file.readText())
            } catch (_: Exception) {
                inMemorySettings
            }
        }
        return inMemorySettings
    }

    override fun saveSettings(settings: AppSettings) {
        inMemorySettings = settings
        val file = settingsFile
        if (file != null) {
            try {
                file.writeText(json.encodeToString(AppSettings.serializer(), settings))
            } catch (_: Exception) {
                // Fallback
            }
        }
    }
}

actual fun createSettingsLocalDataSource(): SettingsLocalDataSource = AndroidSettingsLocalDataSource()
