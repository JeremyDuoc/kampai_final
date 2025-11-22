package com.example.kampai.ui.theme.likely

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
import com.example.kampai.ui.theme.AccentAmber
import kotlinx.coroutines.delay

@Composable
fun MostLikelyScreen(
    viewModel: MostLikelyViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val question by viewModel.question.collectAsState()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0B2E),
                        Color(0xFF2D1B4E)
                    )
                )
            )
    ) {
        // Fondo decorativo animado
        MostLikelyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            MostLikelyHeader(
                onBack = onBack,
                onRefresh = { viewModel.nextQuestion() },
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

                // Contenido principal
                QuestionContent(
                    question = question,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón de siguiente (fijo en la parte inferior)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A0B2E).copy(alpha = 0.9f)
            ) {
                ActionButton(
                    onNext = { viewModel.nextQuestion() },
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )
            }
        }
    }
}

@Composable
fun MostLikelyBackground() {
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
                            AccentAmber.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

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
                            Color(0xFFE879F9).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 100.dp, y = (-100).dp)
                .size(280.dp)
                .rotate(-offset1 * 0.7f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentAmber.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun MostLikelyHeader(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val headerPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val iconSize = (screenWidth * 0.12f).coerceIn(40.dp, 56.dp)
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 26f).sp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A0B2E).copy(alpha = 0.8f)
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
                    text = "👉 ¿Quién es más probable?",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Black,
                    color = AccentAmber
                )
                Text(
                    text = "Votación grupal",
                    fontSize = (titleSize.value * 0.5f).sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(iconSize)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Cambiar pregunta",
                    tint = AccentAmber,
                    modifier = Modifier.size(iconSize * 0.5f)
                )
            }
        }
    }
}

@Composable
fun QuestionContent(
    question: String,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 15f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotation"
    )

    LaunchedEffect(question) {
        isVisible = false
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.06f).coerceIn(20.dp, 32.dp)
    val emojiSize = (screenWidth * 0.2f).value.coerceIn(70f, 110f).sp
    val questionSize = (screenWidth * 0.06f).value.coerceIn(22f, 36f).sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { rotationY = rotation }
    ) {
        // Emoji principal animado
        val infiniteTransition = rememberInfiniteTransition(label = "emoji")

        val emojiScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emojiScale"
        )

        val emojiRotation by infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emojiRotation"
        )

        Text(
            text = "👤",
            fontSize = emojiSize,
            modifier = Modifier
                .scale(emojiScale)
                .graphicsLayer { rotationZ = emojiRotation }
        )

        Spacer(modifier = Modifier.height(contentPadding * 1.5f))

        // Badge de instrucción
        InstructionBadge(
            text = "A la cuenta de 3, señalen...",
            screenWidth = screenWidth
        )

        Spacer(modifier = Modifier.height(contentPadding * 1.5f))

        // Tarjeta de pregunta principal
        QuestionCard(
            question = question,
            contentPadding = contentPadding,
            questionSize = questionSize
        )

        Spacer(modifier = Modifier.height(contentPadding * 2f))

        // Reglas del juego
        RulesSection(
            screenWidth = screenWidth,
            contentPadding = contentPadding
        )
    }
}

@Composable
fun InstructionBadge(text: String, screenWidth: androidx.compose.ui.unit.Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge")

    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeScale"
    )

    val fontSize = (screenWidth * 0.035f).value.coerceIn(12f, 16f).sp

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = Modifier.scale(badgeScale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "👉", fontSize = fontSize * 1.5f)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "👈", fontSize = fontSize * 1.5f)
        }
    }
}

@Composable
fun QuestionCard(
    question: String,
    contentPadding: androidx.compose.ui.unit.Dp,
    questionSize: androidx.compose.ui.unit.TextUnit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = AccentAmber,
                spotColor = AccentAmber
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            AccentAmber.copy(alpha = 0.2f)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AccentAmber.copy(alpha = 0.8f),
                            AccentAmber.copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(contentPadding * 1.5f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono decorativo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AccentAmber.copy(alpha = 0.4f),
                                    AccentAmber.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤔", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pregunta
                Text(
                    text = question,
                    fontSize = questionSize,
                    fontWeight = FontWeight.Black,
                    lineHeight = (questionSize.value * 1.3f).sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RulesSection(
    screenWidth: androidx.compose.ui.unit.Dp,
    contentPadding: androidx.compose.ui.unit.Dp
) {
    val ruleSize = (screenWidth * 0.038f).value.coerceIn(13f, 17f).sp

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "📋 Cómo jugar",
                fontSize = (ruleSize.value * 1.2f).sp,
                fontWeight = FontWeight.Black,
                color = AccentAmber,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            RuleItem(
                number = "1",
                text = "Lean la pregunta en voz alta",
                fontSize = ruleSize
            )

            Spacer(modifier = Modifier.height(12.dp))

            RuleItem(
                number = "2",
                text = "Cuenten: 3... 2... 1... ¡YA!",
                fontSize = ruleSize
            )

            Spacer(modifier = Modifier.height(12.dp))

            RuleItem(
                number = "3",
                text = "Todos señalan a alguien al mismo tiempo",
                fontSize = ruleSize
            )

            Spacer(modifier = Modifier.height(12.dp))

            RuleItem(
                number = "4",
                text = "La persona más señalada BEBE 🍺",
                fontSize = ruleSize,
                highlight = true
            )
        }
    }
}

@Composable
fun RuleItem(
    number: String,
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (highlight) Color(0xFFEF4444) else AccentAmber,
                            if (highlight) Color(0xFFDC2626) else AccentAmber.copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = (fontSize.value * 1.1f).sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = fontSize,
            color = if (highlight) Color.White else Color.LightGray,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            lineHeight = (fontSize.value * 1.4f).sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    onNext: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)
    val buttonPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val fontSize = (screenWidth * 0.045f).value.coerceIn(16f, 22f).sp

    // Animación de brillo
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(buttonPadding)
    ) {
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .scale(scale)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = AccentAmber,
                    spotColor = AccentAmber
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AccentAmber,
                            AccentAmber.copy(alpha = shimmerAlpha)
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
                Text(text = "🔄", fontSize = fontSize * 1.3f)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Siguiente Pregunta",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}