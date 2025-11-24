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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource // Importante
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
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

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeverBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                NeverHeader(
                    onBack = onBack,
                    questionNumber = questionNumber,
                    onReset = { viewModel.reset() },
                    screenWidth = screenWidth
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    InstructionBadge()

                    QuestionCard(
                        question = question,
                        isChanging = isChanging,
                        offsetX = offsetX,
                        isDragging = isDragging,
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            if (abs(offsetX) > 150f) {
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
                                    ) { value, _ -> offsetX = value }
                                }
                            }
                        },
                        onDrag = { delta -> offsetX += delta },
                        screenWidth = screenWidth
                    )

                    SwipeIndicator(offsetX = offsetX)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A).copy(alpha = 0.9f)
            ) {
                NextButton(
                    onClick = {
                        scope.launch { viewModel.nextQuestion() }
                    },
                    isChanging = isChanging,
                    screenWidth = screenWidth
                )
            }
        }
    }
}

@Composable
fun NeverBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val offset1 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(animation = tween(25000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset1")
    val offset2 by infiniteTransition.animateFloat(initialValue = 360f, targetValue = 0f, animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset2")

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.align(Alignment.TopStart).offset(x = (-100).dp, y = (-100).dp).size(300.dp).rotate(offset1).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(AccentCyan.copy(alpha = 0.3f), Color.Transparent))))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp).size(350.dp).rotate(offset2).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(PrimaryViolet.copy(alpha = 0.25f), Color.Transparent))))
        Box(modifier = Modifier.align(Alignment.Center).offset(x = 120.dp, y = (-150).dp).size(250.dp).rotate(-offset1 * 0.5f).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(SecondaryPink.copy(alpha = 0.2f), Color.Transparent))))
    }
}

@Composable
fun NeverHeader(onBack: () -> Unit, questionNumber: Int, onReset: () -> Unit, screenWidth: androidx.compose.ui.unit.Dp) {
    val headerPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val iconSize = (screenWidth * 0.12f).coerceIn(40.dp, 56.dp)
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 26f).sp

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = headerPadding, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(iconSize).background(Color.White.copy(alpha = 0.15f), CircleShape)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(iconSize * 0.5f))
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.never_game_title), fontSize = titleSize, fontWeight = FontWeight.Black, color = AccentCyan)
            // FORMATO: Pregunta #1
            Text(text = stringResource(R.string.never_question_number, questionNumber), fontSize = (titleSize.value * 0.5f).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onReset, modifier = Modifier.size(iconSize).background(Color.White.copy(alpha = 0.15f), CircleShape)) {
            Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.party_cancel), tint = AccentCyan, modifier = Modifier.size(iconSize * 0.5f))
        }
    }
}

@Composable
fun InstructionBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 0.8f, animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse), label = "alpha")

    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(text = "👉", fontSize = 18.sp, modifier = Modifier.graphicsLayer { this.alpha = alpha })
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.never_instruction), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "👈", fontSize = 18.sp, modifier = Modifier.graphicsLayer { this.alpha = alpha })
        }
    }
}

