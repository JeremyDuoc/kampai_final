package com.example.kampai.ui.theme.charades

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
import androidx.compose.ui.res.stringResource // OBLIGATORIO
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.ui.theme.AccentAmber
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import kotlinx.coroutines.delay

@Composable
fun CharadesGameScreen(
    viewModel: CharadesViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val word by viewModel.currentWord.collectAsState()
    val time by viewModel.timeLeft.collectAsState()
    val score by viewModel.score.collectAsState()
    val difficulty by viewModel.currentDifficulty.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CharadesBackground(state = state, time = time)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CharadesHeader(
                onBack = onBack,
                score = score,
                onReset = { viewModel.reset() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is CharadesViewModel.GameState.Idle -> {
                        IdleContent(
                            onStart = { viewModel.startGame() },
                            onDifficultyChange = { viewModel.setDifficulty(it) },
                            selectedDifficulty = difficulty
                        )
                    }
                    is CharadesViewModel.GameState.Playing -> {
                        PlayingContent(
                            word = word,
                            time = time,
                            difficulty = difficulty,
                            onCorrect = { viewModel.gotIt() },
                            onSkip = { viewModel.skip() }
                        )
                    }
                    is CharadesViewModel.GameState.Finished -> {
                        FinishedContent(
                            score = score,
                            onRestart = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharadesBackground(state: CharadesViewModel.GameState, time: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val offset by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset")
    val backgroundColor by animateColorAsState(targetValue = when { state is CharadesViewModel.GameState.Playing && time <= 10 -> Color(0xFFEF4444).copy(alpha = 0.15f); else -> Color.Transparent }, label = "bgColor")

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Box(modifier = Modifier.align(Alignment.TopStart).offset(x = (-100).dp, y = (-100).dp).size(300.dp).rotate(offset).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(AccentAmber.copy(alpha = 0.3f), Color.Transparent))))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp).size(350.dp).rotate(-offset).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(PrimaryViolet.copy(alpha = 0.25f), Color.Transparent))))
    }
}

@Composable
fun CharadesHeader(onBack: () -> Unit, score: Int, onReset: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.charades_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 22.sp), color = AccentAmber)
            if (score > 0) {
                Text(text = stringResource(R.string.charades_score_fmt, score), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFFD700))
            }
        }
        IconButton(onClick = onReset, modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(R.string.party_cancel), tint = AccentAmber)
        }
    }
}

@Composable
fun IdleContent(onStart: () -> Unit, onDifficultyChange: (CharadesViewModel.Difficulty) -> Unit, selectedDifficulty: CharadesViewModel.Difficulty) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")
    LaunchedEffect(Unit) { delay(100); isVisible = true }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale)) {
        val infiniteTransition = rememberInfiniteTransition(label = "emoji")
        val emojiRotation by infiniteTransition.animateFloat(initialValue = -10f, targetValue = 10f, animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse), label = "rotation")
        Text(text = "🎭", fontSize = 120.sp, modifier = Modifier.rotate(emojiRotation))
        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.charades_ready_title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.charades_ready_desc), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Start, lineHeight = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        DifficultySelector(selectedDifficulty = selectedDifficulty, onDifficultyChange = onDifficultyChange)
        Spacer(modifier = Modifier.height(32.dp))
        StartButton(onClick = onStart)
    }
}

@Composable
fun DifficultySelector(selectedDifficulty: CharadesViewModel.Difficulty, onDifficultyChange: (CharadesViewModel.Difficulty) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.charades_difficulty_label), style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CharadesViewModel.Difficulty.values().forEach { difficulty ->
                DifficultyChip(difficulty = difficulty, isSelected = selectedDifficulty == difficulty, onClick = { onDifficultyChange(difficulty) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DifficultyChip(difficulty: CharadesViewModel.Difficulty, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(targetValue = if (isSelected) { when (difficulty) { CharadesViewModel.Difficulty.EASY -> Color(0xFF10B981); CharadesViewModel.Difficulty.MEDIUM -> Color(0xFFF59E0B); CharadesViewModel.Difficulty.HARD -> Color(0xFFEF4444) } } else { Color.White.copy(alpha = 0.1f) }, label = "chipBg")
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "chipScale")

    Box(modifier = modifier.height(70.dp).scale(scale).shadow(elevation = if (isSelected) 12.dp else 4.dp, shape = RoundedCornerShape(16.dp), ambientColor = backgroundColor).clip(RoundedCornerShape(16.dp)).background(backgroundColor).border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = when (difficulty) { CharadesViewModel.Difficulty.EASY -> "😊"; CharadesViewModel.Difficulty.MEDIUM -> "😅"; CharadesViewModel.Difficulty.HARD -> "😰" }, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            // AQUÍ SE USA EL RECURSO DE NOMBRE
            Text(text = stringResource(difficulty.nameRes), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Gray)
            Text(text = "${difficulty.timeSeconds}s", fontSize = 9.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray)
        }
    }
}

@Composable
fun StartButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "buttonScale")

    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth().height(64.dp).scale(scale).shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp), ambientColor = AccentAmber, spotColor = AccentAmber).background(brush = Brush.horizontalGradient(colors = listOf(AccentAmber, Color(0xFFF59E0B))), shape = RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp), interactionSource = interactionSource) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🎬", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.charades_btn_start), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun PlayingContent(word: String, time: Int, difficulty: CharadesViewModel.Difficulty, onCorrect: () -> Unit, onSkip: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularTimer(time = time, difficulty = difficulty)
        Spacer(modifier = Modifier.height(48.dp))
        WordCard(word = word, time = time)
        Spacer(modifier = Modifier.height(48.dp))
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🤐", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.charades_instruction_mute), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        ActionButtons(onCorrect = onCorrect, onSkip = onSkip, time = time)
    }
}

