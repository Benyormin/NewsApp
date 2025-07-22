package com.example.newsapp.utils

import android.util.Log
import kotlinx.coroutines.Deferred

suspend fun <T> safeAwait(deferred: Deferred<T>): T? {
    return try {
        deferred.await()
    } catch (e: Exception) {
        Log.e("News", "Fetch failed", e)
        null
    }
}
