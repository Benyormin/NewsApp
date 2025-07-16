package com.example.yourapp.utils

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

object AdsManager {
    fun loadBannerAd(context: Context, adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}
