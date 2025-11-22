package com.example.kampai.ui.theme.highlow

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.kampai.ui.theme.SecondaryPink
import kotlinx.coroutines.delay

@Composable
fun HighLowGameScreen(
    viewModel: HighLowViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val currentCard by viewModel.currentCard.collectAsState()
    val nextCard by viewModel.nextCard.collectAsState()
    val message by viewModel.message.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val gameActive by viewModel.gameActive.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val cardRelation by viewModel.cardRelation.collectAsState()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
    ) {
        // Fondo decorativo
        HighLowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            HighLowHeader(
                onBack = onBack,
                streak = streak,
                screenWidth = screenWidth
            )

            // Contenido scrolleable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Instrucción
                InstructionBadge(
                    text = "¿La próxima es Mayor o Menor?",
                    screenWidth = screenWidth
                )

                Spacer(modifier = Modifier.height((screenHeight * 0.03f).coerceIn(20.dp, 32.dp)))

                // Sección de cartas
                CardsSection(
                    currentCard = currentCard,
                    nextCard = nextCard,
                    gameActive = gameActive,
                    cardRelation = cardRelation,
                    lastResult = lastResult,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height((screenHeight * 0.03f).coerceIn(20.dp, 32.dp)))

                // Mensaje de resultado
                if (message.isNotEmpty()) {
                    ResultMessage(
                        message = message,
                        lastResult = lastResult,
                        screenWidth = screenWidth
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botones de acción (fijos en la parte inferior)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A).copy(alpha = 0.9f)
            ) {
                ActionButtons(
                    gameActive = gameActive,
                    onGuessHigher = { viewModel.guessHigher() },
                    onGuessLower = { viewModel.guessLower() },
                    onNextRound = { viewModel.nextRound() },
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )
            }
        }
    }
}

@Composable
fun HighLowBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .rotate(offset1)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.2f),
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
                .rotate(-offset1)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEF4444).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun HighLowHeader(
    onBack: () -> Unit,
    streak: Int,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val headerPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val iconSize = (screenWidth * 0.12f).coerceIn(40.dp, 56.dp)
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 26f).sp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F172A).copy(alpha = 0.8f)
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
                Text(
                    text = "🎴 Mayor o Menor",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Black,
                    color = SecondaryPink
                )
                Text(
                    text = "Racha: $streak 🔥",
                    fontSize = (titleSize.value * 0.55f).sp,
                    color = if (streak > 0) Color(0xFFF59E0B) else Color.Gray
                )
            }

            if (streak > 0) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$streak",
                        fontSize = (titleSize.value * 0.7f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(iconSize))
            }
        }
    }
}

