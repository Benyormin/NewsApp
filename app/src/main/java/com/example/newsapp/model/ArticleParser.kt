package com.example.newsapp.model

import android.util.Log
import com.example.newsapp.api.HuggingFaceService
import com.example.newsapp.api.SummaryRequest
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit



class ArticleParser {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val hfClient = Retrofit.Builder()
        .baseUrl("https://api-inference.huggingface.co/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build())
        .build()

        .create(HuggingFaceService::class.java)



    suspend fun getSummaryFromHF(text: String): String? {
        return try {
            if (text.length <  50){
                Log.e("HFDebug","Text too short")
                return null
            }

            Log.d("HFDebug", "Sending request with text length: ${text.length}")
            val response = hfClient.summarize(SummaryRequest(inputs = text))
            Log.d("HFDebug", "Response code: ${response.code()}")
            Log.d("HFDebug", "Response body: ${response.body()}")
            Log.d("HFDebug", "Error body: ${response.errorBody()?.string()}")

            when {
                response.isSuccessful -> response.body()?.firstOrNull()?.summary
                response.code() == 503 -> {
                    Log.d("HFDebug", "Model loading - retrying...")
                    // Model loading required
                    delay(10000) // Wait 10 seconds
                    getSummaryFromHF(text) // Retry
                }
                else -> {
                    Log.e("HFDebug", "API Error: ${response.errorBody()?.string()}")
                    throw Exception("API error: ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("HFService", "Summarization failed", e)
            null
        }
    }

    suspend fun summarizeArticle(url: String): String? {
        return try {
            //Log.d("ArticleParser", "Fetching HTML for URL: $url")
            // Step 1: Get HTML content
            //val html = fetchHtml(url) ?: run{
            //    Log.e("ArticleParser", "Failed to fetch HTML")
             //   return null
           // }
            //Log.d("ArticleParser","html fetched: ${html}")

            Log.d("ArticleParser", "Parsing article text...")
            // Step 2: Parse article text
            val articleText = parseArticle(url)
                ?: throw Exception("Failed to parse article content")
            Log.d("ArticleParser", "Article text length: ${articleText.length}")
            Log.d("ArticleParser","article text: ${articleText}")

            // Step 3: Get summary from Hugging Face

            Log.d("ArticleParser", "Calling HuggingFace API...")
            getSummaryFromHF(articleText)
        } catch (e: Exception) {
            Log.e("ArticleParser", "Summarization failed", e)
            null
        }
    }


    private suspend fun fetchHtml(url: String): String? {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Increased timeout
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS // Log headers for debugging
                })
                .followRedirects(true) // Follow redirects
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("Accept-Language", "en-US,en;q=0.5")
                .addHeader("Referer", "https://www.google.com/") // Pretend to come from Google
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("fetchHtml", "Failed with code: ${response.code} | URL: $url")
                return null
            }
            response.body?.string()?.also {
                Log.d("fetchHtml", "Successfully fetched HTML (length=${it.length})")
            }
        } catch (e: Exception) {
            Log.e("fetchHtml", "Error fetching URL: $url", e)
            null
        }
    }

    private fun parseWithJsoup(html: String): String? {
        return try {
            val doc = Jsoup.parse(html)

            // Try common content containers
            listOf("article", "main", ".post-content", "#content")
                .firstNotNullOfOrNull { selector ->
                    doc.select(selector).text().takeIf { it.isNotEmpty() }
                } ?: doc.body()?.text()
        } catch (e: Exception) {
            null
        }
    }

    fun parseArticle(html: String): String? {
        val doc = Jsoup.parse(html)

        // Guardian-specific selectors (in order of priority)
        val guardianSelectors = listOf(
            "div.article-body-commercial-selector", // Guardian's main article container
            "[itemprop='articleBody']",
            "article",
            "div#maincontent"
        )

        // Try Guardian-specific selectors first
        guardianSelectors.firstNotNullOfOrNull { selector ->
            doc.select(selector).text().takeIf { it.isNotEmpty() }
        }?.let { return it }

        // Fallback to general news selectors
        val generalSelectors = listOf(
            "article",
            "[itemprop='articleBody']",
            ".post-content",
            ".article-content",
            "#main-content",
            "div.content"
        )

        return generalSelectors.firstNotNullOfOrNull { selector ->
            doc.select(selector).text().takeIf { it.isNotEmpty() }
        } ?: run {
            Log.w("ArticleParser", "Using fallback body text - may contain extra content")
            doc.body()?.text()?.take(10000) // Safety truncation
        }
    }







}