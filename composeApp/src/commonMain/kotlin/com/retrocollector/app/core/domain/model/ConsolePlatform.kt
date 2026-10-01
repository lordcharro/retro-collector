package com.retrocollector.app.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ConsolePlatformSerializer : KSerializer<ConsolePlatform> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ConsolePlatform", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ConsolePlatform) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): ConsolePlatform {
        val str = decoder.decodeString()
        return ConsolePlatform.fromPlatformString(str) ?: ConsolePlatform.GAMECUBE
    }
}

@Serializable(with = ConsolePlatformSerializer::class)
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
        regionalAdvice = "European PAL cartridges (NES-xx-FRA, NES-xx-NOE, NES-xx-UKV). UKV and Scandinavian versions feature full English. German NOE editions of text-heavy games (like RPGs) may be German-only.",
        brandColorHex = 0xFFDC2626
    ),
    SNES(
        id = "snes",
        displayName = "Super Nintendo (SNES)",
        shortName = "SNES",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("SNSP-", "SNES-"),
        regionalAdvice = "Super Nintendo PAL cartridges (SNSP-xxxx-UKV/NOE/FRG). Nintendo adventure and RPG titles (Zelda, Secret of Mana) in Switzerland/Germany carry NOE code and are 100% in German. Look for UKV or EUR editions for English.",
        brandColorHex = 0xFF7C3AED
    ),
    N64(
        id = "n64",
        displayName = "Nintendo 64",
        shortName = "N64",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("NUS-"),
        regionalAdvice = "In Europe almost all PAL cartridges include English. Only avoid NUS-xxx-NOE cartridges if the title is text-heavy and exclusively a German release.",
        brandColorHex = 0xFFDC2626
    ),
    GAMECUBE(
        id = "gamecube",
        displayName = "Nintendo GameCube",
        shortName = "GameCube",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("DOL-P-", "DOL-"),
        regionalAdvice = "High attention in Switzerland: Many local editions carry DOL-P-xxxx-(NOE/FRG) and may be German-only! Look for codes ending in UKV, EUR, or DOL-P-xxxx-(EUR) with the Multi-5 badge.",
        brandColorHex = 0xFF6366F1
    ),
    WII(
        id = "wii",
        displayName = "Nintendo Wii",
        shortName = "Wii",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("RVL-P-", "RVL-"),
        regionalAdvice = "Wii PAL discs (RVL-P-xxxx). Most Western releases feature Multi-5 (English, German, French, Spanish, Italian). Check the back of the case for English language support if marked with USK rating.",
        brandColorHex = 0xFF0EA5E9
    ),
    WII_U(
        id = "wii_u",
        displayName = "Nintendo Wii U",
        shortName = "Wii U",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("WUP-P-", "WUP-"),
        regionalAdvice = "Wii U PAL discs (WUP-P-xxxx). The vast majority of European releases include English regardless of the regional cover artwork.",
        brandColorHex = 0xFF0284C7
    ),
    SWITCH(
        id = "switch",
        displayName = "Nintendo Switch",
        shortName = "Switch",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("HAC-P-", "HAC-"),
        regionalAdvice = "The vast majority of European physical cartridges (EUR) include English even if the front cover is in German. Watch out only for rare regional releases without English language support.",
        brandColorHex = 0xFFEF4444
    ),
    SWITCH_2(
        id = "switch_2",
        displayName = "Nintendo Switch 2",
        shortName = "Switch 2",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("NSW2-", "HAC2-"),
        regionalAdvice = "Next-generation Nintendo platform. European multilingual standard with native English support.",
        brandColorHex = 0xFFBE123C
    ),
    GAME_BOY(
        id = "game_boy",
        displayName = "Game Boy / Color",
        shortName = "GB/GBC",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("DMG-", "CGB-"),
        regionalAdvice = "Classic Game Boy and Game Boy Color cartridges. Cartridges are region-free. Beware of German NOE editions of RPGs like Pokémon or Zelda that are locked to German.",
        brandColorHex = 0xFF84CC16
    ),
    GBA(
        id = "gba",
        displayName = "Game Boy Advance",
        shortName = "GBA",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("AGB-"),
        regionalAdvice = "Game Boy Advance cartridges (AGB-xxxx-EUR/NOE/UKV). Beware of cardboard boxes and German NOE cartridges for RPGs. GBA is region-free.",
        brandColorHex = 0xFF8B5CF6
    ),
    NDS(
        id = "nds",
        displayName = "Nintendo DS",
        shortName = "DS",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("NTR-", "TWL-"),
        regionalAdvice = "Nintendo DS game cards (NTR-xxxx-EUR). Standard console and game cards are region-free. 95%+ of European releases include English selectable in-game or via system language.",
        brandColorHex = 0xFF06B6D4
    ),
    N3DS(
        id = "n3ds",
        displayName = "Nintendo 3DS",
        shortName = "3DS",
        ecosystem = PlatformEcosystem.NINTENDO,
        codePrefixes = listOf("CTR-"),
        regionalAdvice = "Nintendo 3DS game cards (CTR-xxxx-EUR). NOTE: Nintendo 3DS is Region Locked (PAL)! NTSC cartridges will not boot on European consoles.",
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
        regionalAdvice = "PS1 PAL discs in original thick double jewel cases. German releases (SLES-xxxxx with German text) frequently feature German-only voiceovers. Look for SCES/SLES UK or Multi-Language editions.",
        brandColorHex = 0xFF475569
    ),
    PS2(
        id = "ps2",
        displayName = "PlayStation 2",
        shortName = "PS2",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("SLES-", "SCES-"),
        regionalAdvice = "PS2 PAL discs. Watch out for exclusive Germany USK releases that may only contain German audio and text. UKV / Multi-5 editions contain full English.",
        brandColorHex = 0xFF1E40AF
    ),
    PS3(
        id = "ps3",
        displayName = "PlayStation 3",
        shortName = "PS3",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("BLES-", "BCES-"),
        regionalAdvice = "Many DACH region PS3 games (exclusive BLES with USK rating logo) come WITHOUT English (e.g. Fallout, Skyrim, Bioshock, Ratchet). Check the BLES code on the spine and disc: in Switzerland BLES-01780 discs are multilingual and safe.",
        brandColorHex = 0xFF0284C7
    ),
    PS4(
        id = "ps4",
        displayName = "PlayStation 4",
        shortName = "PS4",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("CUSA-"),
        regionalAdvice = "PS4 Blu-ray discs (CUSA-xxxxx). The vast majority of European releases include English audio and text, matching the console language setting.",
        brandColorHex = 0xFF2563EB
    ),
    PS5(
        id = "ps5",
        displayName = "PlayStation 5",
        shortName = "PS5",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("PPSA-"),
        regionalAdvice = "PS5 Ultra HD Blu-ray discs (PPSA-xxxxx). Virtually 100% of European physical releases include full English.",
        brandColorHex = 0xFF3B82F6
    ),
    PSP(
        id = "psp",
        displayName = "PlayStation Portable (PSP)",
        shortName = "PSP",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("ULES-", "UCES-"),
        regionalAdvice = "PSP PAL UMD discs (ULES-xxxxx, UCES-xxxxx). The PSP console is region-free for games. Most European UMDs include English.",
        brandColorHex = 0xFF059669
    ),
    PS_VITA(
        id = "ps_vita",
        displayName = "PlayStation Vita",
        shortName = "PS Vita",
        ecosystem = PlatformEcosystem.SONY,
        codePrefixes = listOf("PCSF-", "PCSB-"),
        regionalAdvice = "PS Vita game cards (PCSF-xxxxx, PCSB-xxxxx). Completely region-free. Almost all European and Asian English physical releases contain full English support.",
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
        regionalAdvice = "Xbox Original PAL discs (translucent green cases). European titles generally include English, but verify that German USK releases were not censored.",
        brandColorHex = 0xFF15803D
    ),
    XBOX_360(
        id = "xbox_360",
        displayName = "Xbox 360",
        shortName = "Xbox 360",
        ecosystem = PlatformEcosystem.MICROSOFT,
        codePrefixes = listOf("X360-", "MS-"),
        regionalAdvice = "Xbox 360 PAL DVD discs. Some titles in Germany/Switzerland were released with German-only audio (e.g. German Halo 3 only has German). Check code and look for UK editions.",
        brandColorHex = 0xFF16A34A
    ),
    XBOX_ONE(
        id = "xbox_one",
        displayName = "Xbox One / S / X",
        shortName = "Xbox One",
        ecosystem = PlatformEcosystem.MICROSOFT,
        codePrefixes = listOf("XONE-"),
        regionalAdvice = "Xbox One Blu-ray discs. Region-free for game discs with multilingual support via automatic language pack downloads.",
        brandColorHex = 0xFF22C55E
    ),
    XBOX_SERIES(
        id = "xbox_series",
        displayName = "Xbox Series X | S",
        shortName = "Series X|S",
        ecosystem = PlatformEcosystem.MICROSOFT,
        codePrefixes = listOf("XSX-"),
        regionalAdvice = "Physical Smart Delivery / Series X discs. Full European multilingual support.",
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
        regionalAdvice = "Sega Master System PAL cartridges in classic grid plastic clamshell cases. European releases feature English text or are arcade titles with no language barrier.",
        brandColorHex = 0xFF1D4ED8
    ),
    MEGADRIVE(
        id = "megadrive",
        displayName = "Sega Mega Drive / Genesis",
        shortName = "Mega Drive",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-", "1", "670-"),
        regionalAdvice = "Mega Drive PAL cartridges (black grid or blue clamshell cases). European cartridges are 100% in English. In Switzerland, manuals are frequently multilingual (DE/FR/EN).",
        brandColorHex = 0xFF1E3A8A
    ),
    SEGA_SATURN(
        id = "sega_saturn",
        displayName = "Sega Saturn",
        shortName = "Saturn",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-", "T-"),
        regionalAdvice = "Large PAL Saturn cardboard/plastic cases. Beware of fragile European case hinges. All PAL European releases feature full English.",
        brandColorHex = 0xFF3730A3
    ),
    DREAMCAST(
        id = "dreamcast",
        displayName = "Sega Dreamcast",
        shortName = "Dreamcast",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-", "HDR-", "T-"),
        regionalAdvice = "PAL Dreamcast GD-ROM discs in double blue jewel cases. Cases are notoriously fragile (teeth snap easily). All PAL games feature English and 50Hz/60Hz selector.",
        brandColorHex = 0xFFEA580C
    ),
    GAME_GEAR(
        id = "game_gear",
        displayName = "Sega Game Gear",
        shortName = "Game Gear",
        ecosystem = PlatformEcosystem.SEGA,
        codePrefixes = listOf("MK-", "670-"),
        regionalAdvice = "Sega Game Gear handheld cartridges. Worldwide region-free. European and US English games run on any hardware.",
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
        regionalAdvice = "Covers vintage and pre-NES systems: Atari 2600/7800, ColecoVision, Intellivision, Commodore 64/Amiga, PC Engine, Neo Geo, and MSX. Period cartridge and floppy media.",
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
