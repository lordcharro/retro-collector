package com.retrocollector.app.dashboard.presentation.viewmodel

import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.core.domain.usecase.FindDuplicateGameUseCase
import com.retrocollector.app.dashboard.domain.model.DashboardFilterCriteria
import com.retrocollector.app.dashboard.domain.usecase.DeleteGameUseCase
import com.retrocollector.app.dashboard.domain.usecase.GetDashboardGamesUseCase
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.dossier.domain.usecase.SendFollowUpChatUseCase
import com.retrocollector.app.scanner.domain.usecase.AnalyzeGameWithGeminiUseCase
import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.settings.domain.usecase.TestGeminiConnectionUseCase
import com.retrocollector.app.settings.domain.usecase.UpdateSettingsUseCase
import com.retrocollector.app.wishlist.domain.usecase.EnrichWishlistGameUseCase
import com.retrocollector.app.wishlist.domain.usecase.ImportResult
import com.retrocollector.app.wishlist.domain.usecase.ImportWishlistUseCase
import com.retrocollector.app.discovery.domain.usecase.DiscoverGamesUseCase
import com.retrocollector.app.discovery.domain.usecase.GetSimilarGamesUseCase
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Immutable
data class DashboardUiState(
    val games: List<GameItem> = emptyList(),
    val selectedGame: GameItem? = null,
    val searchQuery: String = "",
    val selectedPlatform: ConsolePlatform? = null,
    val selectedStatus: CollectionStatus? = null,
    val filterEnglishOnly: Boolean = false,
    val filterUskAlertsOnly: Boolean = false,
    val activeChatMessages: List<ChatMessage> = emptyList(),
    val isAnalyzing: Boolean = false,
    val isScanDialogOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val statusMessage: String? = null,
    val settings: AppSettings = AppSettings(),
    // Navegação por secções
    val activeSection: AppSection = AppSection.CATALOG,
    // Wishlist
    val isImportDialogOpen: Boolean = false,
    val importResult: ImportResult? = null,
    val enrichmentProgress: Pair<Int, Int>? = null, // (completed, total)
    // Listas filtradas por secção
    val wishlistGames: List<GameItem> = emptyList(),
    val collectionGames: List<GameItem> = emptyList(),
    val catalogGames: List<GameItem> = emptyList(),
    // Discovery & Similarity
    val discoveredGames: List<DiscoveredGameItem> = emptyList(),
    val selectedDiscoveryGenre: GameGenre = GameGenre.ALL,
    val selectedDiscoveryPlatform: ConsolePlatform? = null,
    val discoverySearchQuery: String = "",
    val isDiscovering: Boolean = false,
    val similarGamesForActiveGame: List<DiscoveredGameItem> = emptyList(),
    val isSimilarGamesLoading: Boolean = false
)

