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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen(onPlay = { userName ->
                val intent = Intent(this, GameActivity::class.java).apply {
                    putExtra("playerName", userName)
                }
                startActivity(intent)
            })
        }
    }
}

@Composable
fun HomeScreen(onPlay: (String) -> Unit) {
    val jaroFont = FontFamily(Font(R.font.jaro))
    var userName: String by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(color = Color(0xfff5f5f5)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
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

        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(id = R.drawable.imagen),
            contentDescription = "Imagen del juego",
            modifier = Modifier
                .width(257.dp)
                .height(257.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = userName,
            onValueChange = { userName = it },
            placeholder = {
                Text(
                    "user_name",
                    fontFamily = jaroFont,
                    fontSize = 18.sp,
                    color = Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier
                .width(250.dp)
                .height(60.dp),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = jaroFont,
                textAlign = TextAlign.Center
            ),
            singleLine = true
        )


        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { onPlay(if (userName.isBlank()) "Tú" else userName) },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .height(70.dp)
                .width(180.dp)
        ) {
            Text(
                text = "PLAY",
                fontSize = 30.sp,
                fontFamily = jaroFont
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onPlay = {})
}

