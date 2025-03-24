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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentNewsListBinding
import com.example.newsapp.model.NewsData

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
        loadNews(category)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            loadNews(category)
        }



        observeData(category)

    }

    private fun observeData(category: String) {
        if( category == "Football"){

            viewModel.footballData.observe(viewLifecycleOwner){
                it ->
                if(!it.isNullOrEmpty()){
                    updateRecyclerView(it)
                }else{
                    Toast.makeText(requireContext(), "Empty football news!", Toast.LENGTH_SHORT).show()
                }
            }
            /*viewModel.rssItems.observe(viewLifecycleOwner){
                it->
                if(!it.isNullOrEmpty()){
                    updateRecyclerView(it)
                }
            }
            /*
            viewModel.guardianNewsData.observe(viewLifecycleOwner){

                it ->
                if (it.isNullOrEmpty()){
                    Toast.makeText(requireContext(), "Empty guardian news!", Toast.LENGTH_SHORT).show()
                }
                updateRecyclerView(it)
            }

             */
        */
        }
        else{
            viewModel.newsData.observe(viewLifecycleOwner){
                it ->
                val articles = it[category]?: emptyList()
                updateRecyclerView(articles)
            }
        }

    }

    private fun loadNews(category: String) {
        if(category == "Football"){
            viewModel.getFootballNews()
            //viewModel.fetchGuardianNews("football")
            //viewModel.fetchBbcSportNews()
        }else{
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
