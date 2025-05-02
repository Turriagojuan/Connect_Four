package com.example.connect_four

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * GameActivity: La Activity responsable de mostrar la pantalla del juego Conecta 4.
 * Obtiene datos del [GameViewModel], muestra la interfaz usando [GameScreen] (Jetpack Compose),
 * y maneja la navegación hacia las pantallas de resultado (Victoria, Derrota o Empate).
 */
class GameActivity : ComponentActivity() {

    // Inyecta (o crea) una instancia de GameViewModel asociada al ciclo de vida de esta Activity.
    // 'by viewModels()' es un delegado de propiedad de androidx.activity.viewModels KTX.
    private val gameViewModel: GameViewModel by viewModels()

    /**
     * Método llamado cuando la Activity es creada.
     * Configura la interfaz, observa el estado del ViewModel y maneja la navegación de resultados.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtiene el nombre del jugador pasado desde MainActivity. Usa "Tú" si no se proporcionó.
        val playerName = intent.getStringExtra("playerName") ?: "Tú"

        // Establece el contenido de la Activity usando Jetpack Compose.
        setContent {
            // Obtiene el estado actual (GameUiState) desde el ViewModel y se suscribe a sus cambios.
            // 'collectAsState()' convierte el StateFlow del ViewModel en un State de Compose,
            // lo que provoca recomposiciones automáticas cuando el estado cambia.
            val uiState by gameViewModel.uiState.collectAsState()
            // Obtiene el contexto actual, necesario para crear Intents.
            val context = LocalContext.current

            /**
             * Efecto lanzado (LaunchedEffect) que se ejecuta cuando el valor de 'uiState.status' cambia.
             * Se usa para reaccionar a la finalización del juego (victoria, derrota, empate)
             * y navegar a la pantalla correspondiente.
             * El bloque dentro de LaunchedEffect se ejecuta en una corutina.
             */
            LaunchedEffect(uiState.status) {
                when (uiState.status) {
                    // Si el usuario gana...
                    GameStatus.USER_WINS -> {
                        // Crea un Intent para ir a VictoryActivity.
                        val intent = Intent(context, VictoryActivity::class.java)
                        // Opcional: Podrías pasar datos extras si VictoryActivity los necesitara.
                        // intent.putExtra("winnerName", playerName)
                        startActivity(intent) // Inicia VictoryActivity.
                        // Llama a resetGame() DESPUÉS de iniciar la nueva actividad,
                        // para que si el usuario vuelve atrás, el juego esté reiniciado.
                        gameViewModel.resetGame()
                    }
                    // Si la CPU gana...
                    GameStatus.CPU_WINS -> {
                        // Crea un Intent para ir a DefeatActivity.
                        val intent = Intent(context, DefeatActivity::class.java)
                        startActivity(intent) // Inicia DefeatActivity.
                        // Reinicia el juego en el ViewModel.
                        gameViewModel.resetGame()
                    }
                    // Si hay un empate...
                    GameStatus.DRAW -> {
                        // Crea un Intent para ir a TieActivity.
                        val intent = Intent(context, TieActivity::class.java)
                        startActivity(intent) // Inicia TieActivity.
                        // Reinicia el juego en el ViewModel.
                        gameViewModel.resetGame()
                    }
                    // Si el juego está en curso, no hacer nada en este efecto.
                    GameStatus.ONGOING -> { /* No navigation needed */ }
                }
            } // Fin de LaunchedEffect

            // Aplica el tema de Material Design definido para la app.
            MaterialTheme {
                // Llama al Composable principal que define la UI de esta pantalla.
                GameScreen(
                    uiState = uiState, // Pasa el estado actual del juego.
                    playerName = playerName, // Pasa el nombre del jugador.
                    // Pasa lambdas que llaman a los métodos correspondientes del ViewModel
                    // cuando ocurren eventos en la UI (clics).
                    onColumnClick = { columnIndex -> gameViewModel.dropPiece(columnIndex) },
                    onRestartClick = { gameViewModel.resetGame() },
                    // Lambda para manejar el clic en el botón "Atrás".
                    onBackClick = {
                        // Crea un Intent para volver a MainActivity.
                        val intent = Intent(context, MainActivity::class.java)
                        // Flags para limpiar las activities anteriores y evitar duplicados.
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent) // Vuelve a MainActivity.
                        finish() // Cierra esta GameActivity para que no quede en el historial.
                    }
                ) // Fin de GameScreen
            } // Fin de MaterialTheme
        } // Fin de setContent
    } // Fin de onCreate

    /*
    // Opcional: Si quisieras forzar el reinicio del juego cada vez que
    // el usuario vuelve a esta pantalla (por ejemplo, desde Victory/Defeat/Tie).
    override fun onResume() {
        super.onResume()
        gameViewModel.resetGame()
    }
    */
}

