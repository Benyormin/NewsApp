package com.example.newsapp.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.newsapp.worker.ForYouNotificationWorker
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WORK_TAG = "for_you_daily_notification"

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
}
