package com.planout.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerCompAdapter(
    val fragmentManager: FragmentManager,
    val lifecycle: Lifecycle,
    private val arrFragList: ArrayList<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return arrFragList.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return arrFragList[0]
            1 -> return arrFragList[1]
            2 -> return arrFragList[2]
        }
        return arrFragList[0]
    }
}