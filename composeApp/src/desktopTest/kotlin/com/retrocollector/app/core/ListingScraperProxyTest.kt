package com.retrocollector.app.core

import com.retrocollector.app.core.data.scraper.ListingScraper
import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.settings.domain.model.ScraperProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListingScraperProxyTest {

    private val scraper = ListingScraper()

    @Test
    fun `buildProxyUrl constructs valid Scrape_do endpoint with render and super parameters`() {
        val targetUrl = "https://www.ricardo.ch/en/a/1318136906"
        val settingsStandard = AppSettings(
            scraperProvider = ScraperProvider.SCRAPE_DO,
            scrapeDoApiKey = "test_token_123",
            scrapeDoSuperProxy = false
        )
        val settingsSuper = AppSettings(
            scraperProvider = ScraperProvider.SCRAPE_DO,
            scrapeDoApiKey = "test_token_123",
            scrapeDoSuperProxy = true
        )
        val settingsWithoutKey = AppSettings(
            scraperProvider = ScraperProvider.SCRAPE_DO,
            scrapeDoApiKey = ""
        )

        val standardUrl = scraper.buildProxyUrl(targetUrl, settingsStandard)
        assertNotNull(standardUrl)
        assertTrue(standardUrl!!.startsWith("https://api.scrape.do?token=test_token_123&url="))
        assertTrue(standardUrl.contains("render=true"))
        assertTrue(!standardUrl.contains("super=true"))

        val superUrl = scraper.buildProxyUrl(targetUrl, settingsSuper)
        assertNotNull(superUrl)
        assertTrue(superUrl!!.startsWith("https://api.scrape.do?token=test_token_123&url="))
        assertTrue(superUrl.contains("render=true"))
        assertTrue(superUrl.contains("&super=true"))

        assertNull(scraper.buildProxyUrl(targetUrl, settingsWithoutKey))
    }

    @Test
    fun `buildProxyUrl constructs valid Custom Proxy endpoints`() {
        val targetUrl = "https://www.ricardo.ch/en/a/mass-effect-3-ps3-1318136906/"
        
        val settingsWithParam = AppSettings(
            scraperProvider = ScraperProvider.CUSTOM_PROXY,
            customScraperProxyUrl = "https://my-scraper.fly.dev/scrape?url="
        )
        val settingsWithoutParam = AppSettings(
            scraperProvider = ScraperProvider.CUSTOM_PROXY,
            customScraperProxyUrl = "https://my-scraper.fly.dev/scrape"
        )
        val settingsEmpty = AppSettings(
            scraperProvider = ScraperProvider.CUSTOM_PROXY,
            customScraperProxyUrl = ""
        )

        val url1 = scraper.buildProxyUrl(targetUrl, settingsWithParam)
        assertNotNull(url1)
        assertTrue(url1!!.startsWith("https://my-scraper.fly.dev/scrape?url=https"))

        val url2 = scraper.buildProxyUrl(targetUrl, settingsWithoutParam)
        assertNotNull(url2)
        assertTrue(url2!!.startsWith("https://my-scraper.fly.dev/scrape?url=https"))

        assertNull(scraper.buildProxyUrl(targetUrl, settingsEmpty))
    }

    @Test
    fun `extractListingId and extractTitleFromUrl parse Ricardo and Tutti URLs`() {
        val shortRicardo = "https://www.ricardo.ch/en/a/1318136906"
        val sluggedRicardo = "https://www.ricardo.ch/en/a/mass-effect-3-ps3-1318136906/"
        val tuttiUrl = "https://www.tutti.ch/de/vi/bern/super-mario-64-55443322"

        assertEquals("1318136906", scraper.extractListingId(shortRicardo))
        assertEquals("1318136906", scraper.extractListingId(sluggedRicardo))
        assertEquals("55443322", scraper.extractListingId(tuttiUrl))

        assertNull(scraper.extractTitleFromUrl(shortRicardo))
        assertEquals("MASS Effect 3 PS3", scraper.extractTitleFromUrl(sluggedRicardo))
        assertEquals("Super Mario 64", scraper.extractTitleFromUrl(tuttiUrl))
    }

    @Test
    fun `isSupportedListing detects Swiss second-hand marketplaces`() {
        assertTrue(scraper.isSupportedListing("https://www.ricardo.ch/en/a/1318136906"))
        assertTrue(scraper.isSupportedListing("https://www.tutti.ch/de/vi/123"))
        assertTrue(scraper.isSupportedListing("https://www.anibis.ch/de/123"))
        assertTrue(scraper.isSupportedListing("https://www.ebay.ch/itm/123"))
    }

    @Test
    fun `extractImageUrlsFromHtml extracts multiple gallery images from Ricardo and Tutti`() {
        val sampleRicardoHtml = """
            <html>
                <head>
                    <meta property="og:image" content="https://img.ricardostatic.ch/images/1111/t_1000x750/front.jpg">
                </head>
                <body>
                    <img src="https://img.ricardostatic.ch/images/2222/t_200x150/plain/images/disc_macro.jpg">
                    <img src="https://img.ricardostatic.ch/images/3333/t_200x150/plain/images/back_cover.jpg">
                </body>
            </html>
        """.trimIndent()

        val images = scraper.extractImageUrlsFromHtml(sampleRicardoHtml, "Ricardo.ch")
        assertEquals(3, images.size)
        assertTrue(images[0].contains("front.jpg"))
        assertTrue(images[1].contains("t_1800x1350"))
        assertTrue(images[2].contains("back_cover.jpg"))
    }
}
