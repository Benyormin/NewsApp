package com.example.newsapp

import ViewPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.newsapp.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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
        //viewModel.text = "I am clicked"

        // Initialize ViewPager2 and Adapter
        //val viewPagerAdapter = ViewPagerAdapter(requireActivity(), userPreferences)
        //binding.vpContent.adapter = viewPagerAdapter

        // Example user preferences (replace with your actual data source)
        val userPreferences = listOf("For u", "Sports", "Politics",
            "Tech", "Health", "Football", "Crypto", "weather")

        //dataList = createData()
        //how to find another layout view in a fragment?->
        //val recyclerViewLayout = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_news_list, null)

        //recyclerView = recyclerViewLayout.findViewById(R.id.rvNews)
        //recyclerView.adapter = NewsAdapter(dataList)
        //recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val tabs = userPreferences.map {
            pref ->
            TabItem(
                title = pref,
                fragment = createFragmentForTab(pref)
            )
        }
        val viewPagerAdapter = ViewPagerAdapter(requireActivity(), tabs)
        binding.vpContent.adapter = viewPagerAdapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tlCategories, binding.vpContent) { tab, position ->
            tab.text = tabs[position].title

        }.attach()

    }



    // memory leak and stuff
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





    fun createFragmentForTab(category: String): Fragment {
        return NewsListFragment.newInstance(category)
    }
}