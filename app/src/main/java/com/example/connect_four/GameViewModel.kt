package com.example.connect_four

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Constantes del Juego ---

/** Número de filas en el tablero de Conecta 4. */
const val ROWS = 6
/** Número de columnas en el tablero de Conecta 4. */
const val COLS = 7

// --- Representación de Jugadores ---

/** Enumera los posibles jugadores en la partida. */
enum class Player {
    /** Representa al jugador humano. */
    USER,
    /** Representa al oponente controlado por la máquina (CPU). */
    CPU
}

// --- Estado de una Celda ---

/**
 * Representa el estado de una celda individual en el tablero.
 * Es una clase sellada (sealed class), lo que significa que todas sus subclases
 * deben estar definidas en este mismo archivo, permitiendo un manejo exhaustivo en bloques 'when'.
 */
sealed class CellState {
    /** Representa una celda vacía. */
    object Empty : CellState()
    /**
     * Representa una celda ocupada por una ficha.
     * @property player El jugador (USER o CPU) que ocupa la celda.
     */
    data class Occupied(val player: Player) : CellState()
}

// --- Estado General del Juego ---

/** Enumera los posibles estados o resultados de la partida. */
enum class GameStatus {
    /** La partida está en curso. */
    ONGOING,
    /** El jugador humano (USER) ha ganado. */
    USER_WINS,
    /** La máquina (CPU) ha ganado. */
    CPU_WINS,
    /** La partida ha terminado en empate (tablero lleno sin ganador). */
    DRAW
}

// --- Estado de la UI del Juego ---

/**
 * Clase de datos (data class) que encapsula todo el estado necesario para
 * representar la interfaz de usuario del juego en un momento dado.
 * Es inmutable por defecto, lo que favorece un manejo de estado predecible.
 *
 * @property board El estado actual del tablero, representado como una lista de listas de [CellState].
 * @property currentPlayer El jugador ([Player]) que tiene el turno actual.
 * @property status El estado actual de la partida ([GameStatus]).
 * @property userPieceColor El color de las fichas del jugador humano.
 * @property cpuPieceColor El color de las fichas de la máquina.
 * @property boardColor El color de fondo del tablero.
 * @property emptyCellColor El color usado para representar las celdas vacías.
 * @property canPlay Indica si el usuario puede interactuar con el tablero (poner fichas). Se usa para deshabilitar la interacción mientras la CPU "piensa".
 */
data class GameUiState(
    val board: List<List<CellState>> = List(ROWS) { List(COLS) { CellState.Empty } }, // Inicializa un tablero vacío
    val currentPlayer: Player = Player.USER, // El usuario empieza
    val status: GameStatus = GameStatus.ONGOING, // El juego empieza en curso
    val userPieceColor: Color = Color(0xFFE57373), // Rojo por defecto para el usuario
    val cpuPieceColor: Color = Color(0xFFFFB74D), // Amarillo por defecto para la CPU
    val boardColor: Color = Color(0xFF1A56B0), // Azul oscuro por defecto para el tablero
    val emptyCellColor: Color = Color.White, // Blanco por defecto para celdas vacías
    val canPlay: Boolean = true // El usuario puede jugar al inicio
)

// --- ViewModel ---

/**
 * GameViewModel: Gestiona la lógica del juego Conecta 4 y mantiene su estado.
 * Separa la lógica del juego de la UI (Activity/Composable).
 * Usa [ViewModel] de Android Architecture Components para sobrevivir a cambios de configuración (como rotaciones).
 * Expone el estado del juego (`GameUiState`) a través de un [StateFlow] para que la UI lo observe.
 */
class GameViewModel : ViewModel() {

    // _uiState: Flujo mutable interno que contiene el estado actual del juego.
    // Solo es modificable desde dentro del ViewModel.
    private val _uiState = MutableStateFlow(GameUiState())

    /**
     * uiState: Flujo público e inmutable (StateFlow) que expone el estado actual del juego (`GameUiState`).
     * La UI (GameActivity/GameScreen) observará este flujo para actualizarse.
     */
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    /**
     * Procesa la acción del usuario de soltar una ficha en una columna específica.
     * Solo actúa si la partida está en curso, es el turno del usuario y la interacción está habilitada.
     *
     * @param columnIndex El índice (0 a COLS-1) de la columna donde el usuario quiere soltar la ficha.
     */
    fun dropPiece(columnIndex: Int) {
        // Validaciones: No hacer nada si el juego terminó, no es turno del USER, o si la CPU está pensando.
        if (_uiState.value.status != GameStatus.ONGOING || _uiState.value.currentPlayer != Player.USER || !_uiState.value.canPlay) {
            return
        }

        // Encuentra la fila disponible más baja en la columna seleccionada.
        val targetRow = findAvailableRow(columnIndex)
        // Si targetRow es -1, la columna está llena, no hacer nada.
        if (targetRow == -1) return

        // Actualiza el estado del tablero colocando la ficha del usuario.
        updateBoard(targetRow, columnIndex, Player.USER)

        // Comprueba si el movimiento actual resultó en victoria para el usuario.
        if (checkWin(Player.USER)) {
            // Actualiza el estado a USER_WINS y deshabilita el juego.
            _uiState.update { it.copy(status = GameStatus.USER_WINS, canPlay = false) }
            // La UI reaccionará a este cambio de estado (navegando a VictoryActivity).
            return
        }
        // Comprueba si el movimiento actual resultó en empate.
        if (checkDraw()) {
            // Actualiza el estado a DRAW y deshabilita el juego.
            _uiState.update { it.copy(status = GameStatus.DRAW, canPlay = false) }
            // La UI reaccionará a este cambio de estado (navegando a DefeatActivity).
            return
        }

        // Si el juego continúa, cambia el turno a la CPU.
        // Deshabilita la interacción del usuario (`canPlay = false`) mientras la CPU realiza su movimiento.
        _uiState.update { it.copy(currentPlayer = Player.CPU, canPlay = false) }
        // Lanza la lógica del movimiento de la CPU.
        triggerCpuMove()
    }

