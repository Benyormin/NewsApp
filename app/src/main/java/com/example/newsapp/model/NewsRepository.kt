package com.example.newsapp.repository
import RssRepository
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.example.newsapp.api.EspnApiService
import com.example.newsapp.api.GuardianApiService
import com.example.newsapp.api.HuggingFaceService
import com.example.newsapp.api.MercuryService
import com.example.newsapp.utils.Constants
import com.example.newsapp.api.NewsApiService
import com.example.newsapp.api.SummaryRequest
import com.example.newsapp.db.ArticlesDAO
import com.example.newsapp.db.Preferences
import com.example.newsapp.db.RssUrl
import com.example.newsapp.model.NewsData
import com.example.newsapp.model.Source
import com.example.newsapp.utils.Constants.Companion.CBSSPORT_RSS_URL
import com.example.newsapp.utils.Constants.Companion.COINTELEGRAPH_RSS_URL
import com.example.newsapp.utils.Constants.Companion.COMPUTERWEEKLY_RSS_URL
import com.example.newsapp.utils.Constants.Companion.CRYPTOPODCAST_RSS_URL
import com.example.newsapp.utils.Constants.Companion.ENVIRONMENTALFACTOR_RSS_URL
import com.example.newsapp.utils.Constants.Companion.GRIST_RSS_URL
import com.example.newsapp.utils.Constants.Companion.SCIENCEDAILYHEALTH_RSS_URL
import com.example.newsapp.utils.Constants.Companion.SCIENCEDAILY_RSS_URL
import com.example.newsapp.utils.Constants.Companion.SPORTSTAR_RSS_URL
import com.example.newsapp.utils.Constants.Companion.TECHREPUBLIC_RSS_URL
import com.example.newsapp.utils.Constants.Companion.TEDTALK_RSS_URL
import com.example.newsapp.utils.Constants.Companion.THEGAMER_RSS_URL
import com.example.newsapp.utils.Constants.Companion.WIREDBUSINESS_RSS_URL
import com.example.newsapp.utils.Constants.Companion.WIREDSCIENCE_RSS_URL
import com.example.newsapp.utils.Constants.Companion.WIRED_RSS_URL
import com.example.newsapp.utils.Constants.Companion.YAHOOSPORTS_RSS_URL
import com.example.newsapp.utils.HelperFuncitons.Companion.toMap
import com.example.newsapp.utils.safeAwait
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class NewsRepository(
    private val newsApiService: NewsApiService,
    private val guardianService: GuardianApiService,
    private val EspnService: EspnApiService,
    private val dao: ArticlesDAO,
    private val context: Context
    ) {



    val bookmarkedArticles: LiveData<List<NewsData>> = dao.getBookmarkedArticles()
    val rssUrls: LiveData<List<RssUrl>> = dao.getAllRssUrlStrings()
    val userCategories: LiveData<Preferences> = dao.getAllCategories()
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun updateCategories(categories: Preferences){

        dao.updateCategories(categories)
        Log.d("NewsRepository", "updated categories")

    }
    suspend fun getLikeStates(): Map<String, Boolean> {

        return dao.getLikedStates().associate { it.url to it.isLiked }
    }

    suspend fun updateLikes(article: NewsData){

        if (article.isLike){
           saveLikeToFirestore(article)
        }
        else {
            removeLikeFromFirestore(article)
        }
        //save it locally

        if (article.category.isBlank()) {
            Log.w("NewsRepository", "Article has no category. Cannot record like for top-category tracking.")
        }

        dao.upsert(article)
        Log.d("NewsRepository", "Like has been added with the id of:\n ${article.title} \n id: ${article.uid} \n")
    }


    suspend fun syncLikesFromFirebaseToRoom(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("likes")
                .get()
                .await()

            val likedUrls = snapshot.documents.mapNotNull { it.toObject(NewsData::class.java) }
            likedUrls.forEach {article ->
                dao.upsert(article.copy(isLike = true))
            }
        }
    }

    private fun removeLikeFromFirestore(article: NewsData) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null){
            firestore.collection("users")
                .document(userId)
                .collection("likes")
                .document(article.articleUrl.hashCode().toString())
                .delete()
        }
    }

    private fun saveLikeToFirestore(article: NewsData) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {

            firestore.collection("users")
                .document(userId)
                .collection("likes")
                .document(article.articleUrl.hashCode().toString())
                .set(article)

        }


    }





    suspend fun addRssUrls(name:String, url:String){
        dao.insertRss(RssUrl(name= name, url = url))
    }

    suspend fun insertRssFeeds(rssList: List<RssUrl>) {
     //this function should be called after user sign in
        dao.insertAll(rssList)
    }
    suspend fun deleteRssUrl(rssUrl: RssUrl){
        dao.deleteRss(rssUrl)
    }


    suspend fun updateBookmark(article: NewsData) {
        Log.d("Bookmark", "News Repo: updateBookmark has been called")

        Log.d("Bookmark", "News Repo: article.isbookmarked = ${article.isBookmarked}")
        // update article bookmark locally
        dao.upsert(article)

        val user = firebaseAuth.currentUser
        Log.d("Bookmark", "user is: ${user}")

        if (user != null) {
            val userId = user.uid
            Log.d("Bookmark", "User UID: $userId") // this shows actual UID

            val bookmarkRef = firestore
                .collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(article.articleUrl.hashCode().toString())

            if (article.isBookmarked) {

               bookmarkRef.set(article.toMap())
                    .addOnSuccessListener {
                        Log.d("Bookmark", "Successfully saved to Firestore")
                    }
                    .addOnFailureListener {
                        Log.e("Bookmark", "Firestore save failed: ${it.message}")
                    }
            } else {
                bookmarkRef.delete()
                    .addOnSuccessListener {
                        Log.d("Bookmark", "Successfully deleted from Firestore")
                    }
                    .addOnFailureListener {
                        Log.e("Bookmark", "Firestore delete failed: ${it.message}")
                    }
            }

        }
        else {
            Log.e("Bookmark", "User is null — not logged in!")
        }


    }

    suspend fun deleteArticle(article: NewsData) = dao.deleteArticle(article)


    /**
     * rerutns a list of newsData form NewsAPI service
     *
     */
    suspend fun getNewsByCategory(category: String): List<NewsData> {
        return try {
            val response = newsApiService.getNewsByCategory(category, Constants.NEWS_KEY)
            if (response.isSuccessful) {
                response.body()?.articles ?: emptyList()
            } else {
                Log.e("REPOSITORY_ERROR", "Failed to fetch news from APInews \n: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("REPOSITORY_ERROR", "Failed to fetch news: ${e.message}")
            emptyList()
        }
    }

    // Fetch news for multiple categories
    /**
     * rerutns a map of <String, list<newsData> form NewsAPI service for a single category
     *
     *
     */
    suspend fun getNewsForCategories(categories: List<String>): Map<String, List<NewsData>> {
        return categories.associateWith { category ->
            getNewsByCategory(category)
        }
    }


    /**
     *
     * Returns a List<newsData> for a given section from guardianService
     * TODO: Change football parameter
     */
    suspend fun getGuardianNews(section: String = "football"): List<NewsData>{
        return try {
            val response = guardianService.getGuardianNews(section = section, apiKey = Constants.GAURDIAN_KEY)
            if(response.isSuccessful){
                response.body()?.response?.results?.map { ga ->
                    NewsData(
                        title = ga.fields.headline,
                        description = ga.fields.description?:" ",
                        imageUrl = ga.fields.imageUrl?:" ",
                        articleUrl = ga.url,
                        publishedAt = ga.publishedDate,
                        source =  Source("-1", "The Guardian")
                    )

                }?: emptyList()

            }else{
                Log.e("GUARDIAN_ERROR", "not successful")
                emptyList()
                }

        }
        catch (e:Exception){
            Log.e("GUARDIAN_ERROR", "Failed to fetch guardian news: ${e.message}")
            Toast.makeText(context, "Failed to fetch guardian news", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    suspend fun searchGuardianNews(query: String): List<NewsData>{
        return try {
            val response = guardianService.searchGuardianNews(query = query, apiKey = Constants.GAURDIAN_KEY)
            if(response.isSuccessful){
                response.body()?.response?.results?.map { ga ->
                    NewsData(
                        title = ga.fields.headline,
                        description = ga.fields.description?:" ",
                        imageUrl = ga.fields.imageUrl?:" ",
                        articleUrl = ga.url,
                        publishedAt = ga.publishedDate,
                        source =  Source("-1", "The Guardian")
                    )

                }?: emptyList()

            }else{
                Log.e("GUARDIAN_ERROR", "not successful")
                emptyList()
            }

        }
        catch (e:Exception){
            Log.e("GUARDIAN_ERROR", "Failed to fetch guardian news from search: ${e.message}")
            Toast.makeText(context, "Failed to fetch guardian news from search query", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    suspend fun getRssNews(url: String, source: String): List<NewsData>
    {
        val rssRepository = RssRepository(url, source)
        Log.d("News Repository","get ${source} news has been called")

        return rssRepository.fetchRssNews()


    }


    suspend fun getEspnNews(): List<NewsData> {
            return try {
                //TODO: change the hardcoded text to changable like esp.1 eng.1 ,...
                val response = EspnService.getEspnNews()
                if(response.isSuccessful){
                    response.body()?.article?.map {
                        esp ->
                        if (esp.links.web == null) {
                            Log.w("ESPN", "Mobile link missing for article: ${esp.headline}")
                        }
                        NewsData(
                            title = esp.headline,
                            description = esp.description,
                            imageUrl = esp.images[0].imageUrl?:"",
                            articleUrl = esp.links.web?.articleUrl?: "",
                            publishedAt = esp.published,
                            source = Source("Espn", "Espn")
                        )
                    }?: emptyList()
                }else{
                    Log.e("ESPN_ERROR", "not successful")

                    emptyList()
                }


            }catch (e: Exception){
                Log.e("ESPN_ERROR", "Failed to fetch espn news: ${e.message}")
                Toast.makeText(context, "Failed to fetch Espn news", Toast.LENGTH_SHORT).show()
                emptyList()
            }
    }


    suspend fun searchNews(query: String): List<NewsData>{
         val newsAPI =  getNewsByCategory(query)
        //TODO:: Change the hard coded football section!
         val guardian = searchGuardianNews(query)

        val newsList = mutableListOf<NewsData>().apply {
            addAll(newsAPI)
            addAll(guardian)
        }
        if(newsList.isNotEmpty()){
         return newsList
        }
        else return emptyList()
    }



    suspend fun getSummary(url: String): String? {
        return try {
            // Step 1: Get Clean Article Text
            val mercury = Retrofit.Builder()
                .baseUrl("https://mercury.postlight.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MercuryService::class.java)

            val articleResponse = mercury.parseArticle(url)
            if (!articleResponse.isSuccessful || articleResponse.body()?.error == true) {
                return null
            }

            val articleText = articleResponse.body()?.content ?: return null

            // Step 2: Summarize
            val hf = Retrofit.Builder()
                .baseUrl("https://api-inference.huggingface.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HuggingFaceService::class.java)

            val summaryResponse = hf.summarize(SummaryRequest(articleText))
            summaryResponse.body()?.firstOrNull()?.summary
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getForYouArticlesForNotification(): List<NewsData> {

        val allArticles = mutableListOf<NewsData>()

        // 1. Get user category preferences
        Log.d("ForYouWorker", "🛠 Getting user preferences...")
        val preferences = dao.getPreferences()
        val categories = preferences?.categories?: emptyList()
       //val pref = userCategories.value
        //val categories = pref?.categories ?: emptyList()
        Log.d("ForYouWorker", "📦 Categories: $categories")

        // 2. Fetch articles from categories
        val news = getNewsForCategories(categories)
        Log.d("ForYouWorker", "📚 Articles: $news")

        for (n in news){
            allArticles.addAll(n.value)
        }
        Log.d("ForYouWorker", "📚 len all articles in the middle of work \n : ${allArticles.size}")

        // 3. Get user RSS preferences (stored in Room or elsewhere)

        //val rssItems = rssUrls.value ?: emptyList()
        val rssItems = dao.getAllRssUrls()
        Log.d("ForYouWorker", "📰 RSS: ${rssItems.size} ")

        for (rss in rssItems) {
            try {
                val rssArticles = getRssNews(rss.url, rss.name)
                allArticles.addAll(rssArticles)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 4. Apply like states if possible
        val likeStates = getLikeStates() // Optional if needed
        val processed = allArticles.distinctBy { it.articleUrl }.map {
            it.copy(isLike = likeStates[it.articleUrl] ?: false)
        }
        Log.d("ForYouWorker", "🧮 Total articles fetched: ${allArticles.size}")
        return processed
    }


    suspend fun getRssForCategory(category: String): List<NewsData> = coroutineScope {
        val rssSources = when (category.lowercase()) {
            "technology" -> listOf(
                async { getRssNews(WIRED_RSS_URL, "Wired") },
                async { getRssNews(COMPUTERWEEKLY_RSS_URL, "Computer Weekly") },
            )
            "science" -> listOf(
                async { getRssNews(WIREDSCIENCE_RSS_URL, "Wired") },
                async { getRssNews(SCIENCEDAILY_RSS_URL, "Science Daily") }
            )
            "environment" -> listOf(
                async { getRssNews(GRIST_RSS_URL, "Grist") }
            )
            "business" -> listOf(
                async { getRssNews(TECHREPUBLIC_RSS_URL, "Tech Republic") },
                async { getRssNews(WIREDBUSINESS_RSS_URL, "Wired")}
            )
            "health" -> listOf(
                async{getRssNews(SCIENCEDAILYHEALTH_RSS_URL, "Science Daily")},
                async { getRssNews(ENVIRONMENTALFACTOR_RSS_URL, "Environmental Factor") }
            )
            "education" -> listOf(
                async { getRssNews(TEDTALK_RSS_URL, "Ted Talk") }
            )
            "games" -> listOf(
                async { getRssNews(THEGAMER_RSS_URL, "The gamer")}
            )
            "crypto" -> listOf(
                async { getRssNews(COINTELEGRAPH_RSS_URL, "Coin Telegraph") },
                async { getRssNews(CRYPTOPODCAST_RSS_URL, "Crypto Podcast") }
            )
            "sports" -> listOf(
                async { getRssNews(CBSSPORT_RSS_URL, "CBS") },
                async { getRssNews(SPORTSTAR_RSS_URL, "Sport Star") },
                async { getRssNews(YAHOOSPORTS_RSS_URL, "Yahoo Sports") }

            )







            // Add other categories here...
            else -> listOf(async { emptyList<NewsData>() })
        }

        val results = rssSources.map { safeAwait(it) ?: emptyList() }
        results.flatten()
    }

    suspend fun getArticlesForCategoryNotification(category: String): List<NewsData> {
        return try {
            val newsDataDefferd = CoroutineScope(Dispatchers.IO).async{getNewsByCategory(category)}
            val rssDeferred = CoroutineScope(Dispatchers.IO).async { getRssForCategory(category) }
            val guardianDeferred = CoroutineScope(Dispatchers.IO).async { getGuardianNews(category) }


            val newsData = safeAwait(newsDataDefferd) ?: emptyList()
            val rssArticles = safeAwait(rssDeferred) ?: emptyList()
            val guardianArticles = safeAwait(guardianDeferred) ?: emptyList()

            (rssArticles + guardianArticles + newsData)
                .sortedByDescending { it.publishedAt }
                .take(10) // Optional: only most recent
        } catch (e: Exception) {
            Log.e("Repository", "Error getting category notification", e)
            emptyList()
        }
    }

    suspend fun getTopLikedCategories(): List<String> {
        val likedStates = dao.getLikedStates()
        Log.d("NewsRepository", "Liked states: $likedStates")
        return likedStates
            .groupingBy { it.category }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }




}
