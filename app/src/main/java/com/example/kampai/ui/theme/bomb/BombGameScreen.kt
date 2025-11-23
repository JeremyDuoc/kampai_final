package com.example.kampai.ui.theme.bomb

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Colores dinámicos basados en el tiempo restante
    val backgroundColor by animateColorAsState(
        targetValue = when {
            uiState is BombViewModel.GameState.Exploded -> Color(0xFFFF0000).copy(alpha = 0.3f)
            timeLeft <= 3 -> Color(0xFFFF4444).copy(alpha = 0.25f)
            timeLeft <= 5 -> Color(0xFFFF6B6B).copy(alpha = 0.15f)
            timeLeft <= 10 -> Color(0xFFFFA500).copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F0F),
                        Color(0xFF1A0000)
                    )
                )
            )
            .background(backgroundColor)
    ) {
        // Efectos de fondo dinámicos
        BombDynamicBackground(uiState, timeLeft)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header responsivo
            BombResponsiveHeader(
                onBack = onBack,
                onReset = { viewModel.resetGame() },
                screenWidth = screenWidth
            )

            // Contenido principal
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp))
            ) {
                when (uiState) {
                    is BombViewModel.GameState.Idle -> {
                        IdleContentImproved(
                            onStart = { viewModel.startGame() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is BombViewModel.GameState.Playing -> {
                        PlayingContentImproved(
                            category = category,
                            timeLeft = timeLeft,
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is BombViewModel.GameState.Exploded -> {
                        ExplodedContentImproved(
                            onReset = { viewModel.resetGame() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BombDynamicBackground(state: BombViewModel.GameState, timeLeft: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    // Círculos pulsantes de alerta
    if (state is BombViewModel.GameState.Playing) {
        val pulseSpeed = when {
            timeLeft <= 3 -> 200
            timeLeft <= 5 -> 400
            timeLeft <= 10 -> 800
            else -> 1500
        }

        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseSpeed),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        // Múltiples círculos de alerta
        repeat(4) { index ->
            val alpha = when {
                timeLeft <= 3 -> 0.3f - index * 0.05f
                timeLeft <= 5 -> 0.2f - index * 0.04f
                timeLeft <= 10 -> 0.15f - index * 0.03f
                else -> 0.1f - index * 0.02f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size((200 + index * 80).dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Red.copy(alpha = alpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }

    // Explosión de fondo
    if (state is BombViewModel.GameState.Exploded) {
        val explosionScale by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "explosion"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .scale(explosionScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF4444).copy(alpha = 0.4f),
                                Color(0xFFFF0000).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun BombResponsiveHeader(
    onBack: () -> Unit,
    onReset: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val headerPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val iconSize = (screenWidth * 0.12f).coerceIn(44.dp, 56.dp)
    val titleSize = (screenWidth * 0.06f).value.coerceIn(20f, 28f).sp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = headerPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(iconSize)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize * 0.5f)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💣", fontSize = titleSize * 1.2f)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LA BOMBA",
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF4444)
                    )
                }
                Text(
                    text = "Pasa el móvil rápido",
                    fontSize = (titleSize.value * 0.45f).sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(iconSize)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Reiniciar",
                    tint = Color(0xFFFF4444),
                    modifier = Modifier.size(iconSize * 0.5f)
                )
            }
        }
    }
}

@Composable
fun IdleContentImproved(
    onStart: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val emojiSize = (screenWidth * 0.35f).value.coerceIn(120f, 180f).sp
    val titleSize = (screenWidth * 0.07f).value.coerceIn(24f, 36f).sp
    val bodySize = (screenWidth * 0.04f).value.coerceIn(14f, 18f).sp
    val buttonHeight = (screenHeight * 0.09f).coerceIn(64.dp, 80.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bomba animada con rotación
            val infiniteTransition = rememberInfiniteTransition(label = "bomb")
            val bombRotation by infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation"
            )

            val bombScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bombScale"
            )

            Box(
                modifier = Modifier
                    .size((screenWidth * 0.5f).coerceIn(180.dp, 240.dp))
                    .scale(bombScale)
                    .rotate(bombRotation)
                    .shadow(
                        elevation = 32.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFFFF4444),
                        spotColor = Color(0xFFFF4444)
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF3A0000),
                                Color(0xFF1F0000)
                            )
                        )
                    )
                    .border(3.dp, Color(0xFFFF4444).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💣", fontSize = emojiSize)
            }

            Spacer(modifier = Modifier.height(contentPadding * 2f))

            // Tarjeta de información mejorada
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2A0000),
                                    Color(0xFF1A0000)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF4444).copy(alpha = 0.6f),
                                    Color(0xFFFF0000).copy(alpha = 0.3f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(contentPadding * 1.5f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ ¿Listo para la presión? ⚠️",
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF4444),
                        textAlign = TextAlign.Center,
                        lineHeight = (titleSize.value * 1.2f).sp
                    )

                    Spacer(modifier = Modifier.height(contentPadding))

                    Divider(
                        color = Color(0xFFFF4444).copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = contentPadding)
                    )

                    Spacer(modifier = Modifier.height(contentPadding))

                    // Reglas con iconos
                    RuleItemImproved("🎯", "Menciona un elemento de la categoría", bodySize)
                    Spacer(modifier = Modifier.height(12.dp))
                    RuleItemImproved("⏱️", "Tiempo aleatorio: 30-60 segundos", bodySize)
                    Spacer(modifier = Modifier.height(12.dp))
                    RuleItemImproved("📱", "¡Pasa el móvil rápido!", bodySize)
                    Spacer(modifier = Modifier.height(12.dp))
                    RuleItemImproved("💥", "Si explota: ¡PENITENCIA!", bodySize)
                }
            }

            Spacer(modifier = Modifier.height(contentPadding * 2f))

            // Botón de inicio mejorado
            ResponsiveBombButton(
                text = "🔥 ENCENDER MECHA",
                onClick = onStart,
                height = buttonHeight,
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
fun RuleItemImproved(icon: String, text: String, textSize: androidx.compose.ui.unit.TextUnit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF4444).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = textSize * 1.3f)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = textSize,
            color = Color.White.copy(alpha = 0.9f),
            lineHeight = (textSize.value * 1.4f).sp
        )
    }
}

