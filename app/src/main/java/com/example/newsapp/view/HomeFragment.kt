package com.example.newsapp.view


import com.example.newsapp.viewmodel.NewsViewModel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.newsapp.NewsViewModelFactory
import com.example.newsapp.R
import com.example.newsapp.RetrofitClient
import com.example.newsapp.TabItem
import com.example.newsapp.ViewPagerAdapter
import com.example.newsapp.databinding.FragmentHomeBinding
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.db.RssUrl
import com.example.newsapp.model.NewsArticle
import com.example.newsapp.repository.NewsRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.apache.commons.net.nntp.Article
import com.example.newsapp.db.ArticlesDAO as ArticlesDAO

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding : FragmentHomeBinding get() = _binding!!


    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<NewsArticle>
    private lateinit var viewModel: NewsViewModel
    private lateinit var btnCustomizeTabs: ImageButton
    private val rssUrls = mutableListOf<RssUrl>()
    private var tabs = mutableListOf<String>()
    private var userPreferences: MutableList<String> = mutableListOf("For you")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = arguments?.getStringArray("userPreferences")?.toList()?: emptyList()
        Log.d("HomeFragment", "argument data  received ${preferences}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding?.root

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // interact with our views
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.visibility = View.VISIBLE

        btnCustomizeTabs = view.findViewById(R.id.btnMoreOptions)
        val database = ArticleDatabase.invoke(requireContext())
        val dao = database.getArticleDao()
        val repository = NewsRepository(RetrofitClient.newsApiService,
            RetrofitClient.guardianApiService,RetrofitClient.espnApiService,
            dao, requireContext())

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity(), NewsViewModelFactory(repository)).get(
            NewsViewModel::class.java)

        // Example user preferences (replace with your actual data source)
        /*val userPreferences = listOf("For u", "Sports", "Football", "Politics",
            "Tech", "Health", "Crypto", "science", "games", "business", "books", "education",
            "environment", "food")
*/
        var categoryIsReady = false
        var rssIsReady = false
        fun checkAndUpdate() {
            if (categoryIsReady && rssIsReady) {
                tabs = addRssUrlsToTabs(userPreferences, rssUrls)
                setupViewPager(tabs)
            }
        }

        viewModel.userCategories.observe(viewLifecycleOwner){
            pref->
            if(pref != null && pref.categories.isNotEmpty()){
                userPreferences = mutableListOf()
                userPreferences.add("For you")
                userPreferences.addAll(pref.categories)
                Log.d("HomeFragment", "userPref= ${userPreferences}")
            }
            else{
             userPreferences.add("For you")
             Log.d("HomeFragment", "This is the default value")
            }
            categoryIsReady = true
            checkAndUpdate()
        }
        viewModel.rssItems.observe(viewLifecycleOwner){
            it ->
            rssUrls.clear()
            rssUrls.addAll(it)
            //tabs = addRssUrlsToTabs(userPreferences, rssUrls)
            Log.d("HomeFragment", "${rssUrls}")
            //setupViewPager(tabs)
            rssIsReady = true
            checkAndUpdate()
        }


/*
        viewModel.combinedData.observe(viewLifecycleOwner){
            (categories, rssUrls)->
            Log.d("DATA_FLOW", "Raw categories from DB: $categories")
            Log.d("DATA_FLOW", "Raw RSS URLs from DB: $rssUrls")
            userPreferences.clear()
            this.rssUrls.clear()

            userPreferences.add("For you")
            if (categories.isNotEmpty()){
                Log.d("HomeFragment", "categories is not empty $categories")

                userPreferences.addAll(categories)
            }
            this.rssUrls.addAll(rssUrls)

            tabs = addRssUrlsToTabs(userPreferences, this.rssUrls)
            // Debug logs
            Log.d("DATA_FLOW", "Final userPrefs: $userPreferences")
            Log.d("DATA_FLOW", "Final RSS URLs: $rssUrls")
            Log.d("HomeFragment", "Tabs: ${tabs}")

            // Update ViewPager
            setupViewPager(tabs)


        }
*/
        btnCustomizeTabs.setOnClickListener {

            val action = HomeFragmentDirections.actionHomeFragmentToTabsManagementFragment()
            findNavController().navigate(action)
        }


    }

    private fun addRssUrlsToTabs(userPreferences: List<String>,
                                 rssUrls: MutableList<RssUrl>): MutableList<String> {
        val allTabs = mutableListOf<String>().apply {
            addAll(userPreferences)
            addAll(rssUrls.map { it.name })
        }

        //add allTabs to the view model so that it can be used in the Notification fragment
        viewModel.setAllTabs(allTabs)

        return allTabs
    }


    // memory leak and stuff
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViewPager(categories: List<String>){
        val tabs = categories.map {
            category ->
            val rssUrl = rssUrls.find { it.name == category }
            TabItem(
                title = category,
                fragment = if (rssUrl != null) {
                    Log.d("HomeFragment", "creating fragment rssUrl: $rssUrl")
                    NewsListFragment.newRssInstance(category, rssUrl.url)
                }else{
                    NewsListFragment.newInstance(category)
                }
            )
        }

        val viewPagerAdapter = ViewPagerAdapter(requireActivity(), tabs)
        binding.vpContent.adapter = viewPagerAdapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tlCategories, binding.vpContent) { tab, position ->
            tab.text = tabs[position].title

        }.attach()

    }



    fun createFragmentForTab(category: String): Fragment {
        return NewsListFragment.newInstance(category)
    }
}