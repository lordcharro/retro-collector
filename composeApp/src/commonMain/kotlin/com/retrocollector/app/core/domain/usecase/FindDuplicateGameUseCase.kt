package com.retrocollector.app.core.domain.usecase

import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.repository.IGameRepository

/**
 * Procura jogos duplicados na coleção por título fuzzy + plataforma.
 * Usado na importação de wishlist e no Quick Scan para detetar jogos existentes.
 */
class FindDuplicateGameUseCase(
    private val repository: IGameRepository
) {
    /**
     * Procura jogos com título semelhante e mesma plataforma.
     * @return Lista de jogos correspondentes (pode ser vazia).
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
