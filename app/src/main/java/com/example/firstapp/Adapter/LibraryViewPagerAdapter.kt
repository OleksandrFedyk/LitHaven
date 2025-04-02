package com.example.firstapp.Adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.firstapp.fragments.LibraryHistoryFragment
import com.example.firstapp.fragments.LibraryLickedFragment

class LibraryViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment)  {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LibraryHistoryFragment()
            1 -> LibraryLickedFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }

}