package com.example.cvmaker.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.R
import com.example.cvmaker.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Example usage:
        // binding.textViewTitle.text = "Welcome to Home"
        doCorrectText()
        initClicks()
    }

    private fun initClicks() {

        binding.cardProfile.setOnClickListener {
            findNavController().navigate(R.id.profileDraftFragment)
        }
        binding.createAiCv.setOnClickListener {
            findNavController().navigate(R.id.profileDraftFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun doCorrectText(){
        // Text to display
        val text = "Ai Powered \nCV Maker"

        // Create Spannable
        val spannable = SpannableString(text)

        // Apply blue color after "Ai"
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#0085FF")),
            3, // start index after "Ai "
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set it to TextView
        binding.appName.text = spannable
    }
}
