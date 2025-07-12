package com.example.newsapp.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.newsapp.worker.ForYouNotificationWorker
import com.example.newsapp.worker.RssUpdateWorker
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WORK_TAG = "for_you_daily_notification"
    private const val RSS_WORK_TAG = "rss_update_notification"

    fun scheduleForYouDaily(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ForYouNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP, // Keep if already scheduled
            workRequest
        )
    }

    fun cancelForYouDaily(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }




    fun scheduleRssUpdateWorker(context: Context, category: String, url: String) {
        val inputData = workDataOf("category" to category, "url" to url)

        val request = PeriodicWorkRequestBuilder<RssUpdateWorker>(
            1, TimeUnit.HOURS)
            .addTag("rss_worker_$category")  // unique tag for each feed
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "rss_worker_$category",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelRssUpdateWorker(context: Context, category: String) {
        WorkManager.getInstance(context).cancelUniqueWork("rss_worker_$category")
    }

}
