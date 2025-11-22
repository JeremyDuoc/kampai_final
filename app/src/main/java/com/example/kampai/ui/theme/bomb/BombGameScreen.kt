package com.example.kampai.ui.theme.bomb

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.delay

@Composable
fun BombGameScreen(
    viewModel: BombViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val category by viewModel.category.collectAsState()

    // Animaciones basadas en estado
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState is BombViewModel.GameState.Playing) {
            if (timeLeft <= 5) 1.2f else 1.1f
        } else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (timeLeft <= 5) 250 else 500
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (timeLeft <= 3) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(50),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    // Colores dinámicos según estado
    val backgroundColor = when {
        uiState is BombViewModel.GameState.Exploded -> Color(0xFFFF4444)
        timeLeft <= 5 && uiState is BombViewModel.GameState.Playing -> Color(0xFFFF6B6B).copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.background
    }

    val backgroundColorAnimated by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(300),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorAnimated)
    ) {
        // Efectos de fondo
        BombBackground(uiState, timeLeft)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            BombHeader(onBack = onBack)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState) {
                    is BombViewModel.GameState.Idle -> {
                        IdleState()
                    }
                    is BombViewModel.GameState.Playing -> {
                        PlayingState(
                            category = category,
                            timeLeft = timeLeft,
                            pulseScale = pulseScale,
                            shakeOffset = shakeOffset
                        )
                    }
                    is BombViewModel.GameState.Exploded -> {
                        ExplodedState()
                    }
                }
            }

            // Botón de acción
            ActionButton(
                uiState = uiState,
                onStart = { viewModel.startGame() },
                onReset = { viewModel.resetGame() }
            )
        }
    }
}

@Composable
fun BombBackground(state: BombViewModel.GameState, timeLeft: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (state is BombViewModel.GameState.Playing) {
            // Círculos de alerta
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size((200 + index * 100).dp)
                        .scale(if (timeLeft <= 5) 1.1f else 1f)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Red.copy(alpha = if (timeLeft <= 5) 0.2f else 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun BombHeader(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = "💣 LA BOMBA",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            ),
            color = Color.Red,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun IdleState() {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Text(
            text = "💣",
            fontSize = 140.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

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
                    text = "¿Listo para la presión?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Menciona un elemento de la categoría antes de que explote la bomba. ¡Pasa el móvil rápido!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun PlayingState(
    category: String,
    timeLeft: Int,
    pulseScale: Float,
    shakeOffset: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer { translationX = shakeOffset }
    ) {
        // Badge de categoría
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "MENCIONA UN...",
                color = Color.Gray,
                letterSpacing = 2.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Categoría
        Text(
            text = category,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 32.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Temporizador circular
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .scale(pulseScale)
        ) {
            // Círculos concéntricos
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size((240 - index * 40).dp)
                        .clip(CircleShape)
                        .border(
                            width = (4 - index).dp,
                            color = if (timeLeft <= 5)
                                Color.Red.copy(alpha = 0.8f - index * 0.2f)
                            else
                                Color(0xFFFF6B6B).copy(alpha = 0.6f - index * 0.15f),
                            shape = CircleShape
                        )
                )
            }

            // Número del temporizador
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Red.copy(alpha = if (timeLeft <= 5) 0.4f else 0.25f),
                                Color.Red.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$timeLeft",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 90.sp
                    ),
                    color = Color.White
                )
            }
        }

        if (timeLeft <= 5) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "¡¡RÁPIDO!!",
                color = Color.Red,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .scale(pulseScale)
                    .graphicsLayer { translationX = -shakeOffset }
            )
        }
    }
}

@Composable
fun ExplodedState() {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .graphicsLayer { rotationZ = rotation }
    ) {
        Text(
            text = "💥",
            fontSize = 180.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "¡BOOM!",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 72.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            )
        ) {
            Text(
                text = "¡Te exploto! 🍺 ¡BEBE!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Composable
fun ActionButton(
    uiState: BombViewModel.GameState,
    onStart: () -> Unit,
    onReset: () -> Unit
) {
    val buttonColor = when (uiState) {
        is BombViewModel.GameState.Exploded -> Color.White
        else -> Color.Red
    }

    val buttonText = when (uiState) {
        is BombViewModel.GameState.Idle -> "🔥 Encender Mecha"
        is BombViewModel.GameState.Playing -> "Jugando..."
        is BombViewModel.GameState.Exploded -> "Reintentar"
    }

    Button(
        onClick = {
            when (uiState) {
                is BombViewModel.GameState.Exploded -> onReset()
                is BombViewModel.GameState.Idle -> onStart()
                else -> {}
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = if (uiState is BombViewModel.GameState.Playing) 0.dp else 12.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        enabled = uiState !is BombViewModel.GameState.Playing,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = buttonText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (uiState is BombViewModel.GameState.Exploded) Color.Red else Color.White
        )
    }
}