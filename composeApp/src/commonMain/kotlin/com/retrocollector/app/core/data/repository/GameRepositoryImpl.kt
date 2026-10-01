package com.retrocollector.app.core.data.repository

import com.retrocollector.app.core.data.datasource.CuratedDiscoveryDataSource
import com.retrocollector.app.core.data.datasource.GeminiRemoteDataSource
import com.retrocollector.app.core.data.datasource.StitchGeminiStructuredVerdict
import com.retrocollector.app.core.data.firestore.FirestoreService
import com.retrocollector.app.core.data.scraper.ListingScraper
import com.retrocollector.app.settings.data.datasource.SettingsLocalDataSource
import com.retrocollector.app.settings.data.datasource.createSettingsLocalDataSource
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

@Suppress("LargeClass")
class GameRepositoryImpl(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val geminiDataSource: GeminiRemoteDataSource = GeminiRemoteDataSource(),
    private val listingScraper: ListingScraper = ListingScraper(),
    private val settingsLocalDataSource: SettingsLocalDataSource = createSettingsLocalDataSource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : IGameRepository {

    private val scope = CoroutineScope(ioDispatcher)

    private val _settings = MutableStateFlow(settingsLocalDataSource.getSettings())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _games = MutableStateFlow<List<GameItem>>(getInitialMockGames())
    override val games: StateFlow<List<GameItem>> = _games.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(getInitialMockChats())
    override val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val discoveryCache = mutableMapOf<String, List<DiscoveredGameItem>>()
    private val similarGamesCache = mutableMapOf<String, List<DiscoveredGameItem>>()

    init {
        val projectId = _settings.value.firebaseProjectId.trim()
        if (projectId.isNotBlank()) {
            scope.launch {
                syncFromFirestore()
            }
        }
    }

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

        // Background synchronization with Firestore if project ID is configured
        val projectId = _settings.value.firebaseProjectId.trim()
        if (projectId.isNotBlank()) {
            scope.launch {
                firestoreService.saveGame(projectId, game)
            }
        }
    }

    override fun deleteGame(id: String) {
        _games.value = _games.value.filter { it.id != id }
        val projectId = _settings.value.firebaseProjectId.trim()
        if (projectId.isNotBlank()) {
            scope.launch {
                firestoreService.deleteGame(projectId, id)
            }
        }
    }

    override fun getChatMessagesForGame(gameId: String): List<ChatMessage> {
        return _chatMessages.value.filter { it.contextId == gameId }
    }

    override fun addChatMessage(message: ChatMessage) {
        _chatMessages.value = _chatMessages.value + message
    }

    override fun updateSettings(settings: AppSettings) {
        val previousProjectId = _settings.value.firebaseProjectId.trim()
        _settings.value = settings
        settingsLocalDataSource.saveSettings(settings)

        val newProjectId = settings.firebaseProjectId.trim()
        if (newProjectId.isNotBlank() && newProjectId != previousProjectId) {
            scope.launch {
                syncFromFirestore()
            }
        }
    }

    override suspend fun syncFromFirestore(): Result<Unit> {
        val projectId = _settings.value.firebaseProjectId.trim()
        if (projectId.isBlank()) return Result.success(Unit)

        val result = firestoreService.getGames(projectId)
        return result.map { remoteGames ->
            if (remoteGames.isNotEmpty()) {
                val localMap = _games.value.associateBy { it.id }.toMutableMap()
                remoteGames.forEach { remote ->
                    localMap[remote.id] = remote
                }
                _games.value = localMap.values.toList()
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

        val resolved = resolveListingInput(query, imageBase64, spottedLocation, askingPriceChf)
        val result = geminiDataSource.inspectGame(resolved.query, resolved.imageBase64, apiKey, model)

        return result.map { (replyText, verdict) ->
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val gameItem = verdict?.let { buildGameItemFromVerdict(it, resolved, nowMs) }

            val chatMsg = ChatMessage(
                id = "msg_$nowMs",
                contextId = gameItem?.id ?: "temp",
                sender = MessageSender.GEMINI,
                text = replyText,
                suggestedGameUpdate = gameItem
            )
            addChatMessage(chatMsg)

            Pair(chatMsg, gameItem)
        }
    }

    private fun findListingUrl(query: String, imageBase64: String?): String? {
        val isUrl = query.startsWith("http://") || query.startsWith("https://")
        if (isUrl || listingScraper.isSupportedListing(query)) return query
        if (imageBase64 != null && listingScraper.isSupportedListing(imageBase64)) return imageBase64
        return null
    }

    private suspend fun fetchListingMetadata(
        listingUrl: String,
        currentQuery: String,
        currentLocation: String,
        currentPrice: Double?,
        currentImage: String?
    ): ResolvedScanInput {
        var query = currentQuery
        var location = currentLocation
        var price = currentPrice
        var image = currentImage

        val sessionCookie = _settings.value.ricardoSessionCookie.ifBlank { null }
        listingScraper.fetchListing(listingUrl, sessionCookie).onSuccess { listing ->
            if (query == listingUrl || query.isBlank()) {
                query = "Listing ${listing.sourcePlatform}: ${listing.title}. Description: ${listing.description.take(250)}"
            }
            if (location.isBlank() || location == "Ricardo.ch") {
                location = listing.sourcePlatform
            }
            if (price == null) {
                price = listing.estimatedPriceChf
            }
            if (image == null && listing.imageUrls.isNotEmpty()) {
                image = listingScraper.fetchImageAsBase64(listing.imageUrls.first(), sessionCookie)
            }
        }
        return ResolvedScanInput(query, image, location, price)
    }

    private suspend fun resolveListingInput(
        query: String,
        imageBase64: String?,
        spottedLocation: String,
        askingPriceChf: Double?
    ): ResolvedScanInput {
        var result = ResolvedScanInput(query, imageBase64, spottedLocation, askingPriceChf)

        if (_settings.value.isScraperEnabled) {
            val listingUrl = findListingUrl(query, imageBase64)
            if (listingUrl != null) {
                result = fetchListingMetadata(
                    listingUrl = listingUrl,
                    currentQuery = query,
                    currentLocation = spottedLocation,
                    currentPrice = askingPriceChf,
                    currentImage = if (listingUrl == imageBase64) null else imageBase64
                )
            }
        }

        val resolvedImg = result.imageBase64
        if (resolvedImg != null && (resolvedImg.startsWith("http://") || resolvedImg.startsWith("https://"))) {
            val sessionCookie = _settings.value.ricardoSessionCookie.ifBlank { null }
            val fetched = listingScraper.fetchImageAsBase64(resolvedImg, sessionCookie)
            if (fetched != null) {
                result = result.copy(imageBase64 = fetched)
            }
        }

        return result
    }

    private fun buildGameItemFromVerdict(
        v: StitchGeminiStructuredVerdict,
        resolved: ResolvedScanInput,
        nowMs: Long
    ): GameItem {
        val platform = ConsolePlatform.fromPlatformString(v.platform) ?: ConsolePlatform.GAMECUBE
        val status = LanguageStatus.fromString(v.languageStatus)
        val radar = SwissMarketRadar(
            spottedPriceChf = resolved.price,
            medianPriceChf = v.swissMarketMedianChf,
            historicalMinChf = v.historicalMinChf,
            historicalMaxChf = v.historicalMaxChf,
            trend = "Stable"
        )
        return GameItem(
            id = "game_${v.title.filter { it.isLetterOrDigit() }.lowercase()}_${platform.id}",
            title = v.title.ifBlank { "Analyzed Game" },
            franchiseName = v.franchise,
            platform = platform,
            releaseYear = v.releaseYear,
            productCode = v.productCode,
            barcode = v.barcode,
            spottedLocation = resolved.location.ifBlank { "Field / Online" },
            askingPriceChf = resolved.price,
            targetPriceChf = v.swissMarketMedianChf,
            languageStatus = status,
            languageAudio = v.audioLanguages,
            languageSubtitles = v.subtitleLanguages,
            safeSkus = v.safeSkus,
            riskySkus = v.riskySkus,
            marketRadar = radar,
            censorshipWarning = v.censorshipWarning,
            collectorVerdict = v.collectorVerdict,
            collectionStatus = if (status == LanguageStatus.GERMAN_ONLY) CollectionStatus.PASS else CollectionStatus.WISHLIST,
            updatedAt = nowMs
        )
    }

    private data class ResolvedScanInput(
        val query: String,
        val imageBase64: String?,
        val location: String,
        val price: Double?
    )

    override suspend fun sendFollowUpChat(
        contextId: String,
        userMessage: String,
        imageBase64: String?
    ): Result<ChatMessage> {
        val apiKey = _settings.value.geminiApiKey
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val game = getGameById(contextId)

        val existingChats = getChatMessagesForGame(contextId)
        val lastIsSame = existingChats.lastOrNull()?.let {
            it.sender == MessageSender.USER && it.text == userMessage
        } ?: false

        if (!lastIsSame) {
            val userMsg = ChatMessage(
                id = "user_$nowMs",
                contextId = contextId,
                sender = MessageSender.USER,
                text = userMessage,
                imageBase64 = imageBase64
            )
            addChatMessage(userMsg)
        }

        val history = getChatMessagesForGame(contextId)
        val model = _settings.value.geminiModel.ifBlank { "gemini-3.7-flash" }

        var resolvedChatImage = imageBase64
        if (resolvedChatImage != null && (resolvedChatImage.startsWith("http://") || resolvedChatImage.startsWith("https://"))) {
            val sessionCookie = _settings.value.ricardoSessionCookie.ifBlank { null }
            val fetched = listingScraper.fetchImageAsBase64(resolvedChatImage, sessionCookie)
            if (fetched != null) {
                resolvedChatImage = fetched
            }
        }

        val result = geminiDataSource.sendFollowUpChat(history, game, userMessage, resolvedChatImage, apiKey, model)

        return result.fold(
            onSuccess = { (replyText, verdict) ->
                val aiMsg = ChatMessage(
                    id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                    contextId = contextId,
                    sender = MessageSender.GEMINI,
                    text = replyText
                )
                addChatMessage(aiMsg)

                if (verdict != null && game != null) {
                    val updatedSafe = (game.safeSkus + verdict.safeSkus).distinctBy { it.code }
                    val updatedRisky = (game.riskySkus + verdict.riskySkus).distinctBy { it.code }
                    upsertGame(game.copy(
                        productCode = verdict.productCode ?: game.productCode,
                        languageStatus = if (verdict.languageStatus != "UNVERIFIED") {
                            LanguageStatus.fromString(verdict.languageStatus)
                        } else game.languageStatus,
                        safeSkus = updatedSafe.ifEmpty { game.safeSkus },
                        riskySkus = updatedRisky.ifEmpty { game.riskySkus },
                        collectorVerdict = verdict.collectorVerdict.ifBlank { game.collectorVerdict }
                    ))
                }

                Result.success(aiMsg)
            },
            onFailure = { err ->
                val errorMsg = ChatMessage(
                    id = "ai_err_${Clock.System.now().toEpochMilliseconds()}",
                    contextId = contextId,
                    sender = MessageSender.GEMINI,
                    text = "⚠️ Could not retrieve answer from Gemini: ${err.message ?: "Connection error"}. Check your API key in Settings."
                )
                addChatMessage(errorMsg)
                Result.failure(err)
            }
        )
    }

    override fun getGamesByStatus(status: CollectionStatus): List<GameItem> {
        return _games.value.filter { it.collectionStatus == status }
    }

    override suspend fun discoverGames(
        query: String?,
        genre: GameGenre?,
        platform: ConsolePlatform?,
        forceRefresh: Boolean
    ): Result<List<DiscoveredGameItem>> {
        val cacheKey = "${query.orEmpty()}_${genre?.name.orEmpty()}_${platform?.name.orEmpty()}"
        if (!forceRefresh && discoveryCache.containsKey(cacheKey)) {
            val cached = discoveryCache[cacheKey].orEmpty()
            return Result.success(enrichWithUserCollection(cached))
        }

        val apiKey = _settings.value.geminiApiKey
        val model = _settings.value.geminiModel.ifBlank { "gemini-3.7-flash" }

        if (apiKey.isBlank()) {
            val fallback = CuratedDiscoveryDataSource.getFilteredCatalog(genre, platform, query)
            discoveryCache[cacheKey] = fallback
            return Result.success(enrichWithUserCollection(fallback))
        }

        val result = geminiDataSource.discoverGames(query, genre, platform, apiKey, model)
        return result.fold(
            onSuccess = { items ->
                val finalItems = if (items.isNotEmpty()) items else CuratedDiscoveryDataSource.getFilteredCatalog(genre, platform, query)
                discoveryCache[cacheKey] = finalItems
                Result.success(enrichWithUserCollection(finalItems))
            },
            onFailure = { _ ->
                val fallback = CuratedDiscoveryDataSource.getFilteredCatalog(genre, platform, query)
                discoveryCache[cacheKey] = fallback
                Result.success(enrichWithUserCollection(fallback))
            }
        )
    }

    override suspend fun getSimilarGames(
        game: GameItem,
        forceRefresh: Boolean
    ): Result<List<DiscoveredGameItem>> {
        val cacheKey = "similar_${game.id}"
        if (!forceRefresh && similarGamesCache.containsKey(cacheKey)) {
            val cached = similarGamesCache[cacheKey].orEmpty()
            return Result.success(enrichWithUserCollection(cached))
        }

        val apiKey = _settings.value.geminiApiKey
        val model = _settings.value.geminiModel.ifBlank { "gemini-3.7-flash" }

        if (apiKey.isBlank()) {
            val fallback = CuratedDiscoveryDataSource.getSimilarGames(game)
            similarGamesCache[cacheKey] = fallback
            return Result.success(enrichWithUserCollection(fallback))
        }

        val result = geminiDataSource.fetchSimilarGames(game.title, game.platform, game.franchiseName, apiKey, model)
        return result.fold(
            onSuccess = { items ->
                val finalItems = if (items.isNotEmpty()) items else CuratedDiscoveryDataSource.getSimilarGames(game)
                similarGamesCache[cacheKey] = finalItems
                Result.success(enrichWithUserCollection(finalItems))
            },
            onFailure = { _ ->
                val fallback = CuratedDiscoveryDataSource.getSimilarGames(game)
                similarGamesCache[cacheKey] = fallback
                Result.success(enrichWithUserCollection(fallback))
            }
        )
    }

    private fun enrichWithUserCollection(items: List<DiscoveredGameItem>): List<DiscoveredGameItem> {
        val currentGames = _games.value
        return items.map { item ->
            val matching = currentGames.find {
                it.title.equals(item.title, ignoreCase = true) && it.platform == item.platform
            }
            item.copy(
                isAlreadyInCollection = matching?.collectionStatus == CollectionStatus.OWNED,
                isAlreadyInWishlist = matching?.collectionStatus == CollectionStatus.WISHLIST
            )
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
                censorshipWarning = "This NOE pressing (DOL-P-G4BP) was heavily censored in Germany: " +
                    "the 'Assignment Ada' and 'The Mercenaries' modes were removed from the disc!",
                collectorVerdict = "Skip this copy at CHF 35.00. Wait and hunt specifically for DOL-P-G4BE (UK PAL) " +
                    "which features 100% uncut English content and all unlockable modes.",
                collectionStatus = CollectionStatus.WISHLIST
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
                collectorVerdict = "Excellent buy. The game selects English automatically if the console BIOS is set to English.",
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
                collectionStatus = CollectionStatus.WISHLIST
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
                collectionStatus = CollectionStatus.WISHLIST
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
                censorshipWarning = "Copy sold in Switzerland/Germany with forced German audio and no English option!",
                collectorVerdict = "Reject this copy for CHF 65. Look specifically for UK version DOL-P-GEDE.",
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
                    SkuInfo(code = "BLES-00561", region = "USK / DACH", editionNote = "German exclusive (No English files on disc)", isSafe = false)
                ),
                safeSkus = listOf(
                    SkuInfo(code = "BLES-00344", region = "UK", editionNote = "British Edition in full English", isSafe = true),
                    SkuInfo(code = "BLES-00778", region = "GOTY UK", editionNote = "Game of the Year Edition UK", isSafe = true)
                ),
                marketRadar = SwissMarketRadar(
                    spottedPriceChf = 15.0,
                    medianPriceChf = 12.0,
                    historicalMinChf = 10.0,
                    historicalMaxChf = 18.0,
                    trend = "Stable"
                ),
                censorshipWarning = "90% of copies on Ricardo.ch are BLES-00561 (USK) which DOES NOT have English!",
                collectorVerdict = "Do not buy version BLES-00561. Look for BLES-00344.",
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
                    SkuInfo(code = "BLES-00246", region = "EUR", editionNote = "Original voice David Hayter in English + multi-language subtitles", isSafe = true)
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
                collectionStatus = CollectionStatus.WISHLIST
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
                    
                    This NOE pressing (`DOL-P-G4BP`) was heavily censored to comply with German regulations at the time:
                    
                    • **Audio & Text**: English voice acting is on disc, but menus and subtitles are locked to German on standard PAL BIOS.
                    • **Removed Content**: Capcom completely removed mini-games **Assignment Ada** and **The Mercenaries** in this version! Decapitations were also toned down.
                    
                    **Collector Verdict**:
                    Skip this copy at **CHF 35.00**. Hunt instead for edition `DOL-P-G4BE` (UK PAL) which is 100% uncut and in full English.
                """.trimIndent()
            )
        )
    }
}
