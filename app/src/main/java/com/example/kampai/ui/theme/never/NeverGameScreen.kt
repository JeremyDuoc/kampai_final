package com.example.kampai.ui.theme.never

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.ui.theme.AccentCyan
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun NeverGameScreen(
    viewModel: NeverViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val question by viewModel.currentQuestion.collectAsState()
    val questionNumber by viewModel.questionNumber.collectAsState()
    val isChanging by viewModel.isChanging.collectAsState()

    var offsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fondo animado
        NeverBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            NeverHeader(
                onBack = onBack,
                questionNumber = questionNumber,
                onReset = { viewModel.reset() }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Instrucción
            InstructionCard()

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de pregunta con swipe
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                QuestionCard(
                    question = question,
                    isChanging = isChanging,
                    offsetX = offsetX,
                    isDragging = isDragging,
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        if (abs(offsetX) > 300f) {
                            scope.launch {
                                viewModel.nextQuestion()
                                delay(100)
                                offsetX = 0f
                            }
                        } else {
                            scope.launch {
                                animate(
                                    initialValue = offsetX,
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) { value, _ ->
                                    offsetX = value
                                }
                            }
                        }
                    },
                    onDrag = { delta ->
                        offsetX += delta
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicador de swipe
            SwipeIndicator(offsetX = offsetX)

            Spacer(modifier = Modifier.height(24.dp))

            // Botón siguiente
            NextButton(
                onClick = {
                    scope.launch {
                        viewModel.nextQuestion()
                    }
                },
                isChanging = isChanging
            )
        }
    }
}

@Composable
fun NeverBackground() {
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

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
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
                            AccentCyan.copy(alpha = 0.3f),
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
                .rotate(offset2)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryViolet.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 120.dp, y = (-150).dp)
                .size(250.dp)
                .rotate(-offset1 * 0.5f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SecondaryPink.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun NeverHeader(
    onBack: () -> Unit,
    questionNumber: Int,
    onReset: () -> Unit
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
                text = "🍻 Yo Nunca Nunca",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                ),
                color = AccentCyan
            )
            Text(
                text = "Pregunta #$questionNumber",
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
                tint = AccentCyan
            )
        }
    }
}

@Composable
fun InstructionCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "👉",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Si lo hiciste, bebes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "👈",
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun QuestionCard(
    question: String,
    isChanging: Boolean,
    offsetX: Float,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isChanging) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isChanging) 15f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isChanging) 0f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // Color de borde dinámico basado en el swipe
    val borderColor by animateColorAsState(
        targetValue = when {
            offsetX > 100f -> Color.Green
            offsetX < -100f -> Color.Red
            else -> AccentCyan
        },
        label = "borderColor"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .scale(scale)
                .graphicsLayer {
                    rotationY = rotation
                    this.alpha = alpha
                    translationX = offsetX
                    rotationZ = offsetX * 0.02f
                }
                .shadow(
                    elevation = if (isDragging) 32.dp else 24.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = borderColor,
                    spotColor = borderColor
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onHorizontalDrag = { _, dragAmount ->
                            onDrag(dragAmount)
                        }
                    )
                },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                AccentCyan.copy(alpha = 0.15f),
                                PrimaryViolet.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = borderColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Emoji decorativo animado
                    val infiniteTransition = rememberInfiniteTransition(label = "emoji")
                    val emojiScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "emojiScale"
                    )

                    Text(
                        text = "🤫",
                        fontSize = 80.sp,
                        modifier = Modifier.scale(emojiScale)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = question,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            lineHeight = 36.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeIndicator(offsetX: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador izquierdo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.graphicsLayer {
                alpha = if (offsetX < -50f) 1f else 0.3f
                scaleX = if (offsetX < -50f) 1.2f else 1f
                scaleY = if (offsetX < -50f) 1.2f else 1f
            }
        ) {
            Text(text = "👈", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pasar",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Red
            )
        }

        // Indicador central
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "↔️",
                fontSize = 24.sp
            )
            Text(
                text = "Desliza",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Indicador derecho
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.graphicsLayer {
                alpha = if (offsetX > 50f) 1f else 0.3f
                scaleX = if (offsetX > 50f) 1.2f else 1f
                scaleY = if (offsetX > 50f) 1.2f else 1f
            }
        ) {
            Text(
                text = "Siguiente",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Green
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "👉", fontSize = 32.sp)
        }
    }
}

@Composable
fun NextButton(
    onClick: () -> Unit,
    isChanging: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "button")

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Button(
        onClick = onClick,
        enabled = !isChanging,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AccentCyan,
                spotColor = PrimaryViolet
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AccentCyan,
                        PrimaryViolet,
                        SecondaryPink
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isChanging) "Cargando..." else "Siguiente Pregunta",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (!isChanging) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "🍺", fontSize = 20.sp)
            }
        }
    }
}