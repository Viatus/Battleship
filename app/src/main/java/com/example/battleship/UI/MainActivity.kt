package com.example.battleship.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.battleship.R
import com.example.battleship.databinding.ActivityMainBinding
import android.widget.LinearLayout
import android.widget.EditText
import com.example.battleship.StatiscticActivity
import com.example.battleship.UI.snackbar.ShotResultSnackbar
import com.example.battleship.game.ShotResult


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var nickname: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nickname = resources.getString(R.string.default_username)
        binding =
            DataBindingUtil.setContentView(this, com.example.battleship.R.layout.activity_main)

        binding.startMacthButton.setOnClickListener {
            val intent = Intent(
                this, MatchActivity::class.java
            )
            intent.putExtra("NICKNAME",nickname)
            startActivity(intent)
        }

        binding.statButton.setOnClickListener {
            val intent = Intent(this, StatiscticActivity::class.java)
            startActivity(intent)
        }

        binding.changeUsernameButton.setOnClickListener { onChangeUsernameClicked() }
    }

    fun onChangeUsernameClicked() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Username")
        alertDialog.setMessage("Change your username")

        val input = EditText(this@MainActivity)
        input.setText(nickname)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        alertDialog.setView(input)

        alertDialog.setPositiveButton(
            "Change"
        ) { dialog, which ->
            nickname = input.text.toString()
        }

        alertDialog.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        alertDialog.show()
    }


}