@Composable
fun CircularTimer(time: Int, difficulty: CharadesViewModel.Difficulty) {
    val scale by animateFloatAsState(targetValue = if (time <= 10) 1.05f else 1f, animationSpec = infiniteRepeatable(animation = tween(if (time <= 5) 200 else 500), repeatMode = RepeatMode.Reverse), label = "timerScale")
    val color by animateColorAsState(targetValue = when { time <= 5 -> Color(0xFFEF4444); time <= 15 -> Color(0xFFF59E0B); else -> Color(0xFF10B981) }, label = "timerColor")

    Box(modifier = Modifier.size(200.dp).scale(scale), contentAlignment = Alignment.Center) {
        repeat(3) { index -> Box(modifier = Modifier.size((200 - index * 30).dp).clip(CircleShape).border(width = (3 - index).dp, color = color.copy(alpha = 0.3f - index * 0.1f), shape = CircleShape)) }
        Box(modifier = Modifier.size(140.dp).shadow(elevation = 24.dp, shape = CircleShape, ambientColor = color, spotColor = color).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.2f)))).border(3.dp, color, CircleShape), contentAlignment = Alignment.Center) {
            Text(text = "$time", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black, fontSize = 72.sp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun WordCard(word: String, time: Int) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "cardScale")
    LaunchedEffect(word) { isVisible = false; delay(100); isVisible = true }

    Card(modifier = Modifier.fillMaxWidth().scale(scale).shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp), ambientColor = AccentAmber, spotColor = AccentAmber), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(modifier = Modifier.fillMaxWidth().background(brush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.surface, AccentAmber.copy(alpha = 0.15f)))).border(width = 3.dp, brush = Brush.linearGradient(colors = listOf(AccentAmber.copy(alpha = 0.8f), AccentAmber.copy(alpha = 0.4f))), shape = RoundedCornerShape(32.dp)).padding(40.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎯", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = word, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 36.sp, lineHeight = 44.sp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun ActionButtons(onCorrect: () -> Unit, onSkip: () -> Unit, time: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActionButton(label = stringResource(R.string.charades_btn_skip), emoji = "⏭️", color = Color(0xFF6B7280), onClick = onSkip, modifier = Modifier.weight(1f))
        ActionButton(label = stringResource(R.string.charades_btn_correct), emoji = "✅", color = Color(0xFF10B981), onClick = onCorrect, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ActionButton(label: String, emoji: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "buttonScale")

    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), modifier = modifier.height(70.dp).scale(scale).shadow(12.dp, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), interactionSource = interactionSource) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 28.sp)
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FinishedContent(score: Int, onRestart: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.5f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")
    LaunchedEffect(Unit) { delay(100); isVisible = true }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale)) {
        val infiniteTransition = rememberInfiniteTransition(label = "trophy")
        val trophyScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.2f, animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse), label = "trophyScale")
        Text(text = "🏆", fontSize = 120.sp, modifier = Modifier.scale(trophyScale))
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(R.string.charades_time_up), style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black, fontSize = 36.sp), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.charades_final_score), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "$score", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black, fontSize = 80.sp), color = Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(8.dp))
                val resultText = when {
                    score >= 15 -> stringResource(R.string.charades_result_expert)
                    score >= 10 -> stringResource(R.string.charades_result_good)
                    score >= 5 -> stringResource(R.string.charades_result_ok)
                    else -> stringResource(R.string.charades_result_bad)
                }
                Text(text = resultText, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth().height(64.dp).shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp), ambientColor = AccentAmber).background(brush = Brush.horizontalGradient(colors = listOf(AccentAmber, Color(0xFFF59E0B))), shape = RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.charades_btn_replay), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}