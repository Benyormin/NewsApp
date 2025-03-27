package com.example.newsapp.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentBookmarksBinding
import com.example.newsapp.viewmodel.NewsViewModel


class BookmarksFragment : Fragment() {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var binding: FragmentBookmarksBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookmarksBinding.bind(view)
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        setUpBookmarksRecyclerView()


        viewModel.bookMarkedArticles.observe(viewLifecycleOwner){
            bookMarked->
            newsAdapter.updateData((bookMarked))
        }

    }

    private fun setUpBookmarksRecyclerView() {
        newsAdapter = NewsAdapter(
            newsDataList = viewModel.bookMarkedArticles.value ?: emptyList(), // Use current data
            onItemClick= {
                url ->
                    if(url == null){
                        Log.e("Bookmarks Fragment", "URL is null")
                        Toast.makeText(requireContext(), "Url is null", Toast.LENGTH_SHORT).show()
                    }else{
                        Log.d("Bookmarks Fragment", "Navigating to ArticleFragment with URL: $url")
                        val action = BookmarksFragmentDirections.actionBookmarksFragmentToArticleFragment(url)
                        findNavController().navigate(action)
                    }

            },
            onBookmarkClick =  {
                    article ->
                viewModel.toggleBookmark((article))
            },
            onLikeClick = {
                    article ->
                viewModel.toggleLikes(article)
            }

        )
        binding.rvBookmark.adapter = newsAdapter
        binding.rvBookmark.layoutManager = LinearLayoutManager(activity)

    }

}