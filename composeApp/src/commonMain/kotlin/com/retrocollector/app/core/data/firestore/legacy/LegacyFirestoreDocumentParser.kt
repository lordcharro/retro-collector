package com.retrocollector.app.core.data.firestore.legacy

import com.retrocollector.app.core.data.firestore.FirestoreDocument
import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.GameCondition
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.model.GameOffer
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.domain.model.MessageSender
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

/**
 * Isolated backwards-compatibility parser for legacy Firestore documents.
 *
 * This class handles reading older documents that contain a raw serialized `data` JSON blob string
 * or legacy stringified fields. When the Firestore database is fully migrated and older schema documents
 * no longer exist, this file and its package can be safely deleted in a single step.
 */
object LegacyFirestoreDocumentParser {

    fun parseLegacyGameItem(doc: FirestoreDocument, json: Json): GameItem? {
        // 1. Try decoding from the legacy 'data' JSON string blob
        val jsonString = doc.fields["data"]?.get("stringValue")?.jsonPrimitive?.content
        if (!jsonString.isNullOrBlank()) {
            try {
                return json.decodeFromString<GameItem>(jsonString)
            } catch (e: Exception) {
                println("LegacyFirestoreDocumentParser: Failed to decode legacy 'data' JSON: ${e.message}")
            }
        }

        // 2. Fallback to extracting whatever legacy flat fields might exist
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
                acquiredCondition = acquiredCondition,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            println("LegacyFirestoreDocumentParser: Failed to extract fallback fields: ${e.message}")
            null
        }
    }

    fun parseLegacyChatMessage(doc: FirestoreDocument, json: Json): ChatMessage? {
        val jsonString = doc.fields["data"]?.get("stringValue")?.jsonPrimitive?.content
        if (!jsonString.isNullOrBlank()) {
            try {
                return json.decodeFromString<ChatMessage>(jsonString)
            } catch (e: Exception) {
                println("LegacyFirestoreDocumentParser: Failed to decode legacy ChatMessage JSON: ${e.message}")
            }
        }

        return try {
            val id = doc.fields["id"]?.get("stringValue")?.jsonPrimitive?.content
                ?: doc.name?.substringAfterLast("/").orEmpty()
            val contextId = doc.fields["contextId"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            val senderStr = doc.fields["sender"]?.get("stringValue")?.jsonPrimitive?.content ?: "USER"
            val sender = if (senderStr == "GEMINI") MessageSender.GEMINI else MessageSender.USER
            val text = doc.fields["text"]?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
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
            println("LegacyFirestoreDocumentParser: Failed to parse ChatMessage fallback: ${e.message}")
            null
        }
    }

    fun parseLegacyOffers(doc: FirestoreDocument, json: Json): List<GameOffer>? {
        val rawJson = doc.fields["offers"]?.get("stringValue")?.jsonPrimitive?.content
        if (!rawJson.isNullOrBlank()) {
            try {
                return json.decodeFromString<List<GameOffer>>(rawJson)
            } catch (_: Exception) {}
        }
        return null
    }
}
