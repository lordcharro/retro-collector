package com.retrocollector.app.discovery.domain.usecase

import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DiscoverGamesUseCaseTest {

    private class DiscoveryFakeRepository : IGameRepository {
        private val _games = MutableStateFlow<List<GameItem>>(emptyList())
        override val games: StateFlow<List<GameItem>> = _games.asStateFlow()
        override val chatMessages: StateFlow<List<ChatMessage>> = MutableStateFlow(emptyList())
        override val settings: StateFlow<AppSettings> = MutableStateFlow(AppSettings())

        override fun getGameById(id: String): GameItem? = null
        override fun upsertGame(game: GameItem) { /* no-op in fake */ }
        override fun deleteGame(id: String) { /* no-op in fake */ }
        override fun getChatMessagesForGame(gameId: String): List<ChatMessage> = emptyList()
        override fun addChatMessage(message: ChatMessage) { /* no-op in fake */ }
        override fun updateSettings(settings: AppSettings) { /* no-op in fake */ }
        override suspend fun syncFromFirestore(): Result<Unit> = Result.success(Unit)
        override suspend fun testGeminiConnection(apiKey: String, model: String): Result<String> = Result.success("OK")
        override suspend fun inspectGameWithAi(query: String, imageBase64: String?, spottedLocation: String, askingPriceChf: Double?) =
            Result.success(Pair(ChatMessage(id = "1", contextId = "1", sender = MessageSender.GEMINI, text = ""), null))
        override suspend fun sendFollowUpChat(contextId: String, userMessage: String, imageBase64: String?) =
            Result.success(ChatMessage(id = "1", contextId = contextId, sender = MessageSender.GEMINI, text = ""))
        override fun getGamesByStatus(status: CollectionStatus): List<GameItem> = emptyList()

        override suspend fun discoverGames(
            query: String?,
            genre: GameGenre?,
            platform: ConsolePlatform?,
            forceRefresh: Boolean
        ): Result<List<DiscoveredGameItem>> {
            val sample = listOf(
                DiscoveredGameItem(
                    id = "disc_monkey_island",
                    title = "Monkey Island 2: LeChuck's Revenge",
                    platform = ConsolePlatform.PS3,
                    genreDisplayName = "Point & Click",
                    genreTags = listOf("Point & Click", "Adventure"),
                    recommendationReason = "Classic Point & Click",
                    languageStatus = LanguageStatus.FULL_ENGLISH,
                    hasModernPortOrRemaster = true,
                    modernPortDetails = "Switch Remaster",
                    estimatedPriceChf = 32.0,
                    similarTitles = listOf("Grim Fandango", "Broken Sword")
                )
            )
            return Result.success(sample)
        }

        override suspend fun getSimilarGames(
            game: GameItem,
            forceRefresh: Boolean
        ): Result<List<DiscoveredGameItem>> {
            val sample = listOf(
                DiscoveredGameItem(
                    id = "disc_grim_fandango",
                    title = "Grim Fandango Remastered",
                    platform = ConsolePlatform.SWITCH,
                    genreDisplayName = "Point & Click",
                    genreTags = listOf("Point & Click"),
                    recommendationReason = "Cult classic adventure",
                    languageStatus = LanguageStatus.FULL_ENGLISH,
                    estimatedPriceChf = 35.0
                )
            )
            return Result.success(sample)
        }
    }

    private lateinit var repository: DiscoveryFakeRepository
    private lateinit var discoverUseCase: DiscoverGamesUseCase
    private lateinit var similarUseCase: GetSimilarGamesUseCase

    @BeforeEach
    fun setUp() {
        repository = DiscoveryFakeRepository()
        discoverUseCase = DiscoverGamesUseCase(repository)
        similarUseCase = GetSimilarGamesUseCase(repository)
    }

    @Test
    fun `discoverGames returns list of suggested games`() = runTest {
        val result = discoverUseCase(genre = GameGenre.POINT_AND_CLICK)
        assertTrue(result.isSuccess)
        val games = result.getOrThrow()
        assertEquals(1, games.size)
        assertEquals("Monkey Island 2: LeChuck's Revenge", games.first().title)
        assertTrue(games.first().hasModernPortOrRemaster)
    }

    @Test
    fun `getSimilarGames returns similar titles for inspected game`() = runTest {
        val game = GameItem(
            id = "test_game",
            title = "Monkey Island",
            platform = ConsolePlatform.PS3
        )
        val result = similarUseCase(game)
        assertTrue(result.isSuccess)
        val similar = result.getOrThrow()
        assertEquals(1, similar.size)
        assertEquals("Grim Fandango Remastered", similar.first().title)
    }

    @Test
    fun `DiscoveredGameItem converts properly to GameItem for wishlist`() {
        val discovered = DiscoveredGameItem(
            id = "disc_1",
            title = "Grim Fandango Remastered",
            platform = ConsolePlatform.SWITCH,
            releaseYear = "2018",
            estimatedPriceChf = 40.0,
            hasModernPortOrRemaster = true,
            modernPortDetails = "Physical iam8bit"
        )
        val gameItem = discovered.toGameItem(status = CollectionStatus.WISHLIST)
        assertEquals("Grim Fandango Remastered", gameItem.title)
        assertEquals(ConsolePlatform.SWITCH, gameItem.platform)
        assertEquals(CollectionStatus.WISHLIST, gameItem.collectionStatus)
        assertTrue(gameItem.personalNotes.contains("Port/Remaster: Physical iam8bit"))
    }
}
