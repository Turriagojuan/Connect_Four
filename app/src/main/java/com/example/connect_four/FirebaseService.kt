package com.example.connect_four

import com.example.connect_four.data.Game
import com.example.connect_four.data.VocabularyWord
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FirebaseService {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val gamesCollection = db.collection("games")
    private val vocabularyCollection = db.collection("vocabulary")


    suspend fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun createGame(playerName: String): String {
        val userId = getCurrentUserId() ?: throw Exception("User not signed in")
        val game = Game(player1Id = userId, player1Name = playerName, currentPlayerId = userId)
        val gameDocument = gamesCollection.add(game).await()
        return gameDocument.id
    }

    suspend fun joinGame(gameId: String, playerName: String) {
        val userId = getCurrentUserId() ?: throw Exception("User not signed in")
        gamesCollection.document(gameId).update(
            mapOf("player2Id" to userId, "player2Name" to playerName, "status" to "IN_PROGRESS")
        ).await()
    }

    fun getAvailableGames(): Flow<List<Game>> = callbackFlow {
        val listener = gamesCollection
            .whereEqualTo("status", "WAITING")
            .addSnapshotListener { snapshot, _ ->
                val games = snapshot?.documents?.mapNotNull {
                    it.toObject(Game::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(games)
            }
        awaitClose { listener.remove() }
    }

    fun getGameUpdates(gameId: String): Flow<Game> = callbackFlow {
        val listener = gamesCollection.document(gameId)
            .addSnapshotListener { snapshot, _ ->
                val game = snapshot?.toObject(Game::class.java)?.copy(id = snapshot.id)
                if (game != null) trySend(game)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateGame(gameId: String, updates: Map<String, Any>) {
        gamesCollection.document(gameId).update(updates).await()
    }

    // NUEVA FUNCIÓN para obtener una palabra aleatoria de Firestore
    suspend fun getRandomVocabularyWord(): VocabularyWord? {
        // Genera una ID aleatoria para empezar la búsqueda
        val randomId = db.collection("tmp").document().id
        val snapshot = vocabularyCollection
            .whereGreaterThan(FieldPath.documentId(), randomId)
            .limit(1)
            .get()
            .await()

        return if (snapshot.documents.isNotEmpty()) {
            snapshot.documents.first().toObject(VocabularyWord::class.java)
        } else {
            // Si no encuentra, busca desde el principio
            val secondSnapshot = vocabularyCollection.limit(1).get().await()
            secondSnapshot.documents.firstOrNull()?.toObject(VocabularyWord::class.java)
        }
    }
}