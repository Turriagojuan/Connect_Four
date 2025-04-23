package com.example.connect_four

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color

class DefeatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerName = intent.getStringExtra("playerName") ?: "Jugador"

        setContent {
            ResultScreen(
                message = "¡Perdiste, $playerName!",
                backgroundColor = Color(0xFFFFD6D6),
                textColor = Color(0xFFB71C1C),
                onRestart = {
                    val intent = Intent(this, GameActivity::class.java)
                    intent.putExtra("playerName", playerName)
                    startActivity(intent)
                },
                onHome = {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}
