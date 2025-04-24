
package com.example.connect_four

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultScreen(
    message: String,
    backgroundColor: Color,
    textColor: Color,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontSize = 40.sp,
            color = textColor,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(onClick = onRestart, modifier = Modifier.padding(8.dp)) {
            Text("Reiniciar")
        }
        Button(onClick = onHome, modifier = Modifier.padding(8.dp)) {
            Text("Volver al inicio")
        }
    }
}
