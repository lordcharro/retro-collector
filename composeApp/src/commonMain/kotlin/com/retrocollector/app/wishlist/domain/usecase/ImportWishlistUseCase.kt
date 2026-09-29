package com.retrocollector.app.wishlist.domain.usecase

import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import kotlinx.datetime.Clock

data class ImportResult(
    val added: List<GameItem>,
    val duplicates: List<String>,
    val invalidLines: List<String>
)

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

                // Verificar duplicados por título normalizado + plataforma
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
        // Suporta formatos: "titulo, plataforma" ou "titulo - plataforma"
        val parts = when {
            line.contains(',') -> line.split(',', limit = 2)
            line.contains(" - ") -> line.split(" - ", limit = 2)
            else -> return null
        }

        if (parts.size < 2) return null

        val title = parts[0].trim()
        val platformStr = parts[1].trim()

        if (title.isBlank()) return null

        val platform = resolvePlatform(platformStr) ?: return null
        return Pair(title, platform)
    }

    private fun resolvePlatform(input: String): ConsolePlatform? {
        val normalized = input.lowercase().trim()
        return when {
            normalized in listOf("n64", "nintendo 64", "nintendo64") -> ConsolePlatform.N64
            normalized in listOf("gc", "gamecube", "game cube", "nintendo gamecube", "gcn") -> ConsolePlatform.GAMECUBE
            normalized in listOf("ps3", "playstation 3", "playstation3") -> ConsolePlatform.PS3
            normalized in listOf("switch", "nintendo switch", "ns") -> ConsolePlatform.SWITCH
            else -> ConsolePlatform.entries.find {
                it.id.equals(normalized, ignoreCase = true) ||
                    it.displayName.equals(input, ignoreCase = true) ||
                    it.shortName.equals(input, ignoreCase = true)
            }
        }
    }

    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
