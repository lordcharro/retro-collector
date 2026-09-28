package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CollectionStatus(
    val label: String,
    val icon: String
) {
    HUNTING("Hunting", "🎯"),
    OWNED("Owned", "📦"),
    PASS("Pass", "❌");

    companion object {
        fun fromString(value: String?): CollectionStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: HUNTING
    }
}
