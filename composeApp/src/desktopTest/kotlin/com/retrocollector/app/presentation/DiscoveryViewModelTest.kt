package com.retrocollector.app.presentation

import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.discovery.domain.usecase.DiscoverGamesUseCase
import com.retrocollector.app.discovery.presentation.viewmodel.DiscoveryViewModel
import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DiscoveryViewModelTest {

    private class FakeRepository(
        private val discoveredList: List<DiscoveredGameItem> = emptyList()
    ) : IGameRepository {
        private val _savedGames = MutableStateFlow<List<GameItem>>(emptyList())
        override val games: StateFlow<List<GameItem>> = _savedGames.asStateFlow()
        override val chatMessages: StateFlow<List<ChatMessage>> = MutableStateFlow<List<ChatMessage>>(emptyList()).asStateFlow()
        override val settings: StateFlow<AppSettings> = MutableStateFlow(AppSettings()).asStateFlow()

        override fun getGameById(id: String): GameItem? = _savedGames.value.find { it.id == id }
        override fun upsertGame(game: GameItem) {
            _savedGames.value = _savedGames.value.filter { it.id != game.id } + game
        }
        override fun deleteGame(id: String) {
            // no-op
        }
        override fun getChatMessagesForGame(gameId: String): List<ChatMessage> = emptyList()
        override fun addChatMessage(message: ChatMessage) {
            // no-op
        }
        override fun updateSettings(settings: AppSettings) {
            // no-op
        }
        override suspend fun syncFromFirestore(): Result<Unit> = Result.success(Unit)
        override suspend fun testGeminiConnection(apiKey: String, model: String): Result<String> = Result.success("OK")
        override suspend fun inspectGameWithAi(
            query: String,
            imageBase64: String?,
            spottedLocation: String,
            askingPriceChf: Double?
        ): Result<Pair<ChatMessage, GameItem?>> = Result.failure(NotImplementedError())

        override suspend fun sendFollowUpChat(
            contextId: String,
            userMessage: String,
            imageBase64: String?
        ): Result<ChatMessage> = Result.failure(NotImplementedError())

        override fun getGamesByStatus(status: CollectionStatus): List<GameItem> = emptyList()

        override suspend fun discoverGames(
            query: String?,
            genre: GameGenre?,
            platform: ConsolePlatform?,
            forceRefresh: Boolean
        ): Result<List<DiscoveredGameItem>> {
            var result = discoveredList
            if (genre != null && genre != GameGenre.ALL) {
                result = result.filter { it.genreDisplayName.contains(genre.displayName, ignoreCase = true) || it.genreTags.contains(genre.displayName) }
            }
            if (platform != null) {
                result = result.filter { it.platform == platform }
            }
            if (!query.isNullOrBlank()) {
                result = result.filter { it.title.contains(query, ignoreCase = true) }
            }
            return Result.success(result)
        }

        override suspend fun getSimilarGames(
            game: GameItem,
            forceRefresh: Boolean
        ): Result<List<DiscoveredGameItem>> = Result.success(emptyList())
    }

    private val sampleDiscovered = listOf(
        DiscoveredGameItem(
            id = "f-zero-x",
            title = "F-Zero X",
            platform = ConsolePlatform.N64,
            genreDisplayName = "Plataformas",
            genreTags = listOf("Plataformas"),
            recommendationReason = "Fast-paced racing classic"
        ),
        DiscoveredGameItem(
            id = "monkey-island",
            title = "Monkey Island 2",
            platform = ConsolePlatform.PS3,
            genreDisplayName = "Point & Click",
            genreTags = listOf("Point & Click"),
            recommendationReason = "Essential LucasArts classic"
        )
    )

    private lateinit var repository: FakeRepository
    private lateinit var viewModel: DiscoveryViewModel

    @BeforeEach
    fun setUp() {
        repository = FakeRepository(sampleDiscovered)

        viewModel = DiscoveryViewModel(
            discoverGamesUseCase = DiscoverGamesUseCase(repository),
            saveGameUseCase = SaveGameUseCase(repository),
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
            scope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun `loads local curated games on launch when auto discovery is disabled`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.discoveredGames.isNotEmpty())
        assertFalse(state.isDiscovering)
        assertFalse(state.isAutoDiscoveryEnabled)
    }

    @Test
    fun `filters local games by platform in manual mode`() = runTest {
        viewModel.onPlatformSelect(ConsolePlatform.N64)

        val state = viewModel.uiState.value
        assertEquals(ConsolePlatform.N64, state.selectedPlatform)
        assertTrue(state.discoveredGames.all { it.platform == ConsolePlatform.N64 })
    }

    @Test
    fun `filters local games by genre in manual mode`() = runTest {
        viewModel.onGenreSelect(GameGenre.POINT_AND_CLICK)

        val state = viewModel.uiState.value
        assertEquals(GameGenre.POINT_AND_CLICK, state.selectedGenre)
        assertTrue(state.discoveredGames.isNotEmpty())
    }

    @Test
    fun `updates query and performs search on submit`() = runTest {
        viewModel.onSearchSubmit("Monkey")

        val state = viewModel.uiState.value
        assertEquals("Monkey", state.searchQuery)
        assertEquals(1, state.discoveredGames.size)
        assertEquals("Monkey Island 2", state.discoveredGames.first().title)
    }

    @Test
    fun `auto discovery enabled calls use case on filter change`() = runTest {
        val autoRepo = object : FakeRepository(sampleDiscovered) {
            override val settings: StateFlow<AppSettings> = MutableStateFlow(AppSettings(isAutoDiscoveryEnabled = true)).asStateFlow()
        }
        val autoVm = DiscoveryViewModel(
            discoverGamesUseCase = DiscoverGamesUseCase(autoRepo),
            saveGameUseCase = SaveGameUseCase(autoRepo),
            repository = autoRepo,
            dispatcher = Dispatchers.Unconfined,
            scope = CoroutineScope(Dispatchers.Unconfined)
        )

        autoVm.onPlatformSelect(ConsolePlatform.N64)
        val state = autoVm.uiState.value
        assertEquals(ConsolePlatform.N64, state.selectedPlatform)
        assertEquals(1, state.discoveredGames.size)
        assertEquals("F-Zero X", state.discoveredGames.first().title)
    }

    @Test
    fun `adds discovered game to wishlist`() = runTest {
        val item = sampleDiscovered.first()
        viewModel.addToWishlist(item, targetPriceChf = 30.0)

        val saved = repository.getGameById(item.id)
        assertNotNull(saved)
        assertEquals(CollectionStatus.WISHLIST, saved?.collectionStatus)
        assertEquals(30.0, saved?.targetPriceChf)
    }
}
