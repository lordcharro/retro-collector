package com.retrocollector.app.discovery.domain.usecase

import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.core.domain.repository.IGameRepository

class DiscoverGamesUseCase(private val repository: IGameRepository) {
    suspend operator fun invoke(
        query: String? = null,
        genre: GameGenre? = null,
        platform: ConsolePlatform? = null,
        forceRefresh: Boolean = false
    ): Result<List<DiscoveredGameItem>> {
        return repository.discoverGames(
            query = query?.trim()?.ifBlank { null },
            genre = genre,
            platform = platform,
            forceRefresh = forceRefresh
        )
    }
}
