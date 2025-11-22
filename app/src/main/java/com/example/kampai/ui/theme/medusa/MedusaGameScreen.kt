package com.example.kampai.ui.theme.medusa

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.ui.theme.AccentCyan
import kotlinx.coroutines.delay

@Composable
fun MedusaGameScreen(
    viewModel: MedusaViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val count by viewModel.countdown.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MedusaBackground(state = state)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MedusaHeader(onBack = onBack)

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is MedusaViewModel.GameState.Instructions -> {
                        InstructionsContent()
                    }
                    is MedusaViewModel.GameState.Counting -> {
                        CountingContent(count = count)
                    }
                    is MedusaViewModel.GameState.Action -> {
                        ActionContent()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MedusaActionButton(
                state = state,
                onStart = { viewModel.startRound() },
                onReset = { viewModel.reset() }
            )
        }
    }
}

@Composable
fun InstructionsContent() {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "instrScale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Text("🐍", fontSize = 120.sp)

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CÓMO JUGAR",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                listOf(
                    "1" to "Todos miran hacia ABAJO",
                    "2" to "Escucha la cuenta regresiva",
                    "3" to "Levanta la vista y MIRA a alguien",
                    "4" to "Si cruzas miradas: ¡MEDUSA! AMBOS BEBEN"
                ).forEach { (step, text) ->
                    MedusaInstructionStep(step = step, text = text)
                    if (step != "4") {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tip adicional
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AccentCyan.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Tip: ¡Mira a alguien diferente cada ronda!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MedusaInstructionStep(step: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentCyan),
            contentAlignment = Alignment.Center
        ) {
            Text(
                step,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CountingContent(count: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "medusaCount")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "medusaScale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "medusaRot"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "👇 Mira hacia abajo 👇",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.4f),
                            AccentCyan.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(3.dp, AccentCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 100.sp
                ),
                color = AccentCyan,
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer { rotationZ = rotation }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "LEVANTA LA VISTA...",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = AccentCyan.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ActionContent() {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "actionScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 180f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "actionRot"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { rotationX = rotation }
    ) {
        // Animación del rayo
        val infiniteTransition = rememberInfiniteTransition(label = "lightning")
        val rayScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(400),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rayScale"
        )

        Text(
            "⚡ ¡YA! ⚡",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 72.sp
            ),
            color = AccentCyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(rayScale)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = AccentCyan.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "¿Cruzaste miradas?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Si es así: ¡AMBOS BEBEN! 🍻",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = AccentCyan,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Puntos decorativos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
fun MedusaBackground(state: MedusaViewModel.GameState) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            is MedusaViewModel.GameState.Counting -> AccentCyan.copy(alpha = 0.05f)
            is MedusaViewModel.GameState.Action -> AccentCyan.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun MedusaHeader(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
        }

        Text(
            text = "🐍 La Medusa",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            ),
            color = AccentCyan,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun MedusaActionButton(
    state: MedusaViewModel.GameState,
    onStart: () -> Unit,
    onReset: () -> Unit
) {
    val isEnabled = state !is MedusaViewModel.GameState.Counting

    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = if (state is MedusaViewModel.GameState.Action) onReset else onStart,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentCyan,
            disabledContainerColor = AccentCyan.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = if (state is MedusaViewModel.GameState.Action) "Reintentar" else "Iniciar Ronda",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}