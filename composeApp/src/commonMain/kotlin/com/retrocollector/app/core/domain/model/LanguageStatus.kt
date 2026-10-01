package com.retrocollector.app.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LanguageStatusSerializer : KSerializer<LanguageStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LanguageStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LanguageStatus) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): LanguageStatus {
        val str = decoder.decodeString()
        return LanguageStatus.fromString(str)
    }
}

@Serializable(with = LanguageStatusSerializer::class)
enum class LanguageStatus(
    val label: String,
    val subLabel: String,
    val isEnglishFriendly: Boolean
) {
    FULL_ENGLISH(
        label = "Full English",
        subLabel = "Audio+Subs",
        isEnglishFriendly = true
    ),
    SUBS_ONLY(
        label = "Subs Only",
        subLabel = "UKV Safe",
        isEnglishFriendly = true
    ),
    GERMAN_ONLY(
        label = "German Only",
        subLabel = "NOE Cut",
        isEnglishFriendly = false
    ),
    EDITION_NOTICE(
        label = "NOE Edition",
        subLabel = "Manual Ger",
        isEnglishFriendly = false
    ),
    UNVERIFIED(
        label = "Unverified",
        subLabel = "Pending Scan",
        isEnglishFriendly = false
    );

    companion object {
        fun fromString(value: String?): LanguageStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: UNVERIFIED
    }
}
