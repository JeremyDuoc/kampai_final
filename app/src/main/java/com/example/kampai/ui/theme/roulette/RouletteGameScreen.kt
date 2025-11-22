package com.example.kampai.ui.theme.roulette

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.ui.theme.AccentRed
import com.example.kampai.ui.theme.PrimaryViolet
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RouletteGameScreen(
    viewModel: RouletteViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val chambers by viewModel.chambers.collectAsState()
    val message by viewModel.message.collectAsState()
    val gameOver by viewModel.gameOver.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val currentRotation by viewModel.currentRotation.collectAsState()
    val tensionLevel by viewModel.tensionLevel.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fondo con efectos de tensión
        RouletteBackground(tensionLevel = tensionLevel, gameOver = gameOver)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            RouletteHeader(
                onBack = onBack,
                onReset = { viewModel.resetGame() },
                chambersRevealed = chambers.count { it != null }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mensaje de estado
            AnimatedMessage(
                message = message,
                gameOver = gameOver,
                tensionLevel = tensionLevel
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Ruleta del revólver (visual principal)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                RevolverCylinder(
                    chambers = chambers,
                    currentRotation = currentRotation,
                    isSpinning = isSpinning,
                    tensionLevel = tensionLevel,
                    onChamberClick = { index ->
                        if (!gameOver && !isSpinning) {
                            viewModel.triggerChamber(index)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Indicador de tensión
            TensionMeter(tensionLevel = tensionLevel)

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de acción
            ActionButton(
                gameOver = gameOver,
                isSpinning = isSpinning,
                onReset = { viewModel.resetGame() }
            )
        }
    }
}

@Composable
fun RouletteBackground(tensionLevel: Float, gameOver: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween((1000 - tensionLevel * 500).toInt().coerceAtLeast(300)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            gameOver -> AccentRed.copy(alpha = 0.2f)
            tensionLevel > 0.6f -> AccentRed.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Efectos de tensión
        if (tensionLevel > 0.4f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(400.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentRed.copy(alpha = pulseAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Círculos decorativos
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryViolet.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun RouletteHeader(
    onBack: () -> Unit,
    onReset: () -> Unit,
    chambersRevealed: Int
) {
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
                text = "🔫 Ruleta Rusa",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = AccentRed
            )
            Text(
                text = "$chambersRevealed/6 cámaras disparadas",
                style = MaterialTheme.typography.labelMedium,
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
                tint = AccentRed
            )
        }
    }
}

@Composable
fun AnimatedMessage(message: String, gameOver: Boolean, tensionLevel: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "message")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (gameOver) 1.1f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (gameOver) 300 else 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "messageScale"
    )

    val color by animateColorAsState(
        targetValue = when {
            gameOver -> AccentRed
            tensionLevel > 0.6f -> Color(0xFFF59E0B)
            else -> Color.White
        },
        label = "messageColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (gameOver) 24.dp else 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (gameOver) AccentRed else Color.White.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gameOver)
                AccentRed.copy(alpha = 0.2f)
            else
                Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (gameOver) {
                Text(
                    text = "💥",
                    fontSize = 48.sp,
                    modifier = Modifier.scale(scale)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (gameOver) 24.sp else 20.sp
                ),
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RevolverCylinder(
    chambers: List<Boolean?>,
    currentRotation: Float,
    isSpinning: Boolean,
    tensionLevel: Float,
    onChamberClick: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cylinder")

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .size(320.dp)
            .rotate(currentRotation),
        contentAlignment = Alignment.Center
    ) {
        // Círculo exterior (cuerpo del tambor)
        Box(
            modifier = Modifier
                .size(320.dp)
                .shadow(
                    elevation = 32.dp,
                    shape = CircleShape,
                    ambientColor = if (tensionLevel > 0.5f) AccentRed else PrimaryViolet,
                    spotColor = if (tensionLevel > 0.5f) AccentRed else PrimaryViolet
                )
                .border(
                    width = 4.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF374151),
                            Color(0xFF6B7280),
                            Color(0xFF374151)
                        )
                    ),
                    shape = CircleShape
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1F2937),
                            Color(0xFF111827)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Efecto de brillo metálico
        Canvas(modifier = Modifier.size(320.dp)) {
            val radius = size.minDimension / 2
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.3f, size.height * 0.3f)
                ),
                radius = radius * 0.6f
            )
        }

        // Las 6 cámaras
        chambers.forEachIndexed { index, state ->
            val angle = (360f / 6f) * index - currentRotation
            val radians = Math.toRadians(angle.toDouble())
            val radius = 110f

            val x = (cos(radians) * radius).toFloat()
            val y = (sin(radians) * radius).toFloat()

            ChamberSlot(
                state = state,
                index = index,
                offsetX = x.dp,
                offsetY = y.dp,
                isSpinning = isSpinning,
                glowPulse = glowPulse,
                onClick = { onChamberClick(index) }
            )
        }

        // Centro del tambor (eje)
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(16.dp, CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4B5563),
                            Color(0xFF1F2937)
                        )
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color(0xFF6B7280), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎯",
                fontSize = 32.sp
            )
        }
    }
}

@Composable
fun ChamberSlot(
    state: Boolean?,
    index: Int,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
    isSpinning: Boolean,
    glowPulse: Float,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            state == true -> 1.1f
            isPressed && !isSpinning -> 0.9f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chamberScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            true -> AccentRed
            false -> Color(0xFF10B981)
            null -> Color(0xFF374151)
        },
        label = "chamberBg"
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(64.dp)
            .scale(scale)
            .shadow(
                elevation = if (state == true) 20.dp else 12.dp,
                shape = CircleShape,
                ambientColor = backgroundColor,
                spotColor = backgroundColor
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 3.dp,
                color = when (state) {
                    true -> Color.White.copy(alpha = 0.8f)
                    false -> Color.White.copy(alpha = 0.4f)
                    null -> Color(0xFF6B7280)
                },
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = state == null && !isSpinning
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Efecto de brillo para bala
        if (state == true) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(glowPulse)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentRed.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Text(
            text = when (state) {
                true -> "💥"
                false -> "✓"
                null -> "${index + 1}"
            },
            fontSize = if (state != null) 32.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            color = when (state) {
                null -> Color.White.copy(alpha = 0.7f)
                else -> Color.White
            }
        )
    }
}

@Composable
fun TensionMeter(tensionLevel: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TENSIÓN",
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            val color by animateColorAsState(
                targetValue = when {
                    tensionLevel > 0.7f -> AccentRed
                    tensionLevel > 0.4f -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                },
                label = "meterColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(tensionLevel)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.6f),
                                color
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                tensionLevel > 0.7f -> "🔥 MUY PELIGROSO"
                tensionLevel > 0.4f -> "⚠️ CUIDADO"
                tensionLevel > 0.2f -> "😰 TENSO"
                else -> "😌 TRANQUILO"
            },
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = when {
                tensionLevel > 0.7f -> AccentRed
                tensionLevel > 0.4f -> Color(0xFFF59E0B)
                else -> Color.Gray
            }
        )
    }
}

@Composable
fun ActionButton(
    gameOver: Boolean,
    isSpinning: Boolean,
    onReset: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    if (gameOver) {
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .scale(scale)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = AccentRed,
                    spotColor = AccentRed
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AccentRed,
                            Color(0xFFDC2626)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            interactionSource = interactionSource
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Girar Tambor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSpinning) "🔄 Girando..." else "👆 Toca una cámara",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}