package com.retrocollector.app.dashboard.presentation.viewmodel

import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.dashboard.domain.model.DashboardFilterCriteria
import com.retrocollector.app.dashboard.domain.usecase.DeleteGameUseCase
import com.retrocollector.app.dashboard.domain.usecase.GetDashboardGamesUseCase
import com.retrocollector.app.dashboard.domain.usecase.SaveGameUseCase
import com.retrocollector.app.dossier.domain.usecase.SendFollowUpChatUseCase
import com.retrocollector.app.scanner.domain.usecase.AnalyzeGameWithGeminiUseCase
import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.settings.domain.usecase.TestGeminiConnectionUseCase
import com.retrocollector.app.settings.domain.usecase.UpdateSettingsUseCase
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    val settings: AppSettings = AppSettings()
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
    private val testGeminiConnectionUseCase: TestGeminiConnectionUseCase = TestGeminiConnectionUseCase(repository),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(dispatcher)
) {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
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

                    state.copy(
                        games = filtered,
                        selectedGame = newSelected,
                        activeChatMessages = chats
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

    fun testGeminiConnection(apiKey: String, model: String = "gemini-3.7-flash", onResult: (Result<String>) -> Unit) {
        scope.launch {
            val result = testGeminiConnectionUseCase(apiKey, model)
            onResult(result)
        }
    }

    fun sendFollowUpMessage(question: String, imageBase64: String? = null) {
        val currentGame = _uiState.value.selectedGame ?: return
        if (question.isBlank() && imageBase64 == null) return

        scope.launch {
            sendFollowUpChatUseCase(currentGame.id, question, imageBase64)
            val updatedChats = repository.getChatMessagesForGame(currentGame.id)
            _uiState.update { it.copy(activeChatMessages = updatedChats) }
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
}
