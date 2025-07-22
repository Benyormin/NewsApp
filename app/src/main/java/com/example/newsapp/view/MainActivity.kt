package com.example.newsapp.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.util.TimeUtils
import android.view.ContextThemeWrapper
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import com.example.newsapp.utils.HelperFuncitons.Companion.getNetworkTime
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.button.MaterialButton
import android.content.res.Resources



class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    //private lateinit var adView: AdView
    private var adView: AdView? = null
    private val PREF_NOTIFICATION_PERMISSION_PROMPT = "pref_notification_permission_prompt"
    private val PREF_DONT_ASK_NOTIFICATION_PERMISSION = "dont_ask_notification_permission"

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
//retrieve current time for caching

        CoroutineScope(Dispatchers.IO).launch {
            val networkTime = getNetworkTime()
            if (networkTime != 0L) {
                com.example.newsapp.utils.TimeUtils.cachedNetworkTime = networkTime
                com.example.newsapp.utils.TimeUtils.cacheTimestamp = System.currentTimeMillis()
            }
        }

            /*Ad logic */


         //adView = AdView(this)

        MobileAds.initialize(this) {
            Log.d("Ads", "MobileAds initialized")
        }

        val testDeviceIds = listOf("E43A9944F04F8CCA14D68E7A05790C9B") //A32
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        loadBanner()




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

        //check for notification permission and battery optimization
        checkAndRequestBatteryOptimization(this, sharedPreferences)
        showNotificationPermissionDialog(this)



        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NavDebug", "Current destination: ${destination.label}")
        }

    }

    private fun loadBanner() {
        //my id : E43A9944F04F8CCA14D68E7A05790C9B
        val adView = AdView(this).apply {
            adUnitId = "ca-app-pub-3940256099942544/9214589741" // Test unit

            val displayMetrics = Resources.getSystem().displayMetrics
            val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()


            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                this@MainActivity,
                screenWidthDp
            )
            setAdSize(adSize)

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("Ads", "Ad loaded ✅")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Ads", "Ad failed ❌: ${adError.message}")
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadAd(AdRequest.Builder().build())
                    }, 5000)
                }
            }
        }

        val container = findViewById<FrameLayout>(R.id.adViewContainer)
        container.removeAllViews()
        container.addView(adView)

        adView.loadAd(AdRequest.Builder().build())
    }

    fun checkAndRequestBatteryOptimization(
        activity: Activity,
        sharedPrefs: SharedPreferences
    ) {
        val packageName = activity.packageName
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager

        val ignored = powerManager.isIgnoringBatteryOptimizations(packageName)
        val dontAskAgain = sharedPrefs.getBoolean("battery_opt_dont_ask_again", false)

        if (ignored || dontAskAgain) return

        val dialog = AlertDialog.Builder(activity)
            .setTitle("Battery Optimization")
            .setMessage("To ensure timely notifications, please allow the app to ignore battery optimizations.")
            .setCancelable(false)
            .setPositiveButton("Allow Now") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                activity.startActivity(intent)
            }
            .setNeutralButton("Remind Me Later") { _, _ -> /* nothing */ }
            .setNegativeButton("Don't Ask Again") { _, _ ->
                sharedPrefs.edit().putBoolean("battery_opt_dont_ask_again", true).apply()
            }
            .create()


        val styledContext = ContextThemeWrapper(activity, R.style.CustomButtonStyle)
        val button = MaterialButton(styledContext).apply {
            text = "Allow Now"
        }


        dialog.setOnShowListener {
            val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            // Style the positive button
            positive.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.Secondary))
            positive.setTextColor(ContextCompat.getColor(activity, R.color.Primary))
            positive.setTypeface(null, Typeface.BOLD)

            // Style the neutral button (e.g., grey background, dark text)
            neutral.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.gray))
            neutral.setTextColor(ContextCompat.getColor(activity, R.color.white))
            neutral.setTypeface(null, Typeface.NORMAL)

            // Style the negative button (e.g., red background, white text)
            negative.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.red))
            negative.setTextColor(ContextCompat.getColor(activity, android.R.color.white))
            negative.setTypeface(null, Typeface.NORMAL)
        }



        dialog.show()
    }

    fun showNotificationPermissionDialog(activity: Activity) {


        val prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val shouldNotAskAgain = prefs.getBoolean(PREF_DONT_ASK_NOTIFICATION_PERMISSION, false)
        val wasPromptedBefore = prefs.getBoolean(PREF_NOTIFICATION_PERMISSION_PROMPT, false)

        if (shouldNotAskAgain || wasPromptedBefore) return

        val dialog = AlertDialog.Builder(activity)
            .setTitle("Enable Notifications")
            .setMessage("To receive important updates, please allow notification access.")
            .setPositiveButton("Enable Now") { _, _ ->
                // Request permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        101 // any unique request code
                    )
                }
                prefs.edit().putBoolean(PREF_NOTIFICATION_PERMISSION_PROMPT, true).apply()
            }
            .setNegativeButton("Don't Ask Again") { _, _ ->
                prefs.edit().putBoolean(PREF_DONT_ASK_NOTIFICATION_PERMISSION, true).apply()
            }
            .setNeutralButton("Remind Me Later") { _, _ ->
                // Don't save anything, dialog will appear again next launch
            }
            .create()

        dialog.setOnShowListener {
            val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            positive.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.Secondary))
            positive.setTextColor(ContextCompat.getColor(activity, R.color.Primary))
            positive.setTypeface(null, Typeface.BOLD)

            neutral.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.gray))
            neutral.setTextColor(ContextCompat.getColor(activity, R.color.white))
            neutral.setTypeface(null, Typeface.NORMAL)

            negative.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.red))
            negative.setTextColor(ContextCompat.getColor(activity, android.R.color.white))
            negative.setTypeface(null, Typeface.NORMAL)
        }

        dialog.show()
    }






}