import android.util.Log
import com.example.newsapp.Constants
import com.example.newsapp.api.NewsApiService
import com.example.newsapp.model.NewsData

class NewsRepository(private val newsApiService: NewsApiService) {

    // Fetch news for a specific category
    suspend fun getNewsByCategory(category: String): List<NewsData> {
        return try {
            val response = newsApiService.getNewsByCategory(category, Constants.NEWS_KEY)
            if (response.isSuccessful) {
                response.body()?.articles ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("REPOSITORY_ERROR", "Failed to fetch news: ${e.message}")
            emptyList()
        }
    }

    // Fetch news for multiple categories
    suspend fun getNewsForCategories(categories: List<String>): Map<String, List<NewsData>> {
        return categories.associateWith { category ->
            getNewsByCategory(category)
        }
    }



    //get sport news
    /*
    class NewsRepository(private val newsApiService: NewsApiService) {

    suspend fun getSportsNews(): List<NewsData> {
        val response1 = newsApiService.getSportsNews1()
        val response2 = newsApiService.getSportsNews2()
        val response3 = newsApiService.getSportsNews3()

        // Combine or process responses as needed
        return response1.articles + response2.articles + response3.articles
    }
}
     */

}
//TODO: Create other class in this package for each API in order to extract text