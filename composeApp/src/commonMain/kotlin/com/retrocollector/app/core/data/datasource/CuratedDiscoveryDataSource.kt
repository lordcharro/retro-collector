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
            genreTags = listOf("Point & Click", "Aventura", "Humor"),
            recommendationReason = "Clássico intemporal da LucasArts com voz integral e gráficos HD alternáveis.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BLES-01124", region = "EUR", editionNote = "Edição com inglês integral", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Disponível na Nintendo Switch (eShop) e coletânea física PS3",
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
            genreTags = listOf("Point & Click", "Noir", "Comédia"),
            recommendationReason = "Obra-prima de Tim Schafer no folclore do Dia dos Mortos com música orquestrada.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "HAC-P-AGFRA", region = "EUR", editionNote = "Edição física e digital com inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Edição física da iam8bit / retail para Nintendo Switch",
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
            genreTags = listOf("Point & Click", "Mistério", "História"),
            recommendationReason = "Aventura de investigação clássica em Paris com George Stobbart.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "DOL-P-GBSE", region = "EUR / UK", editionNote = "Multi-5 com áudio e legendas em inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Reforged Remaster lançado na Nintendo Switch",
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
            genreTags = listOf("Survival Horror", "Psicológico", "Lovecraft"),
            recommendationReason = "Pérola exclusiva com mecânica inovadora de efeitos de insanidade e narrativa por épocas.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "DOL-P-GEDE", region = "UKV", editionNote = "Inglês integral (Evitar versão NOE sem inglês)", isSafe = true)),
            hasModernPortOrRemaster = false,
            modernPortDetails = "Exclusivo físico GameCube (Sem remasters modernos)",
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
            genreTags = listOf("Survival Horror", "Furtividade", "Japão"),
            recommendationReason = "Terror atmosférico japonês com mecânica de visão Sight-Jack.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BCES-00294", region = "EUR", editionNote = "Edição física europeia com áudio em inglês", isSafe = true)),
            hasModernPortOrRemaster = false,
            modernPortDetails = "Exclusivo físico PS3 na Europa",
            estimatedPriceChf = 45.0,
            similarTitles = listOf("Silent Hill", "Fatal Frame", "Dead Space")
        ),
        DiscoveredGameItem(
            id = "disc_pikmin2_gc",
            title = "Pikmin 2",
            franchiseName = "Pikmin",
            platform = ConsolePlatform.GAMECUBE,
            releaseYear = "2004",
            genreDisplayName = "Estratégia & RTS",
            genreTags = listOf("Estratégia & RTS", "Gestão", "Exploração"),
            recommendationReason = "Estratégia em tempo real brilhante com masmorras subterrâneas e tesouros.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "DOL-P-GPVE", region = "EUR", editionNote = "Multi-5 europeu com inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Remaster HD disponível na Nintendo Switch (Pikmin 1+2)",
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
            genreDisplayName = "Estratégia & RTS",
            genreTags = listOf("Estratégia & RTS", "Tático por Turnos", "Anime"),
            recommendationReason = "Combina RPG tático por turnos com ação e estética visual em aguarela.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BLES-00373", region = "EUR", editionNote = "Áudio e legendas em inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Remaster HD disponível na Nintendo Switch",
            estimatedPriceChf = 20.0,
            similarTitles = listOf("Fire Emblem", "XCOM", "Final Fantasy Tactics")
        ),
        DiscoveredGameItem(
            id = "disc_killzone2_ps3",
            title = "Killzone 2",
            franchiseName = "Killzone",
            platform = ConsolePlatform.PS3,
            releaseYear = "2009",
            genreDisplayName = "FPS & Tático",
            genreTags = listOf("FPS & Tático", "Ficção Científica", "Militar"),
            recommendationReason = "Shooter visceral com sensação de peso e atmosfera industrial única.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BCES-00081", region = "EUR", editionNote = "Edição europeia com áudio e legendas em inglês", isSafe = true)),
            hasModernPortOrRemaster = false,
            modernPortDetails = "Exclusivo PS3",
            estimatedPriceChf = 15.0,
            similarTitles = listOf("Resistance 2", "Battlefield: Bad Company")
        ),
        DiscoveredGameItem(
            id = "disc_folklore_ps3",
            title = "Folklore",
            franchiseName = "Folklore",
            platform = ConsolePlatform.PS3,
            releaseYear = "2007",
            genreDisplayName = "Pérolas Ocultas",
            genreTags = listOf("Pérolas Ocultas", "Ação RPG", "Mitologia Celta"),
            recommendationReason = "Pérola de culto baseada na mitologia irlandesa com captura de almas via Sixaxis.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "BCES-00050", region = "EUR", editionNote = "Áudio e texto em inglês integral", isSafe = true)),
            hasModernPortOrRemaster = false,
            modernPortDetails = "Exclusivo físico PS3 - muito procurado por colecionadores",
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
            genreDisplayName = "Ação / Exploração",
            genreTags = listOf("Metroidvania", "Sci-Fi", "Cult"),
            recommendationReason = "Uma das maiores obras-primas da era 16-bit com atmosfera inigualável.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "SNSP-RI-EUR", region = "EUR / UKV", editionNote = "Edição europeia em inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Disponível no Nintendo Switch Online",
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
            genreDisplayName = "Ação RPG",
            genreTags = listOf("Metroidvania", "Gótico", "Cult"),
            recommendationReason = "O jogo que definiu o género Metroidvania no PlayStation com trilha sonora lendária.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "SLES-00524", region = "EUR / UK", editionNote = "Edição PAL com áudio e texto em inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Disponível no Castlevania Requiem (PS4/PS5)",
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
            genreTags = listOf("Survival Horror", "Psicológico", "Cult"),
            recommendationReason = "Marco absoluto do terror psicológico nos videojogos com edição especial PAL de 2 discos.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "SLES-50382", region = "EUR / UK", editionNote = "Digipak PAL 2-Discos com áudio em inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Remake moderno disponível no PS5",
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
            genreDisplayName = "Ação / Aventura",
            genreTags = listOf("Metroidvania", "Sci-Fi", "Portátil"),
            recommendationReason = "Aventura portátil intensa com Samus Aran a enfrentar o parasita X e a sua cópia SA-X.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "AGB-AMTE-EUR", region = "EUR", editionNote = "Multi-5 europeu com inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Disponível no Nintendo Switch Online GBA",
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
            genreDisplayName = "Aventura / Mundo Aberto",
            genreTags = listOf("Aventura", "Mundo Aberto", "Cult", "Artes Marciais"),
            recommendationReason = "Obra revolucionária de Yu Suzuki com ciclo dia/noite e vida quotidiana em Yokosuka.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "MK-51057-50", region = "EUR", editionNote = "Edição 4 GD-ROMs com voz e legendas em inglês", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Shenmue I & II Remaster no PS4 e Xbox One",
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
            genreDisplayName = "Plataformas",
            genreTags = listOf("Platformer", "Velocidade", "Retro"),
            recommendationReason = "Clássico intemporal de plataformas 16-bit com introdução do Tails e Spin Dash.",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "MK-1051-50", region = "EUR", editionNote = "Caixa clássica preta com inglês integral", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Disponível em Sonic Origins (Switch/PS4/PS5/Xbox)",
            estimatedPriceChf = 30.0,
            similarTitles = listOf("Sonic the Hedgehog", "Sonic 3 & Knuckles")
        )
    )
}
