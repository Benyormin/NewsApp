package com.example.newsapp.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.newsapp.RetrofitClient
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.model.NewsData
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notify

class BookmarkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BookmarkReceiver", "Receiver is running")
        val article = intent.getParcelableExtra<NewsData>("article") ?: return
        Log.d("BookmarkReceiver", "Received broadcast for article: ${article}")

        // Toggle bookmark using ViewModel or Repo directly
        CoroutineScope(Dispatchers.IO).launch {
            val database = ArticleDatabase.invoke(context)
            val dao = database.getArticleDao()
            val repository = NewsRepository(
                RetrofitClient.newsApiService,
                RetrofitClient.guardianApiService, RetrofitClient.espnApiService,
                dao, context)

            val updated = article.copy(isBookmarked = true)
            repository.updateBookmark(updated)


            // ✅ Rebuild notification with disabled button
            val updatedNotification = NotificationHelper.buildNotification(context, updated)
            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(article.articleUrl.hashCode(), updatedNotification)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "✔️ Bookmarked!", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
