package com.example.battleship.UI

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.battleship.R
import com.example.battleship.game.GridCell

class FieldCellImageView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    var i: Int = 0
    var j: Int = 0

    var gridCell: GridCell = GridCell.SEA

    fun updateImage() {
        when (gridCell) {
            GridCell.SEA -> this.setImageResource(R.drawable.water)
            GridCell.HIT -> {
                this.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.shipHit
                    )
                )
                this.setImageDrawable(null)
            }
            GridCell.SHIP -> {
                this.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.shipOK
                    )
                )
                this.setImageDrawable(null)
            }
            GridCell.DESTROYED -> {
                this.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.shipDead
                    )
                )
                this.setImageDrawable(null)
            }
            GridCell.CHECKED -> {
                this.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.checked
                    )
                )
                this.setImageDrawable(null)
            }
        }


    }
}