    /**
     * Inicia el proceso del movimiento de la CPU en una corutina.
     * Incluye un pequeño retraso para simular que la CPU "piensa".
     */
    private fun triggerCpuMove() {
        // Usa viewModelScope para lanzar una corutina que se cancela automáticamente si el ViewModel se destruye.
        viewModelScope.launch {
            // Pausa breve antes de que la CPU juegue (simulación).
            delay(800) // 0.8 segundos
            // Ejecuta la lógica de selección de movimiento de la CPU.
            cpuMove()
            // Vuelve a habilitar la interacción del usuario SI el juego sigue en curso después del movimiento de la CPU.
            if (_uiState.value.status == GameStatus.ONGOING) {
                _uiState.update { it.copy(canPlay = true) }
            }
        }
    }

    /**
     * Ejecuta la lógica para el movimiento de la CPU.
     * Actualmente, implementa una estrategia muy simple: elige una columna válida al azar.
     */
    private fun cpuMove() {
        // No hacer nada si el juego ya ha terminado.
        if (_uiState.value.status != GameStatus.ONGOING) return

        var column: Int
        // Bucle para encontrar una columna aleatoria que NO esté llena.
        do {
            column = Random.nextInt(COLS) // Elige un índice de columna aleatorio (0 a COLS-1).
        } while (findAvailableRow(column) == -1) // Repite si la columna aleatoria está llena.

        // Obtiene la fila disponible en la columna seleccionada.
        val targetRow = findAvailableRow(column)
        // Este cheque no debería fallar debido al bucle anterior, pero es una buena práctica.
        if (targetRow != -1) {
            // Actualiza el tablero con la ficha de la CPU.
            updateBoard(targetRow, column, Player.CPU)

            // Comprueba si la CPU ganó con este movimiento.
            if (checkWin(Player.CPU)) {
                // Actualiza el estado a CPU_WINS y deshabilita el juego.
                _uiState.update { it.copy(status = GameStatus.CPU_WINS, canPlay = false) }
                // La UI reaccionará navegando a DefeatActivity.
                return
            }
            // Comprueba si hubo empate tras el movimiento de la CPU.
            if (checkDraw()) {
                // Actualiza el estado a DRAW y deshabilita el juego.
                _uiState.update { it.copy(status = GameStatus.DRAW, canPlay = false) }
                // La UI reaccionará navegando a DefeatActivity.
                return
            }

            // Si el juego continúa, devuelve el turno al usuario.
            _uiState.update { it.copy(currentPlayer = Player.USER) }
        }
        // Asegura que la interacción del usuario esté habilitada después del turno de la CPU,
        // si el juego aún está en curso. Esto se maneja también en triggerCpuMove.
        if (_uiState.value.status == GameStatus.ONGOING) {
            _uiState.update { it.copy(canPlay = true) }
        }
    }

    /**
     * Encuentra la fila más baja disponible (vacía) en una columna dada.
     * Recorre la columna desde abajo hacia arriba.
     *
     * @param columnIndex El índice de la columna a verificar.
     * @return El índice de la fila disponible (0 a ROWS-1), o -1 si la columna está llena o es inválida.
     */
    private fun findAvailableRow(columnIndex: Int): Int {
        // Comprobación de límites.
        if (columnIndex < 0 || columnIndex >= COLS) return -1
        // Itera desde la fila inferior (ROWS - 1) hacia la superior (0).
        for (row in ROWS - 1 downTo 0) {
            // Si encuentra una celda vacía, esa es la fila objetivo.
            if (_uiState.value.board[row][columnIndex] is CellState.Empty) {
                return row
            }
        }
        // Si el bucle termina sin encontrar celdas vacías, la columna está llena.
        return -1
    }

