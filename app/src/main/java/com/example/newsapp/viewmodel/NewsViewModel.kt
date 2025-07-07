package com.example.newsapp.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.db.Preferences

import com.example.newsapp.db.RssUrl
import com.example.newsapp.model.NewsArticle

import com.example.newsapp.model.NewsData
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.utils.Constants
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Collections.addAll

class NewsViewModel(private var repository: NewsRepository) : ViewModel() {

    // LiveData to hold news data for each category
    private val _newsData = MutableLiveData<Map<String, List<NewsData>>>()
    val newsData: LiveData<Map<String, List<NewsData>>> get() = _newsData

    private val _rssNewsData = mutableMapOf<String, MutableLiveData<List<NewsData>>>()
    val rssNewsData: Map<String, LiveData<List<NewsData>>> get() = _rssNewsData

    private val _guardianNewsData = MutableLiveData<List<NewsData>>()
    val guardianNewsData: LiveData<List<NewsData>> get() = _guardianNewsData


    private val _footballData = MutableLiveData<List<NewsData>>()
    val footballData: LiveData<List<NewsData>> get() = _footballData

    private val _searchedData = MutableLiveData<List<NewsData>>()
    val searchedData: LiveData<List<NewsData>> get() = _searchedData

    val bookMarkedArticles: LiveData<List<NewsData>> = repository.bookmarkedArticles

    val rssItems: LiveData<List<RssUrl>> = repository.rssUrls
    val userCategories: LiveData<Preferences> = repository.userCategories

    private val _forYouData: LiveData<List<NewsData>> = MutableLiveData<List<NewsData>>()
    val forYouData: LiveData<List<NewsData>> get() = _forYouData


    fun updateUserPreferences(userPreferences: Preferences){
        viewModelScope.launch {
            repository.updateCategories(userPreferences)
            Log.d("viewModel", "update preferences has been called ${userPreferences.categories}")
        }
    }
    // Initialize LiveData before fetching
    fun CategoryInitialized(category: String) {
        if (!_rssNewsData.containsKey(category)) {
            _rssNewsData[category] = MutableLiveData(emptyList())
        }
    }

    fun addRssUrls(name: String, url: String){
        viewModelScope.launch {
            repository.addRssUrls(name, url)
            Log.d("ViewModel", "Rss has been added")
        }
    }
    fun deleteRssUrl(rssUrl: RssUrl){
        viewModelScope.launch {
            repository.deleteRssUrl(rssUrl)
        }
    }
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

    //viewModel.updateRssUrl(updatedFeed)
    //fun updateRssUrl()
    // Fetch news for multiple categories
    /**
     *
     *
     * fetch news from NewsAPI service
     */
    fun fetchNewsForCategories(categories: List<String>){
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

    fun fetchRssNews(category: String, url: String) {
        viewModelScope.launch {
            // Get raw list from repository
            CategoryInitialized(category)
            val newsList = repository.getRssNews(url, category)
            Log.d("NewsViewModel", "fetchNews: ${newsList}")

         /*   // Create/update LiveData entry
            _rssNewsData.getOrPut(category) {
                MutableLiveData()
            }.value = newsList*/


            _rssNewsData[category]?.postValue(newsList)
        }
    }

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
            val espn2 = espn2Deferred.await()

            val cbs = cbsDeferred.await()
            val espn = espnDeferred.await()
            val goal = goalDeferred.await()
            val bbc = bbcDeferred.await()
            val bbc2 = bbc2Deferred.await()
            val ff2 = ff2Deferred.await()
            val ninety = ninetyDeferred.await()
            val mirror = mirrorDeferred.await()
            val dailyMail = dailyMailDeferred.await()





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

    fun getForYouNews(tabs: List<String>) {
        // Get an average of the users selected categories( tabs)
        //TODO:: set up a recommendation system where user get personalized News

    }

    fun saveBookmarksToRoom(bookmarks: List<NewsData>) {
        viewModelScope.launch(Dispatchers.IO) {

            for (article in bookmarks) {
                toggleBookmark((article))
            }
        }
    }

    fun saveRssFeedsToRoom(rssList: List<RssUrl>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRssFeeds(rssList)
        }
    }


}