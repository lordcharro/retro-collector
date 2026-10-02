package com.retrocollector.app.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object GameConditionSerializer : KSerializer<GameCondition> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("GameCondition", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: GameCondition) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): GameCondition {
        val str = decoder.decodeString()
        return GameCondition.fromString(str)
    }
}

@Serializable(with = GameConditionSerializer::class)
enum class GameCondition(
    val label: String,
    val icon: String
) {
    CIB("CIB / Mint", "✨"),
    BOXED("Boxed", "📦"),
    LOOSE("Loose Disc", "💿");

    companion object {
        fun fromString(value: String?): GameCondition =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: CIB
    }
}
