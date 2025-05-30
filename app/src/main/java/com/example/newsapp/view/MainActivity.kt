package com.example.newsapp.view

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if it's the first launch
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.flFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Inflate the navigation graph
        val navGraph = navController.navInflater.inflate(R.navigation.news_nav_graph)

        // Set start destination based on first launch
        val startDestination = if (isFirstTime) {
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
            R.id.registerFragment
            //TODO: I should remove the bottom nav bar for the aesthetics
        } else {
            R.id.homeFragment
        }
        navGraph.setStartDestination(startDestination)
        // Apply the modified graph
        navController.graph = navGraph

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

       // findViewById<BottomNavigationView>(R.id.bottomNavigationView).setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NavDebug", "Current destination: ${destination.label}")
        }

    }



}