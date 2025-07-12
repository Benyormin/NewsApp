package com.example.newsapp.worker

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.newsapp.RetrofitClient
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.utils.NotificationHelper
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel


class ForYouNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {



    override suspend fun doWork(): Result {
        try {
            Log.d("ForYouWorker", " Worker started...")
            val database = ArticleDatabase.invoke(applicationContext)
            val dao = database.getArticleDao()

            val repository = NewsRepository(
                RetrofitClient.newsApiService,
                RetrofitClient.guardianApiService,
                RetrofitClient.espnApiService,
                dao,
                applicationContext
            )

            // Step 1: Get articles from "For You"
            val forYouArticles = repository.getForYouArticlesForNotification()

            // Step 2: Pick one or more to notify
            val latest = forYouArticles.randomOrNull() ?: return Result.success()
            Log.d("ForYouWorker", "Fetched ${forYouArticles.size} articles")

            // Step 3: Show notification
            NotificationHelper.showForYouNotification(context, latest)
            //NotificationHelper.buildNotification(context, latest)


            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}
