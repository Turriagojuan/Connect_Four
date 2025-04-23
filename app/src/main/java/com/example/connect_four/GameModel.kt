package com.example.connect_four

enum class Cell {
    EMPTY, PLAYER, MACHINE
}
enum class GameStatus {
    PLAYING, PLAYER_WON, MACHINE_WON, DRAW
}
data class GameState(
    val board: List<MutableList<Cell>> = List(6) { MutableList(7) { Cell.EMPTY } },
    var currentPlayer: Cell = Cell.PLAYER,
    var status: GameStatus = GameStatus.PLAYING
)
fun dropPiece(column: Int, state: GameState): Boolean {
    if (column !in 0..6 || state.status != GameStatus.PLAYING) return false
    for (row in 5 downTo 0) {
        if (state.board[row][column] == Cell.EMPTY) {
            state.board[row][column] = state.currentPlayer
            checkGameStatus(state, row, column)
            return true
        }
    }
    return false // Columna llena
}
fun switchTurn(state: GameState) {
    state.currentPlayer = if (state.currentPlayer == Cell.PLAYER) Cell.MACHINE else Cell.PLAYER
}
fun checkGameStatus(state: GameState, row: Int, col: Int) {
    val player = state.board[row][col]
    if (hasFourConnected(state.board, row, col, player)) {
        state.status = if (player == Cell.PLAYER) GameStatus.PLAYER_WON else GameStatus.MACHINE_WON
    } else if (state.board.all { it.all { cell -> cell != Cell.EMPTY } }) {
        state.status = GameStatus.DRAW
    } else {
        switchTurn(state)
    }
}
fun hasFourConnected(board: List<List<Cell>>, row: Int, col: Int, player: Cell): Boolean {
    fun count(dirRow: Int, dirCol: Int): Int {
        var r = row + dirRow
        var c = col + dirCol
        var count = 0
        while (r in 0..5 && c in 0..6 && board[r][c] == player) {
            count++
            r += dirRow
            c += dirCol
        }
        return count
    }

    val directions = listOf(
        Pair(0, 1),  // Horizontal
        Pair(1, 0),  // Vertical
        Pair(1, 1),  // Diagonal ↘
        Pair(1, -1)  // Diagonal ↙
    )

    return directions.any { (dr, dc) ->
        1 + count(dr, dc) + count(-dr, -dc) >= 4
    }
}

