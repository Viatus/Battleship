package com.example.battleship.UI


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.example.battleship.R
import com.example.battleship.databinding.FragmentMatchStatisticsBinding

/**
 * A simple [Fragment] subclass.
 */
class MatchStatisticsFragment : Fragment() {

    lateinit var binding: FragmentMatchStatisticsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_match_statistics, container, false
        )
        return binding.root
    }

}