    /**
     * Actualiza el estado del tablero de forma inmutable.
     * Crea una nueva lista de listas basada en el estado anterior, modifica la celda
     * especificada y actualiza el `_uiState`.
     *
     * @param row Índice de la fila a modificar.
     * @param col Índice de la columna a modificar.
     * @param player Jugador que ocupará la celda.
     */
    private fun updateBoard(row: Int, col: Int, player: Player) {
        // update es una función de extensión segura para MutableStateFlow.
        _uiState.update { currentState ->
            // Crea una copia mutable del tablero actual.
            val newBoard = currentState.board.map { it.toMutableList() }.toMutableList()
            // Modifica la celda específica con el estado Ocupado por el jugador.
            newBoard[row][col] = CellState.Occupied(player)
            // Crea un nuevo GameUiState con el tablero modificado (convertido de nuevo a inmutable).
            currentState.copy(board = newBoard.map { it.toList() })
        }
    }

    /**
     * Comprueba si el jugador especificado ha ganado la partida.
     * Verifica todas las condiciones de victoria: horizontal, vertical y ambas diagonales.
     *
     * @param player El jugador (USER o CPU) para el cual se verifica la victoria.
     * @return `true` si el jugador ha ganado, `false` en caso contrario.
     */
    private fun checkWin(player: Player): Boolean {
        val board = _uiState.value.board
        val targetState = CellState.Occupied(player) // Estado de celda que buscamos.

        // Comprobación Horizontal (4 en línea)
        for (r in 0 until ROWS) { // Por cada fila
            for (c in 0 until COLS - 3) { // Por cada columna inicial posible (dejando espacio para 3 más)
                if (board[r][c] == targetState &&
                    board[r][c + 1] == targetState &&
                    board[r][c + 2] == targetState &&
                    board[r][c + 3] == targetState) {
                    return true // ¡Victoria horizontal encontrada!
                }
            }
        }

        // Comprobación Vertical (4 en línea)
        for (r in 0 until ROWS - 3) { // Por cada fila inicial posible (dejando espacio para 3 más abajo)
            for (c in 0 until COLS) { // Por cada columna
                if (board[r][c] == targetState &&
                    board[r + 1][c] == targetState &&
                    board[r + 2][c] == targetState &&
                    board[r + 3][c] == targetState) {
                    return true // ¡Victoria vertical encontrada!
                }
            }
        }

        // Comprobación Diagonal (Descendente hacia la derecha \)
        for (r in 0 until ROWS - 3) { // Por cada fila inicial posible
            for (c in 0 until COLS - 3) { // Por cada columna inicial posible
                if (board[r][c] == targetState &&
                    board[r + 1][c + 1] == targetState &&
                    board[r + 2][c + 2] == targetState &&
                    board[r + 3][c + 3] == targetState) {
                    return true // ¡Victoria diagonal \ encontrada!
                }
            }
        }

        // Comprobación Diagonal (Ascendente hacia la derecha /)
        for (r in 3 until ROWS) { // Por cada fila inicial posible (empezando desde la fila 3)
            for (c in 0 until COLS - 3) { // Por cada columna inicial posible
                if (board[r][c] == targetState &&
                    board[r - 1][c + 1] == targetState &&
                    board[r - 2][c + 2] == targetState &&
                    board[r - 3][c + 3] == targetState) {
                    return true // ¡Victoria diagonal / encontrada!
                }
            }
        }

        // Si ninguna de las comprobaciones anteriores encontró una victoria.
        return false
    }

    /**
     * Comprueba si la partida ha terminado en empate.
     * Una forma sencilla es verificar si la fila superior está completamente llena.
     * Si lo está, y nadie ha ganado (la comprobación de victoria se hace antes), es un empate.
     *
     * @return `true` si el tablero está lleno (empate), `false` en caso contrario.
     */
    private fun checkDraw(): Boolean {
        // 'all' verifica si todas las celdas en la fila superior (índice 0)
        // NO son del tipo CellState.Empty (es decir, están todas ocupadas).
        // Corrección: 'none' es más directo: si ninguna está vacía.
        return _uiState.value.board[0].none { it is CellState.Empty }
    }


    /**
     * Restablece el juego a su estado inicial.
     * Crea una nueva instancia de `GameUiState` con los valores por defecto
     * y la asigna a `_uiState`, lo que notificará a la UI para que se redibuje.
     */
    fun resetGame() {
        _uiState.value = GameUiState() // Asigna un estado completamente nuevo y limpio.
    }

    /**
     * Función de utilidad (helper) para obtener el color de una celda basado en su estado.
     * Usada por la UI (GameBoard/BoardCell) para saber cómo pintar cada celda.
     *
     * @param cellState El estado de la celda ([CellState]).
     * @return El [Color] correspondiente a ese estado.
     */
    fun getCellColor(cellState: CellState): Color {
        // Usa el estado actual (_uiState.value) para obtener los colores definidos.
        return when (cellState) {
            is CellState.Empty -> _uiState.value.emptyCellColor
            is CellState.Occupied -> when (cellState.player) {
                Player.USER -> _uiState.value.userPieceColor
                Player.CPU -> _uiState.value.cpuPieceColor
            }
        }
    }
}