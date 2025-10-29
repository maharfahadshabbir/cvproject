package com.example.cvmaker.adaptor

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cvmaker.fragments.profile.NewDraftFragment
import com.example.cvmaker.fragments.profile.NewProfileFragment

private const val NUM_TABS = 2

class ProfileDraftViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {

        when (position) {
            0 -> return NewProfileFragment()
            1 -> return NewDraftFragment()

            else -> {
                return NewProfileFragment()
            }
        }

    }
}