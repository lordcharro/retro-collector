package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppSection(val label: String, val icon: String) {
    CATALOG("Catalog", "📋"),
    DISCOVER("Discover", "🧭"),
    WISHLIST("Wishlist", "💝"),
    COLLECTION("Collection", "📦")
}
