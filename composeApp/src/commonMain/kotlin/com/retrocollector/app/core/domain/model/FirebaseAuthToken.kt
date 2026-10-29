package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class FirebaseAuthToken(
    val idToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val localId: String,
    val expiresAtEpochMs: Long
) {
    fun isExpired(bufferSeconds: Long = 60L): Boolean {
        val now = Clock.System.now().toEpochMilliseconds()
        return now >= (expiresAtEpochMs - bufferSeconds * 1000L)
    }
}
