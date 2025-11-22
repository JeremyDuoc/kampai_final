package com.example.kampai.ui.theme.judge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.ui.theme.AccentAmber

@Composable
fun JudgeGameScreen(
    viewModel: JudgeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val rule by viewModel.currentRule.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
            }
            Text(
                text = "El Juez",
                style = MaterialTheme.typography.titleLarge,
                color = AccentAmber,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text("NUEVA REGLA:", color = Color.Gray, letterSpacing = 2.sp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rule,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }

        Text(
            text = "Quien rompa la regla, bebe.",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Button(
            onClick = { viewModel.newRule() },
            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Nueva Sentencia", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}