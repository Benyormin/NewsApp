package com.example.newsapp.view

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? =null
    private val binding : FragmentSettingBinding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null){
            binding.welcomeMessage.text = "Welcome ${currentUser.email}"
        }else{
            binding.welcomeMessage.text = "Please Sign in"
        }

        binding.apply{

            btnSignOut.setOnClickListener {
                firebaseAuth.signOut()
                binding.welcomeMessage.text = "Please Sign in"
                Toast.makeText(requireContext(),"You signed out successfully.",Toast.LENGTH_SHORT).show()
            }
            registerFragment.setOnClickListener {
                val action = SettingFragmentDirections.actionSettingFragmentToRegisterFragment()
                findNavController().navigate(action)
            }

            SignInFragment.setOnClickListener {
                val action = SettingFragmentDirections.actionSettingFragmentToSignInFragment()
                findNavController().navigate(action)
            }

            btnNotification.setOnClickListener {
                val action = SettingFragmentDirections.actionSettingFragmentToNotificationFragment()
                findNavController().navigate(action)
            }



            btnChangeTheme.setOnClickListener {
                //change theme logic

                val action = SettingFragmentDirections.actionSettingFragmentToAppearanceFragment()
                findNavController().navigate(action)


                /*val currentMode = AppCompatDelegate.getDefaultNightMode()

                val newMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES){
                    AppCompatDelegate.MODE_NIGHT_NO
                }else{
                    AppCompatDelegate.MODE_NIGHT_YES
                }

                AppCompatDelegate.setDefaultNightMode(newMode)
                */
            }


            btnAdd.setOnClickListener {
                val action = SettingFragmentDirections.actionSettingFragmentToAddsFragment()
                findNavController().navigate(action)
            }
        }




    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}