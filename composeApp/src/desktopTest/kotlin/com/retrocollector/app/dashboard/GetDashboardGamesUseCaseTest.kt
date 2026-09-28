package com.retrocollector.app.dashboard

import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.domain.model.MessageSender
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.dashboard.domain.model.DashboardFilterCriteria
import com.retrocollector.app.dashboard.domain.usecase.GetDashboardGamesUseCase
import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDashboardGamesUseCaseTest {

    private class FakeGameRepository(initialGames: List<GameItem>) : IGameRepository {
        private val _games = MutableStateFlow(initialGames)
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
        override fun getChatMessagesForGame(gameId: String): List<ChatMessage> = emptyList()
        override fun addChatMessage(message: ChatMessage) {
            _chatMessages.value = _chatMessages.value + message
        }
        override fun updateSettings(settings: AppSettings) {
            _settings.value = settings
        }
        override suspend fun syncFromFirestore(): Result<Unit> = Result.success(Unit)
        override suspend fun inspectGameWithAi(
            query: String,
            imageBase64: String?,
            spottedLocation: String,
            askingPriceChf: Double?
        ): Result<Pair<ChatMessage, GameItem?>> = Result.success(Pair(ChatMessage(id = "1", contextId = "test", sender = MessageSender.GEMINI, text = ""), null))
        override suspend fun sendFollowUpChat(
            contextId: String,
            userMessage: String,
            imageBase64: String?
        ): Result<ChatMessage> = Result.success(ChatMessage(id = "1", contextId = contextId, sender = MessageSender.GEMINI, text = ""))
    }

    private val testGames = listOf(
        GameItem(
            id = "gc_re4",
            title = "Resident Evil 4",
            franchiseName = "Resident Evil",
            platform = ConsolePlatform.GAMECUBE,
            productCode = "DOL-P-G4BE",
            spottedLocation = "Bern",
            languageStatus = LanguageStatus.SUBS_ONLY,
            collectionStatus = CollectionStatus.HUNTING
        ),
        GameItem(
            id = "gc_zelda",
            title = "The Legend of Zelda: The Wind Waker",
            franchiseName = "The Legend of Zelda",
            platform = ConsolePlatform.GAMECUBE,
            productCode = "DOL-P-GZLP",
            spottedLocation = "Zurich",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            collectionStatus = CollectionStatus.OWNED
        ),
        GameItem(
            id = "ps3_fallout3",
            title = "Fallout 3",
            franchiseName = "Fallout",
            platform = ConsolePlatform.PS3,
            productCode = "BLES-00561",
            spottedLocation = "Ricardo.ch",
            languageStatus = LanguageStatus.GERMAN_ONLY,
            collectionStatus = CollectionStatus.PASS
        )
    )

    private lateinit var repository: FakeGameRepository
    private lateinit var useCase: GetDashboardGamesUseCase

    @BeforeEach
    fun setUp() {
        repository = FakeGameRepository(testGames)
        useCase = GetDashboardGamesUseCase(repository)
    }

    @Test
    fun `returns all games when criteria is empty`() = runTest {
        val result = useCase(DashboardFilterCriteria()).first()
        assertEquals(3, result.size)
    }

    @Test
    fun `filters games by title search query`() = runTest {
        val result = useCase(DashboardFilterCriteria(query = "Resident")).first()
        assertEquals(1, result.size)
        assertEquals("Resident Evil 4", result.first().title)
    }

    @Test
    fun `filters games by console platform`() = runTest {
        val result = useCase(DashboardFilterCriteria(platform = ConsolePlatform.PS3)).first()
        assertEquals(1, result.size)
        assertEquals("Fallout 3", result.first().title)
    }

    @Test
    fun `filters games by collection status`() = runTest {
        val result = useCase(DashboardFilterCriteria(status = CollectionStatus.HUNTING)).first()
        assertEquals(1, result.size)
        assertEquals("Resident Evil 4", result.first().title)
    }

    @Test
    fun `filters out german only games when englishOnly is true`() = runTest {
        val result = useCase(DashboardFilterCriteria(englishOnly = true)).first()
        assertEquals(2, result.size)
        assertTrue(result.none { it.languageStatus == LanguageStatus.GERMAN_ONLY })
    }

    @Test
    fun `returns only german only games when uskOnly is true`() = runTest {
        val result = useCase(DashboardFilterCriteria(uskOnly = true)).first()
        assertEquals(1, result.size)
        assertEquals("Fallout 3", result.first().title)
    }
}
