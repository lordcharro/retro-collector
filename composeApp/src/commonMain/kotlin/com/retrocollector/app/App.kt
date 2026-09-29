package com.retrocollector.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.retrocollector.app.core.data.repository.GameRepositoryImpl
import com.retrocollector.app.core.presentation.theme.RetroTactileTheme
import com.retrocollector.app.core.presentation.theme.SurfaceBase
import com.retrocollector.app.dashboard.domain.usecase.DeleteGameUseCase
import com.retrocollector.app.dashboard.domain.usecase.GetDashboardGamesUseCase
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.dashboard.presentation.ui.AdaptiveMainScreen
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardViewModel
import com.retrocollector.app.dossier.domain.usecase.SendFollowUpChatUseCase
import com.retrocollector.app.scanner.domain.usecase.AnalyzeGameWithGeminiUseCase
import com.retrocollector.app.settings.domain.usecase.TestGeminiConnectionUseCase
import com.retrocollector.app.settings.domain.usecase.UpdateSettingsUseCase
import com.retrocollector.app.wishlist.domain.usecase.EnrichWishlistGameUseCase
import com.retrocollector.app.wishlist.domain.usecase.ImportWishlistUseCase
import com.retrocollector.app.discovery.domain.usecase.DiscoverGamesUseCase
import com.retrocollector.app.discovery.domain.usecase.GetSimilarGamesUseCase

@Composable
fun App() {
    val viewModel = remember {
        val repository = GameRepositoryImpl()
        DashboardViewModel(
            repository = repository,
            getGamesUseCase = GetDashboardGamesUseCase(repository),
            saveGameUseCase = SaveGameUseCase(repository),
            deleteGameUseCase = DeleteGameUseCase(repository),
            analyzeGameUseCase = AnalyzeGameWithGeminiUseCase(repository),
            sendFollowUpChatUseCase = SendFollowUpChatUseCase(repository),
            updateSettingsUseCase = UpdateSettingsUseCase(repository),
            testGeminiConnectionUseCase = TestGeminiConnectionUseCase(repository),
            importWishlistUseCase = ImportWishlistUseCase(repository),
            enrichWishlistGameUseCase = EnrichWishlistGameUseCase(repository),
            discoverGamesUseCase = DiscoverGamesUseCase(repository),
            getSimilarGamesUseCase = GetSimilarGamesUseCase(repository)
        )
    }

    RetroTactileTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SurfaceBase
        ) {
            AdaptiveMainScreen(viewModel = viewModel)
        }
    }
}

