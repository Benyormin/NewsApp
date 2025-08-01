package com.example.newsapp.view

import com.example.newsapp.viewmodel.NewsViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentNewsListBinding
import com.example.newsapp.db.RssUrl
import com.example.newsapp.model.NewsData
import com.google.android.material.snackbar.Snackbar

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
    private var rssUrl: String = ""
    private lateinit var category : String

    private var hasLoaded = false

    // Key for passing category as an argument
    companion object {
        private const val ARG_CATEGORY = "category"
        private const val ARG_IS_RSS = "is_rss"
        private const val ARG_RSS_URL = "rss_url"

        fun newInstance(category: String): NewsListFragment {
            val fragment = NewsListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            args.putBoolean(ARG_IS_RSS, false)
            fragment.arguments = args
            return fragment
        }

        fun newRssInstance(name: String, url: String): NewsListFragment {
            return NewsListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, name)
                    putBoolean(ARG_IS_RSS, true)
                    putString(ARG_RSS_URL, url)
                }
            }
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
        category = arguments?.getString(ARG_CATEGORY) ?: "Unknown"
        val isRss = arguments?.getBoolean(ARG_IS_RSS) ?: false

        //TODO:: why I put this? the Toast?
        if(isRss){
            rssUrl = arguments?.getString(ARG_RSS_URL) ?: ""
            //Toast.makeText(requireContext(), "this is the Url: $rssUrl", Toast.LENGTH_SHORT).show()

        }

        Log.d("ForYouDebug", "onViewCreated: called with category=$category")


        //loadNews(category, rssUrl)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            loadNews(category, rssUrl)
        }



        observeData(category)

        viewModel.forYouData.observe(viewLifecycleOwner) {
            Log.d("ForYouDebug", "Observer triggered: ${it.size} items")
            // update UI or whatever
        }

        //viewModel.getForYouNews()

    }


    private fun observeData(category: String) {
        // FOR YOU
        if (category == "For you") {
            viewModel.forYouData.observe(viewLifecycleOwner) { list ->
                Log.d("ForYouDebug", "Observed forYouData: ${list?.size} items, category=$category")
                if (!list.isNullOrEmpty()) {
                    updateRecyclerView(list)
                } else {
                    Toast.makeText(requireContext(), "Empty for you news!", Toast.LENGTH_SHORT).show()
                }
            }

            /*viewModel.isForYouLoading.observe(viewLifecycleOwner) { isLoading ->
                Log.d("ForYouDebug", "Observed isForYouLoading=$isLoading for category=$category")
                if (isLoading) {
                    Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                }
            }*/
        }

        // FOOTBALL
        if (category == "Football") {
            viewModel.footballData.observe(viewLifecycleOwner) { list ->
                if (!list.isNullOrEmpty()) {
                    updateRecyclerView(list)
                } else {
                    Toast.makeText(requireContext(), "Empty football news!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // NORMAL API CATEGORIES (non-RSS)
        if (category != "Football" && category != "For you" &&
            viewModel.rssItems.value?.none { it.name == category } == true
        ) {
            viewModel.newsData.observe(viewLifecycleOwner) { dataMap ->
                val articles = dataMap[category] ?: emptyList()
                updateRecyclerView(articles)
            }
        }

        // RSS CATEGORIES
        if (viewModel.rssItems.value?.any { it.name == category } == true) {
            viewModel.rssNewsData[category]?.observe(viewLifecycleOwner) { list ->
                if (list != null) {
                    updateRecyclerView(list)
                }
            }
        }
    }



    /*
        private fun observeData(category: String) {

            viewModel.rssItems.observe(viewLifecycleOwner) { rssList ->

                Log.d("NewsListFragment", "observe data, rssItems $rssList")

                Log.d("Comparison", "Checking category: '$category'")
                rssList.forEach {
                    Log.d(
                        "Comparison",
                        "RssUrl name: '${it.name}' vs category: '${category}' → ${it.name == category}"
                    )
                }


                if(category == "Football"){

                    viewModel.footballData.observe(viewLifecycleOwner){
                            it ->
                        if(!it.isNullOrEmpty()){
                            updateRecyclerView(it)
                        }else{
                            Toast.makeText(requireContext(), "Empty football news!", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                else if (category == "For you") {

                    viewModel.isForYouLoading.observe(viewLifecycleOwner) { isLoading ->
                        if (isLoading) {
                            Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                        }
                    }

                    viewModel.forYouData.observe(viewLifecycleOwner) { it ->
                        if (!it.isNullOrEmpty()) {
                            updateRecyclerView(it)
                        } else {
                            Toast.makeText(requireContext(), "Empty for you news!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                }
                else if (rssList.any{ it.name == category}){
                    Log.d("NewsListFragment", "rssNewsData.observe has been called")
                    Log.d("NewsListFragment", "rssNewsData.observe${rssList}")
                    Log.d("NewsListFragment","Observing RssNews for ${category}")
                    viewModel.CategoryInitialized(category)
                    viewModel.rssNewsData[category]?.observe(viewLifecycleOwner){
                            it ->
                        if(it != null)
                            updateRecyclerView(it)
                    }
                    // 3. Trigger fetch (now safe)
                    viewModel.fetchRssNews(category, rssUrl)
                }
                else{
                    viewModel.newsData.observe(viewLifecycleOwner){
                            it ->
                        val articles = it[category]?: emptyList()
                        updateRecyclerView(articles)
                    }
                }
            }



        }

        private fun loadNews(category: String, rssUrl: String) {

            viewModel.rssItems.observe(viewLifecycleOwner){
                rssList ->

                if(category == "Football"){
                    viewModel.getFootballNews()
                }
                else if( category == "For you"){
                    viewModel.userCategories.observe(viewLifecycleOwner){
                        val tabs = it?.categories?: listOf()
                        val rssNames = rssList.map { it.name }
                        viewModel.getForYouNews((tabs + rssNames))
                    }
                }
                else if(rssList.any{it.name == category}){
                    Log.d("NewsListFragment", "category is : $category")
                    viewModel.fetchRssNews(category, rssUrl)
                    Log.d("NewsListFragment", "fetchRssNews has been called")
                    Log.d("NewsListFragment", "rssNewsData: ${viewModel.rssNewsData[category]?.value}")
                }
                else
                    viewModel.fetchNewsForCategory(category)

            }




            swipeRefreshLayout.isRefreshing = false

        }
    */

    private fun loadNews(category: String, rssUrl: String) {
        Log.d("ForYouDebug", "loadNews: category=$category, rssURL: $rssUrl")
        val rssList = viewModel.rssItems.value.orEmpty()

        when {
            category == "Football" -> {
                viewModel.getFootballNews()
            }

            category == "For you" -> {
                Log.d("ForYouDebug", "userCategories: ${viewModel.userCategories.value}")

                viewModel.userCategories.observe(viewLifecycleOwner) { userCat ->
                    Log.d("ForYouDebug", "Observed userCategories: $userCat")
                    if (userCat != null) {
                        val tabs = userCat.categories
                        val rssNames = rssList.map { it.name }
                        viewModel.getForYouNews(tabs + rssNames)
                    }
                }
            }

            rssList.any { it.name == category } -> {
                viewModel.fetchRssNews(category, rssUrl)
            }

            else -> {
                viewModel.fetchNewsForCategory(category)
            }
        }

        swipeRefreshLayout.isRefreshing = false
    }



    private fun updateRecyclerView(articles: List<NewsData>){
        var adapter = NewsAdapter(articles,
            onBookmarkClick = {
                article ->
                Log.d("Bookmark", "onBookmarkClick running...")
                viewModel.toggleBookmark(article)
            },
            onItemClick = { article ->
                if(article.articleUrl == null){
                    Log.e("NewsListFragment", "URL is null")
                    Toast.makeText(requireContext(), "Url is null", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("NewsListFragment", "Navigating to ArticleFragment with URL: ${article.articleUrl}")
                    val action = HomeFragmentDirections.actionHomeFragmentToArticleFragment(article)
                    findNavController().navigate(action)
                }
                // Navigate to ArticleFragment using Safe Args

            },
            onLikeClick = {
                article ->
                viewModel.toggleLikes(article)
            }
            )


        //val adapter = NewsAdapter(article)
        binding.rvNews.adapter = adapter
        binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private val networkChangeReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            if (::category.isInitialized && isNetworkAvailable()) {
                loadNews(category,rssUrl)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireContext().registerReceiver(networkChangeReceiver, filter)

        if (!hasLoaded && isVisible) {
            loadNews(category, rssUrl)
            hasLoaded = true
            Log.d("ViewPager", "onResume: category= $category, hasLoaded=$hasLoaded")
        }
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
