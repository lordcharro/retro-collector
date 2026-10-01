package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConsolePlatform(
    val id: String,
    val displayName: String,
    val shortName: String,
    val ecosystem: PlatformEcosystem,
    val codePrefixes: List<String>,
    val regionalAdvice: String,
    val brandColorHex: Long
) {
    // -------------------------------------------------------------
    // NINTENDO ECOSYSTEM
    // -------------------------------------------------------------
    NES(
        id = "nes",
        displayName = "Nintendo Entertainment System",
        shortName = "NES",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("NES-"),
        regionalAdvice = "Cartuchos PAL europeus (NES-xx-FRA, NES-xx-NOE, NES-xx-UKV). Versões UKV e Scandinavian têm inglês integral. Edições alemãs NOE de jogos de texto (como RPGs) podem ter apenas alemão.",
        brandColorHex = 0xFFDC2626
    ),
    SNES(
        id = "snes",
        displayName = "Super Nintendo (SNES)",
        shortName = "SNES",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("SNSP-", "SNES-"),
        regionalAdvice = "Cartuchos PAL Super Nintendo (SNSP-xxxx-UKV/NOE/FRG). Jogos de aventura e RPG da Nintendo (Zelda, Secret of Mana) na Suíça/Alemanha têm código NOE e vêm 100% em alemão. Procure edições UKV ou EUR com inglês.",
        brandColorHex = 0xFF7C3AED
    ),
    N64(
        id = "n64",
        displayName = "Nintendo 64",
        shortName = "N64",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("NUS-"),
        regionalAdvice = "Na Europa quase todos os cartuchos PAL têm inglês. Evite apenas cartuchos com código NUS-xxx-NOE se o jogo tiver texto e for versão exclusivamente alemã.",
        brandColorHex = 0xFFDC2626
    ),
    GAMECUBE(
        id = "gamecube",
        displayName = "Nintendo GameCube",
        shortName = "GameCube",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("DOL-P-", "DOL-"),
        regionalAdvice = "Atenção máxima na Suíça: Muitas edições vendidas cá têm código DOL-P-xxxx-(NOE/FRG) e podem ter apenas alemão! Procure edições com código terminando em UKV, EUR ou DOL-P-xxxx-(EUR) com logo Multi-5.",
        brandColorHex = 0xFF6366F1
    ),
    WII(
        id = "wii",
        displayName = "Nintendo Wii",
        shortName = "Wii",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("RVL-P-", "RVL-"),
        regionalAdvice = "Discos PAL Wii (RVL-P-xxxx). A maioria dos títulos ocidentais inclui Multi-5 (inglês, alemão, francês, espanhol, italiano). Verifique se o verso da caixa indica idioma inglês se o jogo tiver classificação USK.",
        brandColorHex = 0xFF0EA5E9
    ),
    WII_U(
        id = "wii_u",
        displayName = "Nintendo Wii U",
        shortName = "Wii U",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("WUP-P-", "WUP-"),
        regionalAdvice = "Discos PAL Wii U (WUP-P-xxxx). A grande maioria dos lançamentos europeus inclui inglês independentemente da capa regional.",
        brandColorHex = 0xFF0284C7
    ),
    SWITCH(
        id = "switch",
        displayName = "Nintendo Switch",
        shortName = "Switch",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("HAC-P-", "HAC-"),
        regionalAdvice = "A esmagadora maioria dos cartuchos físicos europeus (EUR) inclui inglês mesmo que a capa frontal esteja em alemão. Atenção apenas a edições raras regionais sem patch inglês.",
        brandColorHex = 0xFFEF4444
    ),
    SWITCH_2(
        id = "switch_2",
        displayName = "Nintendo Switch 2",
        shortName = "Switch 2",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("NSW2-", "HAC2-"),
        regionalAdvice = "Próxima geração da Nintendo. Padrão europeu multilingue com suporte nativo de inglês.",
        brandColorHex = 0xFFBE123C
    ),
    GAME_BOY(
        id = "game_boy",
        displayName = "Game Boy / Color",
        shortName = "GB/GBC",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("DMG-", "CGB-"),
        regionalAdvice = "Cartuchos Game Boy clássicos e Game Boy Color. Cartuchos são region-free. Cuidado com edições alemãs NOE de RPGs como Pokémon ou Zelda que estão bloqueados em alemão.",
        brandColorHex = 0xFF84CC16
    ),
    GBA(
        id = "gba",
        displayName = "Game Boy Advance",
        shortName = "GBA",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("AGB-"),
        regionalAdvice = "Cartuchos Game Boy Advance (AGB-xxxx-EUR/NOE/UKV). Cuidado com caixas de cartão e cartuchos alemães NOE em RPGs. AGB é region-free.",
        brandColorHex = 0xFF8B5CF6
    ),
    NDS(
        id = "nds",
        displayName = "Nintendo DS",
        shortName = "DS",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("NTR-", "TWL-"),
        regionalAdvice = "Cartões Nintendo DS (NTR-xxxx-EUR). Consola e cartões normais são region-free. 95%+ dos títulos europeus incluem inglês selecionável nas opções ou idioma do sistema.",
        brandColorHex = 0xFF06B6D4
    ),
    N3DS(
        id = "n3ds",
        displayName = "Nintendo 3DS",
        shortName = "3DS",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("CTR-"),
        regionalAdvice = "Cartões Nintendo 3DS (CTR-xxxx-EUR). ATENÇÃO: O Nintendo 3DS possui bloqueio regional (Region Locked PAL)! Cartuchos NTSC não correm em consolas europeias.",
        brandColorHex = 0xFFF43F5E
    ),

    // -------------------------------------------------------------
    // PLAYSTATION / SONY ECOSYSTEM
    // -------------------------------------------------------------
    PS1(
        id = "ps1",
        displayName = "PlayStation 1",
        shortName = "PS1",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("SLES-", "SCES-"),
        regionalAdvice = "Discos PAL PS1 em caixas grossas originais. Edições alemãs (SLES-xxxxx com textos alemães) frequentemente têm dobragem apenas em alemão. Procure SCES/SLES edições UK ou Multi-Idioma.",
        brandColorHex = 0xFF475569
    ),
    PS2(
        id = "ps2",
        displayName = "PlayStation 2",
        shortName = "PS2",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("SLES-", "SCES-"),
        regionalAdvice = "Discos PAL PS2. Atenção a edições com selo USK exclusivo da Alemanha que podem conter apenas áudio/texto em alemão. Edições UKV / Multi-5 contêm inglês integral.",
        brandColorHex = 0xFF1E40AF
    ),
    PS3(
        id = "ps3",
        displayName = "PlayStation 3",
        shortName = "PS3",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("BLES-", "BCES-"),
        regionalAdvice = "Muitos jogos PS3 da região DACH (BLES exclusivo com logotipo USK) vêm SEM inglês (ex: Fallout, Skyrim, Bioshock, Ratchet). Verifique o código BLES na lombada e no disco: na Suíça discos BLES-01780 são multilingues e seguros.",
        brandColorHex = 0xFF0284C7
    ),
    PS4(
        id = "ps4",
        displayName = "PlayStation 4",
        shortName = "PS4",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("CUSA-"),
        regionalAdvice = "Discos Blu-ray PS4 (CUSA-xxxxx). A grande maioria dos lançamentos europeus inclui áudio e texto em inglês, adaptando-se ao idioma configurado na consola.",
        brandColorHex = 0xFF2563EB
    ),
    PS5(
        id = "ps5",
        displayName = "PlayStation 5",
        shortName = "PS5",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("PPSA-"),
        regionalAdvice = "Discos Ultra HD Blu-ray PS5 (PPSA-xxxxx). Praticamente 100% dos lançamentos físicos europeus incluem inglês integral.",
        brandColorHex = 0xFF3B82F6
    ),
    PSP(
        id = "psp",
        displayName = "PlayStation Portable (PSP)",
        shortName = "PSP",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("ULES-", "UCES-"),
        regionalAdvice = "Discos UMD PAL (ULES-xxxxx, UCES-xxxxx). A consola PSP é region-free para jogos. A maioria dos UMDs europeus inclui inglês.",
        brandColorHex = 0xFF059669
    ),
    PS_VITA(
        id = "ps_vita",
        displayName = "PlayStation Vita",
        shortName = "PS Vita",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("PCSF-", "PCSB-"),
        regionalAdvice = "Cartões PS Vita (PCSF-xxxxx, PCSB-xxxxx). Totalmente region-free. Quase todos os lançamentos físicos europeus e asiáticos em inglês contêm suporte integral de inglês.",
        brandColorHex = 0xFF0D9488
    ),

    // -------------------------------------------------------------
    // XBOX / MICROSOFT ECOSYSTEM
    // -------------------------------------------------------------
    XBOX_OG(
        id = "xbox_og",
        displayName = "Xbox (Original)",
        shortName = "Xbox",
        ecosystem = PlatformEcosystem.MICROSOFT,
        codePrefixes = listOf("MS-", "XBOX-"),
        regionalAdvice = "Discos PAL Xbox clássica (caixas verdes translúcidas). Títulos europeus geralmente trazem inglês, mas certifique-se de que edições alemãs USK não foram censuradas.",
        brandColorHex = 0xFF15803D
    ),
    XBOX_360(
        id = "xbox_360",
        displayName = "Xbox 360",
        shortName = "Xbox 360",
        ecosystem = PlatformEcosystem.MICROSOFT,
        codePrefixes = listOf("X360-", "MS-"),
        regionalAdvice = "Discos DVD PAL Xbox 360. Alguns jogos na Alemanha/Suíça foram lançados com áudio exclusivo em alemão (ex: Halo 3 alemão só tem alemão). Verifique o código e edição UK.",
        brandColorHex = 0xFF16A34A
    ),
    XBOX_ONE(
        id = "xbox_one",
        displayName = "Xbox One / S / X",
        shortName = "Xbox One",
        ecosystem = PlatformEcosystem.MICROSOFT,
        codePrefixes = listOf("XONE-"),
        regionalAdvice = "Discos Blu-ray Xbox One. Sem bloqueio regional para discos de jogo e suporte multilingue com download de pacote de idiomas automático.",
        brandColorHex = 0xFF22C55E
    ),
    XBOX_SERIES(
        id = "xbox_series",
        displayName = "Xbox Series X | S",
        shortName = "Series X|S",
        ecosystem = PlatformEcosystem.MICROSOFT,
        codePrefixes = listOf("XSX-"),
        regionalAdvice = "Discos físicos Smart Delivery / Series X. Suporte integral multilingue europeu.",
        brandColorHex = 0xFF4ADE80
    ),

    // -------------------------------------------------------------
    // SEGA ECOSYSTEM
    // -------------------------------------------------------------
    MASTER_SYSTEM(
        id = "master_system",
        displayName = "Sega Master System",
        shortName = "Master System",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-"),
        regionalAdvice = "Cartuchos PAL Sega Master System em caixas plásticas clássicas com grelha. Lançamentos europeus têm texto em inglês ou são jogos arcade sem barreira linguística.",
        brandColorHex = 0xFF1D4ED8
    ),
    MEGADRIVE(
        id = "megadrive",
        displayName = "Sega Mega Drive / Genesis",
        shortName = "Mega Drive",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-", "1", "670-"),
        regionalAdvice = "Cartuchos PAL Mega Drive (caixas pretas com grelha ou azuis). Cartuchos europeus vêm 100% em inglês. Na Suíça, manuais são frequentemente multilingues (DE/FR/EN).",
        brandColorHex = 0xFF1E3A8A
    ),
    SEGA_SATURN(
        id = "sega_saturn",
        displayName = "Sega Saturn",
        shortName = "Saturn",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-", "T-"),
        regionalAdvice = "Caixas grandes de cartão/plástico PAL Saturn. Atenção às dobradiças frágeis das caixas europeias. Todos os jogos PAL europeus têm inglês integral.",
        brandColorHex = 0xFF3730A3
    ),
    DREAMCAST(
        id = "dreamcast",
        displayName = "Sega Dreamcast",
        shortName = "Dreamcast",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-", "HDR-", "T-"),
        regionalAdvice = "Discos GD-ROM PAL Dreamcast em caixas azuis duplas. Caixas são notoriamente frágeis (dentes partem com facilidade). Todos os jogos PAL têm inglês e seletor 50Hz/60Hz.",
        brandColorHex = 0xFFEA580C
    ),
    GAME_GEAR(
        id = "game_gear",
        displayName = "Sega Game Gear",
        shortName = "Game Gear",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-", "670-"),
        regionalAdvice = "Cartuchos portáteis Sega Game Gear. Region-free mundialmente. Jogos europeus e americanos em inglês correm em qualquer consola.",
        brandColorHex = 0xFF0284C7
    ),

    // -------------------------------------------------------------
    // RETRO & VINTAGE HUB
    // -------------------------------------------------------------
    RETRO_VINTAGE(
        id = "retro_vintage",
        displayName = "Retro Pre-NES / Vintage",
        shortName = "Retro Vintage",
        ecosystem = PlatformEcosystem.RETRO_VINTAGE,
        codePrefixes = listOf("AT-", "CV-", "INT-", "NG-", "PCE-", "C64-"),
        regionalAdvice = "Abrange sistemas clássicos e pré-NES: Atari 2600/7800, ColecoVision, Intellivision, Commodore 64/Amiga, PC Engine, Neo Geo e MSX. Jogos em cartucho e disquete de época.",
        brandColorHex = 0xFFB45309
    );

    companion object {
        fun fromId(id: String?): ConsolePlatform =
            fromPlatformString(id) ?: GAMECUBE

        fun fromPlatformString(input: String?): ConsolePlatform? {
            if (input.isNullOrBlank()) return null
            val normalized = input.lowercase().trim()
            
            // Direct id or name match
            entries.find { it.id.equals(normalized, ignoreCase = true) || it.name.equals(normalized, ignoreCase = true) }?.let {
                return it
            }

            // Common aliases & variations
            return when {
                // Nintendo
                normalized in listOf("nes", "nintendo entertainment system", "famicom") -> NES
                normalized in listOf("snes", "super nintendo", "super nintendo entertainment system", "super famicom", "sfc") -> SNES
                normalized in listOf("n64", "nintendo 64", "nintendo64", "nus") -> N64
                normalized in listOf("gamecube", "gc", "gcn", "nintendo gamecube", "dol") -> GAMECUBE
                normalized in listOf("wii", "nintendo wii", "rvl") -> WII
                normalized in listOf("wii u", "wiiu", "nintendo wii u", "wup") -> WII_U
                normalized in listOf("switch", "nintendo switch", "ns", "hac") -> SWITCH
                normalized in listOf("switch 2", "switch2", "nintendo switch 2", "nsw2", "hac2") -> SWITCH_2
                normalized in listOf("gb", "gameboy", "game boy", "gbc", "game boy color", "dmg", "cgb") -> GAME_BOY
                normalized in listOf("gba", "game boy advance", "gameboy advance", "agb") -> GBA
                normalized in listOf("ds", "nds", "nintendo ds", "ntr", "twl") -> NDS
                normalized in listOf("3ds", "n3ds", "nintendo 3ds", "ctr") -> N3DS

                // Sony PlayStation
                normalized in listOf("ps1", "psx", "playstation", "playstation 1", "playstation1", "sces", "sles") -> PS1
                normalized in listOf("ps2", "playstation 2", "playstation2") -> PS2
                normalized in listOf("ps3", "playstation 3", "playstation3", "bles", "bces") -> PS3
                normalized in listOf("ps4", "playstation 4", "playstation4", "cusa") -> PS4
                normalized in listOf("ps5", "playstation 5", "playstation5", "ppsa") -> PS5
                normalized in listOf("psp", "playstation portable", "ules", "uces") -> PSP
                normalized in listOf("ps vita", "psvita", "vita", "playstation vita", "pcsf", "pcsb") -> PS_VITA

                // Microsoft Xbox
                normalized in listOf("xbox", "xbox original", "original xbox", "xbox og", "xbox 1") -> XBOX_OG
                normalized in listOf("xbox 360", "xbox360", "x360", "360") -> XBOX_360
                normalized in listOf("xbox one", "xboxone", "xone", "xbox one s", "xbox one x") -> XBOX_ONE
                normalized in listOf("xbox series", "xbox series x", "xbox series s", "series x", "series s", "xsx", "xss") -> XBOX_SERIES

                // Sega
                normalized in listOf("master system", "sms", "sega master system") -> MASTER_SYSTEM
                normalized in listOf("megadrive", "mega drive", "genesis", "sega genesis", "sega megadrive") -> MEGADRIVE
                normalized in listOf("saturn", "sega saturn") -> SEGA_SATURN
                normalized in listOf("dreamcast", "sega dreamcast", "dc") -> DREAMCAST
                normalized in listOf("game gear", "gamegear", "sega game gear", "gg") -> GAME_GEAR

                // Retro / Vintage
                normalized in listOf("retro", "vintage", "retro_vintage", "atari", "atari 2600", "atari 7800", "commodore", "c64", "amiga", "colecovision", "intellivision", "neo geo", "neogeo", "pc engine", "msx") -> RETRO_VINTAGE

                else -> entries.find {
                    it.displayName.contains(input, ignoreCase = true) ||
                    it.shortName.equals(input, ignoreCase = true)
                }
            }
        }
    }
}
