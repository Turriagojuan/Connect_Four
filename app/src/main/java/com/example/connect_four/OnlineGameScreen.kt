package com.example.connect_four

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.connect_four.data.Game

@Composable
fun OnlineGameScreen(
    game: Game?,
    currentUserId: String?,
    onColumnClick: (Int) -> Unit,
    uiState: OnlineGameUiState,
    onAnswerSubmit: (String) -> Unit
) {
    if (game == null || currentUserId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val player1Color = Color(0xFFE57373)
    val player2Color = Color(0xFFFFB74D)

    if (uiState.showQuizDialog) {
        VocabularyQuizDialog(
            word = uiState.currentWord?.spanish ?: "",
            onDismiss = { /* No se puede cerrar */ },
            onConfirm = onAnswerSubmit
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PlayerHeader(
                name = game.player1Name,
                color = player1Color,
                isTurn = game.currentPlayerId == game.player1Id
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("VS", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            PlayerHeader(
                name = game.player2Name ?: "...",
                color = player2Color,
                isTurn = game.currentPlayerId == game.player2Id
            )
        }

        Text(uiState.message, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        OnlineGameBoard(
            board = game.board,
            player1Color = player1Color,
            player2Color = player2Color,
            onColumnClick = onColumnClick,
            enabled = uiState.canPlay && uiState.isMyTurn
        )

        // Botón de reinicio o para volver al lobby (se puede añadir aquí)
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun PlayerHeader(name: String, color: Color, isTurn: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                if (isTurn) Color.LightGray.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(modifier = Modifier.size(20.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OnlineGameBoard(
    board: List<List<Int>>,
    player1Color: Color,
    player2Color: Color,
    onColumnClick: (Int) -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(7f / 6f)
            .background(Color(0xFF1A56B0), RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Column {
            repeat(6) { rowIndex ->
                Row(Modifier.weight(1f)) {
                    repeat(7) { colIndex ->
                        val cellValue = board[rowIndex][colIndex]
                        val color = when (cellValue) {
                            1 -> player1Color
                            2 -> player2Color
                            else -> Color.White
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                                .clickable(enabled = enabled) { onColumnClick(colIndex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VocabularyQuizDialog(
    word: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¡Pregunta de Vocabulario!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Traduce la siguiente palabra al inglés:")
                Text("'$word'", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Tu respuesta") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onConfirm(text) }) {
                    Text("Confirmar")
                }
            }
        }
    }
}