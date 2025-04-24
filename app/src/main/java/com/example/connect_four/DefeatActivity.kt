package com.example.connect_four

// import androidx.compose.material.icons.filled.Home // Icono alternativo si se prefiere "Home"
// import androidx.compose.ui.res.fontResource // Import obsoleto si se usa Font()
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * DefeatActivity: La Activity que se muestra cuando el jugador humano pierde
 * (o empata, según la lógica actual).
 * Presenta un mensaje indicando la derrota y opciones para reiniciar el juego o
 * volver a la pantalla principal.
 */
class DefeatActivity : ComponentActivity() {
    /**
     * Método llamado cuando la Activity es creada.
     * Configura la interfaz de usuario (`DefeatScreen`) usando Jetpack Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplica el tema general de la aplicación.
            MaterialTheme { // Asegúrate de usar MaterialTheme si es consistente con el resto.
                // Llama al Composable que define la UI de la pantalla de derrota.
                DefeatScreen(
                    // Lambda que se ejecuta al pulsar el botón "Reiniciar".
                    onRestart = {
                        // Crea un Intent para volver a GameActivity (iniciar nueva partida).
                        val intent = Intent(this, GameActivity::class.java)
                        // Opcional: Añadir flags si es necesario.
                        // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish() // Cierra DefeatActivity.
                    },
                    // Lambda que se ejecuta al pulsar el botón "Home" (Volver al inicio).
                    onHome = {
                        // Crea un Intent para volver a MainActivity (pantalla de inicio).
                        val intent = Intent(this, MainActivity::class.java)
                        // Flags para limpiar actividades anteriores.
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish() // Cierra DefeatActivity.
                    }
                )
            }
        }
    }
}

/**
 * DefeatScreen: El Composable que define la interfaz de usuario para la pantalla de derrota.
 * Muestra el logo, un mensaje de "YOU LOSE!" y botones de acción.
 *
 * @param onRestart Lambda a ejecutar cuando se presiona el botón de reiniciar.
 * @param onHome Lambda a ejecutar cuando se presiona el botón de volver al inicio.
 */
@Composable
fun DefeatScreen(
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    // Carga la fuente personalizada "jaro".
    val jaroFont = FontFamily(Font(R.font.jaro))

    // Columna principal que organiza los elementos verticalmente.
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla.
            .background(color = Color(0xfff5f5f5)) // Fondo gris claro.
            .padding(24.dp), // Padding general.
        horizontalAlignment = Alignment.CenterHorizontally, // Centra horizontalmente.
        // Distribuye el espacio verticalmente entre el logo, el mensaje y los botones.
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Espaciador superior (opcional).
        Spacer(modifier = Modifier.height(20.dp))

        // Contenedor para el logo "Con4nect". (Idéntico a otras pantallas).
        Box(
            modifier = Modifier
                .width(212.dp)
                .height(77.dp)
                .background(color = Color(0xFF1A56B0), shape = RoundedCornerShape(77.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFFF0F8FF))) { append("Con") }
                    withStyle(style = SpanStyle(color = Color(0xFFFFB74D))) { append("4") }
                    withStyle(style = SpanStyle(color = Color(0xFFF0F8FF))) { append("nect") }
                },
                fontSize = 40.sp,
                fontFamily = jaroFont
            )
        }

        // Contenedor para el mensaje de derrota.
        Box(
            modifier = Modifier
                .width(326.dp)
                .height(261.dp)
                // Fondo rojo para indicar derrota, con bordes redondeados.
                .background(Color(0xFFE57373), shape = RoundedCornerShape(32.dp))
                .padding(32.dp) // Padding interno.
        ) {
            // Columna para centrar verticalmente el texto dentro del Box rojo.
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Texto principal de derrota.
                Text(
                    text = "YOU LOSE!",
                    fontSize = 64.sp, // Tamaño muy grande.
                    fontFamily = jaroFont,
                    color = Color.White, // Texto blanco sobre fondo rojo.
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                // Texto secundario de derrota.
                Text(
                    text = "¡Try again next time !", // Mensaje de ánimo.
                    fontSize = 24.sp,
                    fontFamily = jaroFont,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Fila para los botones de acción inferiores.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly // Distribuye los botones.
        ) {
            // Botón Reiniciar (Izquierda).
            IconButton(
                onClick = onRestart, // Llama a la función onRestart.
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)) // Fondo azul claro.
            ) {
                Icon(
                    Icons.Default.Refresh, // Icono de refrescar.
                    contentDescription = "Restart", // Accesibilidad.
                    tint = Color(0xFF2196F3) // Tinte azul.
                )
            }

            // Espacio entre botones (opcional).
            // Spacer(modifier = Modifier.width(50.dp))

            // Botón Volver al Inicio (Derecha).
            IconButton(
                onClick = onHome, // Llama a la función onHome.
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)) // Fondo rojo claro.
            ) {
                Icon(
                    // Icono de Salir/Puerta. Podría usarse Icons.Default.Home si se prefiere.
                    Icons.Default.ExitToApp,
                    contentDescription = "Exit", // El contentDescription podría ser "Home" para mayor claridad.
                    tint = Color(0xFFF44336) // Tinte rojo.
                )
            }
        }

        // Espaciador inferior (opcional).
        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Función de Previsualización para DefeatScreen.
 * Muestra cómo se ve la pantalla de derrota en el panel de diseño.
 */
@Preview(showBackground = true)
@Composable
fun DefeatScreenPreview() {
    MaterialTheme {
        DefeatScreen(
            onRestart = {}, // Lambdas vacías para la previsualización.
            onHome = {}
        )
    }
}