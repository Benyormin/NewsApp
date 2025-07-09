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
    fun loadPreferences(categories: List<String>) {
        val savedPrefs = loadFromStorage() // Placeholder for SharedPreferences or Firebase
        val prefs = categories.map { category ->
            NotificationPreference(
                category = category,
                isEnabled = savedPrefs[category] ?: true // default to true
            )
        }
        _notificationPreferences.value = prefs
    }

    fun updatePreference(pref: NotificationPreference) {
        saveToStorage(pref) // Placeholder to persist change
    }

    private fun loadFromStorage(): Map<String, Boolean> {
        // TODO: Replace with real SharedPreferences or Firebase logic
        return emptyMap()
    }

    private fun saveToStorage(pref: NotificationPreference) {
        // TODO: Save to SharedPreferences or Firebase
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

}
