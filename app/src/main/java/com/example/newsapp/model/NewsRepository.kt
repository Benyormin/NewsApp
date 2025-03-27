import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData

import com.example.newsapp.api.EspnApiService
import com.example.newsapp.api.GuardianApiService
import com.example.newsapp.utils.Constants
import com.example.newsapp.api.NewsApiService
import com.example.newsapp.db.ArticlesDAO
import com.example.newsapp.db.RssUrl
import com.example.newsapp.model.NewsData
import com.example.newsapp.model.Source

class NewsRepository(
    private val newsApiService: NewsApiService,
    private val guardianService: GuardianApiService,
    private val EspnService: EspnApiService,
    private val dao: ArticlesDAO,
    private val context: Context
    ) {



    val bookmarkedArticles: LiveData<List<NewsData>> = dao.getBookmarkedArticles()
    val rssUrls: LiveData<List<RssUrl>> = dao.getAllRssUrlStrings()

    suspend fun getLikeStates(): Map<String, Boolean> {

        return dao.getLikedStates().associate { it.url to it.isLiked }
    }

    suspend fun addRssUrls(name:String, url:String){
        dao.insertRss(RssUrl(name= name, url = url))
    }
    suspend fun deleteRssUrl(rssUrl: RssUrl){
        dao.deleteRss(rssUrl)
    }


    suspend fun updateBookmark(article: NewsData) {
        Log.d("Bookmark", "News Repo: updateBookmark has been called")
        //article.isBookmarked = !article.isBookmarked
        Log.d("Bookmark", "News Repo: article.isbookmarked = ${article.isBookmarked}")
        //dao.updateArticle(article)
        dao.upsert(article)
    }
    suspend fun updateLikes(article: NewsData){
        dao.upsert(article)
        Log.d("NewsRepository", "Like has been added with the id of:\n ${article.title} \n id: ${article.uid} \n")
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
     *
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


    //get sport news

    /**
     *
     * Returns football news
      */
/*
    suspend fun getFootballNews(): List<NewsData> {
        val r2 = guardianService.getGuardianNews(section = "football")
        val r1 = newsApiService.getNewsByCategory("football")

        // Combine or process responses as needed
        return response1.articles + response2.articles + response3.articles
    }


*/
}
//TODO: Create other class in this package for each API in order to extract text