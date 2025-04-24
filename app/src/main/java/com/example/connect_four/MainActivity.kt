package com.example.connect_four

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
 * MainActivity: La Activity principal y punto de entrada de la aplicación.
 * Se lanza cuando el usuario abre la app desde el launcher.
 * Su propósito principal es mostrar la pantalla de inicio (`HomeScreen`)
 * y manejar la navegación inicial hacia la pantalla del juego (`GameActivity`).
 */
class MainActivity : ComponentActivity() {
    /**
     * Método llamado cuando la Activity es creada por primera vez.
     * Configura la interfaz de usuario usando Jetpack Compose.
     * @param savedInstanceState Estado previamente guardado de la Activity (si existe).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el contenido de la Activity usando Jetpack Compose
        setContent {
            // Llama al Composable principal de la pantalla de inicio.
            // Proporciona una función lambda (onPlay) que se ejecutará cuando el usuario pulse "PLAY".
            HomeScreen(onPlay = { userName ->
                // Crea un Intent para iniciar GameActivity
                val intent = Intent(this, GameActivity::class.java).apply {
                    // Añade el nombre del usuario como "extra" al Intent.
                    // GameActivity podrá leer este valor.
                    putExtra("playerName", userName)
                }
                // Inicia GameActivity
                startActivity(intent)
            })
        }
    }
}

/**
 * HomeScreen: El Composable que define la interfaz de usuario de la pantalla de inicio.
 * Muestra el logo, una imagen, un campo para el nombre de usuario y un botón para empezar a jugar.
 *
 * @param onPlay Una función lambda que se invoca cuando el usuario presiona el botón "PLAY".
 * Recibe el nombre de usuario (String) como argumento.
 */
@Composable
fun HomeScreen(onPlay: (String) -> Unit) {
    // Carga la fuente personalizada "jaro" desde los recursos (res/font/jaro.ttf)
    val jaroFont = FontFamily(Font(R.font.jaro))
    // Declara una variable de estado para almacenar el nombre de usuario ingresado.
    // 'remember' asegura que el estado persista entre recomposiciones.
    // 'mutableStateOf' crea un estado observable que, al cambiar, provoca una recomposición.
    var userName: String by remember { mutableStateOf("") }

    // Columna principal que organiza los elementos verticalmente y ocupa toda la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa todo el espacio disponible
            .padding(16.dp) // Añade padding alrededor
            .background(color = Color(0xfff5f5f5)), // Color de fondo gris claro
        verticalArrangement = Arrangement.Center, // Centra los elementos verticalmente
        horizontalAlignment = Alignment.CenterHorizontally // Centra los elementos horizontalmente
    ) {
        // Contenedor para el logo "Con4nect"
        Box(
            modifier = Modifier
                .width(212.dp)
                .height(77.dp)
                // Fondo azul con esquinas redondeadas para dar forma de píldora/logo
                .background(color = Color(0xFF1A56B0), shape = RoundedCornerShape(77.dp)),
            contentAlignment = Alignment.Center // Centra el texto dentro del Box
        ) {
            // Texto del logo con colores específicos para "Con", "4", "nect"
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFFF0F8FF))) { append("Con") } // Blanco/Azul claro
                    withStyle(style = SpanStyle(color = Color(0xFFFFB74D))) { append("4") } // Amarillo/Naranja
                    withStyle(style = SpanStyle(color = Color(0xFFF0F8FF))) { append("nect") } // Blanco/Azul claro
                },
                fontSize = 40.sp, // Tamaño de fuente grande
                fontFamily = jaroFont // Aplica la fuente personalizada
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // Espacio vertical

        // Muestra una imagen decorativa del juego
        Image(
            painter = painterResource(id = R.drawable.imagen), // Carga la imagen desde res/drawable/imagen.png
            contentDescription = "Imagen del juego", // Texto para accesibilidad
            modifier = Modifier
                .width(257.dp)
                .height(257.dp)
        )

        Spacer(modifier = Modifier.height(24.dp)) // Espacio vertical

        // Campo de texto para que el usuario ingrese su nombre
        TextField(
            value = userName, // El valor actual del campo es la variable de estado 'userName'
            onValueChange = { userName = it }, // Actualiza 'userName' cada vez que el texto cambia
            placeholder = { // Texto que se muestra cuando el campo está vacío
                Text(
                    "user_name", // Texto del placeholder
                    fontFamily = jaroFont, // Fuente personalizada para el placeholder
                    fontSize = 18.sp,
                    color = Color.Gray.copy(alpha = 0.4f), // Color gris semitransparente
                    modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho
                    textAlign = TextAlign.Center // Centra el texto del placeholder
                )
            },
            modifier = Modifier
                .width(250.dp) // Ancho fijo
                .height(60.dp), // Altura fija
            // Estilo del texto ingresado por el usuario
            textStyle = LocalTextStyle.current.copy(
                fontFamily = jaroFont, // Fuente personalizada
                textAlign = TextAlign.Center // Texto centrado
            ),
            singleLine = true // Limita la entrada a una sola línea
        )

        Spacer(modifier = Modifier.height(40.dp)) // Espacio vertical más grande

        // Botón para iniciar el juego
        Button(
            // Al hacer clic, llama a la función 'onPlay' pasada como parámetro.
            // Si 'userName' está vacío, pasa "Tú" como nombre por defecto.
            onClick = { onPlay(if (userName.isBlank()) "Tú" else userName) },
            shape = RoundedCornerShape(50), // Botón redondeado
            modifier = Modifier
                .height(70.dp) // Altura del botón
                .width(180.dp) // Ancho del botón
        ) {
            // Texto dentro del botón
            Text(
                text = "PLAY",
                fontSize = 30.sp, // Tamaño de fuente
                fontFamily = jaroFont // Fuente personalizada
            )
        }
    }
}

/**
 * Función de Previsualización para HomeScreen.
 * Permite ver cómo se ve el Composable en el panel de diseño de Android Studio
 * sin necesidad de ejecutar la app en un dispositivo o emulador.
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Llama a HomeScreen con una función 'onPlay' vacía para la previsualización.
    MaterialTheme { // Envuelve en MaterialTheme para aplicar estilos básicos
        HomeScreen(onPlay = {})
    }
}