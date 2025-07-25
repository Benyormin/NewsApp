package com.example.newsapp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.newsapp.view.Cat1Fragment
import com.example.newsapp.view.ForUFragment
import com.example.newsapp.TabItem
import com.example.newsapp.db.RssUrl
import com.example.newsapp.view.NewsListFragment


class ViewPagerAdapter(fragmentActivity: FragmentActivity,
    val tabs: List<TabItem>, private val rssUrls: List<RssUrl>) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return tabs.size // Number of fragments
    }

    /*override fun createFragment(position: Int): Fragment {

        return tabs[position].fragment
    }*/

    override fun createFragment(position: Int): Fragment {
        val category = tabs[position].title
        val rssUrl = rssUrls.find { it.name == category }

        return if (rssUrl != null) {
            NewsListFragment.newRssInstance(category, rssUrl.url)
        } else {
            NewsListFragment.newInstance(category)
        }
    }
}