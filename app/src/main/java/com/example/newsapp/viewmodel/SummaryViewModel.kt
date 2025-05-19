package com.example.newsapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.ArticleParser
import kotlinx.coroutines.launch

class SummaryViewModel : ViewModel() {






    // LiveData declarations
    private val _SummarizedArticles = MutableLiveData<List<String>>(emptyList())
    val SummarizedArticles: LiveData<List<String>> = _SummarizedArticles

    fun addSummarizedArticles(articleUrl: String) {
        val updatedList = _SummarizedArticles.value.orEmpty() + articleUrl
        _SummarizedArticles.value = updatedList
    }


    private val _summary = MutableLiveData<Map<String,Pair<String, String>>>()
    val summary: LiveData<Map<String, Pair<String, String>>> = _summary

    fun setSummary(url: String,title: String, summary: String) {
        val currentMap = _summary.value ?: emptyMap()
        val updatedMap = currentMap + (url to Pair(title, summary))  // Creates a new map
        _summary.value = updatedMap
    }

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // In SummaryViewModel.kt
    fun resetSummaryState() {
//        _summary.value = """"""
        _error.value = ""
    }
}