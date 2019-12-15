package com.example.battleship.UI.snackbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.battleship.R
import com.example.battleship.game.ShotResult
import com.google.android.material.snackbar.BaseTransientBottomBar

class ShotResultSnackbar(
    parent: ViewGroup,
    content: ShotResultSnackbarView
) : BaseTransientBottomBar<ShotResultSnackbar>(parent, content, content) {
    init {
        getView().setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                android.R.color.transparent
            )
        )
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {

        fun make(view: View, shotResult: ShotResult): ShotResultSnackbar {

            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            val customView = LayoutInflater.from(view.context).inflate(
                R.layout.layout_snackbar_shotresult,
                parent,
                false
            ) as ShotResultSnackbarView

            customView.changeShotResult(shotResult)

            return ShotResultSnackbar(
                parent,
                customView
            )
        }

    }

}