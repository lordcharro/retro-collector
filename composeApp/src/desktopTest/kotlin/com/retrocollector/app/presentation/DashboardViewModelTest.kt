package com.retrocollector.app.presentation

import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.dashboard.domain.usecase.DeleteGameUseCase
import com.retrocollector.app.dashboard.domain.usecase.GetDashboardGamesUseCase
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardEffect
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardViewModel
import com.retrocollector.app.discovery.domain.usecase.GetSimilarGamesUseCase
import com.retrocollector.app.dossier.domain.usecase.SendFollowUpChatUseCase
import com.retrocollector.app.scanner.domain.usecase.AnalyzeGameWithGeminiUseCase
import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.settings.domain.usecase.TestGeminiConnectionUseCase
import com.retrocollector.app.settings.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DashboardViewModelTest {

    private class FakeRepository : IGameRepository {
        private val _games = MutableStateFlow<List<GameItem>>(emptyList())
        override val games: StateFlow<List<GameItem>> = _games.asStateFlow()

        private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
        override val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

        private val _settings = MutableStateFlow(AppSettings())
        override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

        override fun getGameById(id: String): GameItem? = _games.value.find { it.id == id }
        override fun upsertGame(game: GameItem) {
            _games.value = _games.value.filter { it.id != game.id } + game
        }
        override fun deleteGame(id: String) {
            _games.value = _games.value.filter { it.id != id }
        }
        override fun getChatMessagesForGame(gameId: String): List<ChatMessage> =
            _chatMessages.value.filter { it.contextId == gameId }
        override fun addChatMessage(message: ChatMessage) {
            _chatMessages.value = _chatMessages.value + message
        }
        override fun updateSettings(settings: AppSettings) {
            _settings.value = settings
        }
        override suspend fun syncFromFirestore(): Result<Unit> = Result.success(Unit)
        override suspend fun testGeminiConnection(apiKey: String, model: String): Result<String> = Result.success("OK")
        override suspend fun inspectGameWithAi(
            query: String,
            imageBase64: String?,
            spottedLocation: String,
            askingPriceChf: Double?
        ): Result<Pair<ChatMessage, GameItem?>> =
            Result.success(Pair(ChatMessage(id = "1", contextId = "test", sender = MessageSender.GEMINI, text = "Analysis"), null))

        override suspend fun sendFollowUpChat(
            contextId: String,
            userMessage: String,
            imageBase64: String?
        ): Result<ChatMessage> =
            Result.success(ChatMessage(id = "2", contextId = contextId, sender = MessageSender.GEMINI, text = "Response"))

        override fun getGamesByStatus(status: CollectionStatus): List<GameItem> =
            _games.value.filter { it.collectionStatus == status }

        override suspend fun discoverGames(
            query: String?,
            genre: GameGenre?,
            platform: ConsolePlatform?,
            forceRefresh: Boolean
        ): Result<List<DiscoveredGameItem>> = Result.success(emptyList())

        override suspend fun getSimilarGames(
            game: GameItem,
            forceRefresh: Boolean
        ): Result<List<DiscoveredGameItem>> = Result.success(emptyList())
    }

    private lateinit var repository: FakeRepository
    private lateinit var viewModel: DashboardViewModel

    private val sampleGame = GameItem(
        id = "n64_mario64",
        title = "Super Mario 64",
        franchiseName = "Super Mario",
        platform = ConsolePlatform.N64,
        productCode = "NUS-NSMP-EUR",
        spottedLocation = "Bern",
        languageStatus = LanguageStatus.FULL_ENGLISH,
        collectionStatus = CollectionStatus.WISHLIST
    )

    @BeforeEach
    fun setUp() {
        repository = FakeRepository()
        repository.upsertGame(sampleGame)

        viewModel = DashboardViewModel(
            repository = repository,
            getGamesUseCase = GetDashboardGamesUseCase(repository),
            saveGameUseCase = SaveGameUseCase(repository),
            deleteGameUseCase = DeleteGameUseCase(repository),
            analyzeGameUseCase = AnalyzeGameWithGeminiUseCase(repository),
            sendFollowUpChatUseCase = SendFollowUpChatUseCase(repository),
            updateSettingsUseCase = UpdateSettingsUseCase(repository),
            testGeminiConnectionUseCase = TestGeminiConnectionUseCase(repository),
            getSimilarGamesUseCase = GetSimilarGamesUseCase(repository),
            dispatcher = Dispatchers.Unconfined,
            scope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun `initializes with games from repository and selects first game`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(1, state.games.size)
        assertEquals(sampleGame.id, state.selectedGame?.id)
    }

    @Test
    fun `updates search query and filters games`() = runTest {
        viewModel.onSearchQueryChange("Zelda")
        val state = viewModel.uiState.value
        assertEquals("Zelda", state.searchQuery)
        assertTrue(state.games.isEmpty())
    }

    @Test
    fun `toggles platform selection`() = runTest {
        viewModel.onPlatformSelect(ConsolePlatform.PS3)
        assertEquals(ConsolePlatform.PS3, viewModel.uiState.value.selectedPlatform)
        assertTrue(viewModel.uiState.value.games.isEmpty())

        viewModel.onPlatformSelect(ConsolePlatform.PS3)
        assertNull(viewModel.uiState.value.selectedPlatform)
        assertEquals(1, viewModel.uiState.value.games.size)
    }

    @Test
    fun `updates game status and saves to repository defaulting paidPrice if owned`() = runTest {
        val gameWithAskingPrice = sampleGame.copy(askingPriceChf = 42.0, paidPriceChf = null)
        repository.upsertGame(gameWithAskingPrice)

        viewModel.updateGameStatus(gameWithAskingPrice, CollectionStatus.OWNED)

        val updated = repository.getGameById(sampleGame.id)
        assertNotNull(updated)
        assertEquals(CollectionStatus.OWNED, updated?.collectionStatus)
        assertEquals(42.0, updated?.paidPriceChf)
    }

    @Test
    fun `updates game paid price directly`() = runTest {
        viewModel.updateGamePaidPrice(sampleGame, 38.5)

        val updated = repository.getGameById(sampleGame.id)
        assertNotNull(updated)
        assertEquals(38.5, updated?.paidPriceChf)
    }

    @Test
    fun `clearFilters resets all filter state`() = runTest {
        viewModel.onSearchQueryChange("Zelda")
        viewModel.onPlatformSelect(ConsolePlatform.N64)
        viewModel.onStatusSelect(CollectionStatus.OWNED)
        viewModel.toggleEnglishOnlyFilter()
        viewModel.toggleUskAlertsFilter()

        viewModel.clearFilters()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertNull(state.selectedPlatform)
        assertNull(state.selectedStatus)
        assertFalse(state.filterEnglishOnly)
        assertFalse(state.filterUskAlertsOnly)
    }

    @Test
    fun `openMobileDetail and closeMobileDetail update isMobileDetailOpen`() = runTest {
        assertFalse(viewModel.uiState.value.isMobileDetailOpen)

        viewModel.openMobileDetail(sampleGame)
        assertTrue(viewModel.uiState.value.isMobileDetailOpen)
        assertEquals(sampleGame.id, viewModel.uiState.value.selectedGame?.id)

        viewModel.closeMobileDetail()
        assertFalse(viewModel.uiState.value.isMobileDetailOpen)
    }

    @Test
    fun `deleteGame removes game from repository and closes mobile detail if selected`() = runTest {
        viewModel.openMobileDetail(sampleGame)
        assertTrue(viewModel.uiState.value.isMobileDetailOpen)

        viewModel.deleteGame(sampleGame.id)

        assertNull(repository.getGameById(sampleGame.id))
        assertFalse(viewModel.uiState.value.isMobileDetailOpen)
    }

    @Test
    fun `testFirestoreConnection handles success and empty project ID`() = runTest {
        var failureResult: Result<String>? = null
        viewModel.testFirestoreConnection("") { result ->
            failureResult = result
        }
        assertNotNull(failureResult)
        assertTrue(failureResult!!.isFailure)

        var successResult: Result<String>? = null
        viewModel.testFirestoreConnection("test-proj") { result ->
            successResult = result
        }
        assertNotNull(successResult)
        assertTrue(successResult!!.isSuccess)
    }

    @Test
    fun `analyze game triggers effects`() = runTest {
        val effects = mutableListOf<DashboardEffect>()
        val job = launch(Dispatchers.Unconfined) {
            viewModel.effects.collect { effects.add(it) }
        }

        viewModel.analyzeNewGame("Super Mario 64", null, "Bern", 45.0)
        assertTrue(effects.any { it is DashboardEffect.ShowToast })
        job.cancel()
    }
}
