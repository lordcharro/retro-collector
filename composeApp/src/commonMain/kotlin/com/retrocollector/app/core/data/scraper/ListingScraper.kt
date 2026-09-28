package com.retrocollector.app.core.data.scraper

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
    private val client: HttpClient = HttpClient()
) {
    fun isSupportedListing(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("ricardo.ch") || lower.contains("tutti.ch") || lower.contains("anibis.ch") || lower.contains("ebay")
    }

    suspend fun fetchListing(url: String): Result<ScrapedListing> {
        return try {
            val response = client.get(url)
            val html = response.bodyAsText()

            val title = extractMetaTag(html, "og:title")
                ?: extractTagContent(html, "title")
                ?: "Anúncio sem título"

            val description = extractMetaTag(html, "og:description")
                ?: extractMetaTag(html, "description")
                ?: ""

            val imageUrl = extractMetaTag(html, "og:image")
            val imageUrls = listOfNotNull(imageUrl)

            val source = when {
                url.contains("ricardo.ch") -> "Ricardo.ch"
                url.contains("tutti.ch") -> "Tutti.ch"
                url.contains("anibis.ch") -> "Anibis.ch"
                url.contains("ebay") -> "eBay"
                else -> "Web"
            }

            Result.success(
                ScrapedListing(
                    url = url,
                    title = cleanText(title),
                    description = cleanText(description),
                    imageUrls = imageUrls,
                    sourcePlatform = source
                )
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            println("Scraping fallback triggered for $url: ${e.message}")
            // Em caso de bloqueio de rede ou CORS no browser, devolvemos um objeto com o URL para o utilizador poder introduzir manualmente ou deixar o Gemini avaliar pelo título/link
            val domain = if (url.contains("ricardo.ch")) "Ricardo.ch" else "Anúncio"
            Result.success(
                ScrapedListing(
                    url = url,
                    title = "Link $domain",
                    description = "Anúncio partilhado: $url",
                    sourcePlatform = domain
                )
            )
        }
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

    private fun cleanText(text: String): String {
        return text.replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }
}
