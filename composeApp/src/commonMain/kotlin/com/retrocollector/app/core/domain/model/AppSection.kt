package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppSection(val label: String, val icon: String) {
    ACTIVITY("Activity", "⚡"),
    DISCOVER("Discover", "🧭"),
    WISHLIST("Wishlist", "🎯"),
    COLLECTION("Collection", "📦")
}
