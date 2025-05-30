package com.example.newsapp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.newsapp.view.Cat1Fragment
import com.example.newsapp.view.ForUFragment
import com.example.newsapp.TabItem


class ViewPagerAdapter(fragmentActivity: FragmentActivity,
    val tabs: List<TabItem>) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return tabs.size // Number of fragments
    }

    override fun createFragment(position: Int): Fragment {

        return tabs[position].fragment
    }
}