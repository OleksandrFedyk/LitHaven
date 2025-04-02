package com.example.firstapp.Adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.firstapp.item_clicked_episodes
import com.example.firstapp.item_clicked_preview

class ViewPagerAdapter(fragment: Fragment, private val bookId: String) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val fragment = item_clicked_preview()
                    fragment.arguments = Bundle().apply {
                        putString("bookId", bookId)
                    }
                    fragment
                }
                1 -> {
                    val fragment = item_clicked_episodes()
                    fragment.arguments = Bundle().apply {
                        putString("bookId", bookId)
                    }
                    fragment
                }
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }

