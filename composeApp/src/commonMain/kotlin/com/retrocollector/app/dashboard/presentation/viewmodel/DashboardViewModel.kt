package com.retrocollector.app.dashboard.presentation.viewmodel

import androidx.compose.runtime.Immutable
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.dashboard.domain.model.DashboardFilterCriteria
import com.retrocollector.app.dashboard.domain.usecase.DeleteGameUseCase
import com.retrocollector.app.dashboard.domain.usecase.GetDashboardGamesUseCase
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.discovery.domain.usecase.GetSimilarGamesUseCase
import com.retrocollector.app.dossier.domain.usecase.SendFollowUpChatUseCase
import com.retrocollector.app.scanner.domain.usecase.AnalyzeGameWithGeminiUseCase
import com.retrocollector.app.settings.domain.model.AiProvider
import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.settings.domain.usecase.TestAiConnectionUseCase
import com.retrocollector.app.settings.domain.usecase.TestGeminiConnectionUseCase
import com.retrocollector.app.settings.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
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
    val settings: AppSettings = AppSettings(),
    val activeSection: AppSection = AppSection.ACTIVITY,
    val collectionGames: List<GameItem> = emptyList(),
    val similarGamesForActiveGame: List<DiscoveredGameItem> = emptyList(),
    val isSimilarGamesLoading: Boolean = false,
    val isMobileDetailOpen: Boolean = false,
    val scanErrorMessage: String? = null,
    val firestoreTestStatusMessage: String? = null,
    val isTestingFirestore: Boolean = false,
    val isSyncing: Boolean = false
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: IGameRepository,
    private val getGamesUseCase: GetDashboardGamesUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
    private val analyzeGameUseCase: AnalyzeGameWithGeminiUseCase,
    private val sendFollowUpChatUseCase: SendFollowUpChatUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val testAiConnectionUseCase: TestAiConnectionUseCase = TestAiConnectionUseCase(repository),
    private val getSimilarGamesUseCase: GetSimilarGamesUseCase = GetSimilarGamesUseCase(repository),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(dispatcher)
) {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects: Flow<DashboardEffect> = _effects.receiveAsFlow()

    init {
        // Observe settings from repository
        scope.launch {
            repository.settings.collect { set ->
                _uiState.update { it.copy(settings = set) }
            }
        }

        // Observe games with applied filters via GetDashboardGamesUseCase
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

                    val collection = filtered.filter {
                        it.collectionStatus == CollectionStatus.OWNED
                    }

                    state.copy(
                        games = filtered,
                        selectedGame = newSelected,
                        activeChatMessages = chats,
                        collectionGames = collection,
                        isMobileDetailOpen = if (newSelected == null) false else state.isMobileDetailOpen
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
        if (game.similarGames.isNotEmpty()) {
            loadSimilarGamesForSelectedGame(game, forceRefresh = false)
        } else if (_uiState.value.settings.isAutoSimilarGamesEnabled) {
            loadSimilarGamesForSelectedGame(game, forceRefresh = false)
        } else {
            val curated = repository.getCuratedSimilarGames(game)
            _uiState.update {
                it.copy(
                    similarGamesForActiveGame = curated,
                    isSimilarGamesLoading = false
                )
            }
        }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedPlatform = null,
                selectedStatus = null,
                filterEnglishOnly = false,
                filterUskAlertsOnly = false
            )
        }
    }

    fun openMobileDetail(game: GameItem) {
        onGameSelected(game)
        _uiState.update { it.copy(isMobileDetailOpen = true) }
    }

    fun closeMobileDetail() {
        _uiState.update { it.copy(isMobileDetailOpen = false) }
    }

    fun updateGameStatus(game: GameItem, newStatus: CollectionStatus) {
        val updatedPaid = if (newStatus == CollectionStatus.OWNED && game.paidPriceChf == null) {
            game.askingPriceChf
        } else {
            game.paidPriceChf
        }
        val updated = game.copy(
            collectionStatus = newStatus,
            paidPriceChf = updatedPaid
        )
        saveGameUseCase(updated)
    }

    fun updateGamePaidPrice(game: GameItem, paidPrice: Double?) {
        val updated = game.copy(paidPriceChf = paidPrice)
        saveGameUseCase(updated)
    }

    fun updateGameProductCode(game: GameItem, newProductCode: String?) {
        val cleanCode = newProductCode?.trim()?.ifBlank { null }
        val updated = game.copy(productCode = cleanCode)
        saveGameUseCase(updated)
        _uiState.update { state ->
            if (state.selectedGame?.id == game.id) {
                state.copy(selectedGame = updated)
            } else state
        }
        scope.launch {
            _effects.send(DashboardEffect.ShowToast("Edition SKU set to ${cleanCode ?: "none"}"))
        }
    }

    fun addOrUpdateOffer(game: GameItem, offer: com.retrocollector.app.core.domain.model.GameOffer) {
        repository.addOrUpdateOffer(game.id, offer)
        scope.launch {
            _effects.send(DashboardEffect.ShowToast("Offer from ${offer.source} saved."))
        }
    }

    fun deleteOffer(game: GameItem, offerId: String) {
        repository.deleteOffer(game.id, offerId)
        scope.launch {
            _effects.send(DashboardEffect.ShowToast("Offer deleted."))
        }
    }

    fun convertOfferToOwned(
        game: GameItem,
        offerId: String?,
        finalPrice: Double,
        condition: com.retrocollector.app.core.domain.model.GameCondition
    ) {
        repository.convertOfferToOwned(game.id, offerId, finalPrice, condition)
        scope.launch {
            _effects.send(DashboardEffect.ShowToast("Added \"${game.title}\" to Collection!"))
        }
    }

    fun deleteGame(gameId: String) {
        val wasSelected = _uiState.value.selectedGame?.id == gameId
        deleteGameUseCase(gameId)
        _uiState.update { state ->
            val shouldCloseDetail = (wasSelected || state.selectedGame?.id == gameId || state.selectedGame == null) && state.isMobileDetailOpen
            state.copy(
                isMobileDetailOpen = if (shouldCloseDetail) false else state.isMobileDetailOpen
            )
        }
        scope.launch {
            _effects.send(DashboardEffect.ShowToast("Game removed successfully."))
        }
    }

    fun setScanDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isScanDialogOpen = isOpen, scanErrorMessage = if (isOpen) null else it.scanErrorMessage) }
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen, firestoreTestStatusMessage = if (isOpen) null else it.firestoreTestStatusMessage) }
    }

    fun testFirestoreConnection(projectId: String, onResult: (Result<String>) -> Unit) {
        if (projectId.isBlank()) {
            val err = IllegalArgumentException(TextKeys.Settings.FIREBASE_STATUS_NO_PROJECT)
            _uiState.update { it.copy(firestoreTestStatusMessage = TextKeys.Settings.FIREBASE_STATUS_NO_PROJECT) }
            onResult(Result.failure(err))
            return
        }
        _uiState.update { it.copy(isTestingFirestore = true, firestoreTestStatusMessage = null) }
        scope.launch {
            try {
                val current = repository.settings.value
                if (current.firebaseProjectId != projectId) {
                    repository.updateSettings(current.copy(firebaseProjectId = projectId))
                }
                val syncResult = repository.syncFromFirestore()
                syncResult.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isTestingFirestore = false,
                                firestoreTestStatusMessage = TextKeys.Settings.FIREBASE_STATUS_CONNECTED
                            )
                        }
                        onResult(Result.success(TextKeys.Settings.FIREBASE_STATUS_CONNECTED))
                    },
                    onFailure = { err ->
                        val msg = err.message ?: TextKeys.Settings.FIREBASE_STATUS_FAILED
                        _uiState.update {
                            it.copy(
                                isTestingFirestore = false,
                                firestoreTestStatusMessage = msg
                            )
                        }
                        onResult(Result.failure(err))
                    }
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.message ?: TextKeys.Settings.FIREBASE_STATUS_FAILED
                _uiState.update {
                    it.copy(
                        isTestingFirestore = false,
                        firestoreTestStatusMessage = msg
                    )
                }
                onResult(Result.failure(e))
            }
        }
    }

    fun saveSettings(newSettings: AppSettings) {
        updateSettingsUseCase(newSettings)
        setSettingsOpen(false)
    }

    fun refreshFromFirestore() {
        val projectId = repository.settings.value.firebaseProjectId.trim()
        if (projectId.isBlank()) {
            scope.launch {
                _effects.send(DashboardEffect.ShowToast("Firebase Project ID not configured in Settings", isError = true))
            }
            return
        }
        _uiState.update { it.copy(isSyncing = true) }
        scope.launch {
            try {
                val result = repository.syncFromFirestore()
                _uiState.update { it.copy(isSyncing = false) }
                result.fold(
                    onSuccess = {
                        _effects.send(DashboardEffect.ShowToast("Cloud sync completed!"))
                    },
                    onFailure = { err ->
                        val msg = err.message ?: "Sync error"
                        _effects.send(DashboardEffect.ShowToast("Sync error: $msg", isError = true))
                    }
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _uiState.update { it.copy(isSyncing = false) }
                val msg = e.message ?: "Sync error"
                _effects.send(DashboardEffect.ShowToast("Sync error: $msg", isError = true))
            }
        }
    }

    fun testAiConnection(
        provider: AiProvider,
        apiKey: String,
        model: String,
        baseUrl: String? = null,
        onResult: (Result<String>) -> Unit
    ) {
        scope.launch {
            try {
                val result = testAiConnectionUseCase(provider, apiKey, model, baseUrl)
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
            _uiState.update { it.copy(isAnalyzing = true, scanErrorMessage = null) }
            val result = analyzeGameUseCase(query, imageBase64, spottedLocation, askingPriceChf)
            result.onSuccess { (_, gameItem) ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        isScanDialogOpen = false,
                        scanErrorMessage = null
                    )
                }
                if (gameItem != null) {
                    saveGameUseCase(gameItem)
                    onGameSelected(gameItem)
                    _effects.send(DashboardEffect.ScanCompleted(gameItem))
                    _effects.send(DashboardEffect.NavigateToGameDetail(gameItem))
                }
                _effects.send(DashboardEffect.ShowToast("Analysis completed successfully!"))
            }.onFailure { err ->
                val msg = err.message ?: "Analysis error"
                _uiState.update { it.copy(isAnalyzing = false, scanErrorMessage = msg) }
                _effects.send(DashboardEffect.ShowToast("Analysis error: $msg", isError = true))
            }
        }
    }

    fun onSectionSelect(section: AppSection) {
        _uiState.update { it.copy(activeSection = section) }
    }

    fun onOpenDiscoveredDossier(discovered: DiscoveredGameItem) {
        val existing = _uiState.value.games.find {
            it.title.equals(discovered.title, ignoreCase = true) && it.platform == discovered.platform
        }
        val targetGame = existing ?: discovered.toGameItem(status = CollectionStatus.PASS)
        onGameSelected(targetGame)
    }

    fun loadSimilarGamesForSelectedGame(game: GameItem, forceRefresh: Boolean = false) {
        _uiState.update { it.copy(isSimilarGamesLoading = true) }
        scope.launch {
            val result = getSimilarGamesUseCase(game, forceRefresh = forceRefresh)
            val items = result.getOrDefault(emptyList())
            _uiState.update { state ->
                state.copy(
                    similarGamesForActiveGame = items,
                    isSimilarGamesLoading = false
                )
            }
            if (items.isNotEmpty()) {
                val currentSavedGame = repository.getGameById(game.id) ?: game
                if (forceRefresh || currentSavedGame.similarGames.isEmpty()) {
                    val updatedGame = currentSavedGame.copy(similarGames = items)
                    saveGameUseCase(updatedGame)
                    _uiState.update { state ->
                        if (state.selectedGame?.id == updatedGame.id) {
                            state.copy(selectedGame = updatedGame)
                        } else state
                    }
                }
            }
        }
    }
}
