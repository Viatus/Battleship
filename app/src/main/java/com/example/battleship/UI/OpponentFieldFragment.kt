package com.example.battleship.UI


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil

import com.example.battleship.R
import com.example.battleship.databinding.FragmentOpponentFieldBinding
import com.example.battleship.game.Grid
import com.example.battleship.game.GridCell
import com.example.battleship.game.ShotResult

/**
 * A simple [Fragment] subclass.
 */
class OpponentFieldFragment : Fragment() {

    var opponentGrid = Grid()

    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var binding: FragmentOpponentFieldBinding

    private lateinit var fieldImages: Array<Array<FieldCellImageView>>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_opponent_field, container, false)
        constraintLayout = binding.opponentFieldConstrLayout
        val set = ConstraintSet()

        setupField(set)

        return binding.root
    }

    private fun setupField(set: ConstraintSet) {
        fieldImages = Array(10) {
            Array(10) { FieldCellImageView(context) }
        }
        for (i in 0..9) {
            for (j in 0..9) {
                fieldImages[i][j].i = i
                fieldImages[i][j].j = j
                fieldImages[i][j].id = ViewCompat.generateViewId()
                fieldImages[i][j].updateImage()
                fieldImages[i][j].background =
                    ContextCompat.getDrawable(context!!, R.drawable.border)
                fieldImages[i][j].scaleType = ImageView.ScaleType.CENTER_CROP
                fieldImages[i][j].cropToPadding = true
                fieldImages[i][j].setOnClickListener { onFieldImageClicked(i, j); }
                constraintLayout.addView(fieldImages[i][j])
                set.clone(constraintLayout)
                set.setDimensionRatio(fieldImages[i][j].id, "1")
                set.setHorizontalWeight(fieldImages[i][j].id, 1.0F)
                set.setVerticalWeight(fieldImages[i][j].id, 1.0F)
                set.constrainWidth(fieldImages[i][j].id, ConstraintSet.MATCH_CONSTRAINT)
                set.constrainHeight(fieldImages[i][j].id, ConstraintSet.MATCH_CONSTRAINT)
                if (i == 0) {
                    set.connect(
                        fieldImages[i][j].id,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP
                    )
                } else {
                    set.connect(
                        fieldImages[i][j].id,
                        ConstraintSet.TOP,
                        fieldImages[i - 1][j].id,
                        ConstraintSet.BOTTOM
                    )
                    set.connect(
                        fieldImages[i - 1][j].id,
                        ConstraintSet.BOTTOM,
                        fieldImages[i][j].id,
                        ConstraintSet.TOP
                    )
                }
                if (j == 0) {
                    set.connect(
                        fieldImages[i][j].id,
                        ConstraintSet.LEFT,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.LEFT
                    )
                } else {
                    set.connect(
                        fieldImages[i][j].id,
                        ConstraintSet.LEFT,
                        fieldImages[i][j - 1].id,
                        ConstraintSet.RIGHT
                    )
                    if (j == 9) {
                        set.connect(
                            fieldImages[i][j].id,
                            ConstraintSet.RIGHT,
                            ConstraintSet.PARENT_ID,
                            ConstraintSet.RIGHT
                        )
                    }
                    set.connect(
                        fieldImages[i][j - 1].id,
                        ConstraintSet.RIGHT,
                        fieldImages[i][j].id,
                        ConstraintSet.LEFT
                    )
                }
                set.applyTo(constraintLayout)
            }
        }

    }

    private fun onFieldImageClicked(i: Int, j: Int) {
        if (fieldImages[i][j].gridCell == GridCell.SEA) {
            val parentActivity = activity as MatchActivity
            if (parentActivity.playerTurn) {
                parentActivity.playerShot = Pair(i, j)
                parentActivity.sendShotAtCurrentCoordinate()
            }
        }
    }

    fun updateAfterShotResult(i: Int, j: Int, result: ShotResult) {
        opponentGrid.updateField(j, i, result)
        for (l in 0..9) {
            for (k in 0..9) {
                if (fieldImages[l][k].gridCell != opponentGrid.field[k][l]) {
                    fieldImages[l][k].gridCell = opponentGrid.field[k][l]
                    fieldImages[l][k].updateImage()
                }
            }
        }
    }

}
