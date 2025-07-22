package com.example.newsapp.model

import android.util.Log
import android.util.Xml
import androidx.core.text.HtmlCompat
import com.example.newsapp.utils.HelperFuncitons.Companion.getNetworkTime
import com.google.common.collect.Iterables.skip
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


enum class RssType {
    SIMPLE, RICH
}


class RssParser (private val source: String){
    fun parse(inputStream: InputStream): List<NewsData> {
        Log.d("Rss Parser","parser has been called")
        val newsList = mutableListOf<NewsData>()
        val parser: XmlPullParser = Xml.newPullParser()

        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)

            // Navigate to channel first
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "channel") {
                    parseChannel(parser, newsList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream.close()
        }
        return newsList
    }


    private fun parseChannel(parser: XmlPullParser, newsList: MutableList<NewsData>) {
        val type = detectRssType(parser)

        // Reset back to channel/item
        while (!(parser.eventType == XmlPullParser.START_TAG && parser.name == "item")) {
            parser.next()
        }

        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "channel")) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                when (type) {
                    RssType.SIMPLE -> parseSimpleItem(parser, newsList)
                    RssType.RICH -> parseRichItem(parser, newsList)
                }
            } else {
                parser.next()
            }
        }
    }



    /*
        private fun parseChannel(parser: XmlPullParser, newsList: MutableList<NewsData>) {
            while (parser.next() != XmlPullParser.END_TAG || parser.name != "channel") {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                    parseItem(parser, newsList)
                }
            }
        }

        private fun parseItem(parser: XmlPullParser, newsList: MutableList<NewsData>) {
            var title = ""
            var description = ""
            var url = ""
            var pubDate = ""
            var imageUrl: String? = null

            while (parser.next() != XmlPullParser.END_TAG || parser.name != "item") {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.name) {
                    "title" -> title = readText(parser)
                    "description" -> description = readText(parser)
                    "link" -> url = readText(parser)
                    "pubDate" -> pubDate = convertRssDateToIsoFormat(readText(parser))
                    "media:thumbnail" -> imageUrl = parser.getAttributeValue(null, "url")
                    "enclosure" -> {
                        imageUrl = parser.getAttributeValue(null, "url")
                        parser.nextTag()
                    }
                    else -> parser.next()
                }
            }

            if (title.isNotEmpty() && url.isNotEmpty()) {
                newsList.add(
                    NewsData(
                        title = title,
                        description = description,
                        articleUrl = url,
                        publishedAt = pubDate,
                        source = Source(id = source, name = source),
                        imageUrl = imageUrl
                    )
                )
            }
            Log.d("RssParser", "Parsing item - Title: $title, URL: $url, Image: $imageUrl")
        }


     */


    private fun parseRichItem(parser: XmlPullParser, newsList: MutableList<NewsData>) {
        var title = ""
        var description = ""
        var url = ""
        var pubDate = ""

        Log.d("RssParser", "🔍 Starting parseRichItem")

        if (parser.eventType != XmlPullParser.START_TAG || parser.name != "item") {
            return // not at item tag
        }

        while (true) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item") break
                }

                XmlPullParser.START_TAG -> {
                    Log.d("RssParser", "🔸 START_TAG: ${parser.name}")
                    when (parser.name) {
                        "title" -> title = readText(parser)
                        "description" -> {
                            val rawDescription = readText(parser)
                            description = stripHtml(rawDescription)
                            pubDate = extractPublicationDateFromDescription(rawDescription).orEmpty()
                        }
                        "link" -> url = readText(parser)
                        else -> {
                            Log.d("RssParser", "⏭️ Skipping tag: ${parser.name}")
                            skip(parser)
                        }
                    }
                }

                XmlPullParser.END_DOCUMENT -> break
            }
        }


        if (title.isNotEmpty() && url.isNotEmpty()) {
            newsList.add(
                NewsData(
                    title = title,
                    description = description,
                    articleUrl = url,
                    publishedAt = convertToIsoFormat(pubDate),
                    source = Source(id = source, name = source),
                    imageUrl = null
                )
            )
            Log.d("RssParser", "✅ Added item - $title")
        } else {
            Log.w("RssParser", "⚠️ Skipped item - missing title or URL")
        }
    }
    private fun extractPublicationDateFromDescription(descriptionHtml: String): String? {
        val regex = Regex("""<p>\s*Publication date:\s*(.*?)\s*</p>""", RegexOption.IGNORE_CASE)
        val match = regex.find(descriptionHtml)
        val publicationDate = match?.groups?.get(1)?.value?.trim()

        Log.d("RssParser", "🗓 Extracted date: $publicationDate")
        return publicationDate
    }





    private fun parseSimpleItem(parser: XmlPullParser, newsList: MutableList<NewsData>) {
        var title = ""
        var description = ""
        var url = ""
        var pubDate = ""
        var imageUrl: String? = null

        while (parser.next() != XmlPullParser.END_TAG || parser.name != "item") {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.name) {
                "title" -> title = readText(parser)
                "description" -> description = readText(parser)
                "link" -> url = readText(parser)
                "pubDate" -> pubDate = convertToIsoFormat(readText(parser))
                "media:thumbnail" -> imageUrl = parser.getAttributeValue(null, "url")
                "enclosure" -> {
                    imageUrl = parser.getAttributeValue(null, "url")
                    parser.nextTag()
                }
                else -> parser.next()
            }
        }

        if (title.isNotEmpty() && url.isNotEmpty()) {
            newsList.add(
                NewsData(
                    title = title,
                    description = description,
                    articleUrl = url,
                    publishedAt = pubDate,
                    source = Source(id = source, name = source),
                    imageUrl = imageUrl
                )
            )
        }
        Log.d("RssParser", "Parsing item - Title: $title, URL: $url, Image: $imageUrl")
    }


    fun stripHtml(html: String): String {
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }



    // Rest of the code remains the same

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            Log.d("RssParser", "📄 readText() got: $result")
            parser.nextTag()
        }
        return result.trim()
    }

    private fun convertToIsoFormat(dateStr: String): String {
        val knownFormats = listOf(
            // Standard RSS pubDate format: "Wed, 17 Jul 2024 16:00:00 +0000"
            "EEE, dd MMM yyyy HH:mm:ss Z",
            // Extracted ScienceDirect format: "15 January 2026"
            "dd MMMM yyyy",
            // ISO-like: "2025-07-17T11:11:46"
            "yyyy-MM-dd'T'HH:mm:ss",
            // ISO with offset: "2025-07-17T11:11:46Z" or "2025-07-17T11:11:46+00:00"
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )

        for (format in knownFormats) {
            try {
                val parser = SimpleDateFormat(format, Locale.ENGLISH)
                parser.timeZone = TimeZone.getTimeZone("UTC") // For consistent parsing
                val date = parser.parse(dateStr)
                if (date != null) {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
                    isoFormat.timeZone = TimeZone.getTimeZone("UTC")
                    return isoFormat.format(date)
                }
            } catch (e: Exception) {
                // Try next format
            }
        }

        Log.e("RssParser", "❌ Unparseable date: \"$dateStr\"")
        return ""
    }

   /* private fun convertRssDateToIsoFormat(rssDate: String): String {
        return try {
            val rssFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val date = rssFormat.parse(rssDate) ?: return ""


            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            isoFormat.format(date)
        } catch (e: Exception) {
            Log.e("RssParser", "Error converting RSS date", e)
            ""
        }
    }
*/
   /* private fun convertExtractedDateToIsoFormat(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(dateStr) ?: return ""

            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.e("RssParser", "❌ Error parsing extracted date", e)
            ""
        }
    }
*/

    private fun detectRssType(parser: XmlPullParser): RssType {
        // Advance to the first <item>
        while (parser.eventType != XmlPullParser.START_TAG || parser.name != "item") {
            parser.next()
        }

        // Look ahead inside first <item> for clues
        var itemDepth = 1
        while (itemDepth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "description" -> {
                            val content = readText(parser)
                            if (content.contains("<p>") || content.contains("<b>") || content.contains("CDATA")) {
                                return RssType.RICH
                            }
                        }
                        "media:thumbnail", "enclosure" -> return RssType.SIMPLE
                    }
                }
                XmlPullParser.END_TAG -> if (parser.name == "item") itemDepth--
            }
        }

        return RssType.SIMPLE
    }


}