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

        val board = currentGame.board.map { it.toMutableList() }
        val targetRow = (5 downTo 0).firstOrNull { board[it][columnIndex] == 0 }

        if (targetRow != null) {
            val playerNumber = if (currentGame.currentPlayerId == currentGame.player1Id) 1 else 2
            board[targetRow][columnIndex] = playerNumber

            val nextPlayerId = if (playerNumber == 1) currentGame.player2Id!! else currentGame.player1Id
            val newBoardState = board.map { it.toList() }

            // Lógica de victoria/empate simplificada
            // (Una implementación completa revisaría horizontales, verticales y diagonales)
            val winnerId = if (checkWin(newBoardState, playerNumber)) currentGame.currentPlayerId else null
            val isDraw = newBoardState.all { row -> row.all { it != 0 } }
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
                _uiState.update { it.copy(canPlay = false) } // Bloquear hasta el próximo turno
            }
        }
    }

    // Función de chequeo de victoria simple (puedes mejorarla)
    private fun checkWin(board: List<List<Int>>, playerNumber: Int): Boolean {
        // Horizontal
        for (r in 0..5) {
            for (c in 0..3) {
                if ((0..3).all { board[r][c + it] == playerNumber }) return true
            }
        }
        // Vertical
        for (r in 0..2) {
            for (c in 0..6) {
                if ((0..3).all { board[r + it][c] == playerNumber }) return true
            }
        }
        // Diagonales (simplificado)
        // ... (implementa la lógica completa)
        return false
    }
}