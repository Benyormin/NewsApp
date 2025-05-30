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
import com.example.newsapp.databinding.RegisterBinding
import com.google.android.play.integrity.internal.f
import com.google.firebase.auth.FirebaseAuth


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

        binding.btnGuest.setOnClickListener {
            //navigate to tabsManagement
            val action = RegisterFragmentDirections.actionRegisterFragmentToTabsManagementFragment()
            findNavController().navigate(action)
        }

        binding.btnRegister.setOnClickListener {
            val emailText = binding.etEmailRegister.text.toString()
            val passText = binding.etPasswordRegister.text.toString()
            firebaseAuth = FirebaseAuth.getInstance()

            //TODO: need to really check email type with regex and stuff. need to check password security as well
            if (emailText.isNotEmpty() && passText.isNotEmpty()){
                firebaseAuth.createUserWithEmailAndPassword(emailText,passText).addOnCompleteListener{
                    if(it.isSuccessful){
                        val action = RegisterFragmentDirections.actionRegisterFragmentToTabsManagementFragment()
                        findNavController().navigate(action)
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




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}