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
    fun `buildProxyUrl constructs valid Scrape_do endpoint with render parameter`() {
        val targetUrl = "https://www.ricardo.ch/en/a/1318136906"
        val settingsWithKey = AppSettings(
            scraperProvider = ScraperProvider.SCRAPE_DO,
            scrapeDoApiKey = "test_token_123"
        )
        val settingsWithoutKey = AppSettings(
            scraperProvider = ScraperProvider.SCRAPE_DO,
            scrapeDoApiKey = ""
        )

        val proxyUrl = scraper.buildProxyUrl(targetUrl, settingsWithKey)
        assertNotNull(proxyUrl)
        assertTrue(proxyUrl!!.startsWith("https://api.scrape.do?token=test_token_123&url="))
        assertTrue(proxyUrl.contains("render=true"))

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
}
