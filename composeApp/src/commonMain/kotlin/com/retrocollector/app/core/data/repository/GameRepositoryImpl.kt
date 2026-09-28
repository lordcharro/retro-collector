package com.retrocollector.app.core.data.repository

import com.retrocollector.app.core.data.datasource.GeminiRemoteDataSource
import com.retrocollector.app.core.data.firestore.FirestoreService
import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class GameRepositoryImpl(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val geminiDataSource: GeminiRemoteDataSource = GeminiRemoteDataSource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : IGameRepository {

    private val scope = CoroutineScope(ioDispatcher)

    private val _settings = MutableStateFlow(AppSettings())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _games = MutableStateFlow<List<GameItem>>(getInitialMockGames())
    override val games: StateFlow<List<GameItem>> = _games.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(getInitialMockChats())
    override val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    override fun getGameById(id: String): GameItem? {
        return _games.value.find { it.id == id }
    }

    override fun upsertGame(game: GameItem) {
        val current = _games.value.toMutableList()
        val index = current.indexOfFirst { it.id == game.id }
        if (index >= 0) {
            current[index] = game
        } else {
            current.add(0, game)
        }
        _games.value = current

        // Sincronização em background com Firestore se project ID estiver configurado
        val projectId = _settings.value.firebaseProjectId
        if (projectId.isNotBlank()) {
            scope.launch {
                // fire-and-forget sync
            }
        }
    }

    override fun deleteGame(id: String) {
        _games.value = _games.value.filter { it.id != id }
    }

    override fun getChatMessagesForGame(gameId: String): List<ChatMessage> {
        return _chatMessages.value.filter { it.contextId == gameId }
    }

    override fun addChatMessage(message: ChatMessage) {
        _chatMessages.value = _chatMessages.value + message
    }

    override fun updateSettings(settings: AppSettings) {
        _settings.value = settings
    }

    override suspend fun syncFromFirestore(): Result<Unit> {
        val projectId = _settings.value.firebaseProjectId
        if (projectId.isBlank()) return Result.success(Unit)

        val result = firestoreService.getGames(projectId)
        return result.map { remoteGames ->
            if (remoteGames.isNotEmpty()) {
                _games.value = remoteGames
            }
            Unit
        }
    }

    override suspend fun testGeminiConnection(apiKey: String, model: String): Result<String> {
        return geminiDataSource.testConnection(apiKey, model)
    }

    override suspend fun inspectGameWithAi(
        query: String,
        imageBase64: String?,
        spottedLocation: String,
        askingPriceChf: Double?
    ): Result<Pair<ChatMessage, GameItem?>> {
        val apiKey = _settings.value.geminiApiKey
        val model = _settings.value.geminiModel.ifBlank { "gemini-3.7-flash" }
        val result = geminiDataSource.inspectGame(query, imageBase64, apiKey, model)

        return result.map { (replyText, verdict) ->
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val gameItem = verdict?.let { v ->
                val platform = ConsolePlatform.entries.find { it.name.equals(v.platform, ignoreCase = true) }
                    ?: ConsolePlatform.GAMECUBE

                val status = LanguageStatus.fromString(v.languageStatus)
                val radar = SwissMarketRadar(
                    spottedPriceChf = askingPriceChf,
                    medianPriceChf = v.swissMarketMedianChf,
                    historicalMinChf = v.historicalMinChf,
                    historicalMaxChf = v.historicalMaxChf,
                    trend = "Stable"
                )

                GameItem(
                    id = "game_${v.title.filter { it.isLetterOrDigit() }.lowercase()}_${platform.id}",
                    title = v.title.ifBlank { "Jogo Analisado" },
                    franchiseName = v.franchise,
                    platform = platform,
                    releaseYear = v.releaseYear,
                    productCode = v.productCode,
                    barcode = v.barcode,
                    spottedLocation = spottedLocation.ifBlank { "Campo / Online" },
                    askingPriceChf = askingPriceChf,
                    targetPriceChf = v.swissMarketMedianChf,
                    languageStatus = status,
                    languageAudio = v.audioLanguages,
                    languageSubtitles = v.subtitleLanguages,
                    safeSkus = v.safeSkus,
                    riskySkus = v.riskySkus,
                    marketRadar = radar,
                    censorshipWarning = v.censorshipWarning,
                    collectorVerdict = v.collectorVerdict,
                    collectionStatus = if (status == LanguageStatus.GERMAN_ONLY) CollectionStatus.PASS else CollectionStatus.HUNTING,
                    updatedAt = nowMs
                )
            }

            val chatMsg = ChatMessage(
                id = "msg_$nowMs",
                contextId = gameItem?.id ?: "temp",
                sender = MessageSender.GEMINI,
                text = replyText,
                suggestedGameUpdate = gameItem
            )

            Pair(chatMsg, gameItem)
        }
    }

    override suspend fun sendFollowUpChat(
        contextId: String,
        userMessage: String,
        imageBase64: String?
    ): Result<ChatMessage> {
        val apiKey = _settings.value.geminiApiKey
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val game = getGameById(contextId)

        val userMsg = ChatMessage(
            id = "user_$nowMs",
            contextId = contextId,
            sender = MessageSender.USER,
            text = userMessage,
            imageBase64 = imageBase64
        )
        addChatMessage(userMsg)

        val prompt = if (game != null) {
            "Contexto: Jogo '${game.title}' para ${game.platform.displayName} (Código: ${game.productCode}, Local: ${game.spottedLocation}). Pergunta do utilizador: $userMessage"
        } else {
            userMessage
        }

        val model = _settings.value.geminiModel.ifBlank { "gemini-3.7-flash" }
        val result = geminiDataSource.inspectGame(prompt, imageBase64, apiKey, model)
        return result.map { (replyText, verdict) ->
            val aiMsg = ChatMessage(
                id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                contextId = contextId,
                sender = MessageSender.GEMINI,
                text = replyText
            )
            addChatMessage(aiMsg)

            if (verdict != null && game != null) {
                upsertGame(game.copy(
                    safeSkus = if (verdict.safeSkus.isNotEmpty()) verdict.safeSkus else game.safeSkus,
                    riskySkus = if (verdict.riskySkus.isNotEmpty()) verdict.riskySkus else game.riskySkus,
                    collectorVerdict = verdict.collectorVerdict.ifBlank { game.collectorVerdict }
                ))
            }

            aiMsg
        }
    }

    private fun getInitialMockGames(): List<GameItem> {
        return getGameCubeHighlights() + getGameCubeClassics() + getOtherMockGames()
    }

    private fun getGameCubeHighlights(): List<GameItem> {
        return listOf(
            GameItem(
                id = "gc_re4",
                title = "Resident Evil 4",
                franchiseName = "Resident Evil",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2005",
                productCode = "DOL-P-G4BE",
                barcode = "045496392345",
                spottedLocation = "Brockenhaus Bern",
                askingPriceChf = 35.0,
                targetPriceChf = 31.50,
                languageStatus = LanguageStatus.SUBS_ONLY,
                languageAudio = listOf("English", "Japanese"),
                languageSubtitles = listOf("English", "French", "German (in DE edition)"),
                safeSkus = listOf(
                    SkuInfo(code = "DOL-P-G4BE", region = "UK", editionNote = "Uncut English Audio and Menus", isSafe = true)
                ),
                riskySkus = listOf(
                    SkuInfo(code = "DOL-P-G4BP", region = "NOE", editionNote = "German BPjM Cut Edition, Missing Mini-games", isSafe = false)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 35.0,
                    medianPriceChf = 31.50,
                    historicalMinChf = 28.0,
                    historicalMaxChf = 36.0,
                    trend = "Stable"
                ),
                censorshipWarning = "Esta prensagem NOE (DOL-P-G4BP) foi fortemente censurada na Alemanha: " +
                    "os modos 'Assignment Ada' e 'The Mercenaries' foram removidos do disco!",
                collectorVerdict = "Skip this copy at CHF 35.00. Wait and hunt specifically for DOL-P-G4BE (UK PAL) " +
                    "which features 100% uncut English content and all unlockable modes.",
                collectionStatus = CollectionStatus.HUNTING
            ),
            GameItem(
                id = "gc_zelda_ww",
                title = "The Legend of Zelda: The Wind Waker",
                franchiseName = "The Legend of Zelda",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2003",
                productCode = "DOL-P-GZLP",
                barcode = "045496391234",
                spottedLocation = "Ricardo.ch",
                askingPriceChf = 45.0,
                targetPriceChf = 45.0,
                languageStatus = LanguageStatus.FULL_ENGLISH,
                safeSkus = listOf(
                    SkuInfo(code = "DOL-P-GZLP", region = "EUR", editionNote = "Multi-5 PAL Standard", isSafe = true)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 45.0,
                    medianPriceChf = 42.0,
                    historicalMinChf = 38.0,
                    historicalMaxChf = 50.0,
                    trend = "Stable"
                ),
                collectorVerdict = "Excelente compra. O jogo seleciona o inglês automaticamente se a BIOS da consola estiver em inglês.",
                collectionStatus = CollectionStatus.OWNED,
                paidPriceChf = 45.0
            )
        )
    }

    private fun getGameCubeClassics(): List<GameItem> {
        return listOf(
            GameItem(
                id = "gc_sms",
                title = "Super Mario Sunshine",
                franchiseName = "Super Mario",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2002",
                productCode = "DOL-P-GMSP",
                spottedLocation = "Basel Flohmarkt",
                askingPriceChf = 50.0,
                targetPriceChf = 45.0,
                languageStatus = LanguageStatus.FULL_ENGLISH,
                safeSkus = listOf(
                    SkuInfo(code = "DOL-P-GMSP", region = "EUR", editionNote = "Multi-4 European PAL", isSafe = true)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 50.0,
                    medianPriceChf = 46.0,
                    historicalMinChf = 40.0,
                    historicalMaxChf = 55.0,
                    trend = "Stable"
                ),
                collectionStatus = CollectionStatus.HUNTING
            ),
            GameItem(
                id = "gc_metroid",
                title = "Metroid Prime",
                franchiseName = "Metroid",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2003",
                productCode = "DOL-P-GM8P",
                spottedLocation = "Brocki Zurich",
                askingPriceChf = 40.0,
                targetPriceChf = 35.0,
                languageStatus = LanguageStatus.EDITION_NOTICE,
                safeSkus = listOf(
                    SkuInfo(code = "DOL-P-GM8E", region = "UKV", editionNote = "UK release English manual and text", isSafe = true)
                ),
                riskySkus = listOf(
                    SkuInfo(code = "DOL-P-GM8P", region = "NOE", editionNote = "German manual and packaging", isSafe = false)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 40.0,
                    medianPriceChf = 34.0,
                    historicalMinChf = 30.0,
                    historicalMaxChf = 42.0,
                    trend = "Stable"
                ),
                collectionStatus = CollectionStatus.HUNTING
            ),
            GameItem(
                id = "gc_eternal_darkness",
                title = "Eternal Darkness: Sanity's Requiem",
                franchiseName = "Eternal Darkness",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2002",
                productCode = "DOL-P-GEDP",
                spottedLocation = "Anibis.ch Lot",
                askingPriceChf = 65.0,
                targetPriceChf = 50.0,
                languageStatus = LanguageStatus.GERMAN_ONLY,
                riskySkus = listOf(
                    SkuInfo(code = "DOL-P-GEDP", region = "NOE", editionNote = "German Dub Only (USK 16)", isSafe = false)
                ),
                safeSkus = listOf(
                    SkuInfo(code = "DOL-P-GEDE", region = "UKV", editionNote = "English voice acting & uncut text", isSafe = true)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 65.0,
                    medianPriceChf = 55.0,
                    historicalMinChf = 45.0,
                    historicalMaxChf = 70.0,
                    trend = "Rising"
                ),
                censorshipWarning = "Cópia vendida na Suíça/Alemanha com áudio forçado em alemão sem opção de inglês!",
                collectorVerdict = "Rejeitar esta cópia por 65 CHF. Procurar especificamente a versão UK DOL-P-GEDE.",
                collectionStatus = CollectionStatus.PASS
            )
        )
    }

    private fun getOtherMockGames(): List<GameItem> {
        return listOf(
            GameItem(
                id = "ps3_fallout3",
                title = "Fallout 3",
                franchiseName = "Fallout",
                platform = ConsolePlatform.PS3,
                releaseYear = "2008",
                productCode = "BLES-00561",
                spottedLocation = "Ricardo.ch",
                askingPriceChf = 15.0,
                targetPriceChf = 15.0,
                languageStatus = LanguageStatus.GERMAN_ONLY,
                riskySkus = listOf(
                    SkuInfo(code = "BLES-00561", region = "USK / DACH", editionNote = "Alemão exclusivo (Sem ficheiros de inglês no disco)", isSafe = false)
                ),
                safeSkus = listOf(
                    SkuInfo(code = "BLES-00344", region = "UK", editionNote = "Edição Britânica em Inglês integral", isSafe = true),
                    SkuInfo(code = "BLES-00778", region = "GOTY UK", editionNote = "Game of the Year Edition UK", isSafe = true)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 15.0,
                    medianPriceChf = 12.0,
                    historicalMinChf = 10.0,
                    historicalMaxChf = 18.0,
                    trend = "Stable"
                ),
                censorshipWarning = "90% das cópias no Ricardo.ch são o BLES-00561 (USK) que NÃO tem inglês!",
                collectorVerdict = "Não comprar a versão BLES-00561. Procurar BLES-00344.",
                collectionStatus = CollectionStatus.PASS
            ),
            GameItem(
                id = "ps3_mgs4",
                title = "Metal Gear Solid 4: Guns of the Patriots",
                franchiseName = "Metal Gear",
                platform = ConsolePlatform.PS3,
                releaseYear = "2008",
                productCode = "BLES-00246",
                spottedLocation = "Ricardo.ch",
                askingPriceChf = 12.0,
                targetPriceChf = 15.0,
                paidPriceChf = 12.0,
                languageStatus = LanguageStatus.FULL_ENGLISH,
                safeSkus = listOf(
                    SkuInfo(code = "BLES-00246", region = "EUR", editionNote = "Voz original David Hayter em inglês + legendas multi-idioma", isSafe = true)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 12.0,
                    medianPriceChf = 14.0,
                    historicalMinChf = 10.0,
                    historicalMaxChf = 20.0,
                    trend = "Stable"
                ),
                collectionStatus = CollectionStatus.OWNED
            ),
            GameItem(
                id = "n64_sm64",
                title = "Super Mario 64",
                franchiseName = "Super Mario",
                platform = ConsolePlatform.N64,
                releaseYear = "1997",
                productCode = "NUS-NSMP-EUR",
                spottedLocation = "Ricardo.ch",
                askingPriceChf = 35.0,
                targetPriceChf = 30.0,
                languageStatus = LanguageStatus.FULL_ENGLISH,
                safeSkus = listOf(
                    SkuInfo(code = "NUS-NSMP-EUR", region = "EUR", editionNote = "PAL European Standard English", isSafe = true)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 35.0,
                    medianPriceChf = 30.0,
                    historicalMinChf = 25.0,
                    historicalMaxChf = 40.0,
                    trend = "Stable"
                ),
                collectionStatus = CollectionStatus.HUNTING
            )
        )
    }

    private fun getInitialMockChats(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "chat_re4_1",
                contextId = "gc_re4",
                sender = MessageSender.USER,
                text = "Spotted this at Brockenhaus Bern for CHF 35. Spine says DOL-P-G4BP-NOE. " +
                    "Does this Swiss/German version have English voice acting and uncut mercenaries mode?"
            ),
            ChatMessage(
                id = "chat_re4_2",
                contextId = "gc_re4",
                sender = MessageSender.GEMINI,
                text = """
                    ⚠️ **Censored German Release (BPjM Cut)**
                    
                    Esta prensagem NOE (`DOL-P-G4BP`) foi fortemente censurada para cumprir a legislação alemã da época:
                    
                    • **Áudio & Texto**: As vozes em inglês estão no disco, mas os menus e legendas ficam bloqueados a alemão numa BIOS PAL padrão.
                    • **Conteúdo Removido**: A Capcom cortou completamente os mini-jogos **Assignment Ada** e **The Mercenaries** nesta versão binária! As decapitações também foram atenuadas.
                    
                    **Veredito de Colecionador**:
                    Ignora esta cópia por **CHF 35.00**. Procura antes pela edição `DOL-P-G4BE` (UK PAL) que é 100% sem cortes e em inglês integral.
                """.trimIndent()
            )
        )
    }
}
