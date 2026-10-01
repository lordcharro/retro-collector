package com.retrocollector.app.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CollectionStatusSerializer : KSerializer<CollectionStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CollectionStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CollectionStatus) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): CollectionStatus {
        val str = decoder.decodeString()
        return CollectionStatus.fromString(str)
    }
}

@Serializable(with = CollectionStatusSerializer::class)
enum class CollectionStatus(
    val label: String,
    val icon: String
) {
    WISHLIST("Wishlist", "🎯"),
    OWNED("Owned", "📦"),
    PASS("Pass", "❌");

    companion object {
        val displayStatuses: List<CollectionStatus> = listOf(WISHLIST, OWNED, PASS)

        fun fromString(value: String?): CollectionStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: WISHLIST
    }
}
