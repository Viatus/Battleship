package com.example.battleship.UI.snackbar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.battleship.R
import com.example.battleship.game.ShotResult
import com.google.android.material.snackbar.ContentViewCallback

class ShotResultSnackbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {
    private val resultImage: ImageView
    private val resultText: TextView

    init {
        View.inflate(context, R.layout.snackbar_layout, this)
        this.resultImage = findViewById(R.id.result_image)
        this.resultText = findViewById(R.id.message)
        clipToPadding = false
    }

    override fun animateContentIn(delay: Int, duration: Int) {
        val scaleX = ObjectAnimator.ofFloat(resultImage, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(resultImage, View.SCALE_Y, 0f, 1f)
        val animatorSet = AnimatorSet().apply {
            interpolator = OvershootInterpolator()
            setDuration(500)
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {

    }

    fun changeShotResult(shotResult: ShotResult) {
        when (shotResult) {
            ShotResult.MISS -> {
                resultImage.setImageDrawable(context.getDrawable(R.drawable.water_splash))
                resultText.text = context.getText(R.string.missed_message)
            }
            ShotResult.HIT -> {
                resultImage.setImageDrawable(context.getDrawable(R.drawable.fire_and_smoke))
                resultText.text = context.getString(R.string.hit_message)
            }
            ShotResult.DESTROYED -> {
                resultImage.setImageDrawable(context.getDrawable(R.drawable.explosion))
                resultText.text = context.getString(R.string.destroyed_message)
            }
        }
    }
}
