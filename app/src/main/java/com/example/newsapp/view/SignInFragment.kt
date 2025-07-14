package com.example.newsapp.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.newsapp.R
import com.example.newsapp.RetrofitClient
import com.example.newsapp.databinding.SigninBinding
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.db.Preferences
import com.example.newsapp.db.RssUrl
import com.example.newsapp.model.NewsData
import com.example.newsapp.model.Source
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.utils.HelperFuncitons
import com.example.newsapp.utils.HelperFuncitons.Companion.fetchCategoriesAndRSSFromFireStore
import com.example.newsapp.utils.HelperFuncitons.Companion.saveCategoriesAndRssToFirestore
import com.example.newsapp.utils.HelperFuncitons.Companion.toMap
import com.example.newsapp.viewmodel.NewsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SignInFragment : Fragment() {
    private var _binding: SigninBinding? = null
    private val binding :SigninBinding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = SigninBinding.inflate(inflater, container, false)
        return _binding?.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        val viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.visibility = View.GONE


        binding.btnGuest.setOnClickListener {
            //navigate to tabsManagement
            val action = SignInFragmentDirections.actionSignInFragmentToTabsManagementFragment()
            findNavController().navigate(action)
            bottomNav.visibility = View.VISIBLE
        }

        binding.btnSignIn.setOnClickListener {
            binding.btnSignIn.setOnClickListener {
                val email = binding.etEmailSignIn.text.toString()
                val pass = binding.etPasswordSignIn.text.toString()
                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    firebaseAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val user = firebaseAuth.currentUser
                                val userId = user?.uid

                                if (userId != null) {
                                    syncBookmarksWithFirestore(viewModel, userId)

                                    val firestore = FirebaseFirestore.getInstance()
                                    saveCategoriesAndRssToFirestore(viewModel, userId, firestore, viewLifecycleOwner)
                                    fetchCategoriesAndRSSFromFireStore(viewModel, userId, firestore)
                                    //sync like states between firestore and room
                                    viewModel.syncLikesFromFirebaseToRoom()

                                    // Navigate to home AFTER syncing
                                    val action = SignInFragmentDirections.actionSignInFragmentToHomeFragment(arrayOf())
                                    findNavController().navigate(action)
                                }
                                bottomNav.visibility = View.VISIBLE



                            } else {
                                Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_SHORT).show()
                                Log.e("SignInFragment", it.exception.toString())
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failure: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            Log.e("SignInFragment", "Failure Listener: ", it)
                        }
                }
            }


        }
    }

    private fun syncBookmarksWithFirestore(viewModel: NewsViewModel, userId: String) {

        val firestore = FirebaseFirestore.getInstance()
        // 1️⃣ Sync local bookmarks to Firebase (unsynced offline ones)
        viewModel.bookMarkedArticles.observe(viewLifecycleOwner) { localBookmarks ->
            if (!localBookmarks.isNullOrEmpty()) {

                val userBookmarksRef = firestore.collection("users")
                    .document(userId)
                    .collection("bookmarks")

                for (article in localBookmarks) {
                    if (article.isBookmarked) {
                        val docId = Uri.encode(article.articleUrl)
                        userBookmarksRef.document(docId).set(article.toMap())
                    }
                }
            }
        }

        // 2️⃣ Fetch all Firebase bookmarks and add them to Room

        val userBookmarksRef = firestore.collection("users")
            .document(userId)
            .collection("bookmarks")

        userBookmarksRef.get()
            .addOnSuccessListener { querySnapshot ->
                val remoteBookmarks = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        val map = doc.data
                        if (map != null) {
                            mapToNewsData(map)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                // Save them to Room
                viewModel.saveBookmarksToRoom(remoteBookmarks)

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    fun mapToNewsData(map: Map<String, Any?>): NewsData {
        val sourceMap = map["source"] as? Map<*, *>
        return NewsData(
            title = map["title"] as? String ?: "",
            imageUrl = map["imageUrl"] as? String,
            description = map["description"] as? String,
            publishedAt = map["publishedAt"] as? String,
            articleUrl = map["articleUrl"] as? String ?: "",
            source = Source(
                id = sourceMap?.get("id") as? String,
                name = sourceMap?.get("name") as? String ?: ""
            ),
            isBookmarked = map["isBookmarked"] as? Boolean ?: false,
            isLike = map["isLike"] as? Boolean ?: false
        )
    }

}