@Suppress("TooManyFunctions")
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: IGameRepository,
    private val getGamesUseCase: GetDashboardGamesUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
    private val analyzeGameUseCase: AnalyzeGameWithGeminiUseCase,
    private val sendFollowUpChatUseCase: SendFollowUpChatUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val testGeminiConnectionUseCase: TestGeminiConnectionUseCase = TestGeminiConnectionUseCase(repository),
    private val importWishlistUseCase: ImportWishlistUseCase = ImportWishlistUseCase(repository),
    private val enrichWishlistGameUseCase: EnrichWishlistGameUseCase = EnrichWishlistGameUseCase(repository),
    private val discoverGamesUseCase: DiscoverGamesUseCase = DiscoverGamesUseCase(repository),
    private val getSimilarGamesUseCase: GetSimilarGamesUseCase = GetSimilarGamesUseCase(repository),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(dispatcher)
) {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Carregar recomendações iniciais de Discovery
        fetchDiscoveryGames()
        // Observar settings do repositório
        scope.launch {
            repository.settings.collect { set ->
                _uiState.update { it.copy(settings = set) }
            }
        }

        // Observar jogos com os filtros aplicados via GetDashboardGamesUseCase
        scope.launch {
            val filterFlow = _uiState.map {
                DashboardFilterCriteria(
                    query = it.searchQuery,
                    platform = it.selectedPlatform,
                    status = it.selectedStatus,
                    englishOnly = it.filterEnglishOnly,
                    uskOnly = it.filterUskAlertsOnly
                )
            }.distinctUntilChanged()

            filterFlow.flatMapLatest { criteria ->
                getGamesUseCase(criteria)
            }.collect { filtered ->
                _uiState.update { state ->
                    val newSelected = state.selectedGame?.let { sel ->
                        filtered.find { it.id == sel.id } ?: filtered.firstOrNull()
                    } ?: filtered.firstOrNull()

                    val chats = if (newSelected != null) {
                        repository.getChatMessagesForGame(newSelected.id)
                    } else emptyList()

                    // Separar jogos por secção
                    val catalog = filtered.filter {
                        it.collectionStatus == CollectionStatus.HUNTING ||
                            it.collectionStatus == CollectionStatus.PASS
                    }
                    val wishlist = filtered.filter {
                        it.collectionStatus == CollectionStatus.WISHLIST
                    }
                    val collection = filtered.filter {
                        it.collectionStatus == CollectionStatus.OWNED
                    }

                    state.copy(
                        games = filtered,
                        selectedGame = newSelected,
                        activeChatMessages = chats,
                        catalogGames = catalog,
                        wishlistGames = wishlist,
                        collectionGames = collection
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onPlatformSelect(platform: ConsolePlatform?) {
        _uiState.update {
            val newPlatform = if (it.selectedPlatform == platform) null else platform
            it.copy(selectedPlatform = newPlatform)
        }
    }

    fun onStatusSelect(status: CollectionStatus?) {
        _uiState.update {
            val newStatus = if (it.selectedStatus == status) null else status
            it.copy(selectedStatus = newStatus)
        }
    }

    fun toggleEnglishOnlyFilter() {
        _uiState.update { it.copy(filterEnglishOnly = !it.filterEnglishOnly) }
    }

    fun toggleUskAlertsFilter() {
        _uiState.update { it.copy(filterUskAlertsOnly = !it.filterUskAlertsOnly) }
    }

    fun onGameSelected(game: GameItem) {
        val chats = repository.getChatMessagesForGame(game.id)
        _uiState.update {
            it.copy(
                selectedGame = game,
                activeChatMessages = chats
            )
        }
        loadSimilarGamesForSelectedGame(game)
    }

    fun updateGameStatus(game: GameItem, newStatus: CollectionStatus) {
        val updated = game.copy(collectionStatus = newStatus)
        saveGameUseCase(updated)
    }

    fun deleteGame(gameId: String) {
        deleteGameUseCase(gameId)
    }

    fun openScanDialog() {
        _uiState.update { it.copy(isScanDialogOpen = true) }
    }

    fun closeScanDialog() {
        _uiState.update { it.copy(isScanDialogOpen = false) }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun saveSettings(newSettings: AppSettings) {
        updateSettingsUseCase(newSettings)
        closeSettings()
    }

    suspend fun testGeminiConnectionSuspend(apiKey: String, model: String = "gemini-3.7-flash"): Result<String> {
        return testGeminiConnectionUseCase(apiKey, model)
    }

    fun testGeminiConnection(apiKey: String, model: String = "gemini-3.7-flash", onResult: (Result<String>) -> Unit) {
        scope.launch {
            try {
                val result = testGeminiConnectionUseCase(apiKey, model)
                onResult(result)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun sendFollowUpMessage(question: String, imageBase64: String? = null) {
        val currentGame = _uiState.value.selectedGame ?: return
        val trimmed = question.trim()
        if (trimmed.isBlank() && imageBase64 == null) return

        // Adicionar mensagem de utilizador imediatamente para atualização reativa do ecrã
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val userMsg = ChatMessage(
            id = "user_$nowMs",
            contextId = currentGame.id,
            sender = MessageSender.USER,
            text = trimmed,
            imageBase64 = imageBase64
        )
        repository.addChatMessage(userMsg)
        _uiState.update {
            it.copy(
                activeChatMessages = repository.getChatMessagesForGame(currentGame.id),
                isAnalyzing = true
            )
        }

        scope.launch {
            sendFollowUpChatUseCase(currentGame.id, trimmed, imageBase64)
            val updatedChats = repository.getChatMessagesForGame(currentGame.id)
            val updatedGame = repository.getGameById(currentGame.id) ?: currentGame
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    selectedGame = updatedGame,
                    activeChatMessages = updatedChats
                )
            }
        }
    }

    fun analyzeNewGame(query: String, imageBase64: String?, spottedLocation: String, askingPriceChf: Double?) {
        scope.launch {
            _uiState.update { it.copy(isAnalyzing = true, statusMessage = null) }
            val result = analyzeGameUseCase(query, imageBase64, spottedLocation, askingPriceChf)
            result.onSuccess { (chatMsg, gameItem) ->
                if (gameItem != null) {
                    saveGameUseCase(gameItem)
                    onGameSelected(gameItem)
                }
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        isScanDialogOpen = false,
                        statusMessage = "Análise concluída com sucesso!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        statusMessage = "Erro na análise: ${err.message}"
                    )
                }
            }
        }
    }

    // ─── Navegação entre Secções ─────────────────────────────────

    fun onSectionSelect(section: AppSection) {
        _uiState.update { it.copy(activeSection = section) }
    }

    // ─── Wishlist: Importação e Enriquecimento ───────────────────

    fun openImportDialog() {
        _uiState.update { it.copy(isImportDialogOpen = true, importResult = null) }
    }

    fun closeImportDialog() {
        _uiState.update { it.copy(isImportDialogOpen = false, importResult = null) }
    }

    fun importWishlistCsv(csvContent: String) {
        val result = importWishlistUseCase(csvContent)
        _uiState.update { it.copy(importResult = result) }

        // Iniciar enriquecimento em background para os jogos adicionados
        if (result.added.isNotEmpty()) {
            enrichGamesInBackground(result.added)
        }
    }

    private fun enrichGamesInBackground(games: List<GameItem>) {
        val total = games.size
        _uiState.update { it.copy(enrichmentProgress = Pair(0, total)) }

        scope.launch {
            var completed = 0
            for (game in games) {
                try {
                    enrichWishlistGameUseCase(game.id)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Falha individual não bloqueia os restantes
                }
                completed++
                _uiState.update { it.copy(enrichmentProgress = Pair(completed, total)) }
            }
            _uiState.update { it.copy(enrichmentProgress = null) }
        }
    }

    fun moveToHunting(game: GameItem) {
        val updated = game.copy(
            collectionStatus = CollectionStatus.HUNTING,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        saveGameUseCase(updated)
    }

    fun retryEnrichment(game: GameItem) {
        scope.launch {
            try {
                enrichWishlistGameUseCase(game.id)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // Erro já tratado internamente pelo use case
            }
        }
    }

    // ─── Discovery & Similarity ───────────────────────────────────

    fun onDiscoveryGenreSelect(genre: GameGenre) {
        _uiState.update { it.copy(selectedDiscoveryGenre = genre) }
        fetchDiscoveryGames()
    }

    fun onDiscoveryQueryChange(query: String) {
        _uiState.update { it.copy(discoverySearchQuery = query) }
    }

    fun onDiscoverySearchSubmit(query: String) {
        _uiState.update { it.copy(discoverySearchQuery = query) }
        fetchDiscoveryGames(forceRefresh = true)
    }

    fun onDiscoveryPlatformSelect(platform: ConsolePlatform?) {
        _uiState.update {
            val newPlat = if (it.selectedDiscoveryPlatform == platform) null else platform
            it.copy(selectedDiscoveryPlatform = newPlat)
        }
        fetchDiscoveryGames()
    }

    fun onAddDiscoveredToWishlist(discovered: DiscoveredGameItem, targetPriceChf: Double? = null) {
        val gameItem = discovered.toGameItem(
            status = CollectionStatus.WISHLIST,
            targetPrice = targetPriceChf
        )
        saveGameUseCase(gameItem)
        _uiState.update { state ->
            val updatedList = state.discoveredGames.map {
                if (it.id == discovered.id) it.copy(isAlreadyInWishlist = true) else it
            }
            state.copy(discoveredGames = updatedList)
        }
    }

    fun onOpenDiscoveredDossier(discovered: DiscoveredGameItem) {
        val existing = _uiState.value.games.find {
            it.title.equals(discovered.title, ignoreCase = true) && it.platform == discovered.platform
        }
        val targetGame = existing ?: discovered.toGameItem(status = CollectionStatus.HUNTING).also {
            saveGameUseCase(it)
        }
        onGameSelected(targetGame)
        _uiState.update { it.copy(activeSection = AppSection.CATALOG) }
    }

    fun loadSimilarGamesForSelectedGame(game: GameItem) {
        _uiState.update { it.copy(isSimilarGamesLoading = true) }
        scope.launch {
            val result = getSimilarGamesUseCase(game)
            _uiState.update { state ->
                state.copy(
                    similarGamesForActiveGame = result.getOrDefault(emptyList()),
                    isSimilarGamesLoading = false
                )
            }
        }
    }

    private fun fetchDiscoveryGames(forceRefresh: Boolean = false) {
        val currentGenre = _uiState.value.selectedDiscoveryGenre
        val currentPlatform = _uiState.value.selectedDiscoveryPlatform
        val currentQuery = _uiState.value.discoverySearchQuery

        _uiState.update { it.copy(isDiscovering = true) }
        scope.launch {
            val result = discoverGamesUseCase(
                query = currentQuery,
                genre = currentGenre,
                platform = currentPlatform,
                forceRefresh = forceRefresh
            )
            _uiState.update { state ->
                state.copy(
                    discoveredGames = result.getOrDefault(emptyList()),
                    isDiscovering = false
                )
            }
        }
    }
}
