package com.example.newsapp.utils

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class ObservableWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

    interface OnScrollChangedCallback {
        fun onScroll(scrollY: Int, oldScrollY: Int)
    }

    var scrollChangedCallback: OnScrollChangedCallback? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        scrollChangedCallback?.onScroll(t, oldt)
    }
}
