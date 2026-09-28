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
            return Result.failure(IllegalArgumentException("Firebase Project ID não configurado"))
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
                        null
                    }
                } else null
            } ?: emptyList()

            Result.success(games)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveGame(projectId: String, game: GameItem): Result<Boolean> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase Project ID não configurado"))
        }

        return try {
            val jsonString = json.encodeToString(GameItem.serializer(), game)
            val docId = game.id.ifBlank { "game_${game.title.filter { it.isLetterOrDigit() }.lowercase()}" }
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/games/$docId"

            val body = buildJsonObject {
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
                })
            }

            val response = client.patch(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGame(projectId: String, gameId: String): Result<Boolean> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase Project ID não configurado"))
        }

        return try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/games/$gameId"
            val response = client.delete(url)
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
