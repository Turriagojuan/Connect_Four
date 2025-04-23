package com.example.connect_four

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.connect_four.ui.theme.ConnectFourTheme
import com.example.connect_four.ui.theme.InGameScreen // Assuming this is your composable

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Jugador"

        setContent {
            ConnectFourTheme {
                InGameScreen(playerName = playerName) { winner ->
                    when (winner) {
                        Cell.PLAYER -> {
                            val intent = Intent(this, VictoryActivity::class.java)
                            intent.putExtra("PLAYER_NAME", playerName)
                            startActivity(intent)
                            finish()
                        }
                        Cell.MACHINE -> {
                            val intent = Intent(this, DefeatActivity::class.java)
                            intent.putExtra("PLAYER_NAME", playerName)
                            startActivity(intent)
                            finish()
                        }
                        null -> {
                            val intent = Intent(this, DrawActivity::class.java) // Assuming you want to create a DrawActivity
                            intent.putExtra("DRAW", true)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}