/**
 * GameScreen: El Composable principal que define la estructura visual de la pantalla de juego.
 * Organiza el logo, los indicadores de jugador, el tablero y los botones de acción.
 * Es 'stateless' en el sentido de que recibe todo el estado (`uiState`) y las funciones
 * de callback (`onColumnClick`, etc.) desde fuera.
 *
 * @param uiState El estado actual del juego ([GameUiState]) proporcionado por el ViewModel.
 * @param playerName El nombre del jugador humano.
 * @param onColumnClick Lambda que se ejecuta cuando el usuario toca una columna del tablero. Recibe el índice de la columna.
 * @param onRestartClick Lambda que se ejecuta cuando el usuario toca el botón de reiniciar.
 * @param onBackClick Lambda que se ejecuta cuando el usuario toca el botón de volver.
 */
@Composable
fun GameScreen(
    uiState: GameUiState,
    playerName: String,
    onColumnClick: (Int) -> Unit,
    onRestartClick: () -> Unit,
    onBackClick: () -> Unit
) {
    // Carga la fuente personalizada.
    val jaroFont = FontFamily(Font(R.font.jaro)) // Asegúrate que R.font.jaro existe

    // Columna principal que organiza los elementos verticalmente.
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla.
            .background(color = Color(0xfff5f5f5)) // Fondo gris claro general.
            .padding(16.dp), // Padding alrededor de la columna.
        horizontalAlignment = Alignment.CenterHorizontally, // Centra los elementos horizontalmente.
        verticalArrangement = Arrangement.SpaceBetween // Coloca espacio entre elementos, empujando los botones hacia abajo.
    ) {
        // 1. Logo del Juego
        GameLogo(fontFamily = jaroFont)

        Spacer(modifier = Modifier.height(20.dp)) // Espacio vertical.

        // 2. Indicadores de Jugador (Nombre, color de ficha, turno actual)
        PlayerIndicators(
            playerName = playerName,
            currentPlayer = uiState.currentPlayer,
            userColor = uiState.userPieceColor,
            cpuColor = uiState.cpuPieceColor
        )

        Spacer(modifier = Modifier.height(20.dp)) // Espacio vertical.

        // 3. Tablero de Juego Interactivo
        GameBoard(
            board = uiState.board, // Pasa el estado actual del tablero.
            boardColor = uiState.boardColor, // Color del tablero.
            userPieceColor = uiState.userPieceColor, // Color ficha usuario.
            cpuPieceColor = uiState.cpuPieceColor, // Color ficha CPU.
            emptyCellColor = uiState.emptyCellColor, // Color celda vacía.
            onColumnClick = onColumnClick, // Pasa la función a llamar al hacer clic en columna.
            enabled = uiState.canPlay // Habilita/deshabilita clics según si el usuario puede jugar.
        )

        // Spacer para empujar los botones hacia abajo si hay espacio extra.
        // Se puede ajustar o quitar dependiendo del tamaño de pantalla.
        Spacer(modifier = Modifier.weight(1f))

        // 4. Botones de Acción Inferiores (Volver, Reiniciar)
        ActionButtons(
            onBackClick = onBackClick,
            onRestartClick = onRestartClick
        )
    }
}

/**
 * Composable para mostrar el logo "Con4nect".
 * @param fontFamily La fuente personalizada a aplicar.
 */
@Composable
fun GameLogo(fontFamily: FontFamily) {
    // Box con fondo azul y forma redondeada.
    Box(
        modifier = Modifier
            .width(212.dp) // Ancho del logo.
            .height(60.dp) // Alto del logo.
            .background(color = Color(0xFF1A56B0), shape = RoundedCornerShape(50.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Texto del logo con formato específico.
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFFF0F8FF))) { append("Con") }
                withStyle(style = SpanStyle(color = Color(0xFFFFB74D))) { append("4") }
                withStyle(style = SpanStyle(color = Color(0xFFF0F8FF))) { append("nect") }
            },
            fontSize = 36.sp,
            fontFamily = fontFamily
        )
    }
}

/**
 * Composable que muestra los indicadores para ambos jugadores (Usuario y CPU).
 * Incluye el nombre, el color de la ficha y resalta quién tiene el turno.
 *
 * @param playerName Nombre del jugador humano.
 * @param currentPlayer Jugador que tiene el turno actual.
 * @param userColor Color de la ficha del usuario.
 * @param cpuColor Color de la ficha de la CPU.
 */
