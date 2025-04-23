package com.example.connect_four

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.connect_four.ui.theme.GameModel.GameState
import com.example.connect_four.ui.theme.GameStatus
import com.example.connect_four.ui.theme.Cell

@Composable
fun InGameScreen(
    playerName: String,
    onGameEnd: (winner: Cell?) -> Unit
) {
    var state by remember { mutableStateOf(GameState()) }

    // Verificar si terminó el juego
    if (state.status != GameStatus.PLAYING) {
        LaunchedEffect(state.status) {
            onGameEnd(
                when (state.status) {
                    GameStatus.PLAYER_WON -> Cell.PLAYER
                    GameStatus.MACHINE_WON -> Cell.MACHINE
                    GameStatus.DRAW -> null
                    else -> null
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Turno: ${if (state.currentPlayer == Cell.PLAYER) playerName else "Máquina"}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Board(state.board) { column ->
            if (state.currentPlayer == Cell.PLAYER && dropPiece(column, state)) {
                if (state.status == GameStatus.PLAYING) {
                    // Movimiento máquina después del jugador
                    machineMove(state)
                }
            }
        }

        if (state.status != GameStatus.PLAYING) {
            Button(
                onClick = { state = GameState() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Reiniciar")
            }
        }
    }
}

@Composable
fun Board(
    board: List<List<Cell>>,
    onColumnClick: (Int) -> Unit
) {
    Column {
        for (row in board) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for ((colIndex, cell) in row.withIndex()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when (cell) {
                                    Cell.EMPTY -> Color.LightGray
                                    Cell.PLAYER -> Color.Red
                                    Cell.MACHINE -> Color.Yellow
                                }
                            )
                            .clickable { onColumnClick(colIndex) },
                        contentAlignment = Alignment.Center
                    ) { }
                }
            }
        }
    }
}

fun machineMove(state: GameState) {
    if (state.currentPlayer != Cell.MACHINE || state.status != GameStatus.PLAYING) return

    val availableColumns = (0..6).filter { col ->
        state.board[0][col] == Cell.EMPTY
    }

    if (availableColumns.isNotEmpty()) {
        val randomColumn = availableColumns.random()
        dropPiece(randomColumn, state)
    }
}
