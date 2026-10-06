package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameItem(
    val id: String,
    val title: String,
    val franchiseName: String = "",
    val platform: ConsolePlatform,
    val releaseYear: String = "",
    val description: String = "",
    val coverImageUrl: String? = null,
    val spineImageUrl: String? = null,
    val productCode: String? = null, // ex: DOL-P-G4BE, BLES-00561
    val barcode: String? = null,
    val spottedLocation: String = "", // ex: Brockenhaus Bern, Ricardo.ch, Basel Flohmarkt
    val askingPriceChf: Double? = null, // spotted / asking price
    val targetPriceChf: Double? = null,
    val paidPriceChf: Double? = null,
    val languageStatus: LanguageStatus = LanguageStatus.UNVERIFIED,
    val languageAudio: List<String> = emptyList(),
    val languageSubtitles: List<String> = emptyList(),
    val safeSkus: List<SkuInfo> = emptyList(),
    val riskySkus: List<SkuInfo> = emptyList(),
    val marketRadar: SwissMarketRadar? = null,
    val censorshipWarning: String? = null,
    val collectorVerdict: String = "",
    val collectionStatus: CollectionStatus = CollectionStatus.WISHLIST,
    val enrichmentStatus: EnrichmentStatus = EnrichmentStatus.COMPLETE,
    val personalNotes: String = "",
    val listingUrl: String? = null,
    val offers: List<GameOffer> = emptyList(),
    val similarGames: List<DiscoveredGameItem> = emptyList(),
    val acquiredCondition: GameCondition? = null,
    val updatedAt: Long = 0L
) {
    val activeOffers: List<GameOffer>
        get() = offers.filter { !it.isArchived }

    val bestOffer: GameOffer?
        get() = activeOffers.minByOrNull { it.totalLandedPriceChf }
}
