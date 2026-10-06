package com.retrocollector.app.core.domain.model

import kotlin.time.Clock
import kotlinx.serialization.Serializable

@Serializable
data class DiscoveredGameItem(
    val id: String,
    val title: String,
    val franchiseName: String = "",
    val platform: ConsolePlatform,
    val releaseYear: String = "",
    val genreDisplayName: String = "",
    val genreTags: List<String> = emptyList(),
    val recommendationReason: String = "",
    val languageStatus: LanguageStatus = LanguageStatus.FULL_ENGLISH,
    val safeSkus: List<SkuInfo> = emptyList(),
    val hasModernPortOrRemaster: Boolean = false,
    val modernPortDetails: String? = null,
    val estimatedPriceChf: Double? = null,
    val similarTitles: List<String> = emptyList(),
    val isAlreadyInCollection: Boolean = false,
    val isAlreadyInWishlist: Boolean = false
) {
    fun toGameItem(
        status: CollectionStatus = CollectionStatus.PASS,
        targetPrice: Double? = targetPriceChfOverride()
    ): GameItem {
        return GameItem(
            id = id.ifBlank { "discovered_${Clock.System.now().toEpochMilliseconds()}" },
            title = title,
            franchiseName = franchiseName,
            platform = platform,
            releaseYear = releaseYear,
            description = recommendationReason,
            productCode = safeSkus.firstOrNull()?.code,
            askingPriceChf = estimatedPriceChf,
            targetPriceChf = targetPrice ?: estimatedPriceChf,
            languageStatus = languageStatus,
            safeSkus = safeSkus,
            collectorVerdict = recommendationReason,
            collectionStatus = status,
            personalNotes = buildString {
                if (!modernPortDetails.isNullOrBlank()) {
                    append("Port/Remaster: $modernPortDetails\n")
                }
                if (genreTags.isNotEmpty()) {
                    append("Genres: ${genreTags.joinToString(", ")}\n")
                }
            }.trim(),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun targetPriceChfOverride(): Double? {
        return estimatedPriceChf?.let { (it * 0.85).coerceAtLeast(5.0) }
    }
}
