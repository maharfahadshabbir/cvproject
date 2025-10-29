package com.example.cvmaker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.cvmaker.R
import com.example.cvmaker.adaptor.OnboardingPagerAdapter
import com.example.cvmaker.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!


    private lateinit var onBoardingPagerAdapter: OnboardingPagerAdapter


    private val titles by lazy {
        listOf(
            getString(R.string.onboarding_title_1),
            getString(R.string.onboarding_title_2),
            getString(R.string.onboarding_title_3)
        )
    }

    private val descriptions by lazy {
        listOf(
            getString(R.string.onboarding_desc_1),
            getString(R.string.onboarding_desc_2),
            getString(R.string.onboarding_desc_3)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onBoardingPagerAdapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = onBoardingPagerAdapter

        // Set up the dot indicator with the ViewPager
        val dotsIndicator = binding.dotsIndicator
        dotsIndicator.setViewPager2(binding.viewPager)


        // Set initial title and description
        updateTitleAndDescription(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTitleAndDescription(position)
            }
        })

        // Continue button action
        binding.textSkip.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
        binding.continueBtn.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onBoardingPagerAdapter.itemCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }


    private fun updateTitleAndDescription(position: Int) {
        binding.textTitle.text = titles.getOrNull(position) ?: ""
        binding.textDescription.text = descriptions.getOrNull(position) ?: ""
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
