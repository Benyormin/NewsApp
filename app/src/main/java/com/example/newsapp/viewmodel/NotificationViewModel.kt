package com.example.newsapp.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.newsapp.model.NotificationPreference
import com.example.newsapp.utils.NotificationScheduler

class NotificationViewModel : ViewModel() {

    private val _notificationPreferences = MutableLiveData<List<NotificationPreference>>()
    val notificationPreferences: LiveData<List<NotificationPreference>> get() = _notificationPreferences

    //load using the current tabs
    fun loadPreferences(context: Context,categories: List<String>) {
        val savedPrefs = loadFromStorage(context)
        val prefs = categories.map { category ->
            NotificationPreference(
                category = category,
                isEnabled = savedPrefs[category] ?: false // default to false
            )
        }
        _notificationPreferences.value = prefs
    }

    fun updatePreference(pref: NotificationPreference, context: Context) {
        saveToStorage(pref, context)
    }

    private fun loadFromStorage(context: Context): Map<String, Boolean> {

        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val result = mutableMapOf<String, Boolean>()
        for ((key, value) in prefs.all){
            if (value is Boolean){
                result[key] = value
            }
        }
        return result
    }

    private fun saveToStorage(pref: NotificationPreference, context: Context) {
        // Save to SharedPreferences
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(pref.category, pref.isEnabled).apply()

    }

    fun onForYouNotificationToggleChanged(enabled: Boolean, context: Context) {
        if (enabled) {
            NotificationScheduler.scheduleForYouDaily(context)
            Toast.makeText(context, "The For you notification is enabled", Toast.LENGTH_SHORT).show()
        } else {
            NotificationScheduler.cancelForYouDaily(context)
            Toast.makeText(context, "The For you notification is disabled", Toast.LENGTH_SHORT).show()
        }
    }

    fun onRssToggleChanged(category: String, url: String, enabled: Boolean, context: Context) {
        if (enabled) {
            NotificationScheduler.scheduleRssUpdateWorker(context, category, url)
            Toast.makeText(context, "$category RSS notification enabled", Toast.LENGTH_SHORT).show()
        } else {
            NotificationScheduler.cancelRssUpdateWorker(context, category)
            Toast.makeText(context, "$category RSS notification disabled", Toast.LENGTH_SHORT).show()
        }
    }


}
