package com.example.cvmaker.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.cvmaker.R
import com.example.cvmaker.activities.MainActivity
import com.example.cvmaker.adaptor.ProfileDraftViewPagerAdapter
import com.example.cvmaker.cv.CvModelRequest
import com.example.cvmaker.databinding.FragmentProfileDraftBinding
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.getValue

class ProfileDraftFragment : Fragment() {

    private var isFirstTime = true

    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private var onBackPressedCallback : OnBackPressedCallback? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentProfileDraftBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileDraftBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarTitle.setText(R.string.profile)
        setupViewpager()
        configureBackPress()

        initListner()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }
    private fun backPressed() {
        findNavController().navigate(R.id.homeFragment)
    }

    private fun setupViewpager() {
        tryCatch {
            context?.let { ctx ->
                val adapter = ProfileDraftViewPagerAdapter(childFragmentManager, lifecycle)
                binding.viewPagerSaved.adapter = adapter

                TabLayoutMediator(binding.tabs, binding.viewPagerSaved) { tab, position ->
                    tab.text = when (position) {
                        0 -> getString(R.string.profile)
                        1 -> ctx.getString(R.string.draft)
                        else -> ""
                    }
                }.attach()

                binding.viewPagerSaved.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        tryCatch {
                            binding.tabs.selectTab(binding.tabs.getTabAt(position))
                        }
                    }
                })

                // Initially show empty container
                showEmptyState(true)
            }
        }
    }

    /**
     * Controls visibility of the empty data layout and the viewpager/tabs
     */
    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyDataConst.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.viewPagerSaved.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.tabs.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }


    private fun configureBackPress(){
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
            onBackPressedCallback?.let {
                getViewLifecycleOwnerOrNull()?.let { it1 ->
                    activity?.onBackPressedDispatcher?.addCallback(
                        it1,
                        it
                    )
                }
            }
        }
    }

    private fun initListner() {
        clickListener()
    }

    private fun clickListener() {
        activity?.let {
            if (it is MainActivity) {
//                it.setVisibility(false)
            }
        }
        binding.backButton.setOnClickListener { backPressed() }

        binding.createCv.setOnClickListener {
            tryCatch {
//                sharedViewModel.selectedimageasFile = null
//                sharedViewModel.selectedimageUri = null
//                sharedViewModel.cvModel = CvModelRequest()
//                sharedViewModel.profileCase = "createProfile"


                findNavController().navigate(R.id.createProfileFragment)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isFirstTime = true
    }

}