package com.example.newsapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.newsapp.RetrofitClient
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.utils.NotificationHelper
import org.apache.commons.net.nntp.Article

class CategoryNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters

): CoroutineWorker(context, workerParams)
{
    override suspend fun doWork(): Result {
        val category = inputData.getString("category") ?: return Result.failure()

        try{
            val database = ArticleDatabase.invoke(applicationContext)
            val dao = database.getArticleDao()

            val repository = NewsRepository(
                RetrofitClient.newsApiService,
                RetrofitClient.guardianApiService,
                RetrofitClient.espnApiService,
                dao,
                applicationContext
            )

            val articles = repository.getArticlesForCategoryNotification(category)
            val latest = articles.randomOrNull() ?: return Result.success() //TODO: latest
            NotificationHelper.showCategoryNotification(context, category, latest)
            return Result.success()

        }catch (e: Exception){
            e.printStackTrace()
            return Result.failure()

        }
    }

}