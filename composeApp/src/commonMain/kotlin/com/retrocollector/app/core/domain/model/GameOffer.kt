package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameOffer(
    val id: String = "",
    val source: String = "Ricardo.ch",
    val priceChf: Double = 0.0,
    val shippingChf: Double? = null,
    val listingUrl: String = "",
    val condition: GameCondition = GameCondition.CIB,
    val sellerOrLocation: String = "",
    val notes: String = "",
    val isArchived: Boolean = false,
    val isPurchased: Boolean = false,
    val createdAt: Long = 0L
) {
    val totalLandedPriceChf: Double
        get() = priceChf + (shippingChf ?: 0.0)

    val hasShipping: Boolean
        get() = shippingChf != null && shippingChf > 0.0

    companion object {
        val PRESET_SOURCES = listOf(
            "Ricardo.ch",
            "Tutti.ch",
            "Anibis.ch",
            "Facebook Marketplace",
            "Brocki / Flea Market",
            "Other"
        )
    }
}
