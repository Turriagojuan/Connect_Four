package com.example.connect_four

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.connect_four.data.Game
import com.example.connect_four.data.VocabularyWord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnlineGameUiState(
    val showQuizDialog: Boolean = false,
    val currentWord: VocabularyWord? = null,
    val isMyTurn: Boolean = false,
    val canPlay: Boolean = false, // True solo después de acertar la pregunta
    val message: String = ""
)

class OnlineGameViewModel(private val gameId: String) : ViewModel() {

    val game: StateFlow<Game?> = FirebaseService.getGameUpdates(gameId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(OnlineGameUiState())
    val uiState: StateFlow<OnlineGameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            game.collect { gameData ->
                if (gameData != null) handleGameUpdate(gameData)
            }
        }
    }


    private fun handleGameUpdate(gameData: Game) {
        val currentUserId = FirebaseService.getCurrentUserId()
        val isMyTurn = gameData.currentPlayerId == currentUserId && gameData.status == "IN_PROGRESS"

        if (isMyTurn && !_uiState.value.showQuizDialog && !_uiState.value.canPlay) {
            startQuiz()
        }
        _uiState.update { it.copy(isMyTurn = isMyTurn, message = getMessage(gameData)) }
    }

    private fun getMessage(gameData: Game): String {
        val currentUserId = FirebaseService.getCurrentUserId()
        return when (gameData.status) {
            "WAITING" -> "Esperando al oponente..."
            "IN_PROGRESS" -> if (gameData.currentPlayerId == currentUserId) "¡Tu turno!" else "Turno del oponente."
            "FINISHED" -> if (gameData.winnerId == null) "¡Empate!" else if (gameData.winnerId == currentUserId) "¡Ganaste!" else "Perdiste."
            else -> ""
        }
    }

    private fun startQuiz() {
        viewModelScope.launch {
            val word = FirebaseService.getRandomVocabularyWord()
            if (word != null) {
                _uiState.update { it.copy(showQuizDialog = true, currentWord = word) }
            } else {
                // Si no hay vocabulario, permitir jugar directamente.
                _uiState.update { it.copy(canPlay = true) }
            }
        }
    }

    fun submitQuizAnswer(answer: String) {
        val isCorrect = answer.equals(_uiState.value.currentWord?.english, ignoreCase = true)
        _uiState.update { it.copy(showQuizDialog = false, canPlay = isCorrect) }

        if (!isCorrect) {
            passTurn()
        }
    }

    private fun passTurn() {
        viewModelScope.launch {
            val currentGame = game.value ?: return@launch
            val nextPlayerId = if (currentGame.player1Id == currentGame.currentPlayerId) {
                currentGame.player2Id
            } else {
                currentGame.player1Id
            }
            if (nextPlayerId != null) {
                FirebaseService.updateGame(gameId, mapOf("currentPlayerId" to nextPlayerId))
            }
        }
    }

    fun dropPiece(columnIndex: Int) {
        val currentGame = game.value ?: return
        if (!_uiState.value.canPlay || currentGame.currentPlayerId != FirebaseService.getCurrentUserId()) return

        // SOLUCIÓN: Cambia la forma en que se crea la copia del tablero
        val board = currentGame.board.toMutableList()
        // SOLUCIÓN: Lógica para encontrar la fila objetivo en una lista 1D
        val targetRow = (5 downTo 0).firstOrNull { board[it * 7 + columnIndex] == 0 }

        if (targetRow != null) {
            val playerNumber = if (currentGame.currentPlayerId == currentGame.player1Id) 1 else 2
            // SOLUCIÓN: Actualiza el tablero 1D
            board[targetRow * 7 + columnIndex] = playerNumber

            val nextPlayerId = if (playerNumber == 1) currentGame.player2Id!! else currentGame.player1Id
            // La nueva lista ya es `board`, que es una lista simple
            val newBoardState = board.toList()

            val winnerId = if (checkWin(newBoardState, playerNumber)) currentGame.currentPlayerId else null
            // SOLUCIÓN: Lógica de empate para el tablero 1D
            val isDraw = newBoardState.all { it != 0 }
            val newStatus = when {
                winnerId != null -> "FINISHED"
                isDraw -> "FINISHED"
                else -> "IN_PROGRESS"
            }

            viewModelScope.launch {
                val updates = mutableMapOf<String, Any>(
                    "board" to newBoardState,
                    "currentPlayerId" to nextPlayerId,
                    "status" to newStatus
                )
                if(winnerId != null) updates["winnerId"] = winnerId

                FirebaseService.updateGame(gameId, updates)
                _uiState.update { it.copy(canPlay = false) }
            }
        }
    }

    // Función de chequeo de victoria simple (puedes mejorarla)
    private fun checkWin(board: List<Int>, playerNumber: Int): Boolean {
        // Comprobación Horizontal
        for (r in 0..5) {
            for (c in 0..3) {
                if ((0..3).all { board[r * 7 + (c + it)] == playerNumber }) return true
            }
        }
        // Comprobación Vertical
        for (r in 0..2) {
            for (c in 0..6) {
                if ((0..3).all { board[(r + it) * 7 + c] == playerNumber }) return true
            }
        }
        // Comprobación Diagonal (descendente, \)
        for (r in 0..2) {
            for (c in 0..3) {
                if ((0..3).all { board[(r + it) * 7 + (c + it)] == playerNumber }) return true
            }
        }
        // Comprobación Diagonal (ascendente, /)
        for (r in 3..5) {
            for (c in 0..3) {
                if ((0..3).all { board[(r - it) * 7 + (c + it)] == playerNumber }) return true
            }
        }
        return false
    }
}