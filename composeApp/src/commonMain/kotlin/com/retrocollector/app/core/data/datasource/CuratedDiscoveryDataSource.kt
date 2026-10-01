package com.retrocollector.app.core.data.datasource

import com.retrocollector.app.core.domain.model.*

object CuratedDiscoveryDataSource {

    fun getFilteredCatalog(
        genre: GameGenre?,
        platform: ConsolePlatform?,
        query: String?
    ): List<DiscoveredGameItem> {
        val allCurated = getCuratedCatalog()
        var filtered = allCurated

        if (platform != null) {
            filtered = filtered.filter { it.platform == platform }
        }

        if (genre != null && genre != GameGenre.ALL) {
            filtered = filterByGenre(filtered, genre)
        }

        if (!query.isNullOrBlank()) {
            val q = query.trim().lowercase()
            val matches = filtered.filter {
                it.title.lowercase().contains(q) ||
                    it.recommendationReason.lowercase().contains(q) ||
                    it.genreTags.any { tag -> tag.lowercase().contains(q) } ||
                    it.similarTitles.any { sim -> sim.lowercase().contains(q) }
            }
            if (matches.isNotEmpty()) return matches
        }

        return filtered.ifEmpty { allCurated.take(8) }
    }

    fun getSimilarGames(game: GameItem): List<DiscoveredGameItem> {
        val allCurated = getCuratedCatalog()
        val siblings = allCurated.filter { it.title != game.title }
        val samePlatform = siblings.filter { it.platform == game.platform }
        return if (samePlatform.size >= 3) samePlatform.take(3) else siblings.take(3)
    }

    private fun filterByGenre(items: List<DiscoveredGameItem>, genre: GameGenre): List<DiscoveredGameItem> {
        val tag = when (genre) {
            GameGenre.POINT_AND_CLICK -> "Point"
            GameGenre.SURVIVAL_HORROR -> "Horror"
            GameGenre.FPS_TACTICAL -> "FPS"
            GameGenre.STRATEGY_RTS -> "Strategy"
            GameGenre.RPG_JRPG -> "RPG"
            GameGenre.STEALTH -> "Stealth"
            GameGenre.PLATFORMER -> "Platformer"
            GameGenre.HIDDEN_GEMS -> "Cult"
            else -> ""
        }
        if (tag.isEmpty()) return items
        return items.filter { it.genreTags.any { t -> t.contains(tag, ignoreCase = true) } }
    }

    fun getCuratedCatalog(): List<DiscoveredGameItem> {
        return getPointAndClickGames() +
            getSurvivalAndStrategyGames() +
            getActionAndCultGames() +
            getMultiEcosystemCuratedGames()
    }

    private fun getPointAndClickGames(): List<DiscoveredGameItem> = listOf(
        DiscoveredGameItem(
            id = "disc_monkey_island_2_ps3",
            title = "Monkey Island 2: LeChuck's Revenge (Special Edition)",
            franchiseName = "Monkey Island",
            platform = ConsolePlatform.PS3,
            releaseYear = "2010",
            genreDisplayName = "Point & Click",
            genreTags = listOf("Point & Click", "Adventure", "Humor"),
            recommendationReason = "Timeless LucasArts classic with full voice acting and switchable HD/classic graphics.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BLES-01124", region = "EUR", editionNote = "Edition with full English audio & text", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Available on Nintendo Switch (eShop) and physical PS3 compilation",
            estimatedPriceChf = 32.0,
            similarTitles = listOf("Grim Fandango", "Broken Sword", "Sam & Max")
        ),
        DiscoveredGameItem(
            id = "disc_grim_fandango_switch",
            title = "Grim Fandango Remastered",
            franchiseName = "Grim Fandango",
            platform = ConsolePlatform.SWITCH,
            releaseYear = "2018",
            genreDisplayName = "Point & Click",
            genreTags = listOf("Point & Click", "Noir", "Comedy"),
            recommendationReason = "Tim Schafer's Day of the Dead folklore masterpiece with orchestral score.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "HAC-P-AGFRA", region = "EUR", editionNote = "Physical and digital release with English support", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "iam8bit / retail physical release for Nintendo Switch",
            estimatedPriceChf = 35.0,
            similarTitles = listOf("Monkey Island", "Day of the Tentacle")
        ),
        DiscoveredGameItem(
            id = "disc_broken_sword_gc",
            title = "Broken Sword: The Shadow of the Templars",
            franchiseName = "Broken Sword",
            platform = ConsolePlatform.GAMECUBE,
            releaseYear = "2002",
            genreDisplayName = "Point & Click",
            genreTags = listOf("Point & Click", "Mystery", "History"),
            recommendationReason = "Classic investigative adventure in Paris following George Stobbart.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "DOL-P-GBSE", region = "EUR / UK", editionNote = "Multi-5 with English voice acting and subtitles", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Reforged Remaster released on Nintendo Switch",
            estimatedPriceChf = 28.0,
            similarTitles = listOf("Monkey Island", "Syberia")
        )
    )

