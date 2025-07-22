package com.example.newsapp.view


import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build.VERSION_CODES.N
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
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
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var searchRv: RecyclerView
    private lateinit var adapter: NewsAdapter
    private lateinit var tvSearch: TextView

    private lateinit var repository: NewsRepository
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
        tvSearch = view.findViewById(R.id.tvSearch)


      /*  val searchEditText = searchView.findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val textColor = ContextCompat.getColor(requireContext(), if (isDark) R.color.white else R.color.black)
        searchEditText.setTextColor(textColor) */

        adapter = NewsAdapter(emptyList(),
            onItemClick = { newsItem ->
                openArticle(newsItem)
            },
            onBookmarkClick = { article -> viewModel.toggleBookmark(article) },
            onLikeClick = { article ->
                viewModel.toggleLikes(article)
            }
        )

        searchRv.adapter = adapter
        searchRv.layoutManager = LinearLayoutManager(requireContext())

        setupSearchView()

        observeData()

    }

    private fun observeData() {
        viewModel.searchedData.observe(viewLifecycleOwner) { it ->
            if (!it.isNullOrEmpty()) {
                adapter.updateData(it)
            }

        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Trigger network call when user presses "Enter"
                tvSearch.isInvisible = true
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
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT)
                .show()
            return
        }



        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.searchNews(query)

            } catch (e: Exception) {
                // Handle error
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch results: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("SearchFragment", "Search error: ${e.message}")
            } finally {

            }
        }
    }

    private fun openArticle(article: NewsData) {
        if (article.articleUrl == null) {
            Log.e("SearchFragment", "URL is null")
            Toast.makeText(requireContext(), "Url is null", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(
                "search fragment",
                "Navigating to ArticleFragment with URL: ${article.articleUrl}"
            )
            val action = SearchFragmentDirections.actionSearchFragmentToArticleFragment(article)
            findNavController().navigate(action)

        }


    }
}