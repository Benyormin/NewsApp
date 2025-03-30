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
    private lateinit var category: String
    private var rssUrl: String = ""

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
        val category = arguments?.getString(ARG_CATEGORY) ?: "Unknown"
        val isRss = arguments?.getBoolean(ARG_IS_RSS) ?: false

        if(isRss){
            rssUrl = arguments?.getString(ARG_RSS_URL) ?: ""
            Toast.makeText(requireContext(), "this is the Url: $rssUrl", Toast.LENGTH_SHORT).show()

        }
        loadNews(category, rssUrl)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            loadNews(category, rssUrl)
        }



        observeData(category)

    }

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
            else if(rssList.any{it.name == category}){
                viewModel.fetchRssNews(category, rssUrl)
                Log.d("NewsListFragment", "fetchRssNews has been called")
                Log.d("NewsListFragment", "rssNewsData: ${viewModel.rssNewsData[category]?.value}")
            }
            else
                viewModel.fetchNewsForCategory(category)

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
            onItemClick = { url ->
                if(url == null){
                    Log.e("NewsListFragment", "URL is null")
                    Toast.makeText(requireContext(), "Url is null", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("NewsListFragment", "Navigating to ArticleFragment with URL: $url")
                    val action = HomeFragmentDirections.actionHomeFragmentToArticleFragment(url)
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
