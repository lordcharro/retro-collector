package com.retrocollector.app.discovery.presentation.viewmodel

import androidx.compose.runtime.Immutable
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameGenre

@Immutable
data class DiscoveryUiState(
    val discoveredGames: List<DiscoveredGameItem> = emptyList(),
    val selectedGenre: GameGenre = GameGenre.ALL,
    val selectedPlatform: ConsolePlatform? = null,
    val searchQuery: String = "",
    val isDiscovering: Boolean = false
)
