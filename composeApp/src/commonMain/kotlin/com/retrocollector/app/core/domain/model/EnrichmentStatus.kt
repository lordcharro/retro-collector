package com.retrocollector.app.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object EnrichmentStatusSerializer : KSerializer<EnrichmentStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EnrichmentStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EnrichmentStatus) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): EnrichmentStatus {
        val str = decoder.decodeString()
        return EnrichmentStatus.fromString(str)
    }
}

@Serializable(with = EnrichmentStatusSerializer::class)
enum class EnrichmentStatus(val label: String) {
    PENDING("Pending"),
    ENRICHING("Enriching"),
    COMPLETE("Complete"),
    FAILED("Failed");

    companion object {
        fun fromString(value: String?): EnrichmentStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: COMPLETE
    }
}
