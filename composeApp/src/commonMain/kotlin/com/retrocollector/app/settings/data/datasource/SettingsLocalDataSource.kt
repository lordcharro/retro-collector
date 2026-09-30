package com.retrocollector.app.settings.data.datasource

import com.retrocollector.app.settings.domain.model.AppSettings

interface SettingsLocalDataSource {
    fun getSettings(): AppSettings
    fun saveSettings(settings: AppSettings)
}

expect fun createSettingsLocalDataSource(): SettingsLocalDataSource
