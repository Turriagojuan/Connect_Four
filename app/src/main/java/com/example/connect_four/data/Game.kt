package com.example.connect_four.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Game(
    val id: String = "",
    val player1Id: String = "",
    val player1Name: String = "Player 1",
    val player2Id: String? = null,
    val player2Name: String? = "Waiting for Player 2...",
    val board: List<List<Int>> = List(6) { List(7) { 0 } },
    val currentPlayerId: String = player1Id,
    val status: String = "WAITING",
    val winnerId: String? = null,
    @ServerTimestamp val createdAt: Date? = null
)

data class VocabularyWord(
    val spanish: String = "",
    val english: String = ""
)