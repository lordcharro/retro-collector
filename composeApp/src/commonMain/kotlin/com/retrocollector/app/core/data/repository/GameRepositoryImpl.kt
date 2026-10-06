package com.retrocollector.app.core.data.repository

import com.retrocollector.app.core.data.datasource.AiDataSourceFactory
import com.retrocollector.app.core.data.datasource.CuratedDiscoveryDataSource
import com.retrocollector.app.core.data.datasource.StitchGeminiStructuredVerdict
import com.retrocollector.app.core.data.firestore.FirestoreService
import com.retrocollector.app.core.data.scraper.ListingScraper
import com.retrocollector.app.settings.data.datasource.SettingsLocalDataSource
import com.retrocollector.app.settings.data.datasource.createSettingsLocalDataSource
import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.settings.domain.model.AiProvider
import com.retrocollector.app.core.domain.model.*
import com.retrocollector.app.core.domain.repository.IGameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

@Suppress("LargeClass", "TooManyFunctions")
class GameRepositoryImpl(
    private val firestoreService: FirestoreService = FirestoreService(),
    private val aiDataSourceFactory: AiDataSourceFactory = AiDataSourceFactory(),
    private val listingScraper: ListingScraper = ListingScraper(),
    private val settingsLocalDataSource: SettingsLocalDataSource = createSettingsLocalDataSource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    loadMockData: Boolean = true
) : IGameRepository {

    private val scope = CoroutineScope(ioDispatcher)

    private val _settings = MutableStateFlow(settingsLocalDataSource.getSettings())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _games = MutableStateFlow<List<GameItem>>(if (loadMockData) getInitialMockGames() else emptyList())
    override val games: StateFlow<List<GameItem>> = _games.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(if (loadMockData) getInitialMockChats() else emptyList())
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

    override fun addOrUpdateOffer(gameId: String, offer: GameOffer) {
        val game = getGameById(gameId) ?: return
        val currentOffers = game.offers.toMutableList()
        val index = currentOffers.indexOfFirst { it.id == offer.id }
        if (index >= 0) {
            currentOffers[index] = offer
        } else {
            currentOffers.add(0, offer)
        }
        val nowMs = Clock.System.now().toEpochMilliseconds()
        upsertGame(game.copy(offers = currentOffers, updatedAt = nowMs))
    }

    override fun deleteOffer(gameId: String, offerId: String) {
        val game = getGameById(gameId) ?: return
        val updatedOffers = game.offers.filter { it.id != offerId }
        val nowMs = Clock.System.now().toEpochMilliseconds()
        upsertGame(game.copy(offers = updatedOffers, updatedAt = nowMs))
    }

    override fun convertOfferToOwned(
        gameId: String,
        offerId: String?,
        finalPriceChf: Double,
        condition: GameCondition
    ) {
        val game = getGameById(gameId) ?: return
        val updatedOffers = game.offers.map { off ->
            if (off.id == offerId) {
                off.copy(isPurchased = true, isArchived = false)
            } else {
                off.copy(isArchived = true)
            }
        }
        val purchasedOffer = game.offers.find { it.id == offerId }
        val newLocation = purchasedOffer?.source?.ifBlank { null } ?: game.spottedLocation
        val nowMs = Clock.System.now().toEpochMilliseconds()
        upsertGame(
            game.copy(
                collectionStatus = CollectionStatus.OWNED,
                paidPriceChf = finalPriceChf,
                acquiredCondition = condition,
                spottedLocation = newLocation,
                offers = updatedOffers,
                updatedAt = nowMs
            )
        )
    }

    override fun getChatMessagesForGame(gameId: String): List<ChatMessage> {
        return _chatMessages.value.filter { it.contextId == gameId }
    }

    override fun addChatMessage(message: ChatMessage) {
        val current = _chatMessages.value.toMutableList()
        val index = current.indexOfFirst { it.id == message.id }
        if (index >= 0) {
            current[index] = message
        } else {
            current.add(message)
        }
        _chatMessages.value = current

        // Background synchronization with Firestore if project ID is configured
        val projectId = _settings.value.firebaseProjectId.trim()
        if (projectId.isNotBlank()) {
            scope.launch {
                firestoreService.saveChatMessage(projectId, message)
            }
        }
    }

    override fun deleteChatMessage(messageId: String) {
        val current = _chatMessages.value.toMutableList()
        current.removeAll { it.id == messageId }
        _chatMessages.value = current

        // Background synchronization with Firestore if project ID is configured
        val projectId = _settings.value.firebaseProjectId.trim()
        if (projectId.isNotBlank()) {
            scope.launch {
                firestoreService.deleteChatMessage(projectId, messageId)
            }
        }
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

        val gamesResult = firestoreService.getGames(projectId)
        gamesResult.onSuccess { remoteGames ->
            if (remoteGames.isNotEmpty()) {
                val localMap = _games.value.associateBy { it.id }.toMutableMap()
                remoteGames.forEach { remote ->
                    localMap[remote.id] = remote
                }
                _games.value = localMap.values.toList()
            }
        }

        val chatsResult = firestoreService.getChatMessages(projectId)
        chatsResult.onSuccess { remoteChats ->
            if (remoteChats.isNotEmpty()) {
                val localMap = _chatMessages.value.associateBy { it.id }.toMutableMap()
                remoteChats.forEach { remote ->
                    localMap[remote.id] = remote
                }
                _chatMessages.value = localMap.values.sortedBy { it.timestamp }
            }
        }

        return if (gamesResult.isSuccess || chatsResult.isSuccess) {
            Result.success(Unit)
        } else {
            gamesResult.map { }
        }
    }

    override suspend fun testAiConnection(
        provider: AiProvider,
        apiKey: String,
        model: String,
        baseUrl: String?
    ): Result<String> {
        return aiDataSourceFactory.getDataSource(provider).testConnection(apiKey, model, baseUrl)
    }

    override suspend fun testGeminiConnection(apiKey: String, model: String): Result<String> {
        return testAiConnection(AiProvider.GEMINI, apiKey, model, null)
    }

    override suspend fun inspectGameWithAi(
        query: String,
        imageBase64: String?,
        spottedLocation: String,
        askingPriceChf: Double?
    ): Result<Pair<ChatMessage, GameItem?>> {
        val config = aiDataSourceFactory.resolveConfig(_settings.value)
        val dataSource = aiDataSourceFactory.getDataSource(config.provider)

        val resolved = resolveListingInput(query, imageBase64, spottedLocation, askingPriceChf)
        val result = dataSource.inspectGame(
            query = resolved.query,
            imageBase64 = resolved.imageBase64,
            apiKey = config.apiKey,
            model = config.model,
            baseUrl = config.baseUrl,
            imagesBase64 = resolved.imagesBase64
        )

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
        val extraImages = mutableListOf<String>()
        var scrapedCoverUrl: String? = null
        var scrapedSpineUrl: String? = null
        var scrapedDesc: String? = null

        val currentSettings = _settings.value
        listingScraper.fetchListing(listingUrl, currentSettings).onSuccess { listing ->
            scrapedCoverUrl = listing.imageUrls.firstOrNull()
            scrapedSpineUrl = if (listing.imageUrls.size > 1) listing.imageUrls[1] else null
            scrapedDesc = listing.description.takeIf { it.isNotBlank() }

            if (query == listingUrl || query.isBlank()) {
                val listingId = listing.listingId ?: listingScraper.extractListingId(listingUrl)
                if (listing.isTitleExtracted && !listing.title.startsWith("${listing.sourcePlatform} Listing")) {
                    query = "Listing ${listing.sourcePlatform}: ${listing.title}. Description: ${listing.description.take(250)}"
                } else if (listingId != null) {
                    query = "Swiss ${listing.sourcePlatform} marketplace listing article ID $listingId ($listingUrl). Search online for this Swiss listing to identify the exact retro game title, console platform, edition, and Swiss market valuation."
                } else {
                    query = "Swiss ${listing.sourcePlatform} marketplace link: $listingUrl. Search online to identify the physical retro game title, platform, and details."
                }
            }
            if (location.isBlank() || location == "Ricardo.ch") {
                location = listing.sourcePlatform
            }
            if (price == null) {
                price = listing.estimatedPriceChf
            }
            if (image == null && listing.imageUrls.isNotEmpty()) {
                listing.imageUrls.take(4).forEach { imgUrl ->
                    val base64 = listingScraper.fetchImageAsBase64(imgUrl)
                    if (base64 != null) {
                        extraImages.add(base64)
                    }
                }
                if (extraImages.isNotEmpty()) {
                    image = extraImages.first()
                }
            }
        }
        return ResolvedScanInput(
            query = query,
            imageBase64 = image,
            location = location,
            price = price,
            listingUrl = listingUrl,
            imagesBase64 = extraImages,
            scrapedCoverImageUrl = scrapedCoverUrl,
            scrapedSpineImageUrl = scrapedSpineUrl,
            scrapedDescription = scrapedDesc,
            rawImageInput = currentImage
        )
    }

    private suspend fun resolveListingInput(
        query: String,
        imageBase64: String?,
        spottedLocation: String,
        askingPriceChf: Double?
    ): ResolvedScanInput {
        val foundUrl = findListingUrl(query, imageBase64)
        var result = ResolvedScanInput(
            query = query,
            imageBase64 = imageBase64,
            location = spottedLocation,
            price = askingPriceChf,
            listingUrl = foundUrl,
            rawImageInput = imageBase64
        )

        if (_settings.value.isScraperEnabled) {
            val listingUrl = foundUrl
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
            val fetched = listingScraper.fetchImageAsBase64(resolvedImg)
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
        val initialOffers = if (resolved.price != null || !resolved.listingUrl.isNullOrBlank() || resolved.location.isNotBlank()) {
            listOf(
                GameOffer(
                    id = "offer_${nowMs}",
                    source = resolved.location.ifBlank { "Listing" },
                    priceChf = resolved.price ?: 0.0,
                    listingUrl = resolved.listingUrl.orEmpty(),
                    condition = GameCondition.CIB,
                    createdAt = nowMs
                )
            )
        } else emptyList()

        val coverImg = resolved.scrapedCoverImageUrl
            ?: resolved.rawImageInput?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
            ?: v.canonicalCoverImageUrl

        val spineImg = resolved.scrapedSpineImageUrl

        val finalDescription = v.description.ifBlank {
            resolved.scrapedDescription.orEmpty()
        }

        return GameItem(
            id = "game_${v.title.filter { it.isLetterOrDigit() }.lowercase()}_${platform.id}",
            title = v.title.ifBlank { "Analyzed Game" },
            franchiseName = v.franchise,
            platform = platform,
            releaseYear = v.releaseYear,
            description = finalDescription,
            coverImageUrl = coverImg,
            spineImageUrl = spineImg,
            productCode = v.productCode,
            barcode = v.barcode,
            spottedLocation = resolved.location.ifBlank { "Field / Online" },
            askingPriceChf = resolved.price,
            targetPriceChf = v.swissMarketMedianChf,
            listingUrl = resolved.listingUrl,
            offers = initialOffers,
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
        val price: Double?,
        val listingUrl: String? = null,
        val imagesBase64: List<String> = emptyList(),
        val scrapedCoverImageUrl: String? = null,
        val scrapedSpineImageUrl: String? = null,
        val scrapedDescription: String? = null,
        val rawImageInput: String? = null
    )

    override suspend fun sendFollowUpChat(
        contextId: String,
        userMessage: String,
        imageBase64: String?
    ): Result<ChatMessage> {
        val config = aiDataSourceFactory.resolveConfig(_settings.value)
        val dataSource = aiDataSourceFactory.getDataSource(config.provider)
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

        var resolvedChatImage = imageBase64
        if (resolvedChatImage != null && (resolvedChatImage.startsWith("http://") || resolvedChatImage.startsWith("https://"))) {
            val fetched = listingScraper.fetchImageAsBase64(resolvedChatImage)
            if (fetched != null) {
                resolvedChatImage = fetched
            }
        }

        val result = dataSource.sendFollowUpChat(history, game, userMessage, resolvedChatImage, config.apiKey, config.model, config.baseUrl)

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
                    val verdictSafeCodes = verdict.safeSkus.map { it.code.lowercase() }.toSet()
                    val verdictRiskyCodes = verdict.riskySkus.map { it.code.lowercase() }.toSet()

                    val cleanExistingSafe = game.safeSkus.filter { it.code.lowercase() !in verdictRiskyCodes }
                    val cleanExistingRisky = game.riskySkus.filter { it.code.lowercase() !in verdictSafeCodes }

                    val updatedSafe = (cleanExistingSafe + verdict.safeSkus).distinctBy { it.code.lowercase() }
                    val updatedRisky = (cleanExistingRisky + verdict.riskySkus).distinctBy { it.code.lowercase() }

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
                    text = "⚠️ Could not retrieve answer from AI assistant (${config.provider.displayName}): ${err.message ?: "Connection error"}. Check your configuration in Settings."
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

        val config = aiDataSourceFactory.resolveConfig(_settings.value)
        val dataSource = aiDataSourceFactory.getDataSource(config.provider)

        if (config.apiKey.isBlank() && config.provider != AiProvider.LOCAL_OLLAMA) {
            val fallback = CuratedDiscoveryDataSource.getFilteredCatalog(genre, platform, query)
            discoveryCache[cacheKey] = fallback
            return Result.success(enrichWithUserCollection(fallback))
        }

        val result = dataSource.discoverGames(query, genre, platform, config.apiKey, config.model, config.baseUrl)
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
        if (!forceRefresh) {
            if (game.similarGames.isNotEmpty()) {
                similarGamesCache[cacheKey] = game.similarGames
                return Result.success(enrichWithUserCollection(game.similarGames))
            }
            if (similarGamesCache.containsKey(cacheKey)) {
                val cached = similarGamesCache[cacheKey].orEmpty()
                return Result.success(enrichWithUserCollection(cached))
            }
        }

        val config = aiDataSourceFactory.resolveConfig(_settings.value)
        val dataSource = aiDataSourceFactory.getDataSource(config.provider)

        if (config.apiKey.isBlank() && config.provider != AiProvider.LOCAL_OLLAMA) {
            val fallback = if (game.similarGames.isNotEmpty()) game.similarGames else CuratedDiscoveryDataSource.getSimilarGames(game)
            similarGamesCache[cacheKey] = fallback
            return Result.success(enrichWithUserCollection(fallback))
        }

        val result = dataSource.fetchSimilarGames(game.title, game.platform, game.franchiseName, config.apiKey, config.model, config.baseUrl)
        return result.fold(
            onSuccess = { items ->
                val finalItems = if (items.isNotEmpty()) items else (if (game.similarGames.isNotEmpty()) game.similarGames else CuratedDiscoveryDataSource.getSimilarGames(game))
                similarGamesCache[cacheKey] = finalItems
                Result.success(enrichWithUserCollection(finalItems))
            },
            onFailure = { _ ->
                val fallback = if (game.similarGames.isNotEmpty()) game.similarGames else CuratedDiscoveryDataSource.getSimilarGames(game)
                similarGamesCache[cacheKey] = fallback
                Result.success(enrichWithUserCollection(fallback))
            }
        )
    }

    override fun getCuratedGames(
        genre: GameGenre?,
        platform: ConsolePlatform?,
        query: String?
    ): List<DiscoveredGameItem> {
        val list = CuratedDiscoveryDataSource.getFilteredCatalog(genre, platform, query)
        return enrichWithUserCollection(list)
    }

    override fun getCuratedSimilarGames(game: GameItem): List<DiscoveredGameItem> {
        val list = CuratedDiscoveryDataSource.getSimilarGames(game)
        return enrichWithUserCollection(list)
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
                description = "Special Agent Leon S. Kennedy is dispatched to a secluded European village to rescue the U.S. President's kidnapped daughter, confronting parasitic horrors in a genre-defining survival action landmark.",
                coverImageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x7h.jpg",
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
                collectionStatus = CollectionStatus.WISHLIST,
                offers = listOf(
                    GameOffer(
                        id = "offer_re4_anibis",
                        source = "Anibis.ch",
                        priceChf = 28.00,
                        shippingChf = null,
                        listingUrl = "https://www.anibis.ch",
                        condition = GameCondition.CIB,
                        sellerOrLocation = "Basel Gundeli",
                        notes = "Top pick! Includes uncut English manual and bonus disc.",
                        createdAt = 1790870000000L
                    ),
                    GameOffer(
                        id = "offer_re4_ricardo",
                        source = "Ricardo.ch",
                        priceChf = 35.00,
                        shippingChf = 1.50,
                        listingUrl = "https://www.ricardo.ch",
                        condition = GameCondition.BOXED,
                        sellerOrLocation = "Zurich",
                        notes = "B-Post letter (+CHF 1.50). Disc has minor hair scratches.",
                        createdAt = 1790871000000L
                    ),
                    GameOffer(
                        id = "offer_re4_tutti",
                        source = "Tutti.ch",
                        priceChf = 32.00,
                        shippingChf = 2.00,
                        listingUrl = "https://www.tutti.ch",
                        condition = GameCondition.CIB,
                        sellerOrLocation = "Bern Breitenrain",
                        notes = "Private seller. TWINT accepted.",
                        createdAt = 1790872000000L
                    )
                )
            ),
            GameItem(
                id = "gc_zelda_ww",
                title = "The Legend of Zelda: The Wind Waker",
                franchiseName = "The Legend of Zelda",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2003",
                description = "Set on a vast ocean archipelago, Link embarks on a seafaring quest with the King of Red Lions to rescue his sister Aryll and restore the lost kingdom of Hyrule.",
                coverImageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co20q3.jpg",
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
                description = "Mario journeys to tropical Isle Delfino for vacation, only to be framed for polluting the paradise and tasked with cleaning the island using Professor E. Gadd's FLUDD water cannon.",
                coverImageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1tca.jpg",
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
                description = "Intergalactic bounty hunter Samus Aran investigates Space Pirate biological experiments with Phazon on the desolate, subterranean world of Tallon IV.",
                coverImageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1w6k.jpg",
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
                description = "A groundbreaking psychological horror journey following Alexandra Roivas across twelve centuries as she investigates her grandfather's brutal death and battles ancient cosmic entities.",
                coverImageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co20pn.jpg",
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
                description = "A post-apocalyptic action RPG set in the radioactive ruins of Washington D.C., following the Lone Wanderer's search for their father across the Capital Wasteland.",
                coverImageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1re9.jpg",
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
                description = "Solid Snake embarks on a final covert mission in war-torn Middle Eastern and European proxy battlefields to assassinate Liquid Ocelot and dismantle the SOP system.",
                coverImageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r8c.jpg",
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
                description = "Nintendo's landmark 3D platformer where Mario infiltrates Princess Peach's castle, diving through magical paintings to recover Power Stars from Bowser.",
                coverImageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x7d.jpg",
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
