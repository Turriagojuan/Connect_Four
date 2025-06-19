package com.example.connect_four

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connect_four.data.Game
import kotlinx.coroutines.launch

class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerName = intent.getStringExtra("playerName") ?: "Player"
        setContent {
            MaterialTheme {
                LobbyScreen(playerName = playerName)
            }
        }
    }
}

@Composable
fun LobbyScreen(playerName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val availableGames by FirebaseService.getAvailableGames().collectAsState(initial = emptyList())
    var isLoading by remember { mutableStateOf(false) }

    val createGame: () -> Unit = {
        scope.launch {
            isLoading = true
            try {
                val gameId = FirebaseService.createGame(playerName)
                val intent = Intent(context, OnlineGameActivity::class.java).apply {
                    putExtra("GAME_ID", gameId)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Handle error
            }
            isLoading = false
        }
    }

    val joinGame: (String) -> Unit = { gameId ->
        scope.launch {
            isLoading = true
            try {
                FirebaseService.joinGame(gameId, playerName)
                val intent = Intent(context, OnlineGameActivity::class.java).apply {
                    putExtra("GAME_ID", gameId)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Handle error
            }
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xfff5f5f5))) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lobby", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = createGame, enabled = !isLoading) {
                Text("Crear Nueva Partida")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Partidas Disponibles", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (availableGames.isEmpty()) {
                Text("No hay partidas disponibles. ¡Crea una!", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(availableGames) { game ->
                        GameItem(game, onJoin = { joinGame(game.id) })
                    }
                }
            }
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun GameItem(game: Game, onJoin: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onJoin),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Partida de ${game.player1Name}", fontWeight = FontWeight.SemiBold)
            Text("Unirse", color = MaterialTheme.colorScheme.primary)
        }
    }
}