package com.example.newsapp.utils

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.newsapp.db.Preferences
import com.example.newsapp.db.RssUrl
import com.example.newsapp.model.NewsData
import com.example.newsapp.viewmodel.NewsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.net.InetAddress
import java.util.Locale
import org.apache.commons.net.ntp.NTPUDPClient


class HelperFuncitons {

    companion object {



        fun saveRSSToFirestore(rssUrl: String, rssName: String, firestore: FirebaseFirestore, userId: String){
            val rssRef = firestore.collection("users").document(userId).collection("rssFeeds")
            val docId = Uri.encode(rssUrl)
            val rssMap = mapOf("name" to rssName, "url" to rssUrl)
            rssRef.document(docId).set(rssMap)
        }


        fun deleteRSSFromFirestore(rssUrl: String, rssName: String, firestore: FirebaseFirestore, userId: String){
            val docId = Uri.encode(rssUrl)
            val rssRef = firestore.collection("users").document(userId).collection("rssFeeds").document(docId)
            rssRef.delete()
                .addOnSuccessListener {
                    Log.d("Rss", "Successfully deleted from Firestore")
                }
                .addOnFailureListener {
                    Log.e("Rss", "Firestore delete failed: ${it.message}")
                }
        }



        fun fetchCategoriesAndRSSFromFireStore(viewModel: NewsViewModel, userId: String, firestore: FirebaseFirestore){
            // 🔄 Fetch RSS feeds
            firestore.collection("users").document(userId).collection("rssFeeds")
                .get()
                .addOnSuccessListener { snapshot ->
                    val rssList = snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name")
                        val url = doc.getString("url")
                        if (name != null && url != null) RssUrl(name = name, url = url) else null
                    }
                    viewModel.saveRssFeedsToRoom(rssList)
                }

            // 🔄 Fetch Categories
            firestore.collection("users").document(userId)
                .collection("settings").document("preferences")
                .get()
                .addOnSuccessListener { doc ->
                    val categories = doc.get("categories") as? List<String>
                    if (categories != null) {
                        viewModel.updateUserPreferences(Preferences(0, categories))
                    }
                }
        }

        fun saveCategoriesAndRssToFirestore(viewModel: NewsViewModel, userId: String,
                                            firestore: FirebaseFirestore,
                                            viewLifecycleOwner: LifecycleOwner
        ){
            viewModel.rssItems.observe(viewLifecycleOwner) { rssList ->

                val rssRef = firestore.collection("users").document(userId).collection("rssFeeds")
                for (rss in rssList) {
                    val docId = Uri.encode(rss.url)
                    val rssMap = mapOf("name" to rss.name, "url" to rss.url)
                    rssRef.document(docId).set(rssMap)
                }
            }

            // Save Category Preferences
            viewModel.userCategories.observe(viewLifecycleOwner) { pref ->
                val prefsMap = mapOf("categories" to pref.categories)
                firestore.collection("users").document(userId).collection("settings").document("preferences")
                    .set(prefsMap)
            }
        }



        fun NewsData.toMap(): Map<String, Any?> {
            return mapOf(
                "title" to title,
                "imageUrl" to imageUrl,
                "description" to description,
                "publishedAt" to publishedAt,
                "articleUrl" to articleUrl,
                "source" to mapOf(
                    "id" to source.id,
                    "name" to source.name
                ),
                "isBookmarked" to isBookmarked,
                "isLike" to isLike
            )
        }




        fun getRelativeTimeAndroid(publishedAt: String): String {
            val formats = listOf(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME,                             // "2025-06-02T13:00:11+00:00"
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),               // "2025-06-02T13:00:11"
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),     // "Tue, 02 Jul 2024 14:22:00 +0000"
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH),   // "Tue, 02 Jul 2024 14:22:00 GMT"
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")         // "2024-06-02T13:00:11.123Z"
            )

            val publishedTime: Instant? = run {
                val rfcString = publishedAt.trim()

                if (rfcString.matches(Regex(".*\\s[A-Z]{3}\$"))) {
                    val withoutZone = rfcString.substringBeforeLast(' ')
                    Log.d("HelperFunctions", "RFC branch: stripping zone → \"$withoutZone\"")

                    val localFormatter = DateTimeFormatter.ofPattern(
                        "EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH
                    )
                    try {
                        val localDateTime = org.threeten.bp.LocalDateTime.parse(
                            withoutZone,
                            localFormatter
                        )
                        val zonedInstant = localDateTime
                            .atZone(ZoneId.of("America/New_York"))
                            .toInstant()

                        Log.d("HelperFunctions", "RFC (textual zone) success → Instant=\"$zonedInstant\"")
                        return@run zonedInstant
                    } catch (e: Exception) {
                        Log.e("HelperFunctions", "RFC parsing failed for \"$withoutZone\"", e)
                    }
                }

                formats.asSequence().mapNotNull { formatter ->
                    try {
                        val instant = when (formatter) {
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME -> {
                                val odt = org.threeten.bp.OffsetDateTime.parse(publishedAt, formatter)
                                odt.toInstant()
                            }
                            else -> {
                                val ldt = org.threeten.bp.LocalDateTime.parse(publishedAt, formatter)
                                val inst = ldt.atZone(ZoneId.of("UTC")).toInstant()
                                inst
                            }
                        }
                        Log.d("HelperFunctions", "Parsed using \"$formatter\" → Instant=\"$instant\"")
                        instant
                    } catch (e: Exception) {
                        Log.d("HelperFunctions", "Failed with formatter \"$formatter\" → $publishedAt")
                        null
                    }
                }.firstOrNull()
            } ?: run {
                Log.e("HelperFunctions", "All parsing attempts failed for \"$publishedAt\"")
                return "Unknown"
            }

            return try {
                val nowInstant = Instant.ofEpochMilli(TimeUtils.getNow())
                val duration = Duration.between(publishedTime, nowInstant)
                val minutes = duration.toMinutes()

                Log.d("HelperFunctions", "Computed duration → $minutes min")

                when {
                    minutes < 1 -> "Just now"
                    minutes < 60 -> "$minutes min ago"
                    minutes < 1440 -> "${minutes / 60} hr. ago"
                    minutes < 2880 -> "Yesterday"
                    minutes < 10080 -> "${minutes / 1440} days ago"
                    minutes < 20160 -> "1 week ago"
                    minutes < 40320 -> "${minutes / 10080} weeks ago"
                    else -> "${minutes / 40320} months ago"
                }
            } catch (e: Exception) {
                Log.e("HelperFunctions", "Relative time calc failed for: $publishedAt", e)
                "Invalid date"
            }
        }




            //TODO: make a better network request, Fix bug for invalid dates.
        fun getNetworkTime(): Long? {
            return try {
                val timeClient = NTPUDPClient()
                timeClient.defaultTimeout = 5000 // 5 seconds timeout
                val inetAddress = InetAddress.getByName("time.google.com") // Use Google's NTP server
                val timeInfo = timeClient.getTime(inetAddress)
                val networkTime = timeInfo.message.transmitTimeStamp.time // Returns current UTC time in milliseconds
                timeClient.close()
                networkTime
            } catch (e: Exception) {
                e.printStackTrace()
                null // Return null if fetching fails
            }
        }





    }
}