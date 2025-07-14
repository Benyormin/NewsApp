package com.example.newsapp.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivityMainBinding
import com.example.newsapp.model.NewsData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        AppearanceFragment.applyTheme(this, this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()



        //for getting relative time:
        AndroidThreeTen.init(this)
        //initialize firebase
        FirebaseApp.initializeApp(this)
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





}