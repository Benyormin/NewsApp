package com.example.newsapp.view

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentAppearanceBinding


class AppearanceFragment : Fragment() {
    private var _binding : FragmentAppearanceBinding?= null
    private val binding : FragmentAppearanceBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAppearanceBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", MODE_PRIVATE)
        val selected = sharedPrefs.getString("selected_theme", "light")

        when (selected){
            "light" ->{
                binding.checkLight.visibility =  View.VISIBLE
            }
            "dark" -> {
                binding.checkDark.visibility = View.VISIBLE
            }
            "green" ->{
                binding.checkGreen.visibility = View.VISIBLE
            }
        }


        // click listeners:
        binding.cardLight.setOnClickListener { highlightSelectedTheme("light", sharedPrefs) }
        binding.cardDark.setOnClickListener { highlightSelectedTheme("dark", sharedPrefs) }
        binding.cardGreen.setOnClickListener { highlightSelectedTheme("green", sharedPrefs) }


    }

    private fun highlightSelectedTheme(selected: String, sharedPrefs: SharedPreferences) {

        //selected ->
        when (selected) {
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPrefs.edit().putString("selected_theme", "light").apply()
                activity?.recreate()

            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPrefs.edit().putString("selected_theme", "dark").apply()
                activity?.recreate()
            }
            "green" -> {
                binding.checkGreen.visibility = View.VISIBLE
                //requireContext().setTheme(R.style.Theme_NewsApp_Green)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPrefs.edit().putString("selected_theme", "green").apply()
                activity?.recreate()
            }



        }

    }


override fun onDestroyView(){
    super.onDestroyView()
    _binding = null

}



    companion object {
        fun applyTheme(context: Context, activity: Activity) {
            val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val selectedTheme = sharedPrefs.getString("selected_theme", "light")

            when (selectedTheme) {
                "light" -> {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    activity.setTheme(R.style.Base_Theme_NewsApp)
                }
                "dark" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    activity.setTheme(R.style.Base_Theme_NewsApp)
                }
                "green" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    activity.setTheme(R.style.Theme_NewsApp_Green)

                }
            }
        }
    }


}