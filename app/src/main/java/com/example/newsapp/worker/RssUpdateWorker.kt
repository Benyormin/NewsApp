package com.example.newsapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.newsapp.RetrofitClient
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.model.NewsData
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RssUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val category = inputData.getString("category") ?: return@withContext Result.failure()
        val url = inputData.getString("url") ?: return@withContext Result.failure()
        Log.d("RssUpdateWorker", "RSS Update Worker started for category: $category,\n URL: $url...")
        val context = applicationContext
        val database = ArticleDatabase.invoke(applicationContext)
        val dao = database.getArticleDao()

        val repository = NewsRepository(
            RetrofitClient.newsApiService,
            RetrofitClient.guardianApiService,
            RetrofitClient.espnApiService,
            dao,
            context
        )

       val newArticles = try {
           repository.getRssNews(url, category)
       } catch (e: Exception){
           Log.e("RssUpdateWorker", " Error fetching RSS $url: ${e.message}")
           return@withContext Result.retry()
       }

        val formatter = DateTimeFormatter.ISO_DATE_TIME // or your actual format
        val latest = newArticles
            .filter { it.publishedAt != null }
            .maxByOrNull {
                try {
                    ZonedDateTime.parse(it.publishedAt, formatter).toInstant()
                } catch (e: Exception) {
                    Instant.EPOCH // fallback
                }
            }
        if (latest == null) {
            Log.d("RssUpdateWorker", "No new articles found for $url")
            return@withContext Result.success()
        }

        val prefs = context.getSharedPreferences("rss_last_seen", Context.MODE_PRIVATE)
        val lastSeenUrl = prefs.getString(url, null)

        Log.d("RssUpdateWorker", "latest article: $latest")
        if (latest.articleUrl != lastSeenUrl) {
            NotificationHelper.sendRssUpdateNotification(context, latest, category)
            Log.d("RssUpdateWorker", "RSS Worker finished. Sent  notifications.")
            prefs.edit().putString(url, latest.articleUrl).apply()
        }



        return@withContext Result.success()
    }
}
