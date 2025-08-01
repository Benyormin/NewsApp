package com.example.newsapp.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsapp.R
import com.example.newsapp.viewmodel.NewsViewModel
import com.example.newsapp.viewmodel.SummaryViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.newsapp.RetrofitClient
import com.example.newsapp.api.UrlRequest
import com.example.newsapp.utils.ObservableWebView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ArticleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val args: ArticleFragmentArgs by navArgs()
    private lateinit var fabBookmark: FloatingActionButton
    private lateinit var viewModel: NewsViewModel
    private lateinit var summaryViewModel: SummaryViewModel
    private lateinit var fabSummary: FloatingActionButton
    private lateinit var webView: WebView
    private var currentSummaryDialog: SummaryDialogFragment? = null


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
        fabBookmark = view.findViewById(R.id.fabBookmark)
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        summaryViewModel = ViewModelProvider(requireActivity()).get(SummaryViewModel::class.java)
        fabSummary = view.findViewById(R.id.fabSummarize)

        val webView = view.findViewById<ObservableWebView>(R.id.webView)
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.visibility = View.VISIBLE
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)



        // Get the URL from arguments
        val article = args.newsData

        if (article.isBookmarked) {
            fabBookmark.setImageResource(R.drawable.bookmark_24dp_black_filled)
        }

        fabSummary.setOnClickListener {

            if (currentSummaryDialog?.isVisible == true) {
                // Dialog is already showing, just bring to front
                if (currentSummaryDialog?.isAdded == true) {
                    currentSummaryDialog?.dismissAllowingStateLoss()

                }
            }else if( article.articleUrl in summaryViewModel.SummarizedArticles.value.orEmpty()) {
                      //the article already has been summarized
                Log.d("Summary", "article already summarized")
                val sum = summaryViewModel.summary.value
                val title = sum?.get(article.articleUrl)?.first
                val summary = sum?.get(article.articleUrl)?.second
                if (title != null && summary != null) {
                    showOrRestoreSummary(title, summary)
                }else{
                    Toast.makeText(context, "Something went wrong!", Toast.LENGTH_LONG)
                        .show()
                }

                } else {

            view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

            lifecycleScope.launchWhenResumed  {
                //val loadingDialog = showLoadingDialog()
                try {

                    //loadingDialog.show() // Show loader

                    Log.d("Summary","article Url: ${article.articleUrl}")
                    val response =
                        RetrofitClient.summaryRetrofit.summarizeArticle(UrlRequest(article.articleUrl))

                   // loadingDialog.dismiss() // Hide loader on success
                    //showSummaryDialog(response.title, response.summary)
                    showOrRestoreSummary(response.title, response.summary)
                    summaryViewModel.setSummary(article.articleUrl, response.title, response.summary)
                    // add to the url so we don't make request again
                    summaryViewModel.addSummarizedArticles(article.articleUrl)
                    // Show the summary in a dialog or new activity
                    //showSummaryDialog(response.title, response.summary)
                    Log.d(
                        "Summary",
                        "received from backend ${response.title} \n ${response.summary}"
                    )
                } catch (e: Exception) {
                    //loadingDialog.dismiss()
                    Log.e("Summary", "Error: ${e.message}")
                    Toast.makeText(context, "Failed to summarize: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                } finally {
                   view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                }


            }
            }
        }


            // Load URL into WebView
            //webView = view.findViewById<WebView>(R.id.webView)
            if (article.articleUrl.isNullOrEmpty()) {
                Log.e("Article Fragment ", "URL is null or empty")
            } else {
                //Web View logic
                webView.settings.javaScriptEnabled = true
                webView.settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                webView.settings.domStorageEnabled = true
                webView.settings.databaseEnabled = true
                webView.settings.loadsImagesAutomatically = true

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                   webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    webView.settings.domStorageEnabled = true
                    webView.settings.loadsImagesAutomatically = true
                }*/
                webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                webView.settings.loadsImagesAutomatically = true
                //webView.webViewClient = WebViewClient()
                webView.loadUrl(article.articleUrl)

                swipeRefresh.setOnRefreshListener {
                    webView.reload()
                }

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        swipeRefresh.isRefreshing = false
                    }
                }


                fabBookmark.setOnClickListener {
                    article.isBookmarked = !article.isBookmarked
                    viewModel.toggleBookmark(article)
                    if (article.isBookmarked) {
                        fabBookmark.setImageResource(R.drawable.bookmark_24dp_black_filled)
                        Toast.makeText(requireContext(), "Added to bookmarks!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        fabBookmark.setImageResource(R.drawable.bookmark_24dp_black)
                        Toast.makeText(
                            requireContext(),
                            "Removed from bookmarks!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                }

            }

        //remove bottom navigation and buttons on scroll
        webView.scrollChangedCallback = object : ObservableWebView.OnScrollChangedCallback {
            override fun onScroll(scrollY: Int, oldScrollY: Int) {
                if (scrollY > oldScrollY) {
                    // Scrolling down → hide bottom nav
                    bottomNav.animate().translationY(bottomNav.height.toFloat()).setDuration(200).start()
                    fabBookmark.hide()
                    fabSummary.hide()
                    resetFabMargin(fabBookmark)
                    resetFabMargin(fabSummary)

                } else if (scrollY < oldScrollY) {
                    // Scrolling up → show bottom nav
                    bottomNav.animate().translationY(0f).setDuration(200).start()
                    fabBookmark.show()
                    fabSummary.show()
                    setFabAboveBottomNav(fabBookmark, bottomNav)
                }
            }
        }


    }



    private fun showSummaryDialog(title: String, summary: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(summary)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }



    private fun showLoadingDialog(): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_loading)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        return dialog
    }



    private fun showOrRestoreSummary(title: String, summary: String) {
        if (!isAdded || isDetached) return

        // If there's an existing dialog and it's attached, dismiss it safely.
        if (currentSummaryDialog?.isAdded == true) {
            currentSummaryDialog?.dismissAllowingStateLoss()
            // Ensure pending transactions are completed before proceeding.
            childFragmentManager.executePendingTransactions()
        }

        // Create and show a new instance.
        currentSummaryDialog = SummaryDialogFragment.newInstance(title, summary).also {
            it.show(childFragmentManager, "summary_dialog")
        }
    }

    fun setFabAboveBottomNav(fab: FloatingActionButton, bottomNav: BottomNavigationView, extraDp: Int = 20) {
        val params = fab.layoutParams as ViewGroup.MarginLayoutParams
        val density = fab.resources.displayMetrics.density

        // Total margin = BottomNav height in px + extraDp converted to px
        params.bottomMargin = bottomNav.height + (extraDp * density).toInt()
        fab.layoutParams = params
    }

    fun resetFabMargin(fab: FloatingActionButton, originalDp: Int = 20) {
        val params = fab.layoutParams as ViewGroup.MarginLayoutParams
        val density = fab.resources.displayMetrics.density
        params.bottomMargin = (originalDp * density).toInt()
        fab.layoutParams = params
    }




}
