package com.retrocollector.app.core.data.scraper

import com.retrocollector.app.settings.domain.model.AppSettings
import com.retrocollector.app.settings.domain.model.ScraperProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.isSuccess
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ScrapedListing(
    val url: String,
    val title: String,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val estimatedPriceChf: Double? = null,
    val sourcePlatform: String, // "Ricardo.ch", "Tutti.ch", "Other"
    val listingId: String? = null,
    val isTitleExtracted: Boolean = true
)

class ListingScraper(
    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 20000
        }
    }
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun isSupportedListing(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("ricardo.ch") || lower.contains("tutti.ch") || lower.contains("anibis.ch") || lower.contains("ebay")
    }

    fun buildProxyUrl(targetUrl: String, settings: AppSettings): String? {
        val encoded = targetUrl.encodeURLQueryComponent()
        return when (settings.scraperProvider) {
            ScraperProvider.SCRAPE_DO -> {
                val token = settings.scrapeDoApiKey.trim()
                if (token.isBlank()) null else {
                    val superParam = if (settings.scrapeDoSuperProxy) "&super=true" else ""
                    "https://api.scrape.do?token=$token&url=$encoded&render=true$superParam"
                }
            }
            ScraperProvider.CUSTOM_PROXY -> {
                val base = settings.customScraperProxyUrl.trim()
                if (base.isBlank()) null
                else if (base.endsWith("=") || base.endsWith("/")) "$base$encoded"
                else if (base.contains("?")) "$base&url=$encoded"
                else "$base?url=$encoded"
            }
        }
    }

    suspend fun fetchListing(url: String, settings: AppSettings): Result<ScrapedListing> {
        val source = resolveSourcePlatform(url)
        val listingId = extractListingId(url)
        val fallbackTitle = extractTitleFromUrl(url)

        if (!settings.isScraperEnabled) {
            return Result.success(
                ScrapedListing(
                    url = url,
                    title = fallbackTitle ?: (if (listingId != null) "$source Listing #$listingId" else "$source Link"),
                    description = "Marketplace scraper is disabled in Settings.",
                    sourcePlatform = source,
                    listingId = listingId,
                    isTitleExtracted = fallbackTitle != null
                )
            )
        }

        val proxyUrl = buildProxyUrl(url, settings)
        if (proxyUrl == null) {
            val configNotice = when (settings.scraperProvider) {
                ScraperProvider.SCRAPE_DO -> "Scrape.do API Token is not configured. Access Settings to add your token."
                ScraperProvider.CUSTOM_PROXY -> "Custom Proxy URL is not configured. Access Settings to add your endpoint."
            }
            return Result.success(
                ScrapedListing(
                    url = url,
                    title = fallbackTitle ?: (if (listingId != null) "$source Listing #$listingId" else "$source Link"),
                    description = "$configNotice (Listing: $url)",
                    sourcePlatform = source,
                    listingId = listingId,
                    isTitleExtracted = fallbackTitle != null
                )
            )
        }

        return try {
            val response = client.get(proxyUrl)
            if (!response.status.isSuccess()) {
                val fallback = fallbackTitle ?: (if (listingId != null) "$source Listing #$listingId" else "$source Link")
                return Result.success(
                    ScrapedListing(
                        url = url,
                        title = fallback,
                        description = "Proxy returned HTTP ${response.status.value}. Using link reference.",
                        sourcePlatform = source,
                        listingId = listingId,
                        isTitleExtracted = fallbackTitle != null
                    )
                )
            }

            val bodyText = response.bodyAsText()

            // Check if response is a JSON payload from a custom microservice
            if (bodyText.trimStart().startsWith("{")) {
                parseJsonListing(bodyText, url, source, listingId, fallbackTitle)
            } else {
                parseHtmlListing(bodyText, url, source, listingId, fallbackTitle)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            println("Scraper proxy failed for $url: ${e.message}")
            val finalTitle = fallbackTitle ?: (if (listingId != null) "$source Listing #$listingId" else "$source Link")
            Result.success(
                ScrapedListing(
                    url = url,
                    title = finalTitle,
                    description = "Shared listing: $url (Proxy error: ${e.message})",
                    sourcePlatform = source,
                    listingId = listingId,
                    isTitleExtracted = fallbackTitle != null
                )
            )
        }
    }

    private fun parseJsonListing(
        jsonString: String,
        originalUrl: String,
        source: String,
        listingId: String?,
        fallbackTitle: String?
    ): Result<ScrapedListing> {
        return try {
            val json = jsonParser.decodeFromString<JsonObject>(jsonString)
            val title = json["title"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                ?: fallbackTitle
                ?: (if (listingId != null) "$source Listing #$listingId" else "$source Listing")
            val description = json["description"]?.jsonPrimitive?.content ?: ""
            val singleImg = json["imageUrl"]?.jsonPrimitive?.content
            val multiImgs = json["imageUrls"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content }
            val imageUrls = multiImgs ?: listOfNotNull(singleImg)
            val price = json["estimatedPriceChf"]?.jsonPrimitive?.doubleOrNull
                ?: json["priceChf"]?.jsonPrimitive?.doubleOrNull

            Result.success(
                ScrapedListing(
                    url = originalUrl,
                    title = cleanText(title),
                    description = cleanText(description),
                    imageUrls = imageUrls,
                    estimatedPriceChf = price,
                    sourcePlatform = source,
                    listingId = listingId,
                    isTitleExtracted = fallbackTitle != null || !title.startsWith("$source Listing")
                )
            )
        } catch (_: Exception) {
            parseHtmlListing(jsonString, originalUrl, source, listingId, fallbackTitle)
        }
    }

    private fun parseHtmlListing(
        html: String,
        originalUrl: String,
        source: String,
        listingId: String?,
        fallbackTitle: String?
    ): Result<ScrapedListing> {
        val parsedTitle = extractMetaTag(html, "og:title")
            ?: extractMetaTag(html, "twitter:title")
            ?: extractTagContent(html, "title")

        val hasValidParsedTitle = !parsedTitle.isNullOrBlank() && !isCaptchaPage(parsedTitle)
        val title = when {
            hasValidParsedTitle -> cleanText(parsedTitle)
            fallbackTitle != null -> fallbackTitle
            listingId != null -> "$source Listing #$listingId"
            else -> "$source Listing"
        }

        val parsedDesc = extractMetaTag(html, "og:description")
            ?: extractMetaTag(html, "description")
            ?: ""

        val imageUrls = extractImageUrlsFromHtml(html, source)

        val price = extractMetaTag(html, "product:price:amount")?.toDoubleOrNull()
            ?: extractPriceFromHtml(html)

        return Result.success(
            ScrapedListing(
                url = originalUrl,
                title = title,
                description = cleanText(parsedDesc),
                imageUrls = imageUrls,
                estimatedPriceChf = price,
                sourcePlatform = source,
                listingId = listingId,
                isTitleExtracted = hasValidParsedTitle || fallbackTitle != null
            )
        )
    }

    fun extractImageUrlsFromHtml(html: String, source: String): List<String> {
        val images = LinkedHashSet<String>()

        // 1. Meta og:image and twitter:image
        extractMetaTag(html, "og:image")?.let { if (it.isNotBlank()) images.add(it) }
        extractMetaTag(html, "twitter:image")?.let { if (it.isNotBlank()) images.add(it) }

        // 2. Marketplace specific and schema images
        extractRicardoImages(html, source, images)
        extractTuttiImages(html, source, images)
        extractJsonLdImages(html, images)

        return images.filter { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://")) }.take(5)
    }

    private fun extractRicardoImages(html: String, source: String, destination: MutableSet<String>) {
        if (!source.contains("ricardo", ignoreCase = true) && !html.contains("ricardostatic.ch", ignoreCase = true)) return
        val ricardoRegex = Regex("""https://img\.ricardostatic\.ch/[^\s"'<>,]+""")
        ricardoRegex.findAll(html).forEach { match ->
            val rawUrl = match.value
            if (isAllowedRicardoImage(rawUrl)) {
                destination.add(normalizeRicardoImageUrl(rawUrl))
            }
        }
    }

    private fun isAllowedRicardoImage(url: String): Boolean =
        !url.contains("avatar") && !url.contains("logo") && !url.contains("badge")

    private fun normalizeRicardoImageUrl(url: String): String =
        if (url.contains("t_") && url.contains("/plain/")) {
            url.replace(Regex("""t_\d+x\d+"""), "t_1800x1350")
        } else url

    private fun extractTuttiImages(html: String, source: String, destination: MutableSet<String>) {
        if (!source.contains("tutti", ignoreCase = true) && !html.contains("tutti.ch", ignoreCase = true)) return
        val tuttiRegex = Regex("""https://c\.tutti\.ch/images/[^\s"'<>,]+""")
        tuttiRegex.findAll(html).forEach { match ->
            destination.add(match.value)
        }
    }

    private fun extractJsonLdImages(html: String, destination: MutableSet<String>) {
        val jsonLdRegex = Regex(""""image"\s*:\s*(\[[^\]]+\]|"[^"]+")""")
        jsonLdRegex.findAll(html).forEach { match ->
            val content = match.groupValues[1]
            if (content.startsWith("[")) {
                val urlRegex = Regex("""https?://[^\s"',]+""")
                urlRegex.findAll(content).forEach { u -> destination.add(u.value) }
            } else {
                val cleanUrl = content.trim('"')
                if (cleanUrl.startsWith("http")) destination.add(cleanUrl)
            }
        }
    }

    fun extractListingId(url: String): String? {
        val lower = url.lowercase()
        if (lower.contains("ricardo.ch")) {
            val segment = url.substringAfter("/a/", "").substringBefore("/").substringBefore("?")
            if (segment.isNotBlank() && segment.all { it.isDigit() }) return segment
            val match = Regex("""-(\d+)$""").find(segment)
            if (match != null) return match.groupValues[1]
        } else if (lower.contains("tutti.ch")) {
            val segment = url.trimEnd('/').substringAfterLast('/')
            val matchPrefix = Regex("""^(\d+)-""").find(segment)
            if (matchPrefix != null) return matchPrefix.groupValues[1]
            val matchSuffix = Regex("""-(\d+)$""").find(segment)
            if (matchSuffix != null) return matchSuffix.groupValues[1]
        }
        return null
    }

    fun extractTitleFromUrl(url: String): String? {
        val lower = url.lowercase()
        if (lower.contains("ricardo.ch")) {
            val segment = url.substringAfter("/a/", "").substringBefore("/").substringBefore("?")
            if (segment.isNotBlank() && !segment.all { it.isDigit() }) {
                val cleanSlug = segment.replace(Regex("""-\d+$"""), "")
                if (cleanSlug.isBlank() || cleanSlug.all { it.isDigit() }) return null
                val words = cleanSlug.split("-").filter { it.isNotBlank() }
                if (words.isNotEmpty()) {
                    return words.joinToString(" ") { word ->
                        if (word.length <= 4) word.uppercase() else word.replaceFirstChar { it.uppercase() }
                    }
                }
            }
        } else if (lower.contains("tutti.ch")) {
            val segment = url.trimEnd('/').substringAfterLast('/')
            if (segment.isNotBlank() && !segment.all { it.isDigit() }) {
                val cleanSlug = segment.replace(Regex("""^\d+-"""), "").replace(Regex("""-\d+$"""), "")
                if (cleanSlug.isBlank() || cleanSlug.all { it.isDigit() }) return null
                val words = cleanSlug.split("-").filter { it.isNotBlank() }
                if (words.isNotEmpty()) {
                    return words.joinToString(" ") { word ->
                        if (word.length <= 4) word.uppercase() else word.replaceFirstChar { it.uppercase() }
                    }
                }
            }
        }
        return null
    }

    suspend fun fetchImageAsBase64(imageUrl: String): String? {
        if (imageUrl.isBlank()) return null
        return try {
            val response = client.get(imageUrl) {
                header(HttpHeaders.UserAgent, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
            }
            if (response.status.isSuccess()) {
                val bytes = response.body<ByteArray>()
                @OptIn(ExperimentalEncodingApi::class)
                val base64 = Base64.Default.encode(bytes)
                val mime = response.contentType()?.let { "${it.contentType}/${it.contentSubtype}" } ?: "image/jpeg"
                "data:$mime;base64,$base64"
            } else null
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            println("Failed to fetch image $imageUrl: ${e.message}")
            null
        }
    }

    private fun resolveSourcePlatform(url: String): String {
        return when {
            url.contains("ricardo.ch", ignoreCase = true) -> "Ricardo.ch"
            url.contains("tutti.ch", ignoreCase = true) -> "Tutti.ch"
            url.contains("anibis.ch", ignoreCase = true) -> "Anibis.ch"
            url.contains("ebay", ignoreCase = true) -> "eBay"
            else -> "Web"
        }
    }

    private fun isCaptchaPage(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("challenge-running") ||
            lower.contains("cf-turnstile") ||
            lower.contains("datadome") ||
            lower.contains("ricardo captcha block") ||
            lower.contains("attention required") ||
            lower.contains("just a moment...") ||
            lower.contains("security check") ||
            lower.contains("captcha")
    }

    private fun extractMetaTag(html: String, property: String): String? {
        val regex = Regex("""<meta\s+(?:property|name)=["']$property["']\s+content=["'](.*?)["']""", RegexOption.IGNORE_CASE)
        val match = regex.find(html)
        if (match != null) return match.groupValues[1]

        val reversedRegex = Regex("""<meta\s+content=["'](.*?)["']\s+(?:property|name)=["']$property["']""", RegexOption.IGNORE_CASE)
        val reversedMatch = reversedRegex.find(html)
        return reversedMatch?.groupValues?.get(1)
    }

    private fun extractTagContent(html: String, tag: String): String? {
        val regex = Regex("""<$tag[^>]*>([\s\S]*?)</$tag>""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.get(1)
    }

    private fun extractPriceFromHtml(html: String): Double? {
        val priceRegex = Regex("""(?:CHF|CHF\s*|Fr\.\s*)([0-9]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
        val match = priceRegex.find(html)
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun cleanText(text: String): String {
        return text.replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }
}
