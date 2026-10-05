package com.retrocollector.app.collection.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.collection.presentation.components.CollectionStatsBar
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.presentation.components.GameListItemRow
import com.retrocollector.app.core.presentation.components.TacticalEmptyState
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.dashboard.presentation.ui.DashboardActions
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardUiState
import org.jetbrains.compose.resources.stringResource

import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Personal collection screen — tactical list of OWNED games
 * with statistics header (game count + total value).
 */
@Composable
fun CollectionScreen(
    state: DashboardUiState,
    actions: DashboardActions,
    onNavigateToDetail: (GameItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currency = remember(state.settings) {
        state.settings.defaultCurrency.ifBlank { "CHF" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        // Collection statistics bar
        CollectionStatsBar(
            games = state.collectionGames,
            currency = currency
        )

        // Collection games list
        if (state.collectionGames.isEmpty()) {
            TacticalEmptyState(
                icon = "📦",
                title = stringResource(Res.string.collection_empty_state),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.collectionGames, key = { it.id }) { game ->
                    GameListItemRow(
                        game = game,
                        isSelected = state.selectedGame?.id == game.id,
                        currency = currency,
                        onClick = {
                            actions.onGameSelected(game)
                            onNavigateToDetail(game)
                        }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                }
            }
        }
    }
}

@Preview
@Composable
fun CollectionScreenPreview() {
    RetroTactileTheme {
        val sampleGames = listOf(
            GameItem(
                id = "gc_re4",
                title = "Resident Evil 4",
                franchiseName = "Resident Evil",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2005",
                productCode = "DOL-P-G4BE",
                spottedLocation = "Brockenhaus Bern",
                askingPriceChf = 35.0,
                paidPriceChf = 35.0,
                collectionStatus = CollectionStatus.OWNED,
                languageStatus = LanguageStatus.SUBS_ONLY
            ),
            GameItem(
                id = "n64_sm64",
                title = "Super Mario 64",
                franchiseName = "Mario",
                platform = ConsolePlatform.N64,
                releaseYear = "1997",
                productCode = "NUS-NSMP-EUR",
                spottedLocation = "Ricardo.ch",
                askingPriceChf = 45.0,
                paidPriceChf = 40.0,
                collectionStatus = CollectionStatus.OWNED,
                languageStatus = LanguageStatus.FULL_ENGLISH
            ),
            GameItem(
                id = "ps3_mgs4",
                title = "Metal Gear Solid 4",
                franchiseName = "Metal Gear",
                platform = ConsolePlatform.PS3,
                releaseYear = "2008",
                productCode = "BLES-00246",
                spottedLocation = "Tutti.ch",
                askingPriceChf = 20.0,
                paidPriceChf = 18.0,
                collectionStatus = CollectionStatus.OWNED,
                languageStatus = LanguageStatus.FULL_ENGLISH
            )
        )
        CollectionScreen(
            state = DashboardUiState(
                collectionGames = sampleGames,
                selectedGame = sampleGames.first()
            ),
            actions = DashboardActions()
        )
    }
}
