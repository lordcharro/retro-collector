package com.retrocollector.app.core.data.firestore

import com.retrocollector.app.core.domain.model.FirebaseAuthToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Serializable
internal data class FirebaseSignUpResponse(
    val idToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: String? = null,
    val localId: String? = null
)

@Serializable
internal data class FirebaseRefreshTokenResponse(
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: String? = null,
    @SerialName("user_id") val userId: String? = null
)

class FirebaseAuthService(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) {
    private val mutex = Mutex()
    private var cachedToken: FirebaseAuthToken? = null
    private var lastApiKey: String = ""

    suspend fun getIdToken(apiKey: String): Result<String> {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Firebase API key is blank"))
        }

        mutex.withLock {
            if (trimmedKey != lastApiKey) {
                cachedToken = null
                lastApiKey = trimmedKey
            }

            val current = cachedToken
            if (current != null && !current.isExpired()) {
                return Result.success(current.idToken)
            }

            if (current != null && current.refreshToken.isNotBlank()) {
                val refreshResult = refreshToken(trimmedKey, current.refreshToken)
                if (refreshResult.isSuccess) {
                    return Result.success(refreshResult.getOrThrow().idToken)
                }
            }

            val signUpResult = signInAnonymously(trimmedKey)
            return signUpResult.map { it.idToken }
        }
    }

    suspend fun signInAnonymously(apiKey: String): Result<FirebaseAuthToken> {
        return try {
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
            val response: FirebaseSignUpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody("""{"returnSecureToken":true}""")
            }.body()

            val idToken = response.idToken
                ?: return Result.failure(IllegalStateException("No idToken returned from Firebase Auth. Ensure Anonymous authentication is enabled in Firebase Console."))
            val refreshToken = response.refreshToken.orEmpty()
            val expiresIn = response.expiresIn?.toLongOrNull() ?: 3600L
            val nowMs = Clock.System.now().toEpochMilliseconds()

            val token = FirebaseAuthToken(
                idToken = idToken,
                refreshToken = refreshToken,
                expiresInSeconds = expiresIn,
                localId = response.localId.orEmpty(),
                expiresAtEpochMs = nowMs + (expiresIn * 1000L)
            )
            cachedToken = token
            Result.success(token)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun refreshToken(apiKey: String, refreshToken: String): Result<FirebaseAuthToken> {
        return try {
            val url = "https://securetoken.googleapis.com/v1/token?key=$apiKey"
            val response: FirebaseRefreshTokenResponse = client.post(url) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("grant_type=refresh_token&refresh_token=$refreshToken")
            }.body()

            val idToken = response.idToken
                ?: return Result.failure(IllegalStateException("No id_token in refresh response"))
            val newRefreshToken = response.refreshToken ?: refreshToken
            val expiresIn = response.expiresIn?.toLongOrNull() ?: 3600L
            val nowMs = Clock.System.now().toEpochMilliseconds()

            val token = FirebaseAuthToken(
                idToken = idToken,
                refreshToken = newRefreshToken,
                expiresInSeconds = expiresIn,
                localId = response.userId.orEmpty(),
                expiresAtEpochMs = nowMs + (expiresIn * 1000L)
            )
            cachedToken = token
            Result.success(token)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearCache() {
        cachedToken = null
    }
}
