package com.retrocollector.app.dashboard.domain.usecase

import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.dashboard.domain.model.DashboardFilterCriteria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDashboardGamesUseCase(private val repository: IGameRepository) {
    operator fun invoke(criteria: DashboardFilterCriteria): Flow<List<GameItem>> {
        return repository.games.map { allGames ->
            allGames.filter { game ->
                val matchesQuery = criteria.query.isBlank() ||
                        game.title.contains(criteria.query, ignoreCase = true) ||
                        game.franchiseName.contains(criteria.query, ignoreCase = true) ||
                        (game.productCode?.contains(criteria.query, ignoreCase = true) == true) ||
                        (game.barcode?.contains(criteria.query, ignoreCase = true) == true) ||
                        game.spottedLocation.contains(criteria.query, ignoreCase = true)

                val matchesPlatform = criteria.platform == null || game.platform == criteria.platform
                val matchesStatus = criteria.status == null || game.collectionStatus == criteria.status
                val matchesEnglish = !criteria.englishOnly || game.languageStatus.isEnglishFriendly
                val matchesUsk = !criteria.uskOnly || game.languageStatus == LanguageStatus.GERMAN_ONLY

                matchesQuery && matchesPlatform && matchesStatus && matchesEnglish && matchesUsk
            }
        }
    }
}
