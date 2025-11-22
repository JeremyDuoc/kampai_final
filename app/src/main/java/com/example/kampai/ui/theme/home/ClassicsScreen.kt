package com.example.kampai.ui.theme.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.ui.theme.PrimaryViolet

@Composable
fun ClassicsScreen(
    viewModel: ClassicsViewModel = hiltViewModel(),
    onGameSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val games by viewModel.classicGames.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Reutilizamos el fondo animado del Home
        AnimatedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header simple con botón atrás
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                Text(
                    text = "Juegos Clásicos",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryViolet
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lista de juegos clásicos
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(games) { index, game ->
                    // Reutilizamos la tarjeta animada del Home
                    AnimatedGameCard(
                        game = game,
                        onClick = onGameSelected,
                        index = index
                    )
                }
            }
        }
    }
}