@Composable
fun PlayingContentImproved(
    category: String,
    timeLeft: Int,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val timerSize = (screenWidth * 0.5f).coerceIn(200.dp, 280.dp)
    val categorySize = (screenWidth * 0.065f).value.coerceIn(22f, 36f).sp

    // Animaciones basadas en urgencia
    val infiniteTransition = rememberInfiniteTransition(label = "playing")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (timeLeft <= 5) 1.15f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (timeLeft <= 3) 150 else if (timeLeft <= 5) 300 else 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (timeLeft <= 3) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(50),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeOffset }
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Badge de categoría mejorado
        CategoryBadgeImproved(category = category, timeLeft = timeLeft, screenWidth = screenWidth)

        // Timer circular ultra-mejorado
        CircularTimerImproved(
            timeLeft = timeLeft,
            pulseScale = pulseScale,
            timerSize = timerSize,
            screenWidth = screenWidth
        )

        // Mensaje de urgencia
        UrgencyMessageImproved(timeLeft = timeLeft, screenWidth = screenWidth)
    }
}

@Composable
fun CategoryBadgeImproved(category: String, timeLeft: Int, screenWidth: androidx.compose.ui.unit.Dp) {
    val categorySize = (screenWidth * 0.065f).value.coerceIn(22f, 36f).sp
    val labelSize = (screenWidth * 0.032f).value.coerceIn(11f, 14f).sp

    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "badgeScale"
    )

    LaunchedEffect(category) {
        isVisible = false
        delay(50)
        isVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2A0000),
                            Color(0xFF1A0000),
                            Color(0xFF2A0000)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFFF4444).copy(alpha = if (timeLeft <= 5) 0.8f else 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFF4444).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "MENCIONA UN...",
                    fontSize = labelSize,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFF4444),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category,
                fontSize = categorySize,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = (categorySize.value * 1.2f).sp
            )
        }
    }
}

@Composable
fun CircularTimerImproved(
    timeLeft: Int,
    pulseScale: Float,
    timerSize: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val timerColor by animateColorAsState(
        targetValue = when {
            timeLeft <= 3 -> Color(0xFFFF0000)
            timeLeft <= 5 -> Color(0xFFFF4444)
            timeLeft <= 10 -> Color(0xFFFFA500)
            else -> Color(0xFFFF6B6B)
        },
        label = "timerColor"
    )

    val numberSize = (screenWidth * 0.25f).value.coerceIn(80f, 140f).sp

    Box(
        modifier = Modifier
            .size(timerSize)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // Múltiples anillos concéntricos
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .size(timerSize - (index * 30).dp)
                    .clip(CircleShape)
                    .border(
                        width = (4 - index).dp,
                        color = timerColor.copy(alpha = 0.4f - index * 0.08f),
                        shape = CircleShape
                    )
            )
        }

        // Círculo interior con gradiente
        Box(
            modifier = Modifier
                .size(timerSize * 0.65f)
                .shadow(
                    elevation = 32.dp,
                    shape = CircleShape,
                    ambientColor = timerColor,
                    spotColor = timerColor
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            timerColor.copy(alpha = 0.5f),
                            timerColor.copy(alpha = 0.2f),
                            Color(0xFF1A0000)
                        )
                    )
                )
                .border(4.dp, timerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$timeLeft",
                fontSize = numberSize,
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = timerColor,
                        offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                        blurRadius = 20f
                    )
                )
            )
        }

        // Partículas flotantes si el tiempo es crítico
        if (timeLeft <= 5) {
            repeat(8) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "particle$index")
                val particleOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 20f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800 + index * 100),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offset$index"
                )

                val angle = (360f / 8f) * index
                val radian = Math.toRadians(angle.toDouble())
                val baseRadius = (timerSize.value / 2) + 20

                Box(
                    modifier = Modifier
                        .offset(
                            x = (Math.cos(radian) * (baseRadius + particleOffset)).dp,
                            y = (Math.sin(radian) * (baseRadius + particleOffset)).dp
                        )
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(timerColor)
                )
            }
        }
    }
}