    private fun getSurvivalAndStrategyGames(): List<DiscoveredGameItem> = listOf(
        DiscoveredGameItem(
            id = "disc_eternal_darkness_gc",
            title = "Eternal Darkness: Sanity's Requiem",
            franchiseName = "Eternal Darkness",
            platform = ConsolePlatform.GAMECUBE,
            releaseYear = "2002",
            genreDisplayName = "Survival Horror",
            genreTags = listOf("Survival Horror", "Psychological", "Lovecraft"),
            recommendationReason = "Exclusive gem featuring innovative sanity effects and an epic multi-era narrative.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "DOL-P-GEDE", region = "UKV", editionNote = "Full English (Avoid NOE version without English)", isSafe = true)),
            hasModernPortOrRemaster = false,
            modernPortDetails = "GameCube physical exclusive (No modern remasters)",
            estimatedPriceChf = 65.0,
            similarTitles = listOf("Resident Evil", "Siren: Blood Curse")
        ),
        DiscoveredGameItem(
            id = "disc_siren_ps3",
            title = "Siren: Blood Curse",
            franchiseName = "Forbidden Siren",
            platform = ConsolePlatform.PS3,
            releaseYear = "2008",
            genreDisplayName = "Survival Horror",
            genreTags = listOf("Survival Horror", "Stealth", "Japan"),
            recommendationReason = "Atmospheric Japanese horror featuring the innovative Sight-Jack perspective mechanic.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BCES-00294", region = "EUR", editionNote = "European physical edition with English audio", isSafe = true)),
            hasModernPortOrRemaster = false,
            modernPortDetails = "PS3 physical exclusive in Europe",
            estimatedPriceChf = 45.0,
            similarTitles = listOf("Silent Hill", "Fatal Frame", "Dead Space")
        ),
        DiscoveredGameItem(
            id = "disc_pikmin2_gc",
            title = "Pikmin 2",
            franchiseName = "Pikmin",
            platform = ConsolePlatform.GAMECUBE,
            releaseYear = "2004",
            genreDisplayName = "Strategy & RTS",
            genreTags = listOf("Strategy & RTS", "Management", "Exploration"),
            recommendationReason = "Brilliant real-time strategy with underground dungeons and treasure hunting.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "DOL-P-GPVE", region = "EUR", editionNote = "European Multi-5 with English support", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "HD Remaster available on Nintendo Switch (Pikmin 1+2)",
            estimatedPriceChf = 45.0,
            similarTitles = listOf("Pikmin", "Overlord", "Battalion Wars")
        )
    )

    private fun getActionAndCultGames(): List<DiscoveredGameItem> = listOf(
        DiscoveredGameItem(
            id = "disc_valkyria_ps3",
            title = "Valkyria Chronicles",
            franchiseName = "Valkyria Chronicles",
            platform = ConsolePlatform.PS3,
            releaseYear = "2008",
            genreDisplayName = "Strategy & RTS",
            genreTags = listOf("Strategy & RTS", "Turn-Based Tactical", "Anime"),
            recommendationReason = "Combines turn-based tactical RPG combat with real-time action and watercolor art.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BLES-00373", region = "EUR", editionNote = "Full English audio and subtitles", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "HD Remaster available on Nintendo Switch / PS4 / PC",
            estimatedPriceChf = 20.0,
            similarTitles = listOf("Fire Emblem", "XCOM", "Final Fantasy Tactics")
        ),
        DiscoveredGameItem(
            id = "disc_killzone2_ps3",
            title = "Killzone 2",
            franchiseName = "Killzone",
            platform = ConsolePlatform.PS3,
            releaseYear = "2009",
            genreDisplayName = "FPS & Tactical",
            genreTags = listOf("FPS & Tactical", "Sci-Fi", "Military"),
            recommendationReason = "Visceral first-person shooter with unmatched weighty combat feel and industrial atmosphere.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BCES-00081", region = "EUR", editionNote = "European edition with English audio and subtitles", isSafe = true)),
            hasModernPortOrRemaster = false,
            modernPortDetails = "PS3 Exclusive",
            estimatedPriceChf = 15.0,
            similarTitles = listOf("Resistance 2", "Battlefield: Bad Company")
        ),
        DiscoveredGameItem(
            id = "disc_folklore_ps3",
            title = "Folklore",
            franchiseName = "Folklore",
            platform = ConsolePlatform.PS3,
            releaseYear = "2007",
            genreDisplayName = "Hidden Gems",
            genreTags = listOf("Hidden Gems", "Action RPG", "Celtic Mythology"),
            recommendationReason = "Cult classic inspired by Irish and Celtic folklore featuring Sixaxis motion soul capture.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BCES-00050", region = "EUR", editionNote = "Full English audio and text", isSafe = true)),
            hasModernPortOrRemaster = false,
            modernPortDetails = "PS3 physical exclusive - highly sought after by collectors",
            estimatedPriceChf = 55.0,
            similarTitles = listOf("NieR", "Demon's Souls", "Valkyria Chronicles")
        )
    )

    private fun getMultiEcosystemCuratedGames(): List<DiscoveredGameItem> = listOf(
        // SNES
        DiscoveredGameItem(
            id = "disc_super_metroid_snes",
            title = "Super Metroid",
            franchiseName = "Metroid",
            platform = ConsolePlatform.SNES,
            releaseYear = "1994",
            genreDisplayName = "Action / Exploration",
            genreTags = listOf("Metroidvania", "Sci-Fi", "Cult"),
            recommendationReason = "One of the greatest 16-bit masterpieces of all time with peerless atmosphere.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "SNSP-RI-EUR", region = "EUR / UKV", editionNote = "European edition in English", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Available on Nintendo Switch Online",
            estimatedPriceChf = 75.0,
            similarTitles = listOf("Castlevania: Symphony of the Night", "Metroid Fusion")
        ),
        // PS1
        DiscoveredGameItem(
            id = "disc_castlevania_sotn_ps1",
            title = "Castlevania: Symphony of the Night",
            franchiseName = "Castlevania",
            platform = ConsolePlatform.PS1,
            releaseYear = "1997",
            genreDisplayName = "Action RPG",
            genreTags = listOf("Metroidvania", "Gothic", "Cult"),
            recommendationReason = "The game that defined the Metroidvania genre on PlayStation with a legendary soundtrack.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "SLES-00524", region = "EUR / UK", editionNote = "PAL edition with English audio and text", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Available in Castlevania Requiem (PS4/PS5)",
            estimatedPriceChf = 120.0,
            similarTitles = listOf("Super Metroid", "Bloodstained")
        ),
        // PS2
        DiscoveredGameItem(
            id = "disc_silent_hill_2_ps2",
            title = "Silent Hill 2",
            franchiseName = "Silent Hill",
            platform = ConsolePlatform.PS2,
            releaseYear = "2001",
            genreDisplayName = "Survival Horror",
            genreTags = listOf("Survival Horror", "Psychological", "Cult"),
            recommendationReason = "Absolute pinnacle of psychological survival horror with PAL 2-disc special edition.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "SLES-50382", region = "EUR / UK", editionNote = "PAL 2-Disc Digipak with English audio", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Modern remake available on PS5",
            estimatedPriceChf = 60.0,
            similarTitles = listOf("Silent Hill 3", "Resident Evil 2", "Forbidden Siren")
        ),
        // GBA
        DiscoveredGameItem(
            id = "disc_metroid_fusion_gba",
            title = "Metroid Fusion",
            franchiseName = "Metroid",
            platform = ConsolePlatform.GBA,
            releaseYear = "2002",
            genreDisplayName = "Action & Adventure",
            genreTags = listOf("Metroidvania", "Sci-Fi", "Handheld"),
            recommendationReason = "Intense handheld adventure following Samus Aran against the X parasite and SA-X mimic.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "AGB-AMTE-EUR", region = "EUR", editionNote = "European Multi-5 with English support", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Available on Nintendo Switch Online GBA",
            estimatedPriceChf = 50.0,
            similarTitles = listOf("Metroid Zero Mission", "Castlevania: Aria of Sorrow")
        ),
        // Sega Dreamcast
        DiscoveredGameItem(
            id = "disc_shenmue_dc",
            title = "Shenmue",
            franchiseName = "Shenmue",
            platform = ConsolePlatform.DREAMCAST,
            releaseYear = "2000",
            genreDisplayName = "Adventure / Open World",
            genreTags = listOf("Adventure", "Open World", "Cult", "Martial Arts"),
            recommendationReason = "Revolutionary work by Yu Suzuki with dynamic day/night cycle and daily life in Yokosuka.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "MK-51057-50", region = "EUR", editionNote = "4 GD-ROM edition with English voice acting and subtitles", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Shenmue I & II Remaster on PS4 and Xbox One",
            estimatedPriceChf = 55.0,
            similarTitles = listOf("Shenmue II", "Yakuza", "Virtua Fighter")
        ),
        // Sega Mega Drive
        DiscoveredGameItem(
            id = "disc_sonic_2_md",
            title = "Sonic the Hedgehog 2",
            franchiseName = "Sonic",
            platform = ConsolePlatform.MEGADRIVE,
            releaseYear = "1992",
            genreDisplayName = "Platformers",
            genreTags = listOf("Platformer", "Speed", "Retro"),
            recommendationReason = "Timeless 16-bit platformer introducing Tails and the iconic Spin Dash mechanic.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "MK-1051-50", region = "EUR", editionNote = "Classic black clamshell with full English support", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Available in Sonic Origins (Switch/PS4/PS5/Xbox)",
            estimatedPriceChf = 30.0,
            similarTitles = listOf("Sonic the Hedgehog", "Sonic 3 & Knuckles")
        )
    )
}
