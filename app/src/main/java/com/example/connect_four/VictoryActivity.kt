package com.example.connect_four

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color

class VictoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerName = intent.getStringExtra("playerName") ?: "Jugador"

        setContent {
            ResultScreen(
                message = "¡Ganaste, $playerName!",
                backgroundColor = Color(0xFFDFFFD6),
                textColor = Color(0xFF1B5E20),
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