package com.example.cvmaker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.R
import com.example.cvmaker.adaptor.ProfileDraftViewPagerAdapter
import com.example.cvmaker.databinding.FragmentProfileDraftBinding
import com.example.cvmaker.viewmodels.SharedViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileDraftFragment : Fragment() {

    private var _binding: FragmentProfileDraftBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDraftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarTitle.setText(R.string.profile)
        setupViewPager()
        configureBackPress()
        setupClickListeners()
    }

    private fun setupViewPager() {
        val adapter = ProfileDraftViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPagerSaved.adapter = adapter

        TabLayoutMediator(binding.tabs, binding.viewPagerSaved) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.profile)   // ← NewProfileFragment
                1 -> getString(R.string.draft)     // ← NewDraftFragment (you will implement later)
                else -> ""
            }
        }.attach()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        binding.createCv.setOnClickListener {
            findNavController().navigate(R.id.createProfileFragment)
        }
    }

    private fun configureBackPress() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        _binding = null
    }
}