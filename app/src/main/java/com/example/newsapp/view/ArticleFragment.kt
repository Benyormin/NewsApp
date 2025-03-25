package com.example.newsapp.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.navigation.fragment.navArgs
import com.example.newsapp.R


class ArticleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val args: ArticleFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_article, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the URL from arguments
        val url = args.articleUrl


        // Load URL into WebView
        val webView = view.findViewById<WebView>(R.id.webView)
        if(url.isNullOrEmpty()){
            Log.e("Article Fragment ","URL is null or empty")
        }else{
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)
        }

    }


}