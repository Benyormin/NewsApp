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

      /*  val homeFragment = HomeFragment()
        val searchFragment = SearchFragment()
        val bookmarksFragment = BookmarksFragment()
        val settingFragment = SettingFragment()

        setCurrentFragment(homeFragment)
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.homeFragment -> setCurrentFragment(homeFragment)
                R.id.exploreFragment -> setCurrentFragment(searchFragment)
                R.id.bookmarksFragment -> setCurrentFragment(bookmarksFragment)
                R.id.profileFragment -> setCurrentFragment(settingFragment)
            }
            true
        }
*/
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.flFragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

       // findViewById<BottomNavigationView>(R.id.bottomNavigationView).setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NavDebug", "Current destination: ${destination.label}")
        }

    }


    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}