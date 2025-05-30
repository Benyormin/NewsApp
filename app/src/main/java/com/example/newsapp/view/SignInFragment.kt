package com.example.newsapp.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.SigninBinding
import com.google.firebase.auth.FirebaseAuth


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
        binding.btnGuest.setOnClickListener {
        //navigate to tabsManagement
            val action = SignInFragmentDirections.actionSignInFragmentToTabsManagementFragment()
            findNavController().navigate(action)
        }
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmailSignIn.text.toString()
            val pass = binding.etPasswordSignIn.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if(it.isSuccessful){
                        //TODO:: I need to save the credintials and move straight to the hoome fragment
                        // FOR Now the user preference is an empty array
                        val action = SignInFragmentDirections.actionSignInFragmentToHomeFragment(arrayOf())
                        findNavController().navigate(action)
                    }
                    else{
                        Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_SHORT).show()
                        Log.e("SignInFragment", it.exception.toString())
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failure: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    Log.e("SignInFragment", "Failure Listener: ", it)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}