package com.example.newsapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.newsapp.adapters.NotificationSettingsAdapter
import com.example.newsapp.databinding.FragmentNotificationBinding
import com.example.newsapp.viewmodel.NewsViewModel
import com.example.newsapp.viewmodel.NotificationViewModel
import com.example.newsapp.worker.ForYouNotificationWorker

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val newsViewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        newsViewModel.allTabs.observe(viewLifecycleOwner){
            prefs->
            notificationViewModel.loadPreferences(requireContext(), prefs)

            notificationViewModel.notificationPreferences.observe(viewLifecycleOwner) {
                preferences ->
                val adapter = NotificationSettingsAdapter(preferences) { item ->
                    notificationViewModel.updatePreference(item, requireContext())
                    if (item.category == "For you") {
                        notificationViewModel.onForYouNotificationToggleChanged(item.isEnabled, requireContext())
                    }else{
                        val rssUrl = newsViewModel.getRssUrlByName(item.category)
                        if (rssUrl != null) {
                            notificationViewModel.onRssToggleChanged(
                                category = item.category,
                                url = rssUrl,
                                enabled = item.isEnabled,
                                context = requireContext()
                            )
                        }
                    }
                    //here should be called


                }
                binding.notificationRecyclerView.adapter = adapter
                binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())

            }
        }

        binding.btnTest.setOnClickListener {
            val testRequest = OneTimeWorkRequestBuilder<ForYouNotificationWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(testRequest)

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
