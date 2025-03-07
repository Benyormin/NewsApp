package com.example.newsapp.view

import com.example.newsapp.viewmodel.NewsViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentNewsListBinding

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class NewsListFragment : Fragment() {

    private var _binding: FragmentNewsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: NewsViewModel
    private lateinit var category: String

    // Key for passing category as an argument
    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): NewsListFragment {
            val fragment = NewsListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)

        swipeRefreshLayout = binding.swipeRefreshLayout

        // Get the category from arguments
        val category = arguments?.getString(ARG_CATEGORY) ?: "Unknown"
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            loadNews(category)
        }

        //loadNews(category)

        // Observe news data for this category
        viewModel.newsData.observe(viewLifecycleOwner) { newsMap ->
            val articles = newsMap[category] ?: emptyList()
            val adapter = NewsAdapter(articles)
            binding.rvNews.adapter = adapter
            binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
        }

    }

    private fun loadNews(category: String) {
        viewModel.fetchNewsForCategory(category)
        swipeRefreshLayout.isRefreshing = false

    }
  /*  private fun loadNews(category: String) {
        // Show loading spinner
        swipeRefreshLayout.isRefreshing = true

        // Launch a coroutine tied to the fragment's lifecycle
        lifecycleScope.launch {
            try {
                // Create Retrofit instance
                val unsafeHttpClient = OkHttpClient.Builder()
                    .sslSocketFactory(createInsecureSslSocketFactory(), InsecureTrustManager())
                    .hostnameVerifier { _, _ -> true }
                    .addInterceptor(HttpLoggingInterceptor().apply { level = Level.BODY })
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://newsapi.org/v2/")
                    .client(unsafeHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                // Create API service
                val newsApiService = retrofit.create(NewsApiService::class.java)

                // Make the API call (suspend function)
                val response = newsApiService.getNewsByCategory(category, Constants.NEWS_KEY)

                // Check if the response is successful
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    val adapter = NewsAdapter(articles)
                    binding.rvNews.adapter = adapter
                    binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
                } else {
                    // Handle API error
                    Toast.makeText(requireContext(), "Failed to fetch news", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle network or other errors
                Log.e("NETWORK_ERROR", "Failed to fetch news: ${e.message}")
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            } finally {
                // Hide loading spinner
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }*/


    /* private fun loadNews(category: String) {

    /*    val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    */


        val unsafeHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(createInsecureSslSocketFactory(), InsecureTrustManager())
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(HttpLoggingInterceptor().apply { level = Level.BODY })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .client(unsafeHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val newsApiService = retrofit.create(NewsApiService::class.java)

        val call = newsApiService.getNewsByCategory(category, Constants.NEWS_KEY)


        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    val adapter = NewsAdapter(articles)
                    binding.rvNews.adapter = adapter
                    binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
                } else {
                    // Handle API error
                    Toast.makeText(requireContext(), "Failed to fetch news", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                // Handle network error
                // Handle network errors (e.g., no internet)
                Log.e("NETWORK_ERROR", "Failed to fetch news: ${t.message}")
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })

        swipeRefreshLayout.isRefreshing = false
    }
*/

    /* private fun loadNewsForCategory(category: String) {
         // TODO: Load news for the given category
         // For example, you can call an API or fetch data from a database
         binding.textViewCategory.text = "News for $category"
     }
 */
    // TODO: recyclerView
/*
    private fun loadNewsForCategory(category: String) {
        // Example: Fetch news for the category
        //val newsList = fetchNewsFromApi(category)

        val newsList = NewsArticle.createData(requireContext())
        // Example: Display news in a RecyclerView
        val adapter = NewsAdapter(newsList)
        binding.rvNews.adapter = adapter
        binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
    }
*/


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private val networkChangeReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            if (::category.isInitialized && isNetworkAvailable()) {
                loadNews(category)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireContext().registerReceiver(networkChangeReceiver, filter)

    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(networkChangeReceiver)
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnectedOrConnecting == true

    }
}

class InsecureTrustManager : X509TrustManager {
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()


}
fun createInsecureSslSocketFactory(): SSLSocketFactory {
    val trustAllCerts = arrayOf<TrustManager>(InsecureTrustManager())
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())
    return sslContext.socketFactory
}
