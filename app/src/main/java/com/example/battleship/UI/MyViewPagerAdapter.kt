package com.example.battleship.UI

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import android.util.SparseArray
import androidx.fragment.app.FragmentManager


class MyViewPagerAdapter(fragmentManager: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fragmentManager, behavior) {
    var registeredFragments = SparseArray<Fragment>()

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return if (position ==0) {
            PlayerFieldFragment()
        } else {
            OpponentFieldFragment()
        }
    }


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    fun getRegisteredFragment(position: Int): Fragment {
        return registeredFragments.get(position)
    }

}