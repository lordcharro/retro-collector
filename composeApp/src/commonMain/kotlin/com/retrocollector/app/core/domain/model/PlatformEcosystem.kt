package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlatformEcosystem(
    val id: String,
    val displayName: String,
    val icon: String,
    val brandColorHex: Long
) {
    NINTENDO(
        id = "nintendo",
        displayName = "Nintendo",
        icon = "🔴",
        brandColorHex = 0xFFE60012
    ),
    SONY(
        id = "sony",
        displayName = "PlayStation",
        icon = "🔵",
        brandColorHex = 0xFF003791
    ),
    MICROSOFT(
        id = "microsoft",
        displayName = "Xbox",
        icon = "🟢",
        brandColorHex = 0xFF107C10
    ),
    SEGA(
        id = "sega",
        displayName = "Sega",
        icon = "🌀",
        brandColorHex = 0xFF0060A8
    ),
    RETRO_VINTAGE(
        id = "retro_vintage",
        displayName = "Retro & Vintage",
        icon = "🕹️",
        brandColorHex = 0xFFD97706
    );

    companion object {
        fun fromId(id: String?): PlatformEcosystem =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: RETRO_VINTAGE
    }
}
