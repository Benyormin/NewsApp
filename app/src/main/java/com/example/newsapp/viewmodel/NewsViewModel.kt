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
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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

    private val _allTabs = MutableLiveData<List<String>>()
    val allTabs: LiveData<List<String>> get() = _allTabs

    private val _forYouData = MutableLiveData<List<NewsData>>()
    val forYouData: LiveData<List<NewsData>> get() = _forYouData

    private val _categoryLoadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _rssLoadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private val _isSubscribed = MutableLiveData(false)
    val isSubscribed: LiveData<Boolean> = _isSubscribed


    fun setSubscriptionStatus(subscribed: Boolean){
        _isSubscribed.value = subscribed
    }


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


    fun getRssUrlByName(name: String): String? {
        return rssItems.value?.find { it.name == name }?.url
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

    fun syncLikesFromFirebaseToRoom(){
        viewModelScope.launch {
            repository.syncLikesFromFirebaseToRoom()
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
        _categoryLoadingStates.update { it + (category to true) }
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
            _categoryLoadingStates.update { it + (category to false) }
        }
    }

    fun fetchGuardianNews(section: String = "football"){
        viewModelScope.launch {
            _guardianNewsData.value = repository.getGuardianNews(section)
        }
    }

    fun fetchRssNews(category: String, url: String) {
        _rssLoadingStates.update { it + (category to true) }
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
            _rssLoadingStates.update {it + (category to false)}
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


    fun getForYouNews(userCategories: List<String>) {
        viewModelScope.launch {
            val likeStatesDeferred = async { repository.getLikeStates() }

            // Clear previous flags
            _categoryLoadingStates.value = userCategories.associateWith { true }
            _rssLoadingStates.value = rssItems.value?.associate { it.name to true } ?: emptyMap()

            // Trigger fetches
            userCategories.forEach { fetchNewsForCategory(it) }
            rssItems.value?.forEach { fetchRssNews(it.name, it.url) }

            // Wait until all flags become false
            combine(_categoryLoadingStates, _rssLoadingStates) { catFlags, rssFlags ->
                val allCatDone = catFlags.values.all { !it }
                val allRssDone = rssFlags.values.all { !it }
                allCatDone && allRssDone
            }.filter { it }.first()

            // Once all done, combine the data
            val allArticles = mutableListOf<NewsData>()
            newsData.value?.forEach { (category, list) -> allArticles.addAll(list) }
            _rssNewsData.forEach { (_, liveData) -> liveData.value?.let { allArticles.addAll(it) } }

            // 5. Apply like state
            val liked = likeStatesDeferred.await()
            val processed = allArticles.distinctBy { it.articleUrl }.map {
                it.copy(isLike = liked[it.articleUrl] ?: false)
            }
            _forYouData.value = processed
        }
    }

    /*
    fun getForYouNews(userCategories: List<String>) {
        viewModelScope.launch {
            val likeStatesDeferred = async { repository.getLikeStates() }

            val allArticles = mutableListOf<NewsData>()

            // 1. Trigger category fetches (these update _newsData internally)
            userCategories.forEach { category ->
                fetchNewsForCategory(category)
            }

            // Wait a bit to allow _newsData to update (optional but helps)
           // delay(800) // Could be adjusted or replaced by a smarter signal

            // 2. Collect articles from updated _newsData
            val currentNewsMap = newsData.value ?: emptyMap()
            userCategories.forEach { category ->
                currentNewsMap[category]?.let { allArticles.addAll(it) }
            }

            // 3. Trigger RSS fetches
            rssItems.value?.forEach { rssItem ->
                fetchRssNews(rssItem.name, rssItem.url)
            }

            // Wait for RSS data to arrive
            //delay(800)

            // 4. Collect from _rssNewsData
            rssItems.value?.forEach { rssItem ->
                _rssNewsData[rssItem.name]?.value?.let { allArticles.addAll(it) }
            }

            // 5. Apply like state
            val liked = likeStatesDeferred.await()
            val processed = allArticles.distinctBy { it.articleUrl }.map {
                it.copy(isLike = liked[it.articleUrl] ?: false)
            }

            _forYouData.value = processed.shuffled()
        }
    }
    */


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

    fun setAllTabs(tabs: List<String>){
        _allTabs.value = tabs
    }


}