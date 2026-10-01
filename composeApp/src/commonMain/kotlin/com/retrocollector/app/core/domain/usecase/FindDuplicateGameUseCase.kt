package com.retrocollector.app.core.domain.usecase

import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.repository.IGameRepository

/**
 * Searches for duplicate games in the library by normalized title + platform.
 * Used in wishlist import and Quick Scan to detect already existing games.
 */
class FindDuplicateGameUseCase(
    private val repository: IGameRepository
) {
    /**
     * Searches for games with matching normalized title and platform.
     * @return List of matching games (can be empty).
     */
    operator fun invoke(title: String, platform: ConsolePlatform): List<GameItem> {
        val normalizedInput = normalizeTitle(title)
        if (normalizedInput.isBlank()) return emptyList()

        return repository.games.value.filter { game ->
            game.platform == platform &&
                normalizeTitle(game.title) == normalizedInput
        }
    }

    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
