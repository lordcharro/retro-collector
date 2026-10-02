package com.retrocollector.app.core

import com.retrocollector.app.core.data.firestore.FirestoreDocument
import com.retrocollector.app.core.data.firestore.FirestoreService
import com.retrocollector.app.core.data.firestore.legacy.LegacyFirestoreDocumentParser
import com.retrocollector.app.core.domain.model.*
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FirestoreServiceSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `ConsolePlatformSerializer handles uppercase enum names, lowercase ids, and aliases`() {
        // Uppercase enum name
        val parsedUpper = json.decodeFromString<ConsolePlatform>("\"GAMECUBE\"")
        assertEquals(ConsolePlatform.GAMECUBE, parsedUpper)

        // Lowercase id
        val parsedLower = json.decodeFromString<ConsolePlatform>("\"gamecube\"")
        assertEquals(ConsolePlatform.GAMECUBE, parsedLower)

        // Lowercase alias
        val parsedAlias = json.decodeFromString<ConsolePlatform>("\"ps3\"")
        assertEquals(ConsolePlatform.PS3, parsedAlias)

        // Unknown fallback
        val parsedUnknown = json.decodeFromString<ConsolePlatform>("\"unknown_console_xyz\"")
        assertEquals(ConsolePlatform.GAMECUBE, parsedUnknown)
    }

    @Test
    fun `CollectionStatusSerializer and LanguageStatusSerializer handle mixed cases`() {
        assertEquals(CollectionStatus.WISHLIST, json.decodeFromString<CollectionStatus>("\"wishlist\""))
        assertEquals(CollectionStatus.OWNED, json.decodeFromString<CollectionStatus>("\"Owned\""))
        assertEquals(CollectionStatus.PASS, json.decodeFromString<CollectionStatus>("\"PASS\""))

        assertEquals(LanguageStatus.FULL_ENGLISH, json.decodeFromString<LanguageStatus>("\"full_english\""))
        assertEquals(LanguageStatus.GERMAN_ONLY, json.decodeFromString<LanguageStatus>("\"GERMAN_ONLY\""))
        assertEquals(LanguageStatus.UNVERIFIED, json.decodeFromString<LanguageStatus>("\"unknown\""))
    }

    @Test
    fun `GameItem deserialization succeeds with real Firestore payloads from mobile`() {
        val payload1 = """{"id":"disc_grim_fandango_switch","title":"Grim Fandango Remastered","franchiseName":"Grim Fandango","platform":"SWITCH","releaseYear":"2018","productCode":"HAC-P-AGFRA","askingPriceChf":35.0,"targetPriceChf":29.75,"languageStatus":"FULL_ENGLISH","safeSkus":[{"code":"HAC-P-AGFRA","region":"EUR","editionNote":"Physical and digital release with English support","isSafe":true}],"collectorVerdict":"Tim Schafer's Day of the Dead folklore masterpiece with orchestral score.","personalNotes":"Port/Remaster: iam8bit / retail physical release for Nintendo Switch\nGenres: Point & Click, Noir, Comedy","updatedAt":1790858957857}"""
        val game1 = json.decodeFromString<GameItem>(payload1)
        assertEquals("disc_grim_fandango_switch", game1.id)
        assertEquals("Grim Fandango Remastered", game1.title)
        assertEquals(ConsolePlatform.SWITCH, game1.platform)

        val payload2 = """{"id":"game_tombraidercombatstrikeedition_ps3","title":"Tomb Raider (Combat Strike Edition)","franchiseName":"Tomb Raider","platform":"PS3","releaseYear":"2013","productCode":"BLES-01780","barcode":"5021290056152","spottedLocation":"Ricardo.ch","targetPriceChf":10.0,"languageStatus":"FULL_ENGLISH","languageAudio":["English","French","German","Spanish","Italian"],"languageSubtitles":["English","French","German","Spanish","Italian"],"safeSkus":[{"code":"BLES-01780","region":"EUR / CH","editionNote":"Standard / Combat Strike Edition. Fully multilingual containing English and French audio.","isSafe":true}],"marketRadar":{"medianPriceChf":10.0,"historicalMinChf":5.0,"historicalMaxChf":15.0},"censorshipWarning":"The game is fully uncensored in all PAL regions. Note that the Combat Strike DLC voucher code included inside is likely expired.","collectorVerdict":"PASS. This is a very common title. The Combat Strike packaging does not add premium value as the DLC code is likely expired. Target a maximum price of CHF 8.00.","updatedAt":1790873046246}"""
        val game2 = json.decodeFromString<GameItem>(payload2)
        assertEquals("game_tombraidercombatstrikeedition_ps3", game2.id)
        assertEquals("Tomb Raider (Combat Strike Edition)", game2.title)
        assertEquals(ConsolePlatform.PS3, game2.platform)
        assertEquals(5, game2.languageAudio.size)
        assertEquals(10.0, game2.marketRadar?.medianPriceChf)
    }

    @Test
    fun `GameItem deserialization succeeds when platform is lowercase id`() {
        val payload = """{"id":"game_test","title":"Test Game","platform":"xbox_360","updatedAt":12345}"""
        val game = json.decodeFromString<GameItem>(payload)
        assertEquals("game_test", game.id)
        assertEquals(ConsolePlatform.XBOX_360, game.platform)
    }

    @Test
    fun `GameOffer and GameCondition serialization and deserialization`() {
        val offer = GameOffer(
            id = "offer_1",
            source = "Ricardo.ch",
            priceChf = 35.0,
            shippingChf = 1.50,
            listingUrl = "https://www.ricardo.ch/de/a/123",
            condition = GameCondition.CIB,
            sellerOrLocation = "Zurich",
            notes = "Ends Sunday"
        )
        val jsonStr = json.encodeToString(GameOffer.serializer(), offer)
        val decoded = json.decodeFromString<GameOffer>(jsonStr)

        assertEquals("offer_1", decoded.id)
        assertEquals("Ricardo.ch", decoded.source)
        assertEquals(35.0, decoded.priceChf)
        assertEquals(1.50, decoded.shippingChf)
        assertEquals(36.50, decoded.totalLandedPriceChf)
        assertEquals(GameCondition.CIB, decoded.condition)

        val game = GameItem(
            id = "game_zelda",
            title = "The Legend of Zelda",
            platform = ConsolePlatform.GAMECUBE,
            offers = listOf(
                offer,
                GameOffer(id = "offer_2", source = "Anibis.ch", priceChf = 28.0, condition = GameCondition.BOXED)
            ),
            acquiredCondition = GameCondition.CIB
        )
        val gameJson = json.encodeToString(GameItem.serializer(), game)
        val decodedGame = json.decodeFromString<GameItem>(gameJson)

        assertEquals(2, decodedGame.offers.size)
        assertEquals(2, decodedGame.activeOffers.size)
        assertEquals("offer_2", decodedGame.bestOffer?.id)
        assertEquals(28.0, decodedGame.bestOffer?.totalLandedPriceChf)
        assertEquals(GameCondition.CIB, decodedGame.acquiredCondition)
    }

    @Test
    fun `FirestoreService buildGameDocumentBody constructs 100 percent structured native types without data blob`() {
        val service = FirestoreService()
        val offer1 = GameOffer(
            id = "off_1",
            source = "Ricardo.ch",
            priceChf = 40.0,
            shippingChf = 2.0,
            listingUrl = "https://www.ricardo.ch/test",
            condition = GameCondition.CIB,
            sellerOrLocation = "Bern",
            notes = "Mint",
            createdAt = 1000L
        )
        val simGame = DiscoveredGameItem(
            id = "disc_grim_fandango",
            title = "Grim Fandango Remastered",
            franchiseName = "Grim Fandango",
            platform = ConsolePlatform.SWITCH,
            releaseYear = "2018",
            genreDisplayName = "Point & Click",
            genreTags = listOf("Point & Click", "Adventure"),
            recommendationReason = "Masterpiece",
            languageStatus = LanguageStatus.FULL_ENGLISH,
            safeSkus = listOf(SkuInfo(code = "HAC-P-AGFRA", region = "EUR", editionNote = "Standard", isSafe = true)),
            hasModernPortOrRemaster = true,
            modernPortDetails = "Physical release on Switch",
            estimatedPriceChf = 35.0,
            similarTitles = listOf("Monkey Island")
        )
        val game = GameItem(
            id = "disc_monkey_island_2_ps3",
            title = "Monkey Island 2: LeChuck's Revenge (Special Edition)",
            franchiseName = "Monkey Island",
            platform = ConsolePlatform.PS3,
            releaseYear = "2010",
            productCode = "BLES-01124",
            barcode = "5021290056152",
            spottedLocation = "Ricardo.ch",
            askingPriceChf = 32.0,
            targetPriceChf = 28.0,
            paidPriceChf = 30.0,
            languageStatus = LanguageStatus.FULL_ENGLISH,
            languageAudio = listOf("English", "German"),
            languageSubtitles = listOf("English", "German", "French"),
            safeSkus = listOf(SkuInfo(code = "BLES-01124", region = "EUR", editionNote = "Multilingual", isSafe = true)),
            riskySkus = listOf(SkuInfo(code = "BLUS-01124", region = "USA", editionNote = "NTSC", isSafe = false)),
            marketRadar = SwissMarketRadar(spottedPriceChf = 32.0, medianPriceChf = 30.0, historicalMinChf = 25.0, historicalMaxChf = 38.0, trend = "Up"),
            censorshipWarning = "Uncensored",
            collectorVerdict = "Classic must-play",
            collectionStatus = CollectionStatus.WISHLIST,
            enrichmentStatus = EnrichmentStatus.COMPLETE,
            personalNotes = "Top priority",
            listingUrl = "https://www.ricardo.ch/mi2",
            offers = listOf(offer1),
            similarGames = listOf(simGame),
            acquiredCondition = GameCondition.CIB,
            updatedAt = 5000L
        )

        val docBody = service.buildGameDocumentBody(game)
        val fields = docBody["fields"]!!.jsonObject

        // Verify that 'data' string blob is NOT present
        assertFalse(fields.containsKey("data"))

        // Verify top-level structured fields
        assertEquals("disc_monkey_island_2_ps3", fields["id"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("Monkey Island 2: LeChuck's Revenge (Special Edition)", fields["title"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("Monkey Island", fields["franchiseName"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("ps3", fields["platform"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("2010", fields["releaseYear"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("BLES-01124", fields["productCode"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("5021290056152", fields["barcode"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("Ricardo.ch", fields["spottedLocation"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("32.0", fields["askingPriceChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertEquals("28.0", fields["targetPriceChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertEquals("30.0", fields["paidPriceChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertEquals("FULL_ENGLISH", fields["languageStatus"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("WISHLIST", fields["status"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("COMPLETE", fields["enrichmentStatus"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("CIB", fields["acquiredCondition"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)

        // Verify structured audio and subtitles
        val audioArray = fields["languageAudio"]!!.jsonObject["arrayValue"]!!.jsonObject["values"]!!.jsonArray
        assertEquals(2, audioArray.size)
        assertEquals("English", audioArray[0].jsonObject["stringValue"]!!.jsonPrimitive.content)

        // Verify structured safeSkus & riskySkus
        val safeSkusArray = fields["safeSkus"]!!.jsonObject["arrayValue"]!!.jsonObject["values"]!!.jsonArray
        assertEquals(1, safeSkusArray.size)
        val safeSkuMap = safeSkusArray[0].jsonObject["mapValue"]!!.jsonObject["fields"]!!.jsonObject
        assertEquals("BLES-01124", safeSkuMap["code"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertTrue(safeSkuMap["isSafe"]!!.jsonObject["booleanValue"]!!.jsonPrimitive.boolean)

        // Verify structured marketRadar map
        val radarMap = fields["marketRadar"]!!.jsonObject["mapValue"]!!.jsonObject["fields"]!!.jsonObject
        assertEquals("30.0", radarMap["medianPriceChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertEquals("Up", radarMap["trend"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)

        // Verify structured offers
        val offersArray = fields["offers"]!!.jsonObject["arrayValue"]!!.jsonObject["values"]!!.jsonArray
        assertEquals(1, offersArray.size)
        val offerMap = offersArray[0].jsonObject["mapValue"]!!.jsonObject["fields"]!!.jsonObject
        assertEquals("off_1", offerMap["id"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("Ricardo.ch", offerMap["source"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)

        // Verify structured similarGames array of maps
        val similarArray = fields["similarGames"]!!.jsonObject["arrayValue"]!!.jsonObject["values"]!!.jsonArray
        assertEquals(1, similarArray.size)
        val simMap = similarArray[0].jsonObject["mapValue"]!!.jsonObject["fields"]!!.jsonObject
        assertEquals("disc_grim_fandango", simMap["id"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("Grim Fandango Remastered", simMap["title"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("switch", simMap["platform"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("Point & Click", simMap["genreDisplayName"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("35.0", simMap["estimatedPriceChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertTrue(simMap["hasModernPortOrRemaster"]!!.jsonObject["booleanValue"]!!.jsonPrimitive.boolean)
    }

    @Test
    fun `FirestoreService parseGameFromDocFields roundtrips fully structured document`() {
        val service = FirestoreService()
        val originalGame = GameItem(
            id = "disc_monkey_island_2_ps3",
            title = "Monkey Island 2: LeChuck's Revenge (Special Edition)",
            franchiseName = "Monkey Island",
            platform = ConsolePlatform.PS3,
            releaseYear = "2010",
            productCode = "BLES-01124",
            barcode = "5021290056152",
            spottedLocation = "Ricardo.ch",
            askingPriceChf = 32.0,
            targetPriceChf = 28.0,
            paidPriceChf = 30.0,
            languageStatus = LanguageStatus.FULL_ENGLISH,
            languageAudio = listOf("English", "German"),
            languageSubtitles = listOf("English", "French"),
            safeSkus = listOf(SkuInfo(code = "BLES-01124", region = "EUR", editionNote = "Multilingual", isSafe = true)),
            riskySkus = listOf(SkuInfo(code = "BLUS-01124", region = "USA", editionNote = "NTSC", isSafe = false)),
            marketRadar = SwissMarketRadar(spottedPriceChf = 32.0, medianPriceChf = 30.0, historicalMinChf = 25.0, historicalMaxChf = 38.0, trend = "Stable"),
            censorshipWarning = "Uncut",
            collectorVerdict = "Masterpiece",
            collectionStatus = CollectionStatus.WISHLIST,
            enrichmentStatus = EnrichmentStatus.COMPLETE,
            personalNotes = "Great game",
            listingUrl = "https://ricardo.ch/mi2",
            offers = listOf(
                GameOffer(id = "off_1", source = "Ricardo.ch", priceChf = 32.0, condition = GameCondition.CIB)
            ),
            similarGames = listOf(
                DiscoveredGameItem(
                    id = "disc_grim",
                    title = "Grim Fandango",
                    platform = ConsolePlatform.SWITCH,
                    genreDisplayName = "Adventure",
                    estimatedPriceChf = 35.0
                )
            ),
            acquiredCondition = GameCondition.CIB,
            updatedAt = 9999L
        )

        val docBody = service.buildGameDocumentBody(originalGame)
        val fields = docBody["fields"]!!.jsonObject.mapValues { it.value.jsonObject }
        val doc = FirestoreDocument(name = "projects/p/databases/(default)/documents/games/disc_monkey_island_2_ps3", fields = fields)

        val parsed = service.parseGameFromDocFields(doc)
        assertNotNull(parsed)
        assertEquals(originalGame.id, parsed!!.id)
        assertEquals(originalGame.title, parsed.title)
        assertEquals(originalGame.franchiseName, parsed.franchiseName)
        assertEquals(originalGame.platform, parsed.platform)
        assertEquals(originalGame.releaseYear, parsed.releaseYear)
        assertEquals(originalGame.productCode, parsed.productCode)
        assertEquals(originalGame.barcode, parsed.barcode)
        assertEquals(originalGame.spottedLocation, parsed.spottedLocation)
        assertEquals(originalGame.askingPriceChf, parsed.askingPriceChf)
        assertEquals(originalGame.targetPriceChf, parsed.targetPriceChf)
        assertEquals(originalGame.paidPriceChf, parsed.paidPriceChf)
        assertEquals(originalGame.languageStatus, parsed.languageStatus)
        assertEquals(originalGame.languageAudio, parsed.languageAudio)
        assertEquals(originalGame.languageSubtitles, parsed.languageSubtitles)
        assertEquals(originalGame.safeSkus, parsed.safeSkus)
        assertEquals(originalGame.riskySkus, parsed.riskySkus)
        assertEquals(originalGame.marketRadar, parsed.marketRadar)
        assertEquals(originalGame.censorshipWarning, parsed.censorshipWarning)
        assertEquals(originalGame.collectorVerdict, parsed.collectorVerdict)
        assertEquals(originalGame.collectionStatus, parsed.collectionStatus)
        assertEquals(originalGame.enrichmentStatus, parsed.enrichmentStatus)
        assertEquals(originalGame.personalNotes, parsed.personalNotes)
        assertEquals(originalGame.listingUrl, parsed.listingUrl)
        assertEquals(1, parsed.offers.size)
        assertEquals("off_1", parsed.offers[0].id)
        assertEquals(1, parsed.similarGames.size)
        assertEquals("disc_grim", parsed.similarGames[0].id)
        assertEquals(GameCondition.CIB, parsed.acquiredCondition)
        assertEquals(9999L, parsed.updatedAt)
    }

    @Test
    fun `LegacyFirestoreDocumentParser parses legacy documents with data JSON string blob`() {
        val legacyJsonString = """{"id":"disc_monkey_island_2_ps3","title":"Monkey Island 2: LeChuck's Revenge (Special Edition)","franchiseName":"Monkey Island","platform":"PS3","releaseYear":"2010","productCode":"BLES-01124","spottedLocation":"Ricardo.ch","askingPriceChf":32.0,"languageStatus":"FULL_ENGLISH","collectionStatus":"WISHLIST","updatedAt":1790882903413}"""
        // In the existing Firestore database, legacy docs have 'data' plus partial top-level fields like 'title' and 'platform'
        val legacyDoc = FirestoreDocument(
            name = "projects/p/databases/(default)/documents/games/disc_monkey_island_2_ps3",
            fields = mapOf(
                "data" to buildJsonObject { put("stringValue", legacyJsonString) },
                "title" to buildJsonObject { put("stringValue", "Monkey Island 2: LeChuck's Revenge (Special Edition)") },
                "platform" to buildJsonObject { put("stringValue", "ps3") },
                "status" to buildJsonObject { put("stringValue", "WISHLIST") },
                "updatedAt" to buildJsonObject { put("integerValue", "1790882903413") },
                "askingPriceChf" to buildJsonObject { put("integerValue", "32") }
            )
        )

        val service = FirestoreService()
        val parsed = service.parseGameFromDocFields(legacyDoc)
        assertNotNull(parsed)
        assertEquals("disc_monkey_island_2_ps3", parsed!!.id)
        assertEquals("Monkey Island 2: LeChuck's Revenge (Special Edition)", parsed.title)
        assertEquals(ConsolePlatform.PS3, parsed.platform)
        assertEquals(CollectionStatus.WISHLIST, parsed.collectionStatus)
        assertEquals("2010", parsed.releaseYear)
        assertEquals("BLES-01124", parsed.productCode)
        assertEquals("Ricardo.ch", parsed.spottedLocation)
        assertEquals(LanguageStatus.FULL_ENGLISH, parsed.languageStatus)
        assertEquals(32.0, parsed.askingPriceChf)
        assertEquals(1790882903413L, parsed.updatedAt)

        // Also test direct LegacyFirestoreDocumentParser call
        val directParsed = LegacyFirestoreDocumentParser.parseLegacyGameItem(legacyDoc, json)
        assertNotNull(directParsed)
        assertEquals("disc_monkey_island_2_ps3", directParsed!!.id)
        assertEquals("2010", directParsed.releaseYear)
        assertEquals(LanguageStatus.FULL_ENGLISH, directParsed.languageStatus)
    }

    @Test
    fun `LegacyFirestoreDocumentParser parses legacy chat messages with data JSON string`() {
        val legacyChatJson = """{"id":"msg_1","contextId":"game_1","sender":"GEMINI","text":"Hello Collector","timestamp":10000}"""
        val doc = FirestoreDocument(
            name = "projects/p/databases/(default)/documents/chat_threads/msg_1",
            fields = mapOf(
                "data" to buildJsonObject { put("stringValue", legacyChatJson) }
            )
        )

        val parsed = LegacyFirestoreDocumentParser.parseLegacyChatMessage(doc, json)
        assertNotNull(parsed)
        assertEquals("msg_1", parsed!!.id)
        assertEquals("game_1", parsed.contextId)
        assertEquals(MessageSender.GEMINI, parsed.sender)
        assertEquals("Hello Collector", parsed.text)
        assertEquals(10000L, parsed.timestamp)
    }
}
