package com.example.newsapp.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.RegisterBinding
import com.example.newsapp.model.NewsData
import com.example.newsapp.utils.HelperFuncitons.Companion.saveCategoriesAndRssToFirestore
import com.example.newsapp.utils.HelperFuncitons.Companion.toMap
import com.example.newsapp.viewmodel.NewsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.integrity.internal.f
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegisterFragment : Fragment() {
    private var _binding: RegisterBinding? =null
    private val binding: RegisterBinding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = RegisterBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.visibility = View.GONE


        binding.btnGuest.setOnClickListener {
            //navigate to tabsManagement
            val action = RegisterFragmentDirections.actionRegisterFragmentToTabsManagementFragment()
            findNavController().navigate(action)
            bottomNav.visibility = View.VISIBLE
        }
        binding.goToSignIn.setOnClickListener {
            val action = RegisterFragmentDirections.actionRegisterFragmentToSignInFragment()
            findNavController().navigate(action)
        }

        binding.btnRegister.setOnClickListener {
            val emailText = binding.etEmailRegister.text.toString()
            val passText = binding.etPasswordRegister.text.toString()
            firebaseAuth = FirebaseAuth.getInstance()

            //TODO: need to really check email type with regex and stuff. need to check password security as well
            //TODO:: add all user's interactions to the database. ( Likes, bookmarks)
            if (emailText.isNotEmpty() && passText.isNotEmpty()){
                firebaseAuth.createUserWithEmailAndPassword(emailText,passText).addOnCompleteListener{
                    if(it.isSuccessful){
                        val userId = firebaseAuth.currentUser?.uid
                        //observe bookmarks and move them into the firebase
                        if (userId != null) {
                            val firestore = FirebaseFirestore.getInstance()
                            sendBookmarksToFirebase(viewModel, userId)
                            Toast.makeText(requireContext(), "syncing bookmarks with firebase...", Toast.LENGTH_SHORT).show()
                            saveCategoriesAndRssToFirestore(viewModel, userId, firestore, viewLifecycleOwner)
                            Toast.makeText(requireContext(), "syncing RSS items with firebase...", Toast.LENGTH_SHORT).show()

                        }


                        val action = RegisterFragmentDirections.actionRegisterFragmentToTabsManagementFragment()
                        findNavController().navigate(action)
                        bottomNav.visibility = View.VISIBLE
                    }else{
                        Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_SHORT).show()
                        Log.e("RegisterFragment", it.exception.toString())
                    }
                }
            }else{
                Toast.makeText(requireContext(), "An error occurred please try again!!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun sendBookmarksToFirebase(viewModel: NewsViewModel, userId: String) {

        viewModel.bookMarkedArticles.observe(viewLifecycleOwner){
                bookmarkedList ->
            if( !bookmarkedList.isNullOrEmpty()){
                Log.d("FirebaseSync", "inside the if condition")
                val firestore = FirebaseFirestore.getInstance()
                val userBookmarksRef = firestore.collection("users")
                    .document(userId)
                    .collection("bookmarks")
                for (article in bookmarkedList) {
                    val docId = Uri.encode(article.articleUrl) // safer than hashCode()
                    val articleData = article.toMap()
                    userBookmarksRef.document(docId)
                        .set(articleData)
                        .addOnSuccessListener {
                            Log.d("FirebaseSync", "Synced: ${article.title}")
                        }
                        .addOnFailureListener {
                            Log.e("FirebaseSync", "Failed to sync: ${article.title}, ${it.message}")
                        }
                }

            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}