package com.retrocollector.app.wishlist.domain.usecase

import com.retrocollector.app.core.domain.model.EnrichmentStatus
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.repository.IGameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock

class EnrichWishlistGameUseCase(
    private val repository: IGameRepository
) {
    /**
     * Enriches a single wishlist game with Gemini AI data.
     * Progressively updates enrichmentStatus: PENDING → ENRICHING → COMPLETE/FAILED.
     */
    suspend operator fun invoke(gameId: String): Result<GameItem> {
        val game = repository.getGameById(gameId)
            ?: return Result.failure(IllegalArgumentException("Game not found: $gameId"))

        // Mark as ENRICHING
        repository.upsertGame(game.copy(enrichmentStatus = EnrichmentStatus.ENRICHING))

        return try {
            // Use inspectGameWithAi with the game title as query (without image, without price)
            val result = repository.inspectGameWithAi(
                query = "${game.title} ${game.platform.displayName} PAL European edition",
                imageBase64 = null,
                spottedLocation = "",
                askingPriceChf = null
            )

            result.fold(
                onSuccess = { (_, enrichedGame) ->
                    val nowMs = Clock.System.now().toEpochMilliseconds()
                    val finalGame = if (enrichedGame != null) {
                        // Preserve existing wishlist data and enrich with Gemini data
                        game.copy(
                            franchiseName = enrichedGame.franchiseName.ifBlank { game.franchiseName },
                            releaseYear = enrichedGame.releaseYear.ifBlank { game.releaseYear },
                            productCode = enrichedGame.productCode ?: game.productCode,
                            barcode = enrichedGame.barcode ?: game.barcode,
                            languageStatus = enrichedGame.languageStatus,
                            languageAudio = enrichedGame.languageAudio.ifEmpty { game.languageAudio },
                            languageSubtitles = enrichedGame.languageSubtitles.ifEmpty { game.languageSubtitles },
                            safeSkus = enrichedGame.safeSkus.ifEmpty { game.safeSkus },
                            riskySkus = enrichedGame.riskySkus.ifEmpty { game.riskySkus },
                            marketRadar = enrichedGame.marketRadar ?: game.marketRadar,
                            censorshipWarning = enrichedGame.censorshipWarning ?: game.censorshipWarning,
                            collectorVerdict = enrichedGame.collectorVerdict.ifBlank { game.collectorVerdict },
                            enrichmentStatus = EnrichmentStatus.COMPLETE,
                            updatedAt = nowMs
                        )
                    } else {
                        game.copy(
                            enrichmentStatus = EnrichmentStatus.COMPLETE,
                            updatedAt = nowMs
                        )
                    }
                    repository.upsertGame(finalGame)
                    Result.success(finalGame)
                },
                onFailure = { error ->
                    val failedGame = game.copy(enrichmentStatus = EnrichmentStatus.FAILED)
                    repository.upsertGame(failedGame)
                    Result.failure(error)
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val failedGame = game.copy(enrichmentStatus = EnrichmentStatus.FAILED)
            repository.upsertGame(failedGame)
            Result.failure(e)
        }
    }
}
