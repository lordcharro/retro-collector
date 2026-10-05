package com.retrocollector.app.wishlist.presentation.viewmodel

import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.wishlist.domain.usecase.EnrichWishlistGameUseCase
import com.retrocollector.app.wishlist.domain.usecase.ImportWishlistUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class WishlistViewModel(
    private val repository: IGameRepository,
    private val importWishlistUseCase: ImportWishlistUseCase = ImportWishlistUseCase(repository),
    private val enrichWishlistGameUseCase: EnrichWishlistGameUseCase = EnrichWishlistGameUseCase(repository),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(dispatcher)
) {
    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            combine(repository.games, _searchQuery) { allGames, query ->
                val allWishlist = allGames.filter {
                    it.collectionStatus == CollectionStatus.WISHLIST
                }
                val filteredAndSorted = filterAndSortWishlist(allWishlist, query)
                Pair(allWishlist.size, filteredAndSorted)
            }.collect { (totalCount, games) ->
                _uiState.update {
                    it.copy(
                        wishlistGames = games,
                        allWishlistCount = totalCount,
                        searchQuery = _searchQuery.value
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    private fun filterAndSortWishlist(games: List<GameItem>, query: String): List<GameItem> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return games.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }

        return games
            .filter { game ->
                game.title.contains(trimmedQuery, ignoreCase = true) ||
                    game.franchiseName.contains(trimmedQuery, ignoreCase = true) ||
                    (game.productCode?.contains(trimmedQuery, ignoreCase = true) == true) ||
                    (game.barcode?.contains(trimmedQuery, ignoreCase = true) == true) ||
                    game.spottedLocation.contains(trimmedQuery, ignoreCase = true)
            }
            .sortedWith(
                compareBy<GameItem> { game ->
                    getRelevanceRank(game, trimmedQuery)
                }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            )
    }

    private fun getRelevanceRank(game: GameItem, query: String): Int {
        val title = game.title
        return when {
            title.equals(query, ignoreCase = true) -> 0
            title.startsWith(query, ignoreCase = true) -> 1
            title.split(" ", "-", ":").any { it.startsWith(query, ignoreCase = true) } -> 2
            title.contains(query, ignoreCase = true) -> 3
            game.franchiseName.contains(query, ignoreCase = true) -> 4
            else -> 5
        }
    }

    fun openImportDialog() {
        _uiState.update { it.copy(isImportDialogOpen = true, importResult = null) }
    }

    fun closeImportDialog() {
        _uiState.update { it.copy(isImportDialogOpen = false, importResult = null) }
    }

    fun resetImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }

    fun importWishlistCsv(csvContent: String) {
        val result = importWishlistUseCase(csvContent)
        _uiState.update { it.copy(importResult = result) }

        if (result.added.isNotEmpty()) {
            enrichGamesInBackground(result.added)
        }
    }

    private fun enrichGamesInBackground(games: List<GameItem>) {
        val total = games.size
        _uiState.update { it.copy(enrichmentProgress = Pair(0, total)) }

        scope.launch {
            var completed = 0
            for (game in games) {
                try {
                    enrichWishlistGameUseCase(game.id)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Individual failure does not block remaining items
                }
                completed++
                _uiState.update { it.copy(enrichmentProgress = Pair(completed, total)) }
            }
            _uiState.update { it.copy(enrichmentProgress = null) }
        }
    }

    fun retryEnrichment(game: GameItem) {
        scope.launch {
            try {
                enrichWishlistGameUseCase(game.id)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // Error handled internally by the use case
            }
        }
    }
}
