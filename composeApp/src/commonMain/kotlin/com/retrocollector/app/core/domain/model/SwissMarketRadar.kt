package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SwissMarketRadar(
    val spottedPriceChf: Double? = null,
    val medianPriceChf: Double? = null,
    val historicalMinChf: Double? = null,
    val historicalMaxChf: Double? = null,
    val trend: String = "Stable"
)
