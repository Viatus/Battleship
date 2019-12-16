package com.example.battleship.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.battleship.R
import com.example.battleship.database.DatabaseHelper
import com.example.battleship.databinding.ActivityStatiscticBinding

class StatiscticActivity : AppCompatActivity() {

    lateinit var binding: ActivityStatiscticBinding
    lateinit var db:DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_statisctic
        )



        val layoutManager = LinearLayoutManager(this)
        val adapter = MatchResultRecyclerViewAdapter()
        binding.matchresultList.layoutManager = layoutManager
        binding.matchresultList.adapter = adapter
        db = DatabaseHelper(this, null)

        adapter.data = db.getAllMatchResults()
    }
}
