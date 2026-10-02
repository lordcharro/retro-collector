package com.retrocollector.app.core.data.firestore

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
                val jsonString = doc.fields["data"]?.get("stringValue")?.jsonPrimitive?.content
                if (jsonString != null) {
                    try {
                        json.decodeFromString<GameItem>(jsonString)
                    } catch (e: Exception) {
                        println("Failed to decode GameItem JSON: ${e.message}")
                        parseGameFromDocFields(doc)
                    }
                } else {
                    parseGameFromDocFields(doc)
                }
            }.orEmpty()

            Result.success(games)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    internal fun parseGameFromDocFields(doc: FirestoreDocument): GameItem? {
        return try {
            val id = doc.fields["id"]?.get("stringValue")?.jsonPrimitive?.content
                ?: doc.name?.substringAfterLast("/").orEmpty()
            val title = doc.fields["title"]?.get("stringValue")?.jsonPrimitive?.content ?: return null
            if (id.isBlank()) return null
            val platformStr = doc.fields["platform"]?.get("stringValue")?.jsonPrimitive?.content
            val platform = ConsolePlatform.fromPlatformString(platformStr) ?: ConsolePlatform.GAMECUBE
            val statusStr = doc.fields["status"]?.get("stringValue")?.jsonPrimitive?.content
                ?: doc.fields["collectionStatus"]?.get("stringValue")?.jsonPrimitive?.content
            val status = CollectionStatus.fromString(statusStr)
            val franchiseName = doc.fields["franchiseName"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val releaseYear = doc.fields["releaseYear"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val coverImageUrl = doc.fields["coverImageUrl"]?.get("stringValue")?.jsonPrimitive?.content
            val spineImageUrl = doc.fields["spineImageUrl"]?.get("stringValue")?.jsonPrimitive?.content
            val productCode = doc.fields["productCode"]?.get("stringValue")?.jsonPrimitive?.content
            val barcode = doc.fields["barcode"]?.get("stringValue")?.jsonPrimitive?.content
            val spottedLocation = doc.fields["spottedLocation"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val askingPriceChf = doc.fields["askingPriceChf"]?.get("doubleValue")?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: doc.fields["askingPriceChf"]?.get("integerValue")?.jsonPrimitive?.content?.toDoubleOrNull()
            val targetPriceChf = doc.fields["targetPriceChf"]?.get("doubleValue")?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: doc.fields["targetPriceChf"]?.get("integerValue")?.jsonPrimitive?.content?.toDoubleOrNull()
            val paidPriceChf = doc.fields["paidPriceChf"]?.get("doubleValue")?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: doc.fields["paidPriceChf"]?.get("integerValue")?.jsonPrimitive?.content?.toDoubleOrNull()
            val languageStatusStr = doc.fields["languageStatus"]?.get("stringValue")?.jsonPrimitive?.content
            val languageStatus = LanguageStatus.fromString(languageStatusStr)
            val collectorVerdict = doc.fields["collectorVerdict"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val personalNotes = doc.fields["personalNotes"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val listingUrl = doc.fields["listingUrl"]?.get("stringValue")?.jsonPrimitive?.content
            val updatedAt = doc.fields["updatedAt"]?.get("integerValue")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

            val acquiredConditionStr = doc.fields["acquiredCondition"]?.get("stringValue")?.jsonPrimitive?.content
            val acquiredCondition = acquiredConditionStr?.let { GameCondition.fromString(it) }

            val offers = parseOffersFromDoc(doc)

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
                collectorVerdict = collectorVerdict,
                collectionStatus = status,
                personalNotes = personalNotes,
                listingUrl = listingUrl,
                offers = offers,
                acquiredCondition = acquiredCondition,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            println("Failed to parse GameItem fields fallback: ${e.message}")
            null
        }
    }

    internal fun parseOffersFromDoc(doc: FirestoreDocument): List<GameOffer> {
        val rawJson = doc.fields["offers"]?.get("stringValue")?.jsonPrimitive?.content
        if (!rawJson.isNullOrBlank()) {
            try {
                return json.decodeFromString<List<GameOffer>>(rawJson)
            } catch (_: Exception) {}
        }

        val valuesArray = doc.fields["offers"]?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray
        if (valuesArray != null) {
            return valuesArray.mapNotNull { element ->
                try {
                    val fields = element.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject ?: return@mapNotNull null
                    val id = fields["id"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
                    val source = fields["source"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: "Listing"
                    val price = fields["priceChf"]?.jsonObject?.get("doubleValue")?.jsonPrimitive?.content?.toDoubleOrNull()
                        ?: fields["priceChf"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toDoubleOrNull()
                        ?: 0.0
                    val shipping = fields["shippingChf"]?.jsonObject?.get("doubleValue")?.jsonPrimitive?.content?.toDoubleOrNull()
                        ?: fields["shippingChf"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toDoubleOrNull()
                    val listingUrl = fields["listingUrl"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
                    val conditionStr = fields["condition"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
                    val condition = GameCondition.fromString(conditionStr)
                    val sellerOrLocation = fields["sellerOrLocation"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
                    val notes = fields["notes"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
                    val isPurchased = fields["isPurchased"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false
                    val isArchived = fields["isArchived"]?.jsonObject?.get("booleanValue")?.jsonPrimitive?.booleanOrNull ?: false
                    val createdAt = fields["createdAt"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

                    if (id.isBlank() && source.isBlank()) return@mapNotNull null

                    GameOffer(
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
                } catch (e: Exception) {
                    null
                }
            }
        }
        return emptyList()
    }

    internal fun buildGameDocumentBody(game: GameItem): JsonObject {
        val jsonString = json.encodeToString(GameItem.serializer(), game)
        val best = game.bestOffer

        return buildJsonObject {
            put("fields", buildJsonObject {
                put("data", buildJsonObject {
                    put("stringValue", jsonString)
                })
                put("title", buildJsonObject {
                    put("stringValue", game.title)
                })
                put("platform", buildJsonObject {
                    put("stringValue", game.platform.id)
                })
                put("status", buildJsonObject {
                    put("stringValue", game.collectionStatus.name)
                })
                put("updatedAt", buildJsonObject {
                    put("integerValue", game.updatedAt.toString())
                })
                if (game.spottedLocation.isNotBlank()) {
                    put("spottedLocation", buildJsonObject {
                        put("stringValue", game.spottedLocation)
                    })
                }
                game.askingPriceChf?.let {
                    put("askingPriceChf", buildJsonObject {
                        put("doubleValue", it)
                    })
                }
                game.paidPriceChf?.let {
                    put("paidPriceChf", buildJsonObject {
                        put("doubleValue", it)
                    })
                }
                game.acquiredCondition?.let { cond ->
                    put("acquiredCondition", buildJsonObject {
                        put("stringValue", cond.name)
                    })
                }
                put("offersCount", buildJsonObject {
                    put("integerValue", game.offers.size.toString())
                })
                if (best != null) {
                    put("bestOfferStore", buildJsonObject {
                        put("stringValue", best.source)
                    })
                    put("bestOfferPriceChf", buildJsonObject {
                        put("doubleValue", best.totalLandedPriceChf)
                    })
                }
                put("offers", buildJsonObject {
                    put("arrayValue", buildJsonObject {
                        put("values", buildJsonArray {
                            game.offers.forEach { offer ->
                                add(buildJsonObject {
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
                                })
                            }
                        })
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
                val jsonString = doc.fields["data"]?.get("stringValue")?.jsonPrimitive?.content
                if (jsonString != null) {
                    try {
                        json.decodeFromString<ChatMessage>(jsonString)
                    } catch (e: Exception) {
                        println("Failed to decode ChatMessage JSON: ${e.message}")
                        null
                    }
                } else {
                    try {
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
                        } else null
                    } catch (e: Exception) {
                        println("Failed to parse ChatMessage fields: ${e.message}")
                        null
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
            val jsonString = json.encodeToString(ChatMessage.serializer(), message)
            val docId = message.id.ifBlank { "msg_${message.timestamp}" }
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/chat_threads/$docId"

            val body = buildJsonObject {
                put("fields", buildJsonObject {
                    put("data", buildJsonObject {
                        put("stringValue", jsonString)
                    })
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
