package com.example.battleship.UI

import android.content.Context
import android.graphics.*
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.battleship.R
import com.example.battleship.database.model.BattleshipMatchResult
import com.example.battleship.game.Grid
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
            BattleshipMatchResult.BATTLESHIP_WIN -> holder.matchResult.text = "Win"
            BattleshipMatchResult.BATTLESHIP_LOSE -> holder.matchResult.text = "Lose"
            else -> holder.matchResult.text = "Unknown result"
        }
        holder.playerGrid.post {
            paintBattleshipGrid(
                item.playerField,
                holder.playerGrid
            )
        }
        holder.opponentGrid.post {
            paintBattleshipGrid(
                item.opponentField,
                holder.opponentGrid
            )
        }
    }

    private fun paintBattleshipGrid(grid: String?, imageView: ImageView) {
        val width = imageView.width
        val height = imageView.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        imageView.setImageBitmap(bitmap)
        paint.color = Color.rgb(186, 182, 209)
        canvas.drawColor(paint.color)
        if (grid != null) {
            for (i in 0..9) {
                for (j in 0..9) {
                    when (grid[i * 10 + j]) {
                        '0' -> {
                            paint.color = Color.rgb(49, 126, 204)
                        }
                        '1' -> {
                            paint.color = Color.rgb(153, 204, 255)
                        }
                        '2' -> {
                            paint.color = Color.rgb(51, 204, 51)
                        }
                        '3' -> {
                            paint.color = Color.rgb(230, 115, 0)
                        }
                        '4' -> {
                            paint.color = Color.rgb(102, 0, 51)
                        }
                    }
                    canvas.drawRect(
                        i * width / 10 + 1f,
                        j * height / 10 + 1f,
                        (i + 1) * width / 10 - 1f,
                        (j + 1) * height / 10 - 1f,
                        paint
                    )
                }
            }
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