@Composable
fun InstructionBadge(text: String, screenWidth: androidx.compose.ui.unit.Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeScale"
    )

    val fontSize = (screenWidth * 0.04f).value.coerceIn(14f, 18f).sp

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        modifier = Modifier.scale(scale)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CardsSection(
    currentCard: Int,
    nextCard: Int,
    gameActive: Boolean,
    cardRelation: HighLowViewModel.CardRelation?,
    lastResult: HighLowViewModel.Result?,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    val cardWidth = (screenWidth * 0.35f).coerceIn(110.dp, 160.dp)
    val cardHeight = (cardWidth.value * 1.4f).dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Carta Actual
        Text(
            text = "CARTA ACTUAL",
            fontSize = (screenWidth * 0.03f).value.coerceIn(10f, 14f).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        AnimatedPlayingCard(
            card = currentCard,
            isRevealed = true,
            width = cardWidth,
            height = cardHeight,
            screenWidth = screenWidth
        )

        Spacer(modifier = Modifier.height((screenHeight * 0.04f).coerceIn(24.dp, 40.dp)))

        // Flecha animada
        AnimatedArrow(
            direction = when (cardRelation) {
                HighLowViewModel.CardRelation.HIGHER -> "↑"
                HighLowViewModel.CardRelation.LOWER -> "↓"
                else -> "?"
            },
            isVisible = cardRelation != null,
            screenWidth = screenWidth
        )

        Spacer(modifier = Modifier.height((screenHeight * 0.04f).coerceIn(24.dp, 40.dp)))

        // Carta Siguiente
        Text(
            text = if (gameActive) "PRÓXIMA CARTA" else "RESULTADO",
            fontSize = (screenWidth * 0.03f).value.coerceIn(10f, 14f).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        AnimatedPlayingCard(
            card = if (gameActive) 0 else nextCard,
            isRevealed = !gameActive,
            width = cardWidth,
            height = cardHeight,
            resultColor = when (lastResult) {
                HighLowViewModel.Result.CORRECT -> Color(0xFF10B981)
                HighLowViewModel.Result.WRONG -> Color(0xFFEF4444)
                else -> SecondaryPink
            },
            screenWidth = screenWidth
        )
    }
}

@Composable
fun AnimatedPlayingCard(
    card: Int,
    isRevealed: Boolean,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    resultColor: Color = Color.White,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var showCard by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (showCard) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    val cardRotation by animateFloatAsState(
        targetValue = if (showCard && isRevealed) 0f else 180f,
        animationSpec = tween(600),
        label = "cardRotation"
    )

    LaunchedEffect(card) {
        showCard = false
        delay(100)
        showCard = true
    }

    val numberSize = (screenWidth * 0.08f).value.coerceIn(28f, 48f).sp
    val mainNumberSize = (screenWidth * 0.18f).value.coerceIn(60f, 100f).sp

    Card(
        modifier = Modifier
            .width(width)
            .height(height)
            .scale(scale)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .graphicsLayer { rotationY = cardRotation },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRevealed) Color.White else Color(0xFF1E293B)
        )
    ) {
        if (isRevealed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val (cardText, cardColor) = getCardDisplay(card)

                    // Número superior
                    Text(
                        text = cardText,
                        fontSize = numberSize,
                        fontWeight = FontWeight.Bold,
                        color = cardColor
                    )

                    // Número central grande
                    Text(
                        text = cardText,
                        fontSize = mainNumberSize,
                        fontWeight = FontWeight.Black,
                        color = cardColor
                    )

                    // Número inferior (invertido)
                    Text(
                        text = cardText,
                        fontSize = numberSize,
                        fontWeight = FontWeight.Bold,
                        color = cardColor,
                        modifier = Modifier.rotate(180f)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF7C3AED),
                                Color(0xFF6366F1)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "?",
                        fontSize = mainNumberSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedArrow(
    direction: String,
    isVisible: Boolean,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arrow")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (direction == "↑") -10f else if (direction == "↓") 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowOffset"
    )

    val arrowSize = (screenWidth * 0.12f).value.coerceIn(40f, 60f).sp

    if (isVisible) {
        Text(
            text = direction,
            fontSize = arrowSize,
            fontWeight = FontWeight.Black,
            color = when (direction) {
                "↑" -> Color(0xFF10B981)
                "↓" -> Color(0xFFEF4444)
                else -> SecondaryPink
            },
            modifier = Modifier.offset(y = offsetY.dp)
        )
    } else {
        Text(
            text = "?",
            fontSize = arrowSize,
            fontWeight = FontWeight.Black,
            color = Color.Gray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ResultMessage(
    message: String,
    lastResult: HighLowViewModel.Result?,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val color = when (lastResult) {
        HighLowViewModel.Result.CORRECT -> Color(0xFF10B981)
        HighLowViewModel.Result.WRONG -> Color(0xFFEF4444)
        else -> Color.Gray
    }

    val fontSize = (screenWidth * 0.045f).value.coerceIn(16f, 22f).sp

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = message,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
fun ActionButtons(
    gameActive: Boolean,
    onGuessHigher: () -> Unit,
    onGuessLower: () -> Unit,
    onNextRound: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    val buttonHeight = (screenHeight * 0.08f).coerceIn(54.dp, 70.dp)
    val buttonPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val fontSize = (screenWidth * 0.04f).value.coerceIn(15f, 20f).sp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(buttonPadding)
    ) {
        if (gameActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PredictionButton(
                    label = "MENOR",
                    emoji = "👇",
                    color = Color(0xFFEF4444),
                    onClick = onGuessLower,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight,
                    fontSize = fontSize
                )

                PredictionButton(
                    label = "MAYOR",
                    emoji = "👆",
                    color = Color(0xFF10B981),
                    onClick = onGuessHigher,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight,
                    fontSize = fontSize
                )
            }
        } else {
            Button(
                onClick = onNextRound,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryPink
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight)
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Siguiente Ronda →",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PredictionButton(
    label: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        interactionSource = interactionSource
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = fontSize * 1.4f
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = fontSize * 0.8f,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getCardDisplay(value: Int): Pair<String, Color> {
    return when (value) {
        1 -> "A" to Color.Black
        11 -> "J" to Color.Red
        12 -> "Q" to Color.Black
        13 -> "K" to Color.Red
        else -> "$value" to if (value % 2 == 0) Color.Black else Color.Red
    }
}