@Composable
fun UrgencyMessageImproved(timeLeft: Int, screenWidth: androidx.compose.ui.unit.Dp) {
    val messageSize = (screenWidth * 0.055f).value.coerceIn(18f, 28f).sp

    AnimatedVisibility(
        visible = timeLeft <= 10,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "message")
        val messageScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(if (timeLeft <= 3) 200 else 400),
                repeatMode = RepeatMode.Reverse
            ),
            label = "msgScale"
        )

        Card(
            modifier = Modifier
                .scale(messageScale)
                .shadow(20.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    timeLeft <= 3 -> Color(0xFFFF0000).copy(alpha = 0.3f)
                    timeLeft <= 5 -> Color(0xFFFF4444).copy(alpha = 0.25f)
                    else -> Color(0xFFFFA500).copy(alpha = 0.2f)
                }
            )
        ) {
            Text(
                text = when {
                    timeLeft <= 3 -> "¡¡RÁPIDO!!"
                    timeLeft <= 5 -> "¡APÚRATE!"
                    else -> "Date prisa..."
                },
                fontSize = messageSize,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun ExplodedContentImproved(
    onReset: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val explosionSize = (screenWidth * 0.4f).value.coerceIn(140f, 220f).sp
    val titleSize = (screenWidth * 0.1f).value.coerceIn(40f, 72f).sp
    val buttonHeight = (screenHeight * 0.09f).coerceIn(64.dp, 80.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .scale(scale)
                .graphicsLayer { rotationZ = rotation },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Explosión animada
            val infiniteTransition = rememberInfiniteTransition(label = "explosion")
            val explosionScale by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "explosionScale"
            )

            Box(
                modifier = Modifier
                    .size((screenWidth * 0.6f).coerceIn(220.dp, 300.dp))
                    .scale(explosionScale),
                contentAlignment = Alignment.Center
            ) {
                // Efecto de onda de choque
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(((screenWidth * 0.6f).coerceIn(220.dp, 300.dp)) + (index * 40).dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF0000).copy(alpha = 0.3f - index * 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Text(
                    text = "💥",
                    fontSize = explosionSize,
                    modifier = Modifier.scale(explosionScale)
                )
            }

            Spacer(modifier = Modifier.height(contentPadding * 2f))

            // Título "BOOM!" con efecto
            Text(
                text = "¡BOOM!",
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF0000),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFFFF0000),
                        offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                        blurRadius = 30f
                    )
                )
            )

            Spacer(modifier = Modifier.height(contentPadding * 1.5f))

            // Tarjeta de resultado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF3A0000),
                                    Color(0xFF1A0000)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF0000),
                                    Color(0xFFFF4444)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(contentPadding * 1.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡TE EXPLOTÓ!",
                            fontSize = (screenWidth * 0.055f).value.coerceIn(18f, 28f).sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🍺", fontSize = 40.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "¡BEBE!",
                                fontSize = (screenWidth * 0.07f).value.coerceIn(24f, 36f).sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF4444)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "🍺", fontSize = 40.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Fallaste bajo presión",
                                fontSize = (screenWidth * 0.035f).value.coerceIn(12f, 16f).sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(contentPadding * 2f))

            // Botón de reinicio
            ResponsiveBombButton(
                text = "🔄 Reintentar",
                onClick = onReset,
                height = buttonHeight,
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
fun ResponsiveBombButton(
    text: String,
    onClick: () -> Unit,
    height: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    val fontSize = (screenWidth * 0.05f).value.coerceIn(18f, 24f).sp

    // Animación de brillo
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFFFF0000),
                spotColor = Color(0xFFFF4444)
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF0000),
                        Color(0xFFFF4444).copy(alpha = shimmerAlpha),
                        Color(0xFFFF0000)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}