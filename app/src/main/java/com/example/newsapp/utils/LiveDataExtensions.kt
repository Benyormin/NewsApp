package com.example.newsapp.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun <T> LiveData<T>.awaitValue(timeoutMillis: Long = 3000): T? = suspendCancellableCoroutine { cont ->
    val observer = object : Observer<T> {
        override fun onChanged(t: T) {
            if (t != null && !(t is Collection<*> && t.isEmpty())) {
                this@awaitValue.removeObserver(this)
                cont.resume(t)
            }
        }
    }

    this.observeForever(observer)

    cont.invokeOnCancellation {
        this.removeObserver(observer)
    }

    // Optional timeout to avoid infinite wait
    CoroutineScope(Dispatchers.Main).launch {
        delay(timeoutMillis)
        this@awaitValue.removeObserver(observer)
        if (cont.isActive) {
            cont.resume(null)
        }
    }
}
