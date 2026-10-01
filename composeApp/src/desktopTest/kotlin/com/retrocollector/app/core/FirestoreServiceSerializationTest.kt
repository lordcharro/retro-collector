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
}
