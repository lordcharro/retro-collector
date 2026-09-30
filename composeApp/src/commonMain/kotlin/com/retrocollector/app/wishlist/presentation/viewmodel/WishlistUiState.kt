package com.retrocollector.app.wishlist.presentation.viewmodel

import androidx.compose.runtime.Immutable
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.wishlist.domain.usecase.ImportResult

@Immutable
data class WishlistUiState(
    val wishlistGames: List<GameItem> = emptyList(),
    val isImportDialogOpen: Boolean = false,
    val importResult: ImportResult? = null,
    val enrichmentProgress: Pair<Int, Int>? = null // (completed, total)
)
