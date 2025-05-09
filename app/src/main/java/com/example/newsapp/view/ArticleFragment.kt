package com.example.newsapp.view

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.newsapp.R
import com.example.newsapp.viewmodel.NewsViewModel
import com.example.newsapp.viewmodel.SummaryViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.newsapp.RetrofitClient
import com.example.newsapp.api.UrlRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


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
            webView = view.findViewById<WebView>(R.id.webView)
            if (article.articleUrl.isNullOrEmpty()) {
                Log.e("Article Fragment ", "URL is null or empty")
            } else {
                webView.settings.javaScriptEnabled = true
                webView.loadUrl(article.articleUrl)
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


        }


        suspend fun fetchHtmlViaWebView(url: String): String? =
            suspendCancellableCoroutine { continuation ->
                val webView = WebView(requireContext()).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        private var isCompleted = false // Flag to prevent double resumption

                        override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                            if (isCompleted) return // Skip if already done
                            isCompleted = true

                            view?.evaluateJavascript("document.documentElement.outerHTML") { html ->
                                if (continuation.isActive) {
                                    continuation.resume(
                                        html?.removeSurrounding("\"") // Remove JSON quotes
                                    )
                                }
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            if (isCompleted) return
                            isCompleted = true
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                            Log.e("WebView", "Error loading URL: $description")
                        }
                    }
                    loadUrl(url)
                }

                // Cleanup on cancellation
                continuation.invokeOnCancellation {
                    webView.destroy()
                }
            }


/*
    private fun showLoadingDialog(): androidx.appcompat.app.AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.dialog_loading) // Create this layout (see below)
            .setCancelable(false)
            .create()
    }
*/
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


}
