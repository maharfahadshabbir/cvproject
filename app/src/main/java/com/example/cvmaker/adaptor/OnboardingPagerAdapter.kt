package com.example.cvmaker.adaptor

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cvmaker.fragments.Ob1Fragment
import com.example.cvmaker.fragments.Ob2Fragment
import com.example.cvmaker.fragments.Ob3Fragment

class OnboardingPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Ob1Fragment()
            1 -> Ob2Fragment()
            2 -> Ob3Fragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