@Composable
fun QuestionCard(question: String, isChanging: Boolean, offsetX: Float, isDragging: Boolean, onDragStart: () -> Unit, onDragEnd: () -> Unit, onDrag: (Float) -> Unit, screenWidth: androidx.compose.ui.unit.Dp) {
    val scale by animateFloatAsState(targetValue = if (isChanging) 0.85f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium), label = "scale")
    val rotation by animateFloatAsState(targetValue = if (isChanging) 15f else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium), label = "rotation")
    val alpha by animateFloatAsState(targetValue = if (isChanging) 0f else 1f, animationSpec = tween(300), label = "alpha")
    val borderColor by animateColorAsState(targetValue = when { abs(offsetX) > 150f -> Color(0xFF10B981); abs(offsetX) > 50f -> AccentCyan.copy(alpha = 0.8f); else -> AccentCyan.copy(alpha = 0.5f) }, label = "borderColor")

    val cardWidth = (screenWidth * 0.9f).coerceIn(280.dp, 400.dp)
    val cardHeight = (screenWidth * 1.3f).coerceIn(360.dp, 520.dp)
    val emojiSize = (screenWidth * 0.18f).value.coerceIn(60f, 100f).sp
    val questionSize = (screenWidth * 0.055f).value.coerceIn(20f, 32f).sp

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.width(cardWidth).height(cardHeight).scale(scale).graphicsLayer { rotationY = rotation; this.alpha = alpha; translationX = offsetX; rotationZ = offsetX * 0.015f }
                .shadow(elevation = if (isDragging) 32.dp else 24.dp, shape = RoundedCornerShape(32.dp), ambientColor = borderColor, spotColor = borderColor)
                .pointerInput(Unit) { detectHorizontalDragGestures(onDragStart = { onDragStart() }, onDragEnd = { onDragEnd() }, onHorizontalDrag = { _, dragAmount -> onDrag(dragAmount) }) },
            shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF1A1A1A), AccentCyan.copy(alpha = 0.15f), PrimaryViolet.copy(alpha = 0.1f))))
                    .border(width = 3.dp, color = borderColor, shape = RoundedCornerShape(32.dp)).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    val infiniteTransition = rememberInfiniteTransition(label = "emoji")
                    val emojiScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.15f, animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse), label = "emojiScale")
                    val emojiRotation by infiniteTransition.animateFloat(initialValue = -5f, targetValue = 5f, animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Reverse), label = "emojiRotation")

                    Text(text = "🤫", fontSize = emojiSize, modifier = Modifier.scale(emojiScale).graphicsLayer { rotationZ = emojiRotation })
                    Spacer(modifier = Modifier.height(24.dp))
                    // AQUÍ SE MUESTRA LA PREGUNTA
                    Text(text = question, fontSize = questionSize, fontWeight = FontWeight.Black, lineHeight = (questionSize.value * 1.3f).sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(32.dp))
                    SwipeHint()
                }
            }
        }
    }
}

@Composable
fun SwipeHint() {
    val infiniteTransition = rememberInfiniteTransition(label = "hint")
    val offsetX by infiniteTransition.animateFloat(initialValue = -8f, targetValue = 8f, animationSpec = infiniteRepeatable(animation = tween(1500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "hintOffset")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.6f, animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse), label = "hintAlpha")

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.graphicsLayer { this.alpha = alpha }) {
        Text(text = "←", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.offset(x = (-offsetX).dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.never_swipe_hint), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "→", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.offset(x = offsetX.dp))
    }
}

@Composable
fun SwipeIndicator(offsetX: Float) {
    val swipeProgress = (abs(offsetX) / 150f).coerceIn(0f, 1f)
    val indicatorAlpha by animateFloatAsState(targetValue = if (swipeProgress > 0.1f) swipeProgress else 0f, animationSpec = tween(200), label = "indicatorAlpha")

    if (indicatorAlpha > 0f) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF10B981).copy(alpha = indicatorAlpha * 0.3f)) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (offsetX > 0) "→" else "←", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = indicatorAlpha))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (swipeProgress >= 1f) stringResource(R.string.never_swipe_release) else stringResource(R.string.never_swipe_dragging),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = indicatorAlpha)
                    )
                }
            }
        }
    }
}

@Composable
fun NextButton(onClick: () -> Unit, isChanging: Boolean, screenWidth: androidx.compose.ui.unit.Dp) {
    val buttonHeight = (screenWidth * 0.15f).coerceIn(56.dp, 72.dp)
    val buttonPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val fontSize = (screenWidth * 0.045f).value.coerceIn(16f, 22f).sp

    Column(modifier = Modifier.fillMaxWidth().padding(buttonPadding)) {
        Button(
            onClick = onClick, enabled = !isChanging,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().height(buttonHeight).shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp), ambientColor = AccentCyan, spotColor = PrimaryViolet).background(brush = Brush.horizontalGradient(colors = listOf(AccentCyan.copy(alpha = if (isChanging) 0.3f else 1f), PrimaryViolet.copy(alpha = if (isChanging) 0.3f else 1f), SecondaryPink.copy(alpha = if (isChanging) 0.3f else 1f))), shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (!isChanging) {
                    Text(text = stringResource(R.string.never_btn_next), fontSize = fontSize, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "🍺", fontSize = fontSize * 1.2f)
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onSurface, strokeWidth = 3.dp)
                }
            }
        }
    }
}