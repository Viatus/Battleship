package com.example.battleship.UI


import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.generateViewId
import androidx.databinding.DataBindingUtil
import com.example.battleship.R
import com.example.battleship.databinding.FragmentPlayerFieldBinding
import com.example.battleship.game.Grid
import com.example.battleship.game.GridCell
import com.example.battleship.game.ShotResult


/**
 * A simple [Fragment] subclass.
 */
class PlayerFieldFragment : Fragment() {

    var playerGrid = Grid()

    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var binding: FragmentPlayerFieldBinding

    private lateinit var fieldImages: Array<Array<FieldCellImageView>>

    private lateinit var chosenShips: Array<ImageView?>
    private lateinit var readyButton: Button

    private var direction = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_player_field, container, false)
        constraintLayout = binding.playerFieldConstrLayout
        val set = ConstraintSet()

        setupField(set)


        setupChosenShips(set)

        return binding.root
    }

    private fun setupChosenShips(set: ConstraintSet) {
        chosenShips = arrayOfNulls(8)
        var previousId = fieldImages[9][5].id
        for (i in 1..4) {
            previousId = addPlacingShip(set, i, true, previousId)
            if (i != 1) {
                addPlacingShip(set, i, false, 0)
            }
        }
        chosenShips[4] = chosenShips[0]
    }

    private fun addPlacingShip(
        set: ConstraintSet,
        length: Int,
        direction: Boolean,
        previousId: Int
    ): Int {
        val ship = ImageView(context)
        ship.id = generateViewId()
        ship.setBackgroundColor(ContextCompat.getColor(context!!, R.color.shipOK))
        ship.tag = "ship$length"
        constraintLayout.addView(ship)
        set.clone(constraintLayout)
        set.constrainWidth(ship.id, ConstraintSet.MATCH_CONSTRAINT)
        set.constrainHeight(ship.id, ConstraintSet.MATCH_CONSTRAINT)
        if (direction) {
            set.setDimensionRatio(ship.id, "$length:1")
            set.connect(
                ship.id,
                ConstraintSet.TOP,
                previousId,
                ConstraintSet.BOTTOM
            )
            set.connect(
                ship.id,
                ConstraintSet.LEFT,
                fieldImages[9][0].id,
                ConstraintSet.LEFT
            )
            set.connect(
                ship.id,
                ConstraintSet.RIGHT,
                fieldImages[9][length - 1].id,
                ConstraintSet.RIGHT
            )
            set.setMargin(ship.id, ConstraintSet.TOP, 10)
            chosenShips[length - 1] = ship
        } else {
            set.setDimensionRatio(ship.id, "1:$length")
            set.connect(
                ship.id,
                ConstraintSet.TOP,
                fieldImages[9][5].id,
                ConstraintSet.BOTTOM
            )
            set.connect(
                ship.id,
                ConstraintSet.LEFT,
                fieldImages[9][length + 4].id,
                ConstraintSet.LEFT
            )
            set.connect(
                ship.id,
                ConstraintSet.RIGHT,
                fieldImages[9][length + 4].id,
                ConstraintSet.RIGHT
            )
            chosenShips[4 + length - 1] = ship
        }
        set.applyTo(constraintLayout)
        ship.setOnLongClickListener { v -> onShipLongClicked(v, direction) }
        return ship.id
    }

    private fun setupField(set: ConstraintSet) {
        fieldImages = Array(10) {
            Array(10) { FieldCellImageView(context) }
        }
        for (i in 0..9) {
            for (j in 0..9) {
                fieldImages[i][j].i = i
                fieldImages[i][j].j = j
                fieldImages[i][j].id = generateViewId()
                fieldImages[i][j].updateImage()
                fieldImages[i][j].background =
                    ContextCompat.getDrawable(context!!, R.drawable.border)
                fieldImages[i][j].scaleType = ImageView.ScaleType.CENTER_CROP
                fieldImages[i][j].cropToPadding = true
                fieldImages[i][j].setOnDragListener { v, event -> onShipDragged(v, event) }
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


    private fun onShipLongClicked(v: View, direction: Boolean): Boolean {
        val item: ClipData.Item = ClipData.Item(v.tag as CharSequence)
        val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
        val data = ClipData(v.tag as CharSequence, mimeTypes, item)
        val dragshadow = View.DragShadowBuilder(v)
        v.startDrag(data, dragshadow, v, 0)
        this.direction = direction
        return true
    }

    private fun onReadyButtonPressed() {
        val act = activity as MatchActivity
        act.setPlayerReady()
        readyButton.visibility = View.GONE
    }


    private fun onShipDragged(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return true
                }
                return false
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                return true
            }
            DragEvent.ACTION_DROP -> {
                if (v !is FieldCellImageView) {
                    return false
                }
                when (event.clipDescription.label) {
                    "ship1" -> {
                        if (playerGrid.field[v.j][v.i] == GridCell.SEA) {
                            if (playerGrid.addShip(v.j, v.i, 1, true)) {
                                v.gridCell = GridCell.SHIP
                                v.updateImage()
                                v.invalidate()
                            }
                            if (playerGrid.getTotalAmountOfShips() == 10) {
                                gameSetup()
                            } else {
                                if (playerGrid.getAmountOfShipsByLength()[3] == 4) {
                                    hideShip(1)
                                }
                            }
                            return true
                        }
                        return false
                    }
                    "ship2" -> {
                        fitShip(v, 2)
                        if (playerGrid.getTotalAmountOfShips() == 10) {
                            gameSetup()
                        } else {
                            if (playerGrid.getAmountOfShipsByLength()[2] == 3) {
                                hideShip(2)
                            }
                        }
                    }
                    "ship3" -> {
                        fitShip(v, 3)
                        if (playerGrid.getTotalAmountOfShips() == 10) {
                            gameSetup()
                        } else {
                            if (playerGrid.getAmountOfShipsByLength()[1] == 2) {
                                hideShip(3)
                            }
                        }
                    }
                    "ship4" -> {
                        fitShip(v, 4)
                        if (playerGrid.getTotalAmountOfShips() == 10) {
                            gameSetup()
                        } else {
                            if (playerGrid.getAmountOfShipsByLength()[0] == 1) {
                                hideShip(4)
                            }
                        }
                    }
                }
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                return true
            }
            else -> {
                Log.e("PlayerFieldFragment: ", "Drag and drop failed due to unknown action")
            }
        }
        return false
    }

    private fun hideShip(deckAmount: Int) {
        chosenShips[deckAmount - 1]!!.visibility = View.INVISIBLE
        chosenShips[deckAmount + 3]!!.visibility = View.INVISIBLE
    }

    private fun gameSetup() {
        for (i in 1..4) {
            constraintLayout.removeView(chosenShips[i])
            if (i != 1) {
                constraintLayout.removeView(chosenShips[3 + i])
            }
        }
        readyButton = Button(context)
        readyButton.text = getString(R.string.readyButton)
        readyButton.id = generateViewId()
        val set = ConstraintSet()
        constraintLayout.addView(readyButton)
        set.clone(constraintLayout)
        set.constrainWidth(readyButton.id, ConstraintSet.MATCH_CONSTRAINT)
        set.constrainHeight(readyButton.id, ConstraintSet.MATCH_CONSTRAINT)
        set.setDimensionRatio(readyButton.id, "10:1")
        set.connect(
            readyButton.id,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT
        )
        set.connect(
            readyButton.id,
            ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT
        )
        set.connect(
            readyButton.id,
            ConstraintSet.TOP,
            fieldImages[9][5].id,
            ConstraintSet.BOTTOM
        )
        readyButton.setOnClickListener { onReadyButtonPressed() }
        set.applyTo(constraintLayout)
    }

    private fun fitShip(v: FieldCellImageView, length: Int) {
        if (direction) {
            val j = v.j - length / 3
            if (playerGrid.addShip(j, v.i, length, direction)) {
                for (i in 0 until length) {
                    fieldImages[v.i][j + i].gridCell = GridCell.SHIP
                    fieldImages[v.i][j + i].updateImage()
                    fieldImages[v.i][j + i].invalidate()
                }
            }
        } else {
            val i = v.i - length / 3
            if (playerGrid.addShip(v.j, i, length, direction)) {
                for (j in 0 until length) {
                    fieldImages[j + i][v.j].gridCell = GridCell.SHIP
                    fieldImages[j + i][v.j].updateImage()
                    fieldImages[j + i][v.j].invalidate()
                }
            }
        }
    }

    private fun updateField() {
        for (i in 0..9) {
            for (j in 0..9) {
                if (fieldImages[i][j].gridCell != playerGrid.field[j][i]) {
                    fieldImages[i][j].gridCell = playerGrid.field[j][i]
                    fieldImages[i][j].updateImage()
                }
            }
        }
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            val str = savedInstanceState.get("playerGrid") as String
            playerGrid.restoreFieldFromString(str)
            updateField()
            if (playerGrid.getAmountOfShipsByLength()[3] == 4) {
                hideShip(1)
            }
            if (playerGrid.getAmountOfShipsByLength()[2] == 3) {
                hideShip(2)
            }
            if (playerGrid.getAmountOfShipsByLength()[1] == 2) {
                hideShip(3)
            }
            if (playerGrid.getAmountOfShipsByLength()[0] == 1) {
                hideShip(4)
            }
        }
    }

    fun getShot(i: Int, j: Int): ShotResult {
        val shotResult = playerGrid.shoot(j, i)
        for (l in 0..9) {
            for (k in 0..9) {
                if (fieldImages[l][k].gridCell != playerGrid.field[k][l]) {
                    fieldImages[l][k].gridCell = playerGrid.field[k][l]
                    fieldImages[l][k].updateImage()
                }
            }
        }
        return shotResult
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("playerGrid", playerGrid.toString())
    }
}
