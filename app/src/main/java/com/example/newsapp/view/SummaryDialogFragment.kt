package com.example.newsapp.view

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.example.newsapp.R
import com.example.newsapp.databinding.DialogSummaryBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SummaryDialogFragment : BottomSheetDialogFragment() {
    private var _binding: DialogSummaryBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SUMMARY = "summary"

        fun newInstance(title: String, summary: String) = SummaryDialogFragment().apply {
            arguments = Bundle().apply{
                putString(ARG_TITLE, title)
                putString(ARG_SUMMARY, summary)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This sets the style for the bottom sheet
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // This adds custom animations to the dialog
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.setWindowAnimations(R.style.DialogAnimation)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title.text = arguments?.getString(ARG_TITLE)
        binding.summary.text = arguments?.getString(ARG_SUMMARY)


        // Setup close button
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}