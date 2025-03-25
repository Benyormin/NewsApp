import android.util.Log
import com.example.newsapp.model.NewsData
import com.example.newsapp.model.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class RssRepository(private val rssUrl: String, private val source: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchRssNews(): List<NewsData> {
        return withContext(Dispatchers.IO) {
            val maxRetries = 3
            var retryCount = 0

            while (retryCount < maxRetries) {
                try {
                    val request = Request.Builder()
                        .url(rssUrl)
                        .build()

                    Log.d("RssRepository", "Executing request (attempt ${retryCount + 1})...")
                    val response = client.newCall(request).execute()
                    Log.d("RssRepository", "Response code: ${response.code}")

                    if (!response.isSuccessful) {
                        Log.e("RssRepository", "Request failed: ${response.code} - ${response.message}")
                        return@withContext emptyList()
                    }

                    val inputStream = response.body?.byteStream()
                    if (inputStream == null) {
                        Log.e("RssRepository", "Response body is null")
                        return@withContext emptyList()
                    }

                    Log.d("RssRepository", "Parsing response...")
                    val parsedData = RssParser(source).parse(inputStream)
                    Log.d("RssRepository", "Parsed ${parsedData.size} items")
                    return@withContext parsedData
                } catch (e: SocketTimeoutException) {
                    Log.e("RssRepository", "Timeout error (attempt ${retryCount + 1})", e)
                    retryCount++
                    if (retryCount >= maxRetries) {
                        Log.e("RssRepository", "Max retries reached")
                        return@withContext emptyList()
                    }
                } catch (e: Exception) {
                    Log.e("RssRepository", "Error fetching RSS feed", e)
                    return@withContext emptyList()
                }
            }
            emptyList()
        }
    }
}