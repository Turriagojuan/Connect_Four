package com.example.connect_four

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.connect_four.ui.theme.Connect_FourTheme

// Factory para pasar el gameId al ViewModel
class OnlineGameViewModelFactory(private val gameId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnlineGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnlineGameViewModel(gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class OnlineGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameId = intent.getStringExtra("GAME_ID") ?: return // O manejar error

        val viewModel: OnlineGameViewModel by viewModels { OnlineGameViewModelFactory(gameId) }

        setContent {
            Connect_FourTheme {
                val uiState by viewModel.uiState.collectAsState()
                val game by viewModel.game.collectAsState()
                val currentUserId = FirebaseService.getCurrentUserId()

                game?.let {
                    // Aquí se manejaría la navegación a pantallas de victoria/derrota/empate
                    // similar a como lo tenías en GameActivity, pero basado en game.status
                }

                OnlineGameScreen(
                    game = game,
                    currentUserId = currentUserId,
                    onColumnClick = { col -> viewModel.dropPiece(col) },
                    uiState = uiState,
                    onAnswerSubmit = { answer -> viewModel.submitQuizAnswer(answer) }
                )
            }
        }
    }
}