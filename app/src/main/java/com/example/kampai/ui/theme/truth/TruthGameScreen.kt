package com.example.kampai.ui.theme.truth

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.domain.models.PlayerModel
import com.example.kampai.ui.theme.AccentAmber
import com.example.kampai.ui.theme.AccentRed
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import com.example.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun TruthGameScreen(
    viewModel: TruthViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val truthCount by viewModel.truthCount.collectAsState()
    val dareCount by viewModel.dareCount.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val currentPlayer by viewModel.currentPlayer.collectAsState()

    // Pasar jugadores al ViewModel
    LaunchedEffect(players) {
        viewModel.setPlayers(players)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fondo animado
        TruthDareBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            TruthDareHeader(
                onBack = onBack,
                truthCount = truthCount,
                dareCount = dareCount,
                onReset = { viewModel.reset() }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is TruthViewModel.GameState.Selection -> {
                        SelectionContent(
                            player = currentPlayer,
                            onTruthClick = { viewModel.pickTruth() },
                            onDareClick = { viewModel.pickDare() }
                        )
                    }
                    is TruthViewModel.GameState.Result -> {
                        ResultContent(
                            type = state.type,
                            text = state.text,
                            player = state.player,
                            onBack = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TruthDareBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Círculo azul (Verdad)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-120).dp, y = (-120).dp)
                .size(350.dp)
                .rotate(offset1)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2563EB).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Círculo rojo (Reto)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 120.dp)
                .size(400.dp)
                .rotate(offset2)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentRed.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Círculo central
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 80.dp, y = (-100).dp)
                .size(280.dp)
                .rotate(-offset1 * 0.7f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentAmber.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun TruthDareHeader(
    onBack: () -> Unit,
    truthCount: Int,
    dareCount: Int,
    onReset: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎭 Verdad o Reto",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    ),
                    color = AccentAmber
                )
                Text(
                    text = "¿Te atreves?",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp
                    ),
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reiniciar",
                    tint = AccentAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                label = "Verdades",
                count = truthCount,
                color = Color(0xFF2563EB),
                emoji = "🤔"
            )

            StatCard(
                label = "Retos",
                count = dareCount,
                color = AccentRed,
                emoji = "🔥"
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    count: Int,
    color: Color,
    emoji: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        modifier = Modifier.width(140.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SelectionContent(
    player: PlayerModel?, // <--- AÑADIR PARÁMETRO
    onTruthClick: () -> Unit,
    onDareClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")
    LaunchedEffect(Unit) { delay(100); isVisible = true }

    Column(modifier = Modifier.fillMaxWidth().scale(scale), horizontalAlignment = Alignment.CenterHorizontally) {

        // --- INICIO: TARJETA DEL JUGADOR (NUEVO) ---
        if (player != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp) // Espacio extra abajo
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = player.getAvatarColor().copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(player.getAvatarColor().copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = player.gender.getEmoji(), fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Turno de:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(text = player.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
        // --- FIN TARJETA ---

        Text(text = "¿Qué eliges?", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 36.sp), color = Color.White, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp)) // Reduje un poco este espacio porque pusimos la tarjeta

        ChoiceButton(label = "VERDAD", emoji = "🤔", color = Color(0xFF2563EB), onClick = onTruthClick, delay = 0)
        Spacer(modifier = Modifier.height(32.dp))
        VsAnimatedSeparator()
        Spacer(modifier = Modifier.height(32.dp))
        ChoiceButton(label = "RETO", emoji = "🔥", color = AccentRed, onClick = onDareClick, delay = 200)
    }
}

@Composable
fun ChoiceButton(
    label: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit,
    delay: Long
) {
    var isVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isVisible -> 1f
            else -> 0.8f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "offsetY"
    )

    LaunchedEffect(Unit) {
        delay(delay)
        isVisible = true
    }

    // Animación de pulsación del emoji
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .scale(scale)
            .offset(y = offsetY.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = color,
                spotColor = color
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    fontSize = 56.sp,
                    modifier = Modifier.scale(emojiScale)
                )

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 40.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun VsAnimatedSeparator() {
    val infiniteTransition = rememberInfiniteTransition(label = "vs")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2563EB),
                            Color.Transparent
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .rotate(rotation)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentAmber.copy(alpha = 0.8f),
                            AccentAmber.copy(alpha = 0.4f)
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "VS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black
                ),
                color = Color.White,
                modifier = Modifier.rotate(-rotation)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AccentRed
                        )
                    )
                )
        )
    }
}

@Composable
fun ResultContent(
    type: TruthViewModel.Type,
    text: String,
    player: com.example.kampai.domain.models.PlayerModel?,
    onBack: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    val color = if (type == TruthViewModel.Type.TRUTH) Color(0xFF2563EB) else AccentRed
    val emoji = if (type == TruthViewModel.Type.TRUTH) "🤔" else "🔥"
    val title = if (type == TruthViewModel.Type.TRUTH) "VERDAD" else "RETO"

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
        delay(600)
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { rotationY = rotation },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- INICIO BLOQUE JUGADOR ---
        if (player != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = player.getAvatarColor().copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(player.getAvatarColor().copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = player.gender.getEmoji(), fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Nombre
                    Column {
                        Text(
                            text = "Turno de:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
        // --- FIN BLOQUE JUGADOR ---
        // Emoji grande animado
        val infiniteTransition = rememberInfiniteTransition(label = "emoji")
        val emojiScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emojiScale"
        )

        Text(
            text = emoji,
            fontSize = 100.sp,
            modifier = Modifier.scale(emojiScale)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Badge del tipo
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.2f)
            )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                ),
                color = color,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tarjeta con el texto
        if (showContent) {
            ResultCard(text = text, color = color)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botón de vuelta
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = color,
                    spotColor = color
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Volver a Elegir",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ResultCard(text: String, color: Color) {
    var isVisible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = color,
                spotColor = color
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            color.copy(alpha = 0.15f)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    color = color.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(32.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    lineHeight = 34.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}