package com.example.connect_four

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class InGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerName = intent.getStringExtra("playerName") ?: "Jugador"

        setContent {
            InGameScreen(
                playerName = playerName,
                onGameEnd = { winner ->
                    // TODO: navegar a VictoryActivity o DefeatActivity según winner
                }
            )
        }
    }
}