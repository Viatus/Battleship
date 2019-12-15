package com.example.battleship.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.battleship.R
import com.example.battleship.database.model.BattleshipMatchResult
import kotlinx.android.synthetic.main.match_stat_item.view.*
import java.lang.StringBuilder

class MatchResultRecyclerViewAdapter() :
    RecyclerView.Adapter<MatchResultRecyclerViewAdapter.ViewHolder>() {
    var data: List<BattleshipMatchResult> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.matchDuration.text =
            StringBuilder(
                "${item.duration?.div(3600) ?: 0}:${item.duration?.rem(3600)?.div(60)
                    ?: 0}:${item.duration?.rem(60) ?: 0}"
            ).toString()
        holder.opponentName.text = item.opponentName ?: "Unknown player"
        holder.id.text = item.id.toString()
        when (item.result) {
            BattleshipMatchResult.BATTLESHIP_WIN -> holder.matchResult.text ="Win"
            BattleshipMatchResult.BATTLESHIP_LOSE -> holder.matchResult.text ="Lose"
            else -> holder.matchResult.text = "Unknown result"
        }

    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.match_stat_item, parent, false)

        return ViewHolder(view)
    }

    class ViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        val opponentName: TextView = view.textview_opponent_name
        val matchDuration: TextView = view.textview_time
        val matchResult: TextView = view.textview_match_result
        val id: TextView = view.textview_id
        val playerGrid: ImageView = view.imageview_player_grid
        val opponentGrid: ImageView = view.imageview_opponent_grid
    }

}