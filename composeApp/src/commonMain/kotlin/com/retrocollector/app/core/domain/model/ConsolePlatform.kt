package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConsolePlatform(
    val id: String,
    val displayName: String,
    val shortName: String,
    val codePrefixes: List<String>,
    val regionalAdvice: String
) {
    N64(
        id = "n64",
        displayName = "Nintendo 64",
        shortName = "N64",
        codePrefixes = listOf("NUS-"),
        regionalAdvice = "Na Europa quase todos os cartuchos PAL têm inglês. Evite apenas cartuchos com código NUS-xxx-NOE se o jogo tiver texto e for versão exclusivamente alemã."
    ),
    GAMECUBE(
        id = "gamecube",
        displayName = "Nintendo GameCube",
        shortName = "GameCube",
        codePrefixes = listOf("DOL-P-", "DOL-"),
        regionalAdvice = "Atenção máxima na Suíça: Muitas edições vendidas cá têm código " +
            "DOL-P-xxxx-(NOE/FRG) e podem ter apenas alemão! Procure edições com código terminando " +
            "em UKV, EUR ou P-xxxx-(EUR) com logo Multi-5."
    ),
    PS3(
        id = "ps3",
        displayName = "PlayStation 3",
        shortName = "PS3",
        codePrefixes = listOf("BLES-", "BCES-"),
        regionalAdvice = "Muitos jogos PS3 da região DACH (BLES exclusivo com logotipo USK) vêm " +
            "SEM inglês (ex: Fallout, Skyrim, Bioshock, Ratchet). Verifique o código BLES na lombada: " +
            "compare com o código BLES da edição do Reino Unido (UK)."
    ),
    SWITCH(
        id = "switch",
        displayName = "Nintendo Switch",
        shortName = "Switch",
        codePrefixes = listOf("HAC-"),
        regionalAdvice = "A esmagadora maioria dos cartuchos físicos europeus (EUR) inclui inglês " +
            "mesmo que a capa frontal esteja em alemão. Atenção apenas a edições raras regionais sem patch inglês."
    );

    companion object {
        fun fromId(id: String?): ConsolePlatform =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: PS3
    }
}
