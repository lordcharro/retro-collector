package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CollectionStatus(
    val label: String,
    val icon: String
) {
    WISHLIST("Wishlist", "🎯"),
    OWNED("Owned", "📦"),
    PASS("Pass", "❌"),
    @Deprecated("Merged into WISHLIST", ReplaceWith("WISHLIST"))
    HUNTING("Wishlist", "🎯");

    companion object {
        val displayStatuses: List<CollectionStatus> = listOf(WISHLIST, OWNED, PASS)

        @Suppress("DEPRECATION")
        fun fromString(value: String?): CollectionStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?.let { if (it == HUNTING) WISHLIST else it }
                ?: WISHLIST
    }
}
