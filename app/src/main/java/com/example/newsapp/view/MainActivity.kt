package com.example.newsapp.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.NewsViewModelFactory
import com.example.newsapp.R
import com.example.newsapp.RetrofitClient
import com.example.newsapp.databinding.ActivityMainBinding
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.model.NewsData
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import com.example.yourapp.utils.AdsManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    //private lateinit var adView: AdView
    private var adView: AdView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        AppearanceFragment.applyTheme(this, this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        val database = ArticleDatabase.invoke(this)
        val dao = database.getArticleDao()
        val repository = NewsRepository(
            RetrofitClient.newsApiService,
            RetrofitClient.guardianApiService, RetrofitClient.espnApiService,
            dao, this)
        //adView = findViewById(R.id.adView)


        val viewModel = ViewModelProvider(this,
            NewsViewModelFactory(repository))[NewsViewModel::class.java]

            /*Ad logic */


         //adView = AdView(this)
        MobileAds.initialize(this) {}
        loadBanner()

        adView?.adListener ?:  object : AdListener() {
            override fun onAdLoaded() {
                Log.d("Ads", "Ad loaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("Ads", "Failed to load banner: ${adError.message}")
            }
        }


        /*Ad logic */


        //for getting relative time:
        AndroidThreeTen.init(this)
        //initialize firebase
        FirebaseApp.initializeApp(this)
        //initialize google ads

        //MobileAds.initialize(this) {}


       /* viewModel.isSubscribed.observe(this){
            subscribed ->
            if (!subscribed){
                binding.adView.visibility = View.VISIBLE
                AdsManager.loadBannerAd(this, adView)
            } else {
                binding.adView.visibility = View.GONE
            }
        }*/




        // Check if it's the first launch
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

        if (isFirstTime){
            sharedPreferences.edit().putString("selected_theme", "light").apply()
        }



        val navHostFragment = supportFragmentManager.findFragmentById(R.id.flFragment) as NavHostFragment
        navController = navHostFragment.navController


        //val articleUrl = intent.getStringExtra("article_url")
        val article = intent.getParcelableExtra<NewsData>("article_data")
        if (article != null) {
            val bundle = bundleOf("newsData" to article)
            navController.navigate(R.id.articleFragment, bundle)
        }

        // Inflate the navigation graph
        val navGraph = navController.navInflater.inflate(R.navigation.news_nav_graph)

        // Set start destination based on first launch
        val startDestination = if (isFirstTime) {
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
            R.id.registerFragment

        } else {
            R.id.homeFragment
        }
        navGraph.setStartDestination(startDestination)
        // Apply the modified graph
        navController.graph = navGraph

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)



        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NavDebug", "Current destination: ${destination.label}")
        }

    }

    private fun loadBanner() {
        val adView = AdView(this).apply {
            adUnitId = "ca-app-pub-3940256099942544/9214589741" // Test unit

            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this@MainActivity, 360))
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("Ads", "Ad loaded ✅")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Ads", "Ad failed ❌: ${adError.message}")
                }
            }
        }

        val container = findViewById<FrameLayout>(R.id.adViewContainer)
        container.removeAllViews()
        container.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }




}