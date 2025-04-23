package com.example.connect_four

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DefeatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DefeatScreen(
                    onRestart = {
                        val intent = Intent(this, GameActivity::class.java)
                        startActivity(intent)
                    },
                    onHome = {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun DefeatScreen(
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val jaroFont = FontFamily(Font(R.font.jaro))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xfff5f5f5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

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

        Box(
            modifier = Modifier
                .width(326.dp)
                .height(261.dp)
                .background(Color(0xFFE57373), shape = RoundedCornerShape(32.dp))
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "YOU LOSE!",
                    fontSize = 64.sp,
                    fontFamily = jaroFont,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "¡Try again next time !",
                    fontSize = 24.sp,
                    fontFamily = jaroFont,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = onRestart, modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD))
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.width(50.dp))

            IconButton(
                onClick = onHome, modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE))
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Exit",
                    tint = Color(0xFFF44336)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
@Preview(showBackground = true)
@Composable
fun DefeatScreenPreview() {
    DefeatScreen(
        onRestart = {},
        onHome = {}
    )
}