package com.retrocollector.app.settings.domain.model

import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.ConsolePlatform
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.7-flash",
    val firebaseProjectId: String = "",
    val firebaseApiKey: String = "",
    val defaultCurrency: String = "CHF",
    val isScraperEnabled: Boolean = true,
    val ricardoSessionCookie: String = "",
    val selectedPlatformFilter: ConsolePlatform? = null,
    val onlyEnglishFilter: Boolean = false,
    val statusFilter: CollectionStatus? = null
)
