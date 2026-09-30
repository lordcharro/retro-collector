package com.retrocollector.app.dashboard.presentation.viewmodel

import com.retrocollector.app.core.domain.model.GameItem

/**
 * One-off effects for the Dashboard / Catalog feature.
 * Delivered via Channel<DashboardEffect> to ensure single consumption
 * and avoid stale consumable state.
 */
sealed interface DashboardEffect {
    data class ShowToast(val message: String, val isError: Boolean = false) : DashboardEffect
    data class NavigateToGameDetail(val game: GameItem) : DashboardEffect
    data class ScanCompleted(val game: GameItem) : DashboardEffect
}