@Composable
fun PlayerIndicators(
    playerName: String,
    currentPlayer: Player,
    userColor: Color,
    cpuColor: Color
) {
    // Fila para colocar los indicadores uno al lado del otro.
    Row(
        modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho.
        horizontalArrangement = Arrangement.SpaceEvenly, // Distribuye espacio equitativamente.
        verticalAlignment = Alignment.CenterVertically // Centra verticalmente.
    ) {
        // Indicador para el jugador humano.
        PlayerChip(name = playerName, color = userColor, isTurn = currentPlayer == Player.USER)
        // Indicador para la CPU.
        PlayerChip(name = "CPU", color = cpuColor, isTurn = currentPlayer == Player.CPU)
    }
}

/**
 * Composable individual para mostrar la información de un jugador (nombre y ficha).
 * Resalta visualmente si es el turno de este jugador.
 *
 * @param name Nombre del jugador a mostrar.
 * @param color Color de la ficha del jugador.
 * @param isTurn Boolean que indica si es el turno de este jugador.
 */
@Composable
fun PlayerChip(name: String, color: Color, isTurn: Boolean) {
    // Fila para el círculo de color y el nombre.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            // Aplica un fondo gris claro semitransparente si es el turno del jugador.
            .background(
                color = if (isTurn) Color.LightGray.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp) // Bordes redondeados para el fondo.
            )
            .padding(horizontal = 12.dp, vertical = 6.dp) // Padding interno.
    ) {
        // Círculo que representa el color de la ficha.
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, CircleShape) // Fondo con el color del jugador y forma circular.
                .border(1.dp, Color.Gray, CircleShape) // Borde gris sutil.
        )
        Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el círculo y el texto.
        // Nombre del jugador.
        Text(
            text = name,
            fontSize = 16.sp,
            // Pone el texto en negrita si es el turno de este jugador.
            fontWeight = if (isTurn) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Composable que dibuja el tablero de Conecta 4 (6x7).
 * Muestra las fichas según el estado del `board` y maneja los clics en las columnas.
 *
 * @param board El estado actual del tablero (Lista de listas de CellState).
 * @param boardColor Color de fondo del tablero.
 * @param userPieceColor Color de la ficha del usuario.
 * @param cpuPieceColor Color de la ficha de la CPU.
 * @param emptyCellColor Color para celdas vacías.
 * @param onColumnClick Lambda a ejecutar cuando se toca una columna.
 * @param enabled Boolean para habilitar o deshabilitar la interacción (clics).
 */
@Composable
fun GameBoard(
    board: List<List<CellState>>,
    boardColor: Color,
    userPieceColor: Color,
    cpuPieceColor: Color,
    emptyCellColor: Color,
    onColumnClick: (Int) -> Unit,
    enabled: Boolean
) {
    // Contenedor principal para el tablero con fondo azul y bordes redondeados.
    Box(
        modifier = Modifier
            .fillMaxWidth() // Ocupa el ancho disponible.
            // Mantiene una proporción aproximada de 7 columnas / 6 filas.
            .aspectRatio(7f / 6f)
            .background(boardColor, RoundedCornerShape(16.dp))
            .padding(8.dp) // Padding interno para separar las celdas del borde.
            // Recorta el contenido para que respete los bordes redondeados.
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Organiza las filas verticalmente.
        Column {
            // Itera por cada fila (0 a ROWS-1).
            repeat(ROWS) { rowIndex ->
                // Fila para las celdas de una fila del tablero.
                Row(
                    // 'weight(1f)' distribuye el espacio vertical equitativamente entre las filas.
                    modifier = Modifier.weight(1f),
                    // Distribuye el espacio horizontal entre las celdas.
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Itera por cada columna (0 a COLS-1).
                    repeat(COLS) { colIndex ->
                        // Obtiene el estado de la celda actual.
                        val cellState = board[rowIndex][colIndex]
                        // Dibuja la celda individual.
                        BoardCell(
                            cellState = cellState,
                            userPieceColor = userPieceColor,
                            cpuPieceColor = cpuPieceColor,
                            emptyCellColor = emptyCellColor,
                            modifier = Modifier
                                // 'weight(1f)' distribuye el espacio horizontal equitativamente.
                                .weight(1f)
                                // 'aspectRatio(1f)' hace que la celda sea cuadrada.
                                .aspectRatio(1f)
                                .padding(4.dp) // Pequeño espacio alrededor de cada celda.
                                // Hace la celda clicable solo si 'enabled' es true.
                                .clickable(enabled = enabled) {
                                    // Al hacer clic, llama a la función pasando el índice de la COLUMNA.
                                    onColumnClick(colIndex)
                                }
                        )
                    } // Fin repeat columnas
                } // Fin Row
            } // Fin repeat filas
        } // Fin Column
    } // Fin Box
}

/**
 * Composable que dibuja una única celda (círculo) del tablero.
 *
 * @param cellState El estado de la celda a dibujar ([CellState]).
 * @param userPieceColor Color para la ficha del usuario.
 * @param cpuPieceColor Color para la ficha de la CPU.
 * @param emptyCellColor Color para celdas vacías.
 * @param modifier Modificador para aplicar a la celda.
 */
@Composable
fun BoardCell(
    cellState: CellState,
    userPieceColor: Color,
    cpuPieceColor: Color,
    emptyCellColor: Color,
    modifier: Modifier = Modifier
) {
    // Determina el color a usar basado en el estado de la celda.
    val color = when (cellState) {
        is CellState.Empty -> emptyCellColor
        is CellState.Occupied -> if (cellState.player == Player.USER) userPieceColor else cpuPieceColor
    }

    // Dibuja un Box con forma de círculo y el color determinado.
    Box(
        modifier = modifier
            .background(color, CircleShape)
            .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape) // Borde gris sutil.
    )
    // No contiene nada dentro, es solo el círculo coloreado.
}

