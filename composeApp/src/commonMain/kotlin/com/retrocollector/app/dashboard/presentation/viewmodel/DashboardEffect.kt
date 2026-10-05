package com.retrocollector.app.dashboard.presentation.viewmodel

import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.presentation.util.UiText

/**
 * One-off effects for the Dashboard / Catalog feature.
 * Delivered via Channel<DashboardEffect> to ensure single consumption
 * and avoid stale consumable state.
 */
sealed interface DashboardEffect {
    data class ShowToast(val message: UiText, val isError: Boolean = false) : DashboardEffect {
        constructor(text: String, isError: Boolean = false) : this(UiText.DynamicString(text), isError)
    }
    data class NavigateToGameDetail(val game: GameItem) : DashboardEffect
    data class ScanCompleted(val game: GameItem) : DashboardEffect
}
