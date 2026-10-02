package com.retrocollector.app.core

import com.retrocollector.app.core.data.firestore.FirestoreDocument
import com.retrocollector.app.core.data.firestore.FirestoreListResponse
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

        // GameItem with offers
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
    fun `FirestoreService buildGameDocumentBody constructs structured offers and root fields`() {
        val service = com.retrocollector.app.core.data.firestore.FirestoreService()
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
        val offer2 = GameOffer(
            id = "off_2",
            source = "Anibis.ch",
            priceChf = 30.0,
            condition = GameCondition.BOXED,
            createdAt = 2000L
        )
        val game = GameItem(
            id = "game_metroid",
            title = "Metroid Prime",
            platform = ConsolePlatform.GAMECUBE,
            collectionStatus = CollectionStatus.WISHLIST,
            offers = listOf(offer1, offer2),
            acquiredCondition = GameCondition.CIB,
            updatedAt = 3000L
        )

        val docBody = service.buildGameDocumentBody(game)
        val fields = docBody["fields"]!!.jsonObject

        assertEquals("Metroid Prime", fields["title"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("gamecube", fields["platform"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("WISHLIST", fields["status"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("2", fields["offersCount"]!!.jsonObject["integerValue"]!!.jsonPrimitive.content)
        assertEquals("Anibis.ch", fields["bestOfferStore"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("30.0", fields["bestOfferPriceChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertEquals("CIB", fields["acquiredCondition"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)

        val offersArray = fields["offers"]!!.jsonObject["arrayValue"]!!.jsonObject["values"]!!.jsonArray
        assertEquals(2, offersArray.size)

        val firstOfferFields = offersArray[0].jsonObject["mapValue"]!!.jsonObject["fields"]!!.jsonObject
        assertEquals("off_1", firstOfferFields["id"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("Ricardo.ch", firstOfferFields["source"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("40.0", firstOfferFields["priceChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertEquals("2.0", firstOfferFields["shippingChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertEquals("42.0", firstOfferFields["totalLandedPriceChf"]!!.jsonObject["doubleValue"]!!.jsonPrimitive.content)
        assertEquals("CIB", firstOfferFields["condition"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
    }

    @Test
    fun `FirestoreService parseGameFromDocFields parses structured offers array and acquiredCondition`() {
        val service = com.retrocollector.app.core.data.firestore.FirestoreService()
        val doc = FirestoreDocument(
            name = "projects/test/databases/(default)/documents/games/game_zelda",
            fields = mapOf(
                "id" to buildJsonObject { put("stringValue", "game_zelda") },
                "title" to buildJsonObject { put("stringValue", "Zelda Wind Waker") },
                "platform" to buildJsonObject { put("stringValue", "gamecube") },
                "status" to buildJsonObject { put("stringValue", "OWNED") },
                "acquiredCondition" to buildJsonObject { put("stringValue", "CIB") },
                "offers" to buildJsonObject {
                    put("arrayValue", buildJsonObject {
                        put("values", buildJsonArray {
                            add(buildJsonObject {
                                put("mapValue", buildJsonObject {
                                    put("fields", buildJsonObject {
                                        put("id", buildJsonObject { put("stringValue", "off_1") })
                                        put("source", buildJsonObject { put("stringValue", "Tutti.ch") })
                                        put("priceChf", buildJsonObject { put("doubleValue", 35.0) })
                                        put("shippingChf", buildJsonObject { put("doubleValue", 1.50) })
                                        put("condition", buildJsonObject { put("stringValue", "CIB") })
                                        put("sellerOrLocation", buildJsonObject { put("stringValue", "Zurich") })
                                        put("isPurchased", buildJsonObject { put("booleanValue", true) })
                                        put("createdAt", buildJsonObject { put("integerValue", "1700000000") })
                                    })
                                })
                            })
                        })
                    })
                }
            )
        )

        val parsedGame = service.parseGameFromDocFields(doc)
        assertNotNull(parsedGame)
        assertEquals("game_zelda", parsedGame!!.id)
        assertEquals("Zelda Wind Waker", parsedGame.title)
        assertEquals(CollectionStatus.OWNED, parsedGame.collectionStatus)
        assertEquals(GameCondition.CIB, parsedGame.acquiredCondition)
        assertEquals(1, parsedGame.offers.size)

        val offer = parsedGame.offers.first()
        assertEquals("off_1", offer.id)
        assertEquals("Tutti.ch", offer.source)
        assertEquals(35.0, offer.priceChf)
        assertEquals(1.50, offer.shippingChf)
        assertEquals(36.50, offer.totalLandedPriceChf)
        assertEquals(GameCondition.CIB, offer.condition)
        assertEquals("Zurich", offer.sellerOrLocation)
        assertTrue(offer.isPurchased)
    }
}
