package com.retrocollector.app.wishlist.domain.usecase

import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import kotlin.time.Clock

data class ImportResult(
    val added: List<GameItem>,
    val duplicates: List<String>,
    val invalidLines: List<String>
) {
    val totalProcessed: Int get() = added.size + duplicates.size + invalidLines.size
}

class ImportWishlistUseCase(
    private val repository: IGameRepository
) {
    operator fun invoke(csvContent: String): ImportResult {
        val added = mutableListOf<GameItem>()
        val duplicates = mutableListOf<String>()
        val invalidLines = mutableListOf<String>()

        val lines = csvContent.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val existingGames = repository.games.value

        for (line in lines) {
            val parsed = parseLine(line)
            if (parsed == null) {
                invalidLines.add(line)
            } else {
                val (title, platform) = parsed
                val normalizedTitle = normalizeTitle(title)

                // Verify duplicates by normalized title + platform
                val isDuplicate = existingGames.any { game ->
                    normalizeTitle(game.title) == normalizedTitle &&
                        game.platform == platform
                } || added.any { game ->
                    normalizeTitle(game.title) == normalizedTitle &&
                        game.platform == platform
                }

                if (isDuplicate) {
                    duplicates.add("$title (${platform.displayName})")
                } else {
                    val gameId = "wish_${normalizedTitle.filter { it.isLetterOrDigit() }.take(30)}_${platform.id}"
                    val nowMs = Clock.System.now().toEpochMilliseconds()

                    val gameItem = GameItem(
                        id = gameId,
                        title = title,
                        platform = platform,
                        collectionStatus = CollectionStatus.WISHLIST,
                        enrichmentStatus = EnrichmentStatus.PENDING,
                        updatedAt = nowMs
                    )

                    repository.upsertGame(gameItem)
                    added.add(gameItem)
                }
            }
        }

        return ImportResult(added, duplicates, invalidLines)
    }

    private fun parseLine(line: String): Pair<String, ConsolePlatform>? {
        // Supports formats: "title, platform" or "title - platform"
        val parts = when {
            line.contains(',') -> line.split(',', limit = 2)
            line.contains(" - ") -> line.split(" - ", limit = 2)
            else -> return null
        }

        if (parts.size < 2) return null

        val title = parts[0].trim()
        val platformStr = parts[1].trim()

        if (title.isBlank()) return null

        val platform = ConsolePlatform.fromPlatformString(platformStr) ?: return null
        return Pair(title, platform)
    }

    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
