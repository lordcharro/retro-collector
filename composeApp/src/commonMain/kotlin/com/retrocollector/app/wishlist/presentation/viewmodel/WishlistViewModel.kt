package com.retrocollector.app.wishlist.presentation.viewmodel

import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.wishlist.domain.usecase.EnrichWishlistGameUseCase
import com.retrocollector.app.wishlist.domain.usecase.ImportWishlistUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class WishlistViewModel(
    private val repository: IGameRepository,
    private val importWishlistUseCase: ImportWishlistUseCase = ImportWishlistUseCase(repository),
    private val enrichWishlistGameUseCase: EnrichWishlistGameUseCase = EnrichWishlistGameUseCase(repository),
    private val saveGameUseCase: SaveGameUseCase = SaveGameUseCase(repository),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(dispatcher)
) {
    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.games.collect { allGames ->
                val wishlist = allGames.filter {
                    it.collectionStatus == CollectionStatus.WISHLIST ||
                        @Suppress("DEPRECATION") (it.collectionStatus == CollectionStatus.HUNTING)
                }
                _uiState.update { it.copy(wishlistGames = wishlist) }
            }
        }
    }

    fun openImportDialog() {
        _uiState.update { it.copy(isImportDialogOpen = true, importResult = null) }
    }

    fun closeImportDialog() {
        _uiState.update { it.copy(isImportDialogOpen = false, importResult = null) }
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
                    // Falha individual não bloqueia os restantes
                }
                completed++
                _uiState.update { it.copy(enrichmentProgress = Pair(completed, total)) }
            }
            _uiState.update { it.copy(enrichmentProgress = null) }
        }
    }

    fun moveToHunting(game: GameItem) {
        val updated = game.copy(
            collectionStatus = CollectionStatus.WISHLIST,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        saveGameUseCase(updated)
    }

    fun retryEnrichment(game: GameItem) {
        scope.launch {
            try {
                enrichWishlistGameUseCase(game.id)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // Erro tratado internamente pelo use case
            }
        }
    }
}
