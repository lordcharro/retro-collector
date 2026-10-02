package com.retrocollector.app.presentation

import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.wishlist.domain.usecase.EnrichWishlistGameUseCase
import com.retrocollector.app.wishlist.domain.usecase.ImportWishlistUseCase
import com.retrocollector.app.wishlist.presentation.viewmodel.WishlistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WishlistViewModelTest {

    private class FakeRepository : IGameRepository {
        private val _games = MutableStateFlow<List<GameItem>>(emptyList())
        override val games: StateFlow<List<GameItem>> = _games.asStateFlow()
        override val chatMessages: StateFlow<List<ChatMessage>> = MutableStateFlow<List<ChatMessage>>(emptyList()).asStateFlow()
        override val settings: StateFlow<AppSettings> = MutableStateFlow(AppSettings()).asStateFlow()

        override fun getGameById(id: String): GameItem? = _games.value.find { it.id == id }
        override fun upsertGame(game: GameItem) {
            _games.value = _games.value.filter { it.id != game.id } + game
        }
        override fun deleteGame(id: String) {
            _games.value = _games.value.filter { it.id != id }
        }
        override fun addOrUpdateOffer(gameId: String, offer: com.retrocollector.app.core.domain.model.GameOffer) {
            val g = getGameById(gameId) ?: return
            val currentOffers = g.offers.toMutableList()
            val index = currentOffers.indexOfFirst { it.id == offer.id }
            if (index >= 0) currentOffers[index] = offer else currentOffers.add(0, offer)
            upsertGame(g.copy(offers = currentOffers))
        }
        override fun deleteOffer(gameId: String, offerId: String) {
            val g = getGameById(gameId) ?: return
            upsertGame(g.copy(offers = g.offers.filter { it.id != offerId }))
        }
        override fun convertOfferToOwned(
            gameId: String,
            offerId: String?,
            finalPriceChf: Double,
            condition: com.retrocollector.app.core.domain.model.GameCondition
        ) {
            val g = getGameById(gameId) ?: return
            upsertGame(
                g.copy(
                    collectionStatus = com.retrocollector.app.core.domain.model.CollectionStatus.OWNED,
                    paidPriceChf = finalPriceChf,
                    acquiredCondition = condition
                )
            )
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
    private lateinit var viewModel: WishlistViewModel

    private val sampleWishlistGame = GameItem(
        id = "n64_goldeneye",
        title = "GoldenEye 007",
        franchiseName = "James Bond",
        platform = ConsolePlatform.N64,
        productCode = "NUS-NGEP-EUR",
        spottedLocation = "Geneva",
        languageStatus = LanguageStatus.FULL_ENGLISH,
        collectionStatus = CollectionStatus.WISHLIST
    )

    private val sampleOwnedGame = GameItem(
        id = "gc_mario_sunshine",
        title = "Super Mario Sunshine",
        franchiseName = "Super Mario",
        platform = ConsolePlatform.GAMECUBE,
        productCode = "DOL-GMSE-EUR",
        spottedLocation = "Zurich",
        languageStatus = LanguageStatus.FULL_ENGLISH,
        collectionStatus = CollectionStatus.OWNED
    )

    @BeforeEach
    fun setUp() {
        repository = FakeRepository()
        repository.upsertGame(sampleWishlistGame)
        repository.upsertGame(sampleOwnedGame)

        viewModel = WishlistViewModel(
            repository = repository,
            importWishlistUseCase = ImportWishlistUseCase(repository),
            enrichWishlistGameUseCase = EnrichWishlistGameUseCase(repository),
            dispatcher = Dispatchers.Unconfined,
            scope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun `initializes with only wishlist games from repository`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(1, state.wishlistGames.size)
        assertEquals(sampleWishlistGame.id, state.wishlistGames.first().id)
    }

    @Test
    fun `opens and closes import dialog`() = runTest {
        assertFalse(viewModel.uiState.value.isImportDialogOpen)

        viewModel.openImportDialog()
        assertTrue(viewModel.uiState.value.isImportDialogOpen)

        viewModel.closeImportDialog()
        assertFalse(viewModel.uiState.value.isImportDialogOpen)
    }

    @Test
    fun `importing CSV adds new games to wishlist and updates state`() = runTest {
        val csv = "Perfect Dark, N64"

        viewModel.importWishlistCsv(csv)

        val state = viewModel.uiState.value
        assertNotNull(state.importResult)
        assertEquals(1, state.importResult?.added?.size)
        assertEquals(2, state.wishlistGames.size)
    }

    @Test
    fun `resetImportResult clears importResult in state`() = runTest {
        val csv = "Perfect Dark, N64"
        viewModel.importWishlistCsv(csv)
        assertNotNull(viewModel.uiState.value.importResult)

        viewModel.resetImportResult()
        assertNull(viewModel.uiState.value.importResult)
    }

    @Test
    fun `wishlist games are sorted alphabetically A-Z by default`() = runTest {
        val gameA = sampleWishlistGame.copy(id = "w1", title = "Banjo-Kazooie")
        val gameB = sampleWishlistGame.copy(id = "w2", title = "Zelda: Majora's Mask")
        val gameC = sampleWishlistGame.copy(id = "w3", title = "F-Zero X")
        repository.upsertGame(gameB)
        repository.upsertGame(gameA)
        repository.upsertGame(gameC)

        val titles = viewModel.uiState.value.wishlistGames.map { it.title }
        assertEquals(listOf("Banjo-Kazooie", "F-Zero X", "GoldenEye 007", "Zelda: Majora's Mask"), titles)
    }

    @Test
    fun `search query filters wishlist games`() = runTest {
        val marioGame = sampleWishlistGame.copy(id = "w1", title = "Super Mario 64")
        val fzeroGame = sampleWishlistGame.copy(id = "w2", title = "F-Zero X")
        repository.upsertGame(marioGame)
        repository.upsertGame(fzeroGame)

        viewModel.onSearchQueryChange("Mario")

        val state = viewModel.uiState.value
        assertEquals(1, state.wishlistGames.size)
        assertEquals("Super Mario 64", state.wishlistGames.first().title)
        assertEquals(3, state.allWishlistCount)
    }

    @Test
    fun `search query ranks exact and prefix matches above partial matches`() = runTest {
        val exactMatch = sampleWishlistGame.copy(id = "w1", title = "Mario")
        val prefixMatch = sampleWishlistGame.copy(id = "w2", title = "Mario Kart 64")
        val wordPrefixMatch = sampleWishlistGame.copy(id = "w3", title = "Super Mario 64")
        val containsMatch = sampleWishlistGame.copy(id = "w4", title = "Paper-Marioland")
        repository.upsertGame(containsMatch)
        repository.upsertGame(wordPrefixMatch)
        repository.upsertGame(exactMatch)
        repository.upsertGame(prefixMatch)

        viewModel.onSearchQueryChange("Mario")

        val state = viewModel.uiState.value
        val titles = state.wishlistGames.map { it.title }
        assertEquals("Mario", titles[0])
        assertEquals("Mario Kart 64", titles[1])
        assertTrue(titles.contains("Super Mario 64"))
        assertTrue(titles.contains("Paper-Marioland"))
    }

    @Test
    fun `clearSearch restores full sorted wishlist`() = runTest {
        val marioGame = sampleWishlistGame.copy(id = "w1", title = "Super Mario 64")
        repository.upsertGame(marioGame)

        viewModel.onSearchQueryChange("Mario")
        assertEquals(1, viewModel.uiState.value.wishlistGames.size)

        viewModel.clearSearch()
        assertEquals(2, viewModel.uiState.value.wishlistGames.size)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }
}
