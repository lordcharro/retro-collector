package com.retrocollector.app.core.data.firestore

import com.retrocollector.app.core.data.firestore.legacy.LegacyFirestoreDocumentParser
import com.retrocollector.app.core.domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class FirestoreDocument(
    val name: String? = null,
    val fields: Map<String, JsonObject> = emptyMap(),
    val createTime: String? = null,
    val updateTime: String? = null
)

@Serializable
data class FirestoreListResponse(
    val documents: List<FirestoreDocument>? = null
)

class FirestoreService(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getGames(projectId: String): Result<List<GameItem>> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase Project ID is not configured"))
        }

        return try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/games"
            val response: FirestoreListResponse = client.get(url).body()
            val games = response.documents?.mapNotNull { doc ->
                parseGameFromDocFields(doc)
            }.orEmpty()

            Result.success(games)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    internal fun parseGameFromDocFields(doc: FirestoreDocument): GameItem? {
        if (doc.fields.containsKey("data")) {
            return LegacyFirestoreDocumentParser.parseLegacyGameItem(doc, json)
        }

        return try {
            val id = doc.fields["id"]?.get("stringValue")?.jsonPrimitive?.content
                ?: doc.name?.substringAfterLast("/").orEmpty()
            val title = doc.fields["title"]?.get("stringValue")?.jsonPrimitive?.content
            if (title.isNullOrBlank() || id.isBlank()) {
                return LegacyFirestoreDocumentParser.parseLegacyGameItem(doc, json)
            }

            val platformStr = doc.fields["platform"]?.get("stringValue")?.jsonPrimitive?.content
            val platform = ConsolePlatform.fromPlatformString(platformStr) ?: ConsolePlatform.GAMECUBE
            val statusStr = doc.fields["status"]?.get("stringValue")?.jsonPrimitive?.content
                ?: doc.fields["collectionStatus"]?.get("stringValue")?.jsonPrimitive?.content
            val status = CollectionStatus.fromString(statusStr)
            val enrichmentStr = doc.fields["enrichmentStatus"]?.get("stringValue")?.jsonPrimitive?.content
            val enrichmentStatus = EnrichmentStatus.fromString(enrichmentStr)
            val franchiseName = doc.fields["franchiseName"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val releaseYear = doc.fields["releaseYear"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val coverImageUrl = doc.fields["coverImageUrl"]?.get("stringValue")?.jsonPrimitive?.content
            val spineImageUrl = doc.fields["spineImageUrl"]?.get("stringValue")?.jsonPrimitive?.content
            val productCode = doc.fields["productCode"]?.get("stringValue")?.jsonPrimitive?.content
            val barcode = doc.fields["barcode"]?.get("stringValue")?.jsonPrimitive?.content
            val spottedLocation = doc.fields["spottedLocation"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()

            val askingPriceChf = doc.fields["askingPriceChf"]?.let { parseDoubleOrInt(it) }
            val targetPriceChf = doc.fields["targetPriceChf"]?.let { parseDoubleOrInt(it) }
            val paidPriceChf = doc.fields["paidPriceChf"]?.let { parseDoubleOrInt(it) }

            val languageStatusStr = doc.fields["languageStatus"]?.get("stringValue")?.jsonPrimitive?.content
            val languageStatus = LanguageStatus.fromString(languageStatusStr)
            val languageAudio = parseStringArray(doc.fields["languageAudio"])
            val languageSubtitles = parseStringArray(doc.fields["languageSubtitles"])

            val safeSkus = parseSkuList(doc.fields["safeSkus"])
            val riskySkus = parseSkuList(doc.fields["riskySkus"])
            val marketRadar = doc.fields["marketRadar"]?.let { parseMarketRadar(it) }

            val censorshipWarning = doc.fields["censorshipWarning"]?.get("stringValue")?.jsonPrimitive?.content
            val collectorVerdict = doc.fields["collectorVerdict"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val personalNotes = doc.fields["personalNotes"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val listingUrl = doc.fields["listingUrl"]?.get("stringValue")?.jsonPrimitive?.content
            val updatedAt = doc.fields["updatedAt"]?.get("integerValue")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

            val acquiredConditionStr = doc.fields["acquiredCondition"]?.get("stringValue")?.jsonPrimitive?.content
            val acquiredCondition = acquiredConditionStr?.let { GameCondition.fromString(it) }

            val offers = parseOffersList(doc)
            val similarGames = parseSimilarGamesList(doc)

            GameItem(
                id = id,
                title = title,
                franchiseName = franchiseName,
                platform = platform,
                releaseYear = releaseYear,
                coverImageUrl = coverImageUrl,
                spineImageUrl = spineImageUrl,
                productCode = productCode,
                barcode = barcode,
                spottedLocation = spottedLocation,
                askingPriceChf = askingPriceChf,
                targetPriceChf = targetPriceChf,
                paidPriceChf = paidPriceChf,
                languageStatus = languageStatus,
                languageAudio = languageAudio,
                languageSubtitles = languageSubtitles,
                safeSkus = safeSkus,
                riskySkus = riskySkus,
                marketRadar = marketRadar,
                censorshipWarning = censorshipWarning,
                collectorVerdict = collectorVerdict,
                collectionStatus = status,
                enrichmentStatus = enrichmentStatus,
                personalNotes = personalNotes,
                listingUrl = listingUrl,
                offers = offers,
                similarGames = similarGames,
                acquiredCondition = acquiredCondition,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            println("FirestoreService: Error parsing structured doc fields, falling back to legacy: ${e.message}")
            LegacyFirestoreDocumentParser.parseLegacyGameItem(doc, json)
        }
    }

    internal fun parseSkuInfo(element: JsonElement): SkuInfo? {
        val fields = element.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject ?: return null
        val code = fields["code"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val region = fields["region"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val editionNote = fields["editionNote"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val isSafe = fields["isSafe"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: true
        return if (code.isNotBlank() || region.isNotBlank()) {
            SkuInfo(code = code, region = region, editionNote = editionNote, isSafe = isSafe)
        } else null
    }

    internal fun encodeSkuInfo(sku: SkuInfo): JsonObject = buildJsonObject {
        put("mapValue", buildJsonObject {
            put("fields", buildJsonObject {
                put("code", buildJsonObject { put("stringValue", sku.code) })
                put("region", buildJsonObject { put("stringValue", sku.region) })
                put("editionNote", buildJsonObject { put("stringValue", sku.editionNote) })
                put("isSafe", buildJsonObject { put("booleanValue", sku.isSafe) })
            })
        })
    }

    internal fun parseMarketRadar(element: JsonElement): SwissMarketRadar? {
        val fields = element.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject ?: return null
        val spottedPrice = fields["spottedPriceChf"]?.jsonObject?.let { parseDoubleOrInt(it) }
        val medianPrice = fields["medianPriceChf"]?.jsonObject?.let { parseDoubleOrInt(it) }
        val minPrice = fields["historicalMinChf"]?.jsonObject?.let { parseDoubleOrInt(it) }
        val maxPrice = fields["historicalMaxChf"]?.jsonObject?.let { parseDoubleOrInt(it) }
        val trend = fields["trend"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "Stable"
        return SwissMarketRadar(
            spottedPriceChf = spottedPrice,
            medianPriceChf = medianPrice,
            historicalMinChf = minPrice,
            historicalMaxChf = maxPrice,
            trend = trend
        )
    }

    internal fun encodeMarketRadar(radar: SwissMarketRadar): JsonObject = buildJsonObject {
        put("fields", buildJsonObject {
            radar.spottedPriceChf?.let { put("spottedPriceChf", buildJsonObject { put("doubleValue", it) }) }
            radar.medianPriceChf?.let { put("medianPriceChf", buildJsonObject { put("doubleValue", it) }) }
            radar.historicalMinChf?.let { put("historicalMinChf", buildJsonObject { put("doubleValue", it) }) }
            radar.historicalMaxChf?.let { put("historicalMaxChf", buildJsonObject { put("doubleValue", it) }) }
            put("trend", buildJsonObject { put("stringValue", radar.trend) })
        })
    }

    internal fun parseGameOffer(element: JsonElement): GameOffer? {
        val fields = element.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject ?: return null
        val id = fields["id"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val source = fields["source"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "Listing"
        val price = fields["priceChf"]?.jsonObject?.let { parseDoubleOrInt(it) } ?: 0.0
        val shipping = fields["shippingChf"]?.jsonObject?.let { parseDoubleOrInt(it) }
        val listingUrl = fields["listingUrl"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val conditionStr = fields["condition"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
        val condition = GameCondition.fromString(conditionStr)
        val sellerOrLocation = fields["sellerOrLocation"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val notes = fields["notes"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val isPurchased = fields["isPurchased"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false
        val isArchived = fields["isArchived"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false
        val createdAt = fields["createdAt"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

        if (id.isBlank() && source.isBlank()) return null

        return GameOffer(
            id = id.ifBlank { "offer_${createdAt}" },
            source = source,
            priceChf = price,
            shippingChf = shipping,
            listingUrl = listingUrl,
            condition = condition,
            sellerOrLocation = sellerOrLocation,
            notes = notes,
            isPurchased = isPurchased,
            isArchived = isArchived,
            createdAt = createdAt
        )
    }

    internal fun encodeGameOffer(offer: GameOffer): JsonObject = buildJsonObject {
        put("mapValue", buildJsonObject {
            put("fields", buildJsonObject {
                put("id", buildJsonObject { put("stringValue", offer.id) })
                put("source", buildJsonObject { put("stringValue", offer.source) })
                put("priceChf", buildJsonObject { put("doubleValue", offer.priceChf) })
                offer.shippingChf?.let {
                    put("shippingChf", buildJsonObject { put("doubleValue", it) })
                }
                put("totalLandedPriceChf", buildJsonObject { put("doubleValue", offer.totalLandedPriceChf) })
                put("listingUrl", buildJsonObject { put("stringValue", offer.listingUrl) })
                put("condition", buildJsonObject { put("stringValue", offer.condition.name) })
                put("sellerOrLocation", buildJsonObject { put("stringValue", offer.sellerOrLocation) })
                put("notes", buildJsonObject { put("stringValue", offer.notes) })
                put("isPurchased", buildJsonObject { put("booleanValue", offer.isPurchased) })
                put("isArchived", buildJsonObject { put("booleanValue", offer.isArchived) })
                put("createdAt", buildJsonObject { put("integerValue", offer.createdAt.toString()) })
            })
        })
    }

    internal fun parseDiscoveredGameItem(element: JsonElement): DiscoveredGameItem? {
        val fields = element.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject ?: return null
        val id = fields["id"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val title = fields["title"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        if (title.isBlank()) return null

        val franchiseName = fields["franchiseName"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val platformStr = fields["platform"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
        val platform = ConsolePlatform.fromPlatformString(platformStr) ?: ConsolePlatform.GAMECUBE
        val releaseYear = fields["releaseYear"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val genreDisplayName = fields["genreDisplayName"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val genreTags = parseStringArray(fields["genreTags"])
        val recommendationReason = fields["recommendationReason"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
        val languageStatus = LanguageStatus.fromString(fields["languageStatus"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content)
        val safeSkus = parseSkuList(fields["safeSkus"])
        val hasModernPort = fields["hasModernPortOrRemaster"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false
        val modernPortDetails = fields["modernPortDetails"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
        val estimatedPrice = fields["estimatedPriceChf"]?.jsonObject?.let { parseDoubleOrInt(it) }
        val similarTitles = parseStringArray(fields["similarTitles"])
        val isAlreadyInCollection = fields["isAlreadyInCollection"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false
        val isAlreadyInWishlist = fields["isAlreadyInWishlist"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false

        return DiscoveredGameItem(
            id = id.ifBlank { "sim_${title.filter { it.isLetterOrDigit() }.lowercase()}" },
            title = title,
            franchiseName = franchiseName,
            platform = platform,
            releaseYear = releaseYear,
            genreDisplayName = genreDisplayName,
            genreTags = genreTags,
            recommendationReason = recommendationReason,
            languageStatus = languageStatus,
            safeSkus = safeSkus,
            hasModernPortOrRemaster = hasModernPort,
            modernPortDetails = modernPortDetails,
            estimatedPriceChf = estimatedPrice,
            similarTitles = similarTitles,
            isAlreadyInCollection = isAlreadyInCollection,
            isAlreadyInWishlist = isAlreadyInWishlist
        )
    }

    internal fun encodeDiscoveredGameItem(item: DiscoveredGameItem): JsonObject = buildJsonObject {
        put("mapValue", buildJsonObject {
            put("fields", buildJsonObject {
                put("id", buildJsonObject { put("stringValue", item.id) })
                put("title", buildJsonObject { put("stringValue", item.title) })
                if (item.franchiseName.isNotBlank()) {
                    put("franchiseName", buildJsonObject { put("stringValue", item.franchiseName) })
                }
                put("platform", buildJsonObject { put("stringValue", item.platform.id) })
                if (item.releaseYear.isNotBlank()) {
                    put("releaseYear", buildJsonObject { put("stringValue", item.releaseYear) })
                }
                if (item.genreDisplayName.isNotBlank()) {
                    put("genreDisplayName", buildJsonObject { put("stringValue", item.genreDisplayName) })
                }
                if (item.genreTags.isNotEmpty()) {
                    put("genreTags", buildJsonObject {
                        put("arrayValue", buildJsonObject {
                            put("values", buildJsonArray {
                                item.genreTags.forEach { add(buildJsonObject { put("stringValue", it) }) }
                            })
                        })
                    })
                }
                if (item.recommendationReason.isNotBlank()) {
                    put("recommendationReason", buildJsonObject { put("stringValue", item.recommendationReason) })
                }
                put("languageStatus", buildJsonObject { put("stringValue", item.languageStatus.name) })
                if (item.safeSkus.isNotEmpty()) {
                    put("safeSkus", buildJsonObject {
                        put("arrayValue", buildJsonObject {
                            put("values", buildJsonArray {
                                item.safeSkus.forEach { add(encodeSkuInfo(it)) }
                            })
                        })
                    })
                }
                put("hasModernPortOrRemaster", buildJsonObject { put("booleanValue", item.hasModernPortOrRemaster) })
                item.modernPortDetails?.let {
                    put("modernPortDetails", buildJsonObject { put("stringValue", it) })
                }
                item.estimatedPriceChf?.let {
                    put("estimatedPriceChf", buildJsonObject { put("doubleValue", it) })
                }
                if (item.similarTitles.isNotEmpty()) {
                    put("similarTitles", buildJsonObject {
                        put("arrayValue", buildJsonObject {
                            put("values", buildJsonArray {
                                item.similarTitles.forEach { add(buildJsonObject { put("stringValue", it) }) }
                            })
                        })
                    })
                }
                put("isAlreadyInCollection", buildJsonObject { put("booleanValue", item.isAlreadyInCollection) })
                put("isAlreadyInWishlist", buildJsonObject { put("booleanValue", item.isAlreadyInWishlist) })
            })
        })
    }

    private fun parseDoubleOrInt(fieldObject: JsonObject): Double? {
        return fieldObject["doubleValue"]?.jsonPrimitive?.content?.toDoubleOrNull()
            ?: fieldObject["integerValue"]?.jsonPrimitive?.content?.toDoubleOrNull()
    }

    private fun parseStringArray(fieldElement: JsonElement?): List<String> {
        val values = fieldElement?.jsonObject?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray ?: return emptyList()
        return values.mapNotNull { it.jsonObject["stringValue"]?.jsonPrimitive?.content }
    }

    private fun parseSkuList(fieldElement: JsonElement?): List<SkuInfo> {
        val values = fieldElement?.jsonObject?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray ?: return emptyList()
        return values.mapNotNull { parseSkuInfo(it) }
    }

    private fun parseOffersList(doc: FirestoreDocument): List<GameOffer> {
        val values = doc.fields["offers"]?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray
        if (values != null) {
            val parsed = values.mapNotNull { parseGameOffer(it) }
            if (parsed.isNotEmpty()) return parsed
        }
        return LegacyFirestoreDocumentParser.parseLegacyOffers(doc, json) ?: emptyList()
    }

    private fun parseSimilarGamesList(doc: FirestoreDocument): List<DiscoveredGameItem> {
        val values = doc.fields["similarGames"]?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray ?: return emptyList()
        return values.mapNotNull { parseDiscoveredGameItem(it) }
    }

    internal fun buildGameDocumentBody(game: GameItem): JsonObject {
        val best = game.bestOffer

        return buildJsonObject {
            put("fields", buildJsonObject {
                buildIdentityFields(game, this)
                buildPricingAndConditionFields(game, this)
                buildLanguageAndMediaFields(game, this)
                buildRadarAndSkuFields(game, this)
                buildOffersAndSimilarFields(game, best, this)
            })
        }
    }

    private fun buildIdentityFields(game: GameItem, builder: JsonObjectBuilder) = with(builder) {
        put("id", buildJsonObject { put("stringValue", game.id) })
        put("title", buildJsonObject { put("stringValue", game.title) })
        put("platform", buildJsonObject { put("stringValue", game.platform.id) })
        put("status", buildJsonObject { put("stringValue", game.collectionStatus.name) })
        put("collectionStatus", buildJsonObject { put("stringValue", game.collectionStatus.name) })
        put("enrichmentStatus", buildJsonObject { put("stringValue", game.enrichmentStatus.name) })
        put("languageStatus", buildJsonObject { put("stringValue", game.languageStatus.name) })
        put("updatedAt", buildJsonObject { put("integerValue", game.updatedAt.toString()) })

        if (game.franchiseName.isNotBlank()) {
            put("franchiseName", buildJsonObject { put("stringValue", game.franchiseName) })
        }
        if (game.releaseYear.isNotBlank()) {
            put("releaseYear", buildJsonObject { put("stringValue", game.releaseYear) })
        }
        game.productCode?.let { put("productCode", buildJsonObject { put("stringValue", it) }) }
        game.barcode?.let { put("barcode", buildJsonObject { put("stringValue", it) }) }
        if (game.spottedLocation.isNotBlank()) {
            put("spottedLocation", buildJsonObject { put("stringValue", game.spottedLocation) })
        }
    }

    private fun buildPricingAndConditionFields(game: GameItem, builder: JsonObjectBuilder) = with(builder) {
        game.askingPriceChf?.let { put("askingPriceChf", buildJsonObject { put("doubleValue", it) }) }
        game.targetPriceChf?.let { put("targetPriceChf", buildJsonObject { put("doubleValue", it) }) }
        game.paidPriceChf?.let { put("paidPriceChf", buildJsonObject { put("doubleValue", it) }) }
        game.acquiredCondition?.let { put("acquiredCondition", buildJsonObject { put("stringValue", it.name) }) }
        game.censorshipWarning?.let { put("censorshipWarning", buildJsonObject { put("stringValue", it) }) }
        if (game.collectorVerdict.isNotBlank()) {
            put("collectorVerdict", buildJsonObject { put("stringValue", game.collectorVerdict) })
        }
        if (game.personalNotes.isNotBlank()) {
            put("personalNotes", buildJsonObject { put("stringValue", game.personalNotes) })
        }
        game.listingUrl?.let { put("listingUrl", buildJsonObject { put("stringValue", it) }) }
    }

    private fun buildLanguageAndMediaFields(game: GameItem, builder: JsonObjectBuilder) = with(builder) {
        game.coverImageUrl?.let { put("coverImageUrl", buildJsonObject { put("stringValue", it) }) }
        game.spineImageUrl?.let { put("spineImageUrl", buildJsonObject { put("stringValue", it) }) }

        if (game.languageAudio.isNotEmpty()) {
            put("languageAudio", buildJsonObject {
                put("arrayValue", buildJsonObject {
                    put("values", buildJsonArray {
                        game.languageAudio.forEach { add(buildJsonObject { put("stringValue", it) }) }
                    })
                })
            })
        }
        if (game.languageSubtitles.isNotEmpty()) {
            put("languageSubtitles", buildJsonObject {
                put("arrayValue", buildJsonObject {
                    put("values", buildJsonArray {
                        game.languageSubtitles.forEach { add(buildJsonObject { put("stringValue", it) }) }
                    })
                })
            })
        }
    }

    private fun buildRadarAndSkuFields(game: GameItem, builder: JsonObjectBuilder) = with(builder) {
        if (game.safeSkus.isNotEmpty()) {
            put("safeSkus", buildJsonObject {
                put("arrayValue", buildJsonObject {
                    put("values", buildJsonArray {
                        game.safeSkus.forEach { add(encodeSkuInfo(it)) }
                    })
                })
            })
        }
        if (game.riskySkus.isNotEmpty()) {
            put("riskySkus", buildJsonObject {
                put("arrayValue", buildJsonObject {
                    put("values", buildJsonArray {
                        game.riskySkus.forEach { add(encodeSkuInfo(it)) }
                    })
                })
            })
        }
        game.marketRadar?.let { radar ->
            put("marketRadar", buildJsonObject {
                put("mapValue", encodeMarketRadar(radar))
            })
        }
    }

    private fun buildOffersAndSimilarFields(game: GameItem, best: GameOffer?, builder: JsonObjectBuilder) = with(builder) {
        put("offersCount", buildJsonObject { put("integerValue", game.offers.size.toString()) })
        if (best != null) {
            put("bestOfferStore", buildJsonObject { put("stringValue", best.source) })
            put("bestOfferPriceChf", buildJsonObject { put("doubleValue", best.totalLandedPriceChf) })
        }
        put("offers", buildJsonObject {
            put("arrayValue", buildJsonObject {
                put("values", buildJsonArray {
                    game.offers.forEach { add(encodeGameOffer(it)) }
                })
            })
        })
        if (game.similarGames.isNotEmpty()) {
            put("similarGames", buildJsonObject {
                put("arrayValue", buildJsonObject {
                    put("values", buildJsonArray {
                        game.similarGames.forEach { add(encodeDiscoveredGameItem(it)) }
                    })
                })
            })
        }
    }

    suspend fun saveGame(projectId: String, game: GameItem): Result<Boolean> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase Project ID is not configured"))
        }

        return try {
            val docId = game.id.ifBlank { "game_${game.title.filter { it.isLetterOrDigit() }.lowercase()}" }
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/games/$docId"
            val body = buildGameDocumentBody(game)

            val response = client.patch(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            Result.success(response.status.isSuccess())
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGame(projectId: String, gameId: String): Result<Boolean> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase Project ID is not configured"))
        }

        return try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/games/$gameId"
            val response = client.delete(url)
            Result.success(response.status.isSuccess())
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatMessages(projectId: String): Result<List<ChatMessage>> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase Project ID is not configured"))
        }

        return try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/chat_threads"
            val response: FirestoreListResponse = client.get(url).body()
            val messages = response.documents?.mapNotNull { doc ->
                if (doc.fields.containsKey("data")) {
                    LegacyFirestoreDocumentParser.parseLegacyChatMessage(doc, json)
                } else {
                    val id = doc.fields["id"]?.get("stringValue")?.jsonPrimitive?.content
                        ?: doc.name?.substringAfterLast("/") ?: ""
                    val contextId = doc.fields["contextId"]?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    val senderStr = doc.fields["sender"]?.get("stringValue")?.jsonPrimitive?.content ?: "USER"
                    val sender = if (senderStr == "GEMINI") MessageSender.GEMINI else MessageSender.USER
                    val text = doc.fields["text"]?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    val imageBase64 = doc.fields["imageBase64"]?.get("stringValue")?.jsonPrimitive?.content
                    val timestamp = doc.fields["timestamp"]?.get("integerValue")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

                    if (id.isNotBlank() && contextId.isNotBlank()) {
                        ChatMessage(
                            id = id,
                            contextId = contextId,
                            sender = sender,
                            text = text,
                            imageBase64 = imageBase64,
                            timestamp = timestamp
                        )
                    } else {
                        LegacyFirestoreDocumentParser.parseLegacyChatMessage(doc, json)
                    }
                }
            } ?: emptyList()

            Result.success(messages)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveChatMessage(projectId: String, message: ChatMessage): Result<Boolean> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase Project ID is not configured"))
        }

        return try {
            val docId = message.id.ifBlank { "msg_${message.timestamp}" }
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/chat_threads/$docId"

            val body = buildJsonObject {
                put("fields", buildJsonObject {
                    put("id", buildJsonObject {
                        put("stringValue", message.id)
                    })
                    put("contextId", buildJsonObject {
                        put("stringValue", message.contextId)
                    })
                    put("sender", buildJsonObject {
                        put("stringValue", message.sender.name)
                    })
                    put("text", buildJsonObject {
                        put("stringValue", message.text)
                    })
                    put("timestamp", buildJsonObject {
                        put("integerValue", message.timestamp.toString())
                    })
                    if (message.imageBase64 != null) {
                        put("imageBase64", buildJsonObject {
                            put("stringValue", message.imageBase64)
                        })
                    }
                })
            }

            val response = client.patch(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            Result.success(response.status.isSuccess())
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChatMessage(projectId: String, messageId: String): Result<Boolean> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase Project ID is not configured"))
        }

        return try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/chat_threads/$messageId"
            val response = client.delete(url)
            Result.success(response.status.isSuccess())
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
