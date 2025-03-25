package com.example.newsapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.newsapp.R

/**
 * A simple [Fragment] subclass.
 * Use the [ForUFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ForUFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_for_u, container, false)
    }

}