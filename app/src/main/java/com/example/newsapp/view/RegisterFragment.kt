package com.example.newsapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.RegisterBinding


class RegisterFragment : Fragment() {
    private var _binding: RegisterBinding? =null
    private val binding: RegisterBinding get() = _binding!!

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
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}