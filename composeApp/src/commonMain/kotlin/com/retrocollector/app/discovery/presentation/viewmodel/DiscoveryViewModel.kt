package com.retrocollector.app.discovery.presentation.viewmodel

import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.discovery.domain.usecase.DiscoverGamesUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    private val discoverGamesUseCase: DiscoverGamesUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(dispatcher)
) {
    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    init {
        fetchDiscoveryGames()
    }

    fun onGenreSelect(genre: GameGenre) {
        _uiState.update { it.copy(selectedGenre = genre) }
        fetchDiscoveryGames()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSearchSubmit(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        fetchDiscoveryGames(forceRefresh = true)
    }

    fun onPlatformSelect(platform: ConsolePlatform?) {
        _uiState.update {
            val newPlat = if (it.selectedPlatform == platform) null else platform
            it.copy(selectedPlatform = newPlat)
        }
        fetchDiscoveryGames()
    }

    fun addToWishlist(discovered: DiscoveredGameItem, targetPriceChf: Double? = null) {
        val gameItem = discovered.toGameItem(
            status = CollectionStatus.WISHLIST,
            targetPrice = targetPriceChf
        )
        saveGameUseCase(gameItem)
        _uiState.update { state ->
            val updatedList = state.discoveredGames.map {
                if (it.id == discovered.id) it.copy(isAlreadyInWishlist = true) else it
            }
            state.copy(discoveredGames = updatedList)
        }
    }

    fun fetchDiscoveryGames(forceRefresh: Boolean = false) {
        val currentGenre = _uiState.value.selectedGenre
        val currentPlatform = _uiState.value.selectedPlatform
        val currentQuery = _uiState.value.searchQuery

        _uiState.update { it.copy(isDiscovering = true) }
        scope.launch {
            val result = discoverGamesUseCase(
                query = currentQuery,
                genre = currentGenre,
                platform = currentPlatform,
                forceRefresh = forceRefresh
            )
            _uiState.update { state ->
                state.copy(
                    discoveredGames = result.getOrDefault(emptyList()),
                    isDiscovering = false
                )
            }
        }
    }
}
