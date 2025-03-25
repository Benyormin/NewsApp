package com.example.newsapp.view

import NewsRepository
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION_CODES.N
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.RetrofitClient
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.api.NewsApiService
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.model.NewsData
import com.example.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var searchRv: RecyclerView
    private lateinit var adapter: NewsAdapter

    private lateinit var  repository : NewsRepository
    private lateinit var viewModel: NewsViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = ArticleDatabase.invoke(requireContext().applicationContext)
        val dao = database.getArticleDao()
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        repository = NewsRepository(
            RetrofitClient.newsApiService,
            RetrofitClient.guardianApiService,
            RetrofitClient.espnApiService,
            dao,
            requireContext()
        )

        // Setup views
        searchView = view.findViewById(R.id.searchView)
        searchRv = view.findViewById(R.id.searchRv)
        adapter = NewsAdapter(emptyList(),
            onItemClick =  { newsItem ->
            openArticle(newsItem)
        },
            onBookmarkClick = {article -> viewModel.toggleBookmark(article)},
            onLikeClick = {
                    article ->
                viewModel.toggleLikes(article)
            }
            )

        searchRv.adapter = adapter
        searchRv.layoutManager = LinearLayoutManager(requireContext())

        setupSearchView()

        observeData()

    }

    private fun observeData() {
        viewModel.searchedData.observe(viewLifecycleOwner){
            it ->
            if(!it.isNullOrEmpty()){
                adapter.updateData(it)
            }

        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Trigger network call when user presses "Enter"
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Trigger search as user types (debounce recommended)
                return false
            }
        })
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading indicator (you can use a ProgressBar or a custom loading view)
        showLoadingIndicator(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.searchNews(query)

            } catch (e: Exception) {
                // Handle error
                Toast.makeText(requireContext(), "Failed to fetch results: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SearchFragment", "Search error: ${e.message}")
            } finally {
                // Hide loading indicator
                showLoadingIndicator(false)
            }
        }
    }

    private fun openArticle(url: String) {

        Log.d("search fragment", "Navigating to ArticleFragment with URL: $url")
        val action = SearchFragmentDirections.actionSearchFragmentToArticleFragment(url)
        findNavController().navigate(action)
    }

    private fun showLoadingIndicator(show: Boolean) {
        // Implement a loading indicator (e.g., ProgressBar)
        // Example: If you have a ProgressBar in your layout, toggle its visibility
        // progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}