package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class LanguageStatus(
    val label: String,
    val subLabel: String,
    val isEnglishFriendly: Boolean
) {
    FULL_ENGLISH(
        label = "Full English",
        subLabel = "Audio+Subs",
        isEnglishFriendly = true
    ),
    SUBS_ONLY(
        label = "Subs Only",
        subLabel = "UKV Safe",
        isEnglishFriendly = true
    ),
    GERMAN_ONLY(
        label = "German Only",
        subLabel = "NOE Cut",
        isEnglishFriendly = false
    ),
    EDITION_NOTICE(
        label = "NOE Edition",
        subLabel = "Manual Ger",
        isEnglishFriendly = false
    ),
    UNVERIFIED(
        label = "Unverified",
        subLabel = "Pending Scan",
        isEnglishFriendly = false
    );

    companion object {
        fun fromString(value: String?): LanguageStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: UNVERIFIED
    }
}
