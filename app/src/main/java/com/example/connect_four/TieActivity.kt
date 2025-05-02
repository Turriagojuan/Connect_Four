@file:OptIn(ExperimentalMaterial3Api::class) // Necesario para algunos componentes de Material 3

package com.example.connect_four

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
import androidx.compose.material.icons.filled.ExitToApp // Icono para Salir/Volver al inicio
import androidx.compose.material.icons.filled.Refresh // Icono para Reiniciar
import androidx.compose.material3.ExperimentalMaterial3Api
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
 * TieActivity: La Activity que se muestra cuando la partida termina en empate.
 * Presenta un mensaje indicando el empate y opciones para reiniciar el juego o
 * volver a la pantalla principal.
 */
class TieActivity : ComponentActivity() {
    /**
     * Método llamado cuando la Activity es creada.
     * Configura la interfaz de usuario (`TieScreen`) usando Jetpack Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplica el tema general de la aplicación.
            MaterialTheme {
                // Llama al Composable que define la UI de la pantalla de empate.
                TieScreen(
                    // Lambda que se ejecuta al pulsar el botón "Reiniciar".
                    onRestart = {
                        // Crea un Intent para volver a GameActivity (iniciar nueva partida).
                        val intent = Intent(this, GameActivity::class.java)
                        // Opcional: Añadir flags si es necesario gestionar el historial de Activities.
                        // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish() // Cierra TieActivity para que no quede en el historial.
                    },
                    // Lambda que se ejecuta al pulsar el botón "Salir/Inicio".
                    onExit = {
                        // Crea un Intent para volver a MainActivity (pantalla de inicio).
                        val intent = Intent(this, MainActivity::class.java)
                        // Flags para limpiar actividades anteriores y tener solo MainActivity al inicio.
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish() // Cierra TieActivity.
                    }
                )
            }
        }
    }
}

/**
 * TieScreen: El Composable que define la interfaz de usuario para la pantalla de empate.
 * Muestra el logo, un mensaje de "IT'S A TIE!" y botones de acción.
 *
 * @param onRestart Lambda a ejecutar cuando se presiona el botón de reiniciar.
 * @param onExit Lambda a ejecutar cuando se presiona el botón de salir/inicio.
 */
@Composable
fun TieScreen(
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    // Carga la fuente personalizada "jaro".
    val customFont = FontFamily(Font(R.font.jaro))

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
        // Espaciador superior (opcional, para ajustar el logo hacia abajo).
        Spacer(modifier = Modifier.height(20.dp))

        // Contenedor para el logo "Con4nect". (Similar a otras pantallas).
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
                fontFamily = customFont
            )
        }

        // Contenedor para el mensaje de empate.
        Box(
            modifier = Modifier
                .width(326.dp)
                .height(261.dp)
                // Fondo amarillo/naranja para indicar empate, con bordes redondeados.
                .background(Color(0xFFFFCA28), shape = RoundedCornerShape(32.dp))
                .padding(32.dp) // Padding interno para el texto.
        ) {
            // Columna para centrar verticalmente el texto dentro del Box amarillo/naranja.
            Column(
                modifier = Modifier.fillMaxHeight(), // Ocupa toda la altura del Box.
                horizontalAlignment = Alignment.CenterHorizontally, // Centra texto horizontalmente.
                verticalArrangement = Arrangement.Center // Centra texto verticalmente.
            ) {
                // Texto principal de empate.
                Text(
                    text = "IT'S A TIE!",
                    fontSize = 64.sp, // Tamaño muy grande.
                    fontFamily = customFont, // Fuente personalizada.
                    color = Color.White, // Texto blanco sobre fondo amarillo/naranja.
                    textAlign = TextAlign.Center, // Texto centrado.
                    modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho.
                )
                Spacer(modifier = Modifier.height(12.dp)) // Espacio entre textos.
                // Texto secundario de empate.
                Text(
                    text = "The board is full!", // Mensaje indicando que el tablero está lleno.
                    fontSize = 24.sp, // Tamaño más pequeño.
                    fontFamily = customFont,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Fila para los botones de acción inferiores.
        Row(
            modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho.
            // Distribuye los botones equitativamente.
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botón Reiniciar (Izquierda).
            IconButton(
                onClick = onRestart, // Llama a la función onRestart al hacer clic.
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape) // Forma circular.
                    .background(Color(0xFFE3F2FD)) // Fondo azul claro.
            ) {
                Icon(
                    Icons.Default.Refresh, // Icono de refrescar.
                    contentDescription = "Restart", // Accesibilidad.
                    tint = Color(0xFF2196F3) // Color del icono (azul).
                )
            }

            // Espacio entre botones (opcional, si SpaceEvenly no es suficiente).
            // Spacer(modifier = Modifier.width(50.dp))

            // Botón Salir/Inicio (Derecha).
            IconButton(
                onClick = onExit, // Llama a la función onExit al hacer clic.
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)) // Fondo rojo claro.
            ) {
                Icon(
                    Icons.Default.ExitToApp, // Icono de salir.
                    contentDescription = "Exit", // Accesibilidad.
                    tint = Color(0xFFF44336) // Color del icono (rojo).
                )
            }
        }

        // Espaciador inferior (opcional, para ajustar los botones hacia arriba).
        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Función de Previsualización para TieScreen.
 * Muestra cómo se ve la pantalla de empate en el panel de diseño.
 */
@Preview(showBackground = true)
@Composable
fun TieScreenPreview() {
    MaterialTheme {
        TieScreen(
            onRestart = {}, // Lambdas vacías para la previsualización.
            onExit = {}
        )
    }
}