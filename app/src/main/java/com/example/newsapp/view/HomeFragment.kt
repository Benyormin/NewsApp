package com.example.newsapp.view

import NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import ViewPagerAdapter
import android.os.Bundle
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
import com.example.newsapp.databinding.FragmentHomeBinding
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.model.NewsArticle
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




    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val userPreferences = listOf("For u", "Sports", "Football", "Politics",
            "Tech", "Health", "Crypto", "science", "games", "business", "books", "education",
            "environment", "food")


        setupViewPager(userPreferences)
        btnCustomizeTabs.setOnClickListener {

            val action = HomeFragmentDirections.actionHomeFragmentToTabsManagementFragment()
            findNavController().navigate(action)

        }


    }



    // memory leak and stuff
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViewPager(categories: List<String>){
        val tabs = categories.map {
            category ->
            TabItem(
                title = category,
                fragment = createFragmentForTab(category)
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