/**
 * Composable que muestra los botones de acción en la parte inferior de la pantalla.
 *
 * @param onBackClick Lambda a ejecutar al pulsar el botón de volver.
 * @param onRestartClick Lambda a ejecutar al pulsar el botón de reiniciar.
 */
@Composable
fun ActionButtons(
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    // Fila para colocar los botones horizontalmente.
    Row(
        modifier = Modifier
            .fillMaxWidth() // Ocupa todo el ancho.
            .padding(vertical = 16.dp), // Padding vertical.
        horizontalArrangement = Arrangement.SpaceEvenly, // Distribuye espacio.
        verticalAlignment = Alignment.CenterVertically // Centra verticalmente.
    ) {
        // Botón Izquierdo (Volver)
        IconButton( // Botón que solo contiene un icono.
            onClick = onBackClick, // Acción al hacer clic.
            modifier = Modifier
                .size(64.dp) // Tamaño del botón.
                .clip(CircleShape) // Forma circular.
                .background(Color(0xFFE3F2FD)) // Fondo azul claro.
        ) {
            Icon(
                Icons.Default.ArrowBack, // Icono de flecha atrás de Material Icons.
                contentDescription = "Volver al inicio", // Accesibilidad.
                tint = Color(0xFF2196F3) // Color del icono (azul).
            )
        }

        // Botón Derecho (Reiniciar)
        IconButton(
            onClick = onRestartClick, // Acción al hacer clic.
            modifier = Modifier
                .size(64.dp) // Tamaño.
                .clip(CircleShape) // Forma circular.
                .background(Color(0xFFFFEBEE)) // Fondo rojo claro.
        ) {
            Icon(
                Icons.Default.Refresh, // Icono de refrescar de Material Icons.
                contentDescription = "Reiniciar Juego", // Accesibilidad.
                tint = Color(0xFFF44336) // Color del icono (rojo).
            )
        }
    }
}

// --- Preview ---
/**
 * Función de Previsualización para GameScreen.
 * Muestra cómo se ve la pantalla del juego con un estado de ejemplo.
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun GameScreenPreview() {
    // Crea un estado de ejemplo para el tablero para la previsualización.
    val previewState = GameUiState(
        board = listOf(
            List(COLS) { CellState.Empty },
            List(COLS) { CellState.Empty },
            List(COLS) { CellState.Empty },
            listOf(CellState.Empty, CellState.Empty, CellState.Occupied(Player.USER), CellState.Occupied(Player.CPU), CellState.Empty, CellState.Empty, CellState.Empty),
            listOf(CellState.Empty, CellState.Occupied(Player.CPU), CellState.Occupied(Player.USER), CellState.Occupied(Player.USER), CellState.Empty, CellState.Empty, CellState.Empty),
            listOf(CellState.Occupied(Player.USER), CellState.Occupied(Player.CPU), CellState.Occupied(Player.CPU), CellState.Occupied(Player.USER), CellState.Occupied(Player.CPU), CellState.Empty, CellState.Empty)
        ),
        currentPlayer = Player.USER,
        status = GameStatus.ONGOING
    )
    // Envuelve en MaterialTheme para aplicar estilos básicos en la preview.
    MaterialTheme {
        GameScreen(
            uiState = previewState,
            playerName = "Usuario",
            onColumnClick = {}, // Lambdas vacías para la preview.
            onRestartClick = {},
            onBackClick = {}
        )
    }
}