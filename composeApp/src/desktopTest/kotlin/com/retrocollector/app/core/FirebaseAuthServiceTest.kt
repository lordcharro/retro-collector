package com.retrocollector.app.core

import com.retrocollector.app.core.domain.model.FirebaseAuthToken
import com.retrocollector.app.settings.domain.model.AppSettings
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Clock

class FirebaseAuthServiceTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `FirebaseAuthToken serialization round-trip succeeds`() {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val token = FirebaseAuthToken(
            idToken = "mock_id_token_xyz",
            refreshToken = "mock_refresh_token_abc",
            expiresInSeconds = 3600L,
            localId = "mock_local_user_id",
            expiresAtEpochMs = nowMs + 3600_000L
        )

        val serialized = json.encodeToString(FirebaseAuthToken.serializer(), token)
        val deserialized = json.decodeFromString<FirebaseAuthToken>(serialized)

        assertEquals("mock_id_token_xyz", deserialized.idToken)
        assertEquals("mock_refresh_token_abc", deserialized.refreshToken)
        assertEquals("mock_local_user_id", deserialized.localId)
        assertEquals(3600L, deserialized.expiresInSeconds)
        assertFalse(deserialized.isExpired())
    }

    @Test
    fun `FirebaseAuthToken isExpired detects expired and nearly-expired tokens`() {
        val nowMs = Clock.System.now().toEpochMilliseconds()

        val expiredToken = FirebaseAuthToken(
            idToken = "token_expired",
            refreshToken = "refresh",
            expiresInSeconds = 3600L,
            localId = "uid",
            expiresAtEpochMs = nowMs - 1000L
        )
        assertTrue(expiredToken.isExpired())

        val soonExpiringToken = FirebaseAuthToken(
            idToken = "token_expiring_soon",
            refreshToken = "refresh",
            expiresInSeconds = 3600L,
            localId = "uid",
            expiresAtEpochMs = nowMs + 30_000L // within default 60s buffer
        )
        assertTrue(soonExpiringToken.isExpired(bufferSeconds = 60L))
        assertFalse(soonExpiringToken.isExpired(bufferSeconds = 10L))

        val freshToken = FirebaseAuthToken(
            idToken = "token_fresh",
            refreshToken = "refresh",
            expiresInSeconds = 3600L,
            localId = "uid",
            expiresAtEpochMs = nowMs + 3600_000L
        )
        assertFalse(freshToken.isExpired())
    }

    @Test
    fun `AppSettings hasFirebaseAuthConfig checks both project ID and API key`() {
        val emptySettings = AppSettings()
        assertFalse(emptySettings.hasFirebaseAuthConfig)

        val projectOnly = AppSettings(firebaseProjectId = "my-project")
        assertFalse(projectOnly.hasFirebaseAuthConfig)

        val keyOnly = AppSettings(firebaseApiKey = "AIzaSy...")
        assertFalse(keyOnly.hasFirebaseAuthConfig)

        val completeConfig = AppSettings(
            firebaseProjectId = "my-project",
            firebaseApiKey = "AIzaSy..."
        )
        assertTrue(completeConfig.hasFirebaseAuthConfig)
    }
}
