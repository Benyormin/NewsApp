import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentSettingBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class AdsManager(
    private val premiumRepository: PremiumRepository
) {

    companion object {
        var shouldShowNativeAd = true
        var adStatus: Boolean = false

        fun isAdVisible(): Boolean {
            return adStatus
        }
    }


        // track whether ad is allowed for close button


    fun loadBanner(container: FrameLayout, adView: AdView, fragmentId: String) {
        if (premiumRepository.isUserPremium()) {
            Log.d("AdsManager", "premium user! ad is off")
            adView.visibility = View.GONE
            adStatus = false
            return
        }



        val adRequest = AdRequest.Builder().build()
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("AdsManager", "Ad loaded for $fragmentId")
                premiumRepository.setLastAdLoadTime(fragmentId, System.currentTimeMillis())
                adView.visibility = View.VISIBLE
                adStatus = true
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                Log.d("AdsManager", "Ad failed to load for $fragmentId: ${p0}")
                adView.visibility = View.GONE
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d("AdsManager", "Retrying banner load for $fragmentId")
                    loadBanner(container, adView, fragmentId)
                }, 5000)
            }

        }


        // Clean container and add the AdView
        container.removeAllViews()
        container.addView(adView)

        adView.loadAd(adRequest)

    }

    fun hideAds(adView: AdView) {
        adView.visibility = View.GONE
        adStatus = false
    }


    fun createBannerAdView(context: Context): AdView {
        val adView = AdView(context)
        adView.adUnitId = context.getString(R.string.admob_banner_id)
        Log.d("AdsManager", "create Banner with ID: $adView.adUnitId")


        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()


        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context,
            screenWidthDp
        )

        //adView.setAdSize(adSize)
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 360))
        return adView
    }


    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.mediaView = adView.findViewById(R.id.ad_media)



        // Headline (required)
        (adView.headlineView as TextView).text = nativeAd.headline

        // Body (optional)
        nativeAd.body?.let {
            (adView.bodyView as TextView).text = it
            adView.bodyView?.visibility = View.VISIBLE
        } ?: run { adView.bodyView?.visibility = View.GONE }

        // Icon (optional)
        nativeAd.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(it.drawable)
            adView.iconView?.visibility = View.VISIBLE
        } ?: run { adView.iconView?.visibility = View.GONE }

        // Call-to-action (required)
        (adView.callToActionView as Button).text = nativeAd.callToAction
        adView.callToActionView?.visibility = View.VISIBLE

        // Advertiser (optional)
        nativeAd.advertiser?.let {
            (adView.advertiserView as TextView).text = it
            adView.advertiserView?.visibility = View.VISIBLE
        } ?: run { adView.advertiserView?.visibility = View.GONE }

        // Media content (image or video)
        nativeAd.mediaContent?.let { mediaContent ->
            adView.mediaView?.mediaContent = mediaContent
            adView.mediaView?.setImageScaleType(ImageView.ScaleType.FIT_XY)

            if (mediaContent.hasVideoContent()) {
                val videoController = mediaContent.videoController
                videoController.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoStart() {
                        Log.d("AdVideo", "Video started")
                    }

                    override fun onVideoEnd() {
                        Log.d("AdVideo", "Video ended")
                    }
                }
            }
        }

        // Register the native ad
        adView.setNativeAd(nativeAd)
    }

    fun loadNativeAd(nativeAdContainer: FrameLayout, _binding: FragmentSettingBinding?, isAdded: Boolean, context: Context, adView: NativeAdView) {

        if (!shouldShowNativeAd) return

        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { nativeAd ->

                if (!isAdded || _binding == null) {
                    nativeAd.destroy() // Avoid memory leaks
                    return@forNativeAd
                }

                populateNativeAdView(nativeAd, adView)
                nativeAdContainer.removeAllViews()
                nativeAdContainer.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    //Toast.makeText(context, "Ad failed: ${adError.message}", Toast.LENGTH_SHORT).show()
                    Log.d("AdsManager", adError.message)
                }
                override fun onAdImpression() {
                    // optional: log impression
                }
                override fun onAdClicked() {
                    // optional: log click
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())


    }
    fun closeNativeAd(
        nativeAdContainer: FrameLayout,
        binding: FragmentSettingBinding?,
        isAdded: Boolean,
        context: Context,
        adView: NativeAdView
    ) {
        // Hide the ad
        nativeAdContainer.removeAllViews()
        shouldShowNativeAd = false

        // Schedule ad to reload after 30 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && binding != null && shouldShowNativeAd.not()) {
                shouldShowNativeAd = true // allow reloading
                loadNativeAd(nativeAdContainer, binding, isAdded, context, adView)
            }
        }, 30_000) // 30,000 ms = 30 seconds
    }



}



