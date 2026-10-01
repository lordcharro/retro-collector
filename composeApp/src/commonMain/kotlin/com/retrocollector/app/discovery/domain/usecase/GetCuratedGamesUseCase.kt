package com.retrocollector.app.discovery.domain.usecase

import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.core.domain.repository.IGameRepository

class GetCuratedGamesUseCase(private val repository: IGameRepository) {
    operator fun invoke(
        genre: GameGenre? = null,
        platform: ConsolePlatform? = null,
        query: String? = null
    ): List<DiscoveredGameItem> {
        return repository.getCuratedGames(
            genre = genre,
            platform = platform,
            query = query?.trim()?.ifBlank { null }
        )
    }
}
