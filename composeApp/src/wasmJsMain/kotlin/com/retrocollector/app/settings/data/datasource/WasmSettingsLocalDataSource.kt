package com.retrocollector.app.settings.data.datasource

import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json

class WasmSettingsLocalDataSource(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : SettingsLocalDataSource {
    private var cachedSettings = AppSettings()

    override fun getSettings(): AppSettings {
        return try {
            val stored = localStorage.getItem("retrocollector_settings")
            if (stored != null) {
                json.decodeFromString<AppSettings>(stored)
            } else {
                cachedSettings
            }
        } catch (_: Throwable) {
            cachedSettings
        }
    }

    override fun saveSettings(settings: AppSettings) {
        cachedSettings = settings
        try {
            val content = json.encodeToString(AppSettings.serializer(), settings)
            localStorage.setItem("retrocollector_settings", content)
        } catch (_: Throwable) {
            // Fallback
        }
    }
}

actual fun createSettingsLocalDataSource(): SettingsLocalDataSource = WasmSettingsLocalDataSource()
