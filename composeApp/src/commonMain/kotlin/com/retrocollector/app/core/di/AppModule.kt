package com.retrocollector.app.core.di

import com.retrocollector.app.core.data.datasource.AiDataSourceFactory
import com.retrocollector.app.core.data.datasource.AnthropicRemoteDataSource
import com.retrocollector.app.core.data.datasource.GeminiRemoteDataSource
import com.retrocollector.app.core.data.datasource.OpenAiCompatibleRemoteDataSource
import com.retrocollector.app.core.data.firestore.FirestoreService
import com.retrocollector.app.core.data.repository.GameRepositoryImpl
import com.retrocollector.app.core.data.scraper.ListingScraper
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.dashboard.domain.usecase.DeleteGameUseCase
import com.retrocollector.app.dashboard.domain.usecase.GetDashboardGamesUseCase
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardViewModel
import com.retrocollector.app.discovery.domain.usecase.DiscoverGamesUseCase
import com.retrocollector.app.discovery.domain.usecase.GetCuratedGamesUseCase
import com.retrocollector.app.discovery.domain.usecase.GetSimilarGamesUseCase
import com.retrocollector.app.discovery.presentation.viewmodel.DiscoveryViewModel
import com.retrocollector.app.dossier.domain.usecase.SendFollowUpChatUseCase
import com.retrocollector.app.scanner.domain.usecase.AnalyzeGameWithAiUseCase
import com.retrocollector.app.scanner.domain.usecase.AnalyzeGameWithGeminiUseCase
import com.retrocollector.app.settings.data.datasource.SettingsLocalDataSource
import com.retrocollector.app.settings.data.datasource.createSettingsLocalDataSource
import com.retrocollector.app.settings.domain.model.AiProvider
import com.retrocollector.app.settings.domain.usecase.TestAiConnectionUseCase
import com.retrocollector.app.settings.domain.usecase.TestGeminiConnectionUseCase
import com.retrocollector.app.settings.domain.usecase.UpdateSettingsUseCase
import com.retrocollector.app.wishlist.domain.usecase.EnrichWishlistGameUseCase
import com.retrocollector.app.wishlist.domain.usecase.ImportWishlistUseCase
import com.retrocollector.app.wishlist.presentation.viewmodel.WishlistViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// Set it false to launch the app without mock data (ideal for testing Firestore sync from a blank slate)
const val LOAD_MOCK_DATA: Boolean = false

val dataModule = module {
    single<SettingsLocalDataSource> { createSettingsLocalDataSource() }
    single { FirestoreService() }
    single { GeminiRemoteDataSource() }
    single { AnthropicRemoteDataSource() }
    single { OpenAiCompatibleRemoteDataSource(AiProvider.OPENAI_COMPATIBLE) }
    single {
        AiDataSourceFactory(
            geminiDataSource = get(),
            claudeDataSource = get(),
            openAiDataSource = get(),
            localOllamaDataSource = OpenAiCompatibleRemoteDataSource(AiProvider.LOCAL_OLLAMA)
        )
    }
    single { ListingScraper() }
    single<IGameRepository> {
        GameRepositoryImpl(
            firestoreService = get(),
            aiDataSourceFactory = get(),
            listingScraper = get(),
            settingsLocalDataSource = get(),
            loadMockData = LOAD_MOCK_DATA
        )
    }
}

val domainModule = module {
    factory { GetDashboardGamesUseCase(get()) }
    factory { SaveGameUseCase(get()) }
    factory { DeleteGameUseCase(get()) }
    factory { AnalyzeGameWithGeminiUseCase(get()) }
    factory { AnalyzeGameWithAiUseCase(get()) }
    factory { SendFollowUpChatUseCase(get()) }
    factory { UpdateSettingsUseCase(get()) }
    factory { TestGeminiConnectionUseCase(get()) }
    factory { TestAiConnectionUseCase(get()) }
    factory { GetSimilarGamesUseCase(get()) }
    factory { DiscoverGamesUseCase(get()) }
    factory { GetCuratedGamesUseCase(get()) }
    factory { ImportWishlistUseCase(get()) }
    factory { EnrichWishlistGameUseCase(get()) }
}

val presentationModule = module {
    factory {
        DashboardViewModel(
            repository = get(),
            getGamesUseCase = get(),
            saveGameUseCase = get(),
            deleteGameUseCase = get(),
            analyzeGameUseCase = get(),
            sendFollowUpChatUseCase = get(),
            updateSettingsUseCase = get(),
            testAiConnectionUseCase = get(),
            getSimilarGamesUseCase = get()
        )
    }
    factory {
        DiscoveryViewModel(
            discoverGamesUseCase = get(),
            getCuratedGamesUseCase = get(),
            saveGameUseCase = get(),
            repository = get()
        )
    }
    factory {
        WishlistViewModel(
            repository = get(),
            importWishlistUseCase = get(),
            enrichWishlistGameUseCase = get()
        )
    }
}

val appModules: List<Module> = listOf(dataModule, domainModule, presentationModule)

fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    startKoin {
        appDeclaration?.invoke(this)
        modules(appModules)
    }
}
