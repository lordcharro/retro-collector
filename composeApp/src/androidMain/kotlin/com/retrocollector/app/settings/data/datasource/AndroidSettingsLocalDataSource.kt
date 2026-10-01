package com.retrocollector.app.settings.data.datasource

import android.content.Context
import android.content.SharedPreferences
import com.retrocollector.app.RetroCollectorApplication
import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.serialization.json.Json

class AndroidSettingsLocalDataSource(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true },
    private val sharedPreferencesProvider: () -> SharedPreferences? = {
        RetroCollectorApplication.appContext?.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }
) : SettingsLocalDataSource {
    private var inMemorySettings = AppSettings()

    override fun getSettings(): AppSettings {
        val prefs = sharedPreferencesProvider() ?: return inMemorySettings
        val serialized = prefs.getString(KEY_SETTINGS, null) ?: return inMemorySettings
        return try {
            json.decodeFromString<AppSettings>(serialized)
        } catch (_: Exception) {
            inMemorySettings
        }
    }

    override fun saveSettings(settings: AppSettings) {
        inMemorySettings = settings
        val prefs = sharedPreferencesProvider() ?: return
        try {
            val serialized = json.encodeToString(AppSettings.serializer(), settings)
            prefs.edit().putString(KEY_SETTINGS, serialized).apply()
        } catch (_: Exception) {
            // Fallback to in-memory state
        }
    }

    companion object {
        private const val PREFS_NAME = "retrocollector_settings_prefs"
        private const val KEY_SETTINGS = "app_settings"
    }
}

actual fun createSettingsLocalDataSource(): SettingsLocalDataSource = AndroidSettingsLocalDataSource()

