package com.example.newsapp.model

import android.util.Log
import android.util.Xml
import com.example.newsapp.utils.HelperFuncitons.Companion.getNetworkTime
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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

    // Rest of the code remains the same

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result.trim()
    }


    private fun convertRssDateToIsoFormat(rssDate: String): String {
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
}