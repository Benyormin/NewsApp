package com.example.newsapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.newsapp.R
import com.example.newsapp.model.NewsData
import com.example.newsapp.view.MainActivity
import java.net.URLEncoder

object NotificationHelper {

    private const val CHANNEL_ID = "channel_for_you"
    private const val CHANNEL_NAME = "For You News"

    fun showForYouNotification(context: Context, article: NewsData) {
        createChannel(context)
        val notificationId = article.articleUrl.hashCode()
        val notification = buildNotification(context, article)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Personalized daily news from For You section"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
    private fun createRssChannelForCategory(context: Context, channelId: String, category: String) {
        val name = "$category RSS Updates"
        val descriptionText = "Notifications for updates in $category feed"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }





    fun sendRssUpdateNotification(context: Context, article: NewsData, category: String) {
        val channelId = "channel_rss_$category" // Unique per RSS category
        createRssChannelForCategory(context, channelId, category)
        val notificationId = article.articleUrl.hashCode()
        val notification = buildNotification(context, article)


        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }


    fun buildNotification(context: Context, article: NewsData): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("article_data", article)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            article.articleUrl.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(article.title)
            .setContentText(article.description ?: "Tap to read more")
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true) // prevents flashing when updated
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val actionLabel = if (article.isBookmarked) "Bookmarked" else "Bookmark"

        if (article.isBookmarked) {
            // Disable action: no PendingIntent
            builder.addAction(R.drawable.bookmark_white_24dp, actionLabel, null)
        } else {
            // Active action: add intent to trigger receiver
            val bookmarkIntent = Intent(context, BookmarkReceiver::class.java).apply {
                putExtra("article", article)
            }

            val bookmarkPendingIntent = PendingIntent.getBroadcast(
                context,
                article.articleUrl.hashCode(),
                bookmarkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(R.drawable.ic_launcher_background, actionLabel, bookmarkPendingIntent)
        }

        return builder.build()
    }


}
