package com.example.newsapp.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.newsapp.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds


class AddsFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_adds, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        MobileAds.initialize(requireContext()) {}
        loadBanner(view, requireContext())


    }



    private fun loadBanner(view: View, context: Context) {
        val adView = AdView(context).apply {
            adUnitId = "ca-app-pub-3940256099942544/9214589741" // Test unit

            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 360))
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("Ads", "Ad loaded ✅")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Ads", "Ad failed ❌: ${adError.message}")
                }
            }
        }

        val container = view.findViewById<FrameLayout>(R.id.adviewFrameLayout)
        container.removeAllViews()
        container.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }
}