package com.example.newsapp.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.CustomRSSListAdapter
import com.example.newsapp.databinding.FragmentTabsManagementBinding
import com.example.newsapp.db.Preferences
import com.example.newsapp.db.RssUrl
import com.example.newsapp.viewmodel.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class TabsManagementFragment : Fragment() {

    private lateinit var rssFeedAdapter: CustomRSSListAdapter
    private var rssUrls: MutableList<RssUrl> = emptyList<RssUrl>().toMutableList()
    private lateinit var viewModel: NewsViewModel
    private lateinit var userPreference: MutableList<String>
    private lateinit var Buttons: MutableList<Button>

    private var _binding: FragmentTabsManagementBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTabsManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etRssUrl = view.findViewById<EditText>(R.id.etRssUrl)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val rvRss = view.findViewById<RecyclerView>(R.id.rvRssList)
        val etname = view.findViewById<EditText>(R.id.etRssName)
        val btnApply = view.findViewById<Button>(R.id.btnSave)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        addAllButtons()



        rssFeedAdapter = CustomRSSListAdapter(rssUrls,
            { feed, pos ->
                //edit
                // Edit button clicked
                showEditDialog(feed, pos)
            }, { feed, pos ->
                // Handle delete
                rssFeedAdapter.removeRssFeed(pos)
                //delete from database
                viewModel.deleteRssUrl(feed)
                Toast.makeText(
                    requireContext(),
                    "${feed.name} has been deleted",
                    Toast.LENGTH_SHORT
                ).show()
            })
        rvRss.layoutManager = LinearLayoutManager(requireContext())
        rvRss.adapter = rssFeedAdapter


        viewModel.rssItems.observe(viewLifecycleOwner) { it ->
            if (!it.isNullOrEmpty()) {
                rssUrls = it.toMutableList()
                rssFeedAdapter.updateData(rssUrls)
            } else {
                rssUrls = emptyList<RssUrl>().toMutableList()
                rssFeedAdapter.updateData(rssUrls)
            }

        }
        //set style of selected buttons
        viewModel.userCategories.observe(viewLifecycleOwner){
            val categories = it?.categories?: listOf("For you")
            if(categories.isNotEmpty()){
                val prefs = categories
                for (btn in Buttons){
                    if (prefs.contains(btn.text.toString())){
                        btn.isSelected = true
                    }
                }
            }

        }


        btnAdd.setOnClickListener {
            val url = etRssUrl.text.toString().trim()
            val name = etname.text.toString().trim()
            //TODO: Check if the Url is valid
            if (url.isNotEmpty() && name.isNotEmpty()) {
                rssFeedAdapter.addRssFeed(RssUrl(name = name, url = url))
                viewModel.addRssUrls(name, url)
                etRssUrl.text.clear()
                etname.text.clear()
            } else {
                if (url.isEmpty()) {
                    etRssUrl.error = "Please enter a URL"
                }
                if (name.isEmpty()) {
                    etname.error = "Please enter a name"
                }
                Snackbar.make(view, "please enter both URL and name", Snackbar.LENGTH_SHORT).apply {
                    setAction("Ok") {
                        dismiss()
                    }
                    setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }.show()

            }


        }
        btnApply.setOnClickListener {
            userPreference = mutableListOf()
            for (btn in Buttons){
                if (btn.isSelected){
                    userPreference.add(btn.text.toString())
                }
            }
            if (userPreference.size < 2){
                Snackbar.make(view, "You must at least pick two categories", Snackbar.LENGTH_SHORT).apply {
                    setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }.show()
            }
            else{
                viewModel.updateUserPreferences(Preferences(0, userPreference))
                val action= TabsManagementFragmentDirections.
                actionTabsManagementFragmentToHomeFragment(userPreference.toTypedArray())
                findNavController().navigate(action)

            }


        }


        btnBack.setOnClickListener {
            Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()

        }

        binding.btnFootball.setOnClickListener { btn ->
            btn.isSelected = !btn.isSelected
          btnAnimaiton(btn)

        }
        binding.btnHealth.setOnClickListener{btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn)
        }
        binding.btnFood.setOnClickListener {
                btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn)
        }
        binding.btnTech.setOnClickListener{
                btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn)
        }
        binding.btnGames.setOnClickListener{
                btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn)
        }
        binding.btnCrypto.setOnClickListener { btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn) }

        binding.btnBooks.setOnClickListener { btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn) }

        binding.btnBusiness.setOnClickListener { btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn) }



        binding.btnEducation.setOnClickListener { btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn) }

        binding.btnPolitics.setOnClickListener {
                btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn)
        }
        binding.btnSience.setOnClickListener { btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn) }

        binding.btnEnvironment.setOnClickListener {
                btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn)
        }
        binding.btnSports.setOnClickListener { btn ->
            btn.isSelected = !btn.isSelected
            btnAnimaiton(btn) }

    }

    private fun showEditDialog(feed: RssUrl, pos: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_rss, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etUrl = dialogView.findViewById<TextInputEditText>(R.id.etUrl)

        etName.setText(feed.name)
        etUrl.setText(feed.url)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit RSS Feed")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newUrl = etUrl.text.toString().trim()

                if (newName.isNotEmpty() && newUrl.isNotEmpty()) {
                    val updatedFeed = feed.copy(
                        name = newName,
                        url = newUrl
                    )

                    // Update adapter
                    rssFeedAdapter.updateRssFeed(pos, updatedFeed)

                    // Update database
                    //viewModel.updateRssUrl(updatedFeed)
                    viewModel.deleteRssUrl(feed)
                    viewModel.addRssUrls(newName, newUrl)

                    Toast.makeText(
                        requireContext(),
                        "Changes saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun addAllButtons() {
        Buttons = mutableListOf()
        Buttons.addAll(listOf(binding.btnFootball,
            binding.btnSports,
            binding.btnBooks,
            binding.btnTech,
            binding.btnEnvironment,
            binding.btnPolitics,
            binding.btnSience,
            binding.btnBusiness,
            binding.btnGames,
            binding.btnFood,
            binding.btnCrypto,
            binding.btnEducation,
            binding.btnHealth
            ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun btnAnimaiton(btn: View){
        btn.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                btn.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
            }
    }
}