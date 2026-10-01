package com.retrocollector.app.core.data.scraper

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.Serializable

@Serializable
data class ScrapedListing(
    val url: String,
    val title: String,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val estimatedPriceChf: Double? = null,
    val sourcePlatform: String // "Ricardo.ch", "Tutti.ch", "Other"
)

class ListingScraper(
    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 8000
        }
        defaultRequest {
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            header(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            header(HttpHeaders.AcceptLanguage, "de-CH,de;q=0.9,en-US;q=0.8,en;q=0.7")
            header("sec-ch-ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"")
            header("sec-ch-ua-mobile", "?0")
            header("sec-ch-ua-platform", "\"macOS\"")
            header("Sec-Fetch-Dest", "document")
            header("Sec-Fetch-Mode", "navigate")
            header("Sec-Fetch-Site", "none")
            header("Sec-Fetch-User", "?1")
            header("Upgrade-Insecure-Requests", "1")
        }
    }
) {
    fun isSupportedListing(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("ricardo.ch") || lower.contains("tutti.ch") || lower.contains("anibis.ch") || lower.contains("ebay")
    }

    suspend fun fetchListing(url: String, sessionCookie: String? = null): Result<ScrapedListing> {
        val source = resolveSourcePlatform(url)

        return try {
            val response = client.get(url) {
                if (!sessionCookie.isNullOrBlank() && url.contains("ricardo.ch", ignoreCase = true)) {
                    val formattedCookie = if (sessionCookie.contains("=")) sessionCookie else "ricardo_session=$sessionCookie"
                    header(HttpHeaders.Cookie, formattedCookie)
                }
            }
            val html = response.bodyAsText()
            val isBlocked = !response.status.isSuccess() || isCaptchaPage(html)

            val parsedTitle = if (!isBlocked) {
                extractMetaTag(html, "og:title") ?: extractTagContent(html, "title")
            } else null

            val urlFallbackTitle = extractTitleFromUrl(url)
            val title = if (!parsedTitle.isNullOrBlank() && !isCaptchaPage(parsedTitle)) {
                cleanText(parsedTitle)
            } else {
                urlFallbackTitle ?: "$source Listing"
            }

            val parsedDesc = if (!isBlocked) {
                extractMetaTag(html, "og:description") ?: extractMetaTag(html, "description") ?: ""
            } else {
                "Item identified via link ($url). The page presented a temporary anti-bot verification challenge."
            }

            val imageUrl = if (!isBlocked) extractMetaTag(html, "og:image") else null
            val imageUrls = listOfNotNull(imageUrl)

            val price = if (!isBlocked) {
                extractMetaTag(html, "product:price:amount")?.toDoubleOrNull() ?: extractPriceFromHtml(html)
            } else null

            Result.success(
                ScrapedListing(
                    url = url,
                    title = title,
                    description = cleanText(parsedDesc),
                    imageUrls = imageUrls,
                    estimatedPriceChf = price,
                    sourcePlatform = source
                )
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            println("Scraping fallback triggered for $url: ${e.message}")
            val fallbackTitle = extractTitleFromUrl(url) ?: "$source Link"
            Result.success(
                ScrapedListing(
                    url = url,
                    title = fallbackTitle,
                    description = "Shared listing: $url",
                    sourcePlatform = source
                )
            )
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

    fun extractTitleFromUrl(url: String): String? {
        val lower = url.lowercase()
        if (lower.contains("ricardo.ch")) {
            val segment = url.substringAfter("/a/", "").substringBefore("/").substringBefore("?")
            if (segment.isNotBlank()) {
                val cleanSlug = segment.replace(Regex("""-\d+$"""), "")
                val words = cleanSlug.split("-").filter { it.isNotBlank() }
                if (words.isNotEmpty()) {
                    return words.joinToString(" ") { word ->
                        if (word.length <= 4) word.uppercase() else word.replaceFirstChar { it.uppercase() }
                    }
                }
            }
        } else if (lower.contains("tutti.ch")) {
            val segment = url.trimEnd('/').substringAfterLast('/')
            val cleanSlug = segment.replace(Regex("""^\d+-"""), "").replace(Regex("""-\d+$"""), "")
            val words = cleanSlug.split("-").filter { it.isNotBlank() }
            if (words.isNotEmpty()) {
                return words.joinToString(" ") { word ->
                    if (word.length <= 4) word.uppercase() else word.replaceFirstChar { it.uppercase() }
                }
            }
        }
        return null
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

    suspend fun fetchImageAsBase64(imageUrl: String, sessionCookie: String? = null): String? {
        if (imageUrl.isBlank()) return null
        return try {
            val response = client.get(imageUrl) {
                if (!sessionCookie.isNullOrBlank() && imageUrl.contains("ricardo", ignoreCase = true)) {
                    val formattedCookie = if (sessionCookie.contains("=")) sessionCookie else "ricardo_session=$sessionCookie"
                    header(HttpHeaders.Cookie, formattedCookie)
                }
            }
            if (response.status.isSuccess()) {
                val bytes = response.readRawBytes()
                val base64 = bytes.encodeBase64()
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
