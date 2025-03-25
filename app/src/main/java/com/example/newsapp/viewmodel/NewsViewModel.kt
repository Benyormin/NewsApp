package com.example.newsapp.viewmodel

import NewsRepository
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope

import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.db.ArticlesDAO
import com.example.newsapp.model.NewsData
import com.example.newsapp.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewsViewModel(private var repository: NewsRepository) : ViewModel() {

    // LiveData to hold news data for each category
    private val _newsData = MutableLiveData<Map<String, List<NewsData>>>()
    val newsData: LiveData<Map<String, List<NewsData>>> get() = _newsData

    private val _guardianNewsData = MutableLiveData<List<NewsData>>()
    val guardianNewsData: LiveData<List<NewsData>> get() = _guardianNewsData

    private val _rssItems = MutableLiveData<List<NewsData>>()
    val rssItems: LiveData<List<NewsData>> get() = _rssItems

    private val _footballData = MutableLiveData<List<NewsData>>()
    val footballData: LiveData<List<NewsData>> get() = _footballData

    private val _searchedData = MutableLiveData<List<NewsData>>()
    val searchedData: LiveData<List<NewsData>> get() = _searchedData

    val bookMarkedArticles: LiveData<List<NewsData>> = repository.bookmarkedArticles





    fun toggleBookmark(article: NewsData) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Bookmark", "Toggle bookmark has been called")
            repository.updateBookmark(article)
        }
    }

    fun toggleLikes(article:NewsData){
        viewModelScope.launch {
            repository.updateLikes(article)
        }
    }
    // Fetch news for multiple categories
    /**
     *
     *
     * fetch news from NewsAPI service
     */
    fun fetchNewsForCategories(categories: List<String>) {
        viewModelScope.launch {
            val result = repository.getNewsForCategories(categories)
            _newsData.value = result
        }
    }
    /**
     *  fetch news from NewsAPI service
     */
    fun fetchNewsForCategory(category: String) {
        viewModelScope.launch {
            val currentData = _newsData.value ?: emptyMap()
            val newData = async{ repository.getNewsByCategory(category) }
            val guardian = async{ repository.getGuardianNews(category) }
            val likeStateDefered = async{ repository.getLikeStates() }

            val ls = likeStateDefered.await()
            Log.d("News ViewModel", "Like info || :\n ${ls.values} \n id: ${ls.keys} \n")
            val gnData = guardian.await()
            val newsApiData = newData.await()
            var combinedList = mutableListOf<NewsData>().apply {
                addAll(gnData)
                addAll(newsApiData)
            }

            val processed = combinedList.map {
                article -> article.copy(isLike = ls[article.articleUrl]?: false)
            }
            val shuffled  = processed.shuffled()
            /*val updatedData = currentData.toMutableMap().apply {
                put(category, newData)
            }*/

            val updatedData = currentData.toMutableMap().apply{
                put(category, shuffled)
            }
            _newsData.value = updatedData
        }
    }

    fun fetchGuardianNews(section: String = "football"){
        viewModelScope.launch {
            _guardianNewsData.value = repository.getGuardianNews(section)
        }
    }

    /*fun fetchBbcSportNews(){
        Log.d("NewsViewModel","Fetch BBC Sport News has been called!")
        viewModelScope.launch {
            _rssItems.value = repository.getRssNews()
        }

    }*/

    fun getFootballNews() {
        // TODO: I can receive a list<map<url, source>> then loop through it
        // TODO: I should be aware of the process of retrieving data. API news would get call by a delay. why?
        viewModelScope.launch {

            val guardianDeferred = async { repository.getGuardianNews("football") }
            val cbsDeferred = async { repository.getRssNews(Constants.CBS_RSS_URL, "CBS Sport") }
            val espnDeferred = async { repository.getRssNews(Constants.ESPN_RSS_URL, "ESPN") }
            val goalDeferred = async { repository.getRssNews(Constants.GOAL_RSS_URL, "Goal") }
            val bbcDeferred = async { repository.getRssNews(Constants.BBC_RSS_URL, "BBC") }
            val bbc2Deferred = async { repository.getRssNews(Constants.BBC2_RSS_URL, "BBC2") }
            val ff2Deferred = async { repository.getRssNews(Constants.FOUR_FOUR_TWO_RSS_URL, "442") }
            val ninetyDeferred = async { repository.getRssNews(Constants.NINETY_RSS_URL, "90 min") }
            val mirrorDeferred = async { repository.getRssNews(Constants.MIRROR_RSS_URL, "Mirror") }
            val dailyMailDeferred = async { repository.getRssNews(Constants.DAILY_MAIL_RSS_URL, "Daily Mail") }
            val espn2Deferred = async { repository.getEspnNews() }
            val likeStateDefered = async{ repository.getLikeStates() }
            val ls = likeStateDefered.await()

            // Wait for all network calls to complete and get their results
            val guardian = guardianDeferred.await()
            val cbs = cbsDeferred.await()
            val espn = espnDeferred.await()
            val goal = goalDeferred.await()
            val bbc = bbcDeferred.await()
            val bbc2 = bbc2Deferred.await()
            val ff2 = ff2Deferred.await()
            val ninety = ninetyDeferred.await()
            val mirror = mirrorDeferred.await()
            val dailyMail = dailyMailDeferred.await()
            val espn2 = espn2Deferred.await()




            var combinedList = mutableListOf<NewsData>().apply {
                addAll(guardian)
                addAll(cbs)
                addAll(espn)
                addAll(goal)
                addAll(bbc)
                addAll(bbc2)
                addAll(ff2)
                addAll(ninety)
                addAll(mirror)
                addAll(dailyMail)
                addAll(espn2)
            }

            combinedList = combinedList.map{ article->
                article.copy(isLike = ls[article.articleUrl] ?: false)
            }.toMutableList()

            // Shuffle the combined list
            val shuffledList = combinedList.shuffled()

            // Update _footballData with the shuffled list
            _footballData.value = shuffledList

        }
    }

    fun searchNews(query: String) {
        viewModelScope.launch {
            val searchResults = repository.searchNews(query)
            _searchedData.value = searchResults
        }

    }

}