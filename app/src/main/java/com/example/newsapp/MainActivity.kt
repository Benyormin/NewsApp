package com.example.newsapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.newsapp.databinding.ActivityMainBinding
import com.example.newsapp.HomeFragment


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val homeFragment = HomeFragment()
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



    }


    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}