package com.example.kampai.ui.theme.hot

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.domain.models.HotChallenge
import com.example.kampai.domain.models.HotIntensity
import com.example.kampai.domain.models.PlayerModel
import kotlin.math.abs
import kotlin.random.Random

private val ColorSoft = Color(0xFF4CAF50)
private val ColorMedium = Color(0xFFFF9800)
private val ColorHot = Color(0xFFF44336)
private val ColorExtreme = Color(0xFF9C27B0)
private val Gold = Color(0xFFFFD700)
private val DiabloColor = Color(0xFFD50000)
private val DarkBackground = Color(0xFF121212)

@Composable
fun HotGameScreen(
    viewModel: HotGameViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentCard by viewModel.currentCard.collectAsState()
    val text by viewModel.currentText.collectAsState()
    val p1 by viewModel.playerA.collectAsState()
    val p2 by viewModel.playerB.collectAsState()
    val intensity by viewModel.intensity.collectAsState()
    val progress by viewModel.thermometerProgress.collectAsState()
    val isDiablo by viewModel.isDiabloMode.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()
    val isRevealed by viewModel.isRevealed.collectAsState()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val targetColor = if (isDiablo) DiabloColor else getIntensityColor(intensity)
    val themeColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(1000), label = "themeColor")

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        HotBackground(themeColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HotHeaderWithDiablo(onBack, onNavigateToCreate, isDiablo, { viewModel.toggleDiabloMode() })

            Spacer(modifier = Modifier.height(16.dp))

            IntensityThermometer(progress, intensity, themeColor, isDiablo)

            Spacer(modifier = Modifier.height(16.dp))

            if (p1 != null) {
                PlayersInteractionDisplay(p1!!, p2)
            } else {
                Spacer(modifier = Modifier.height(50.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = screenHeight * 0.4f)
            ) {
                AnimatedContent(
                    targetState = gameState,
                    label = "gameMode",
                    transitionSpec = { fadeIn(tween(500)) + scaleIn() togetherWith fadeOut(tween(300)) }
                ) { mode ->
                    when (mode) {
                        HotGameViewModel.HotState.Card -> {
                            if (currentCard != null || text.isNotEmpty()) {
                                HotCardDisplay(
                                    card = currentCard,
                                    textOverride = text,
                                    isRevealed = isRevealed,
                                    onReveal = { viewModel.revealCard() },
                                    themeColor = themeColor
                                )
                            }
                        }
                        HotGameViewModel.HotState.Slots -> SlotsGameDisplay(viewModel)
                        HotGameViewModel.HotState.Haptic -> HapticGameDisplay(viewModel)
                        HotGameViewModel.HotState.Scratch -> ScratchGameDisplay(onFinish = { viewModel.revealCard() })
                        HotGameViewModel.HotState.Heartbeat -> HeartbeatGameDisplay()
                        HotGameViewModel.HotState.LieDetector -> LieDetectorDisplay(viewModel)
                        HotGameViewModel.HotState.GyroCup -> GyroCupDisplay()
                        HotGameViewModel.HotState.Whisper -> AudioGameDisplay(isBlow = false)
                        HotGameViewModel.HotState.Blow -> AudioGameDisplay(isBlow = true)
                        HotGameViewModel.HotState.RouletteWheel -> RouletteWheelDisplay()
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            SafetyTrafficLight(
                onGreen = { viewModel.safetyGreen() },
                onYellow = { viewModel.safetyYellow() },
                onRed = { viewModel.safetyRed() }
            )
        }

        if (showTutorial) {
            TutorialDialog(onDismiss = { viewModel.closeTutorial() })
        }
    }
}

@Composable
fun HotHeaderWithDiablo(onBack: () -> Unit, onCreate: () -> Unit, isDiablo: Boolean, onToggleDiablo: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) {
            Icon(Icons.Filled.ArrowBack, null, tint = Color.White)
        }
        Button(
            onClick = onToggleDiablo,
            colors = ButtonDefaults.buttonColors(containerColor = if (isDiablo) DiabloColor.copy(0.3f) else Color.Transparent),
            border = BorderStroke(1.dp, if (isDiablo) DiabloColor else Color.Gray),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = stringResource(if (isDiablo) R.string.hot_mode_diablo_on else R.string.hot_mode_diablo_off),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDiablo) Color.White else Color.Gray
            )
        }
        IconButton(onClick = onCreate, modifier = Modifier.background(Gold.copy(0.2f), CircleShape)) {
            Icon(Icons.Filled.Add, null, tint = Gold)
        }
    }
}

@Composable
fun IntensityThermometer(progress: Float, intensity: HotIntensity, color: Color, isDiablo: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.hot_label_temp), color = Color.Gray, fontSize = 12.sp)
            Text(
                text = if (isDiablo) stringResource(R.string.hot_mode_diablo) else stringResource(
                    when (intensity) {
                        HotIntensity.SOFT -> R.string.hot_mode_soft
                        HotIntensity.MEDIUM -> R.string.hot_mode_medium
                        HotIntensity.HOT -> R.string.hot_mode_hot
                        HotIntensity.EXTREME -> R.string.hot_mode_extreme
                    }
                ),
                color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.1f))) {
            Box(modifier = Modifier.fillMaxWidth(if (isDiablo) 1f else progress).fillMaxHeight().background(brush = Brush.horizontalGradient(colors = if (isDiablo) listOf(DiabloColor, Color.Black) else listOf(ColorSoft, ColorMedium, ColorHot))))
        }
    }
}

@Composable
fun PlayersInteractionDisplay(p1: PlayerModel, p2: PlayerModel?) {
    Row(
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.08f), RoundedCornerShape(50)).border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(p1.getDisplayEmoji(), fontSize = 24.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(p1.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (p2 != null) {
            Text(" 👉 ", fontSize = 20.sp, color = Color.Gray)
            Text(p2.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(p2.getDisplayEmoji(), fontSize = 24.sp)
        }
    }
}

@Composable
fun HotCardDisplay(card: HotChallenge?, textOverride: String, isRevealed: Boolean, onReveal: () -> Unit, themeColor: Color) {
    val isCustom = card?.isCustom == true
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.8f).clickable(enabled = !isRevealed) { onReveal() },
        shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), elevation = CardDefaults.cardElevation(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().then(if (isCustom) Modifier.border(4.dp, Brush.sweepGradient(listOf(Gold.copy(0.2f), Gold, Gold.copy(0.2f))), RoundedCornerShape(32.dp)) else Modifier.border(2.dp, themeColor.copy(0.5f), RoundedCornerShape(32.dp)))) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors = listOf(Color(0xFF2D2D2D), Color(0xFF121212)))))
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (isCustom) Text(stringResource(R.string.hot_card_custom), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 16.dp))
                if (isRevealed) {
                    Text(text = textOverride, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp), color = Color.White, textAlign = TextAlign.Center)
                } else {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = themeColor, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.hot_card_reveal), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(Modifier.fillMaxWidth().height(16.dp).background(Color.DarkGray, RoundedCornerShape(8.dp)).blur(12.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth(0.7f).height(16.dp).background(Color.DarkGray, RoundedCornerShape(8.dp)).blur(12.dp))
                }
            }
        }
    }
}

@Composable
fun SlotsGameDisplay(viewModel: HotGameViewModel) {
    val action by viewModel.slotAction.collectAsState()
    val part by viewModel.slotBodyPart.collectAsState()
    val isSpinning by viewModel.isSpinningSlots.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.hot_mg_slots_title), color = Gold, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SlotReel(text = action, label = stringResource(R.string.hot_mg_slots_label_action))
            SlotReel(text = part, label = stringResource(R.string.hot_mg_slots_label_zone))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { viewModel.spinSlots() }, enabled = !isSpinning, colors = ButtonDefaults.buttonColors(containerColor = Gold), modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)) {
            Text(stringResource(R.string.hot_mg_slots_btn), color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SlotReel(text: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Card(modifier = Modifier.size(130.dp, 100.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)), border = BorderStroke(1.dp, Gold.copy(0.5f))) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun LieDetectorDisplay(viewModel: HotGameViewModel) {
    val result by viewModel.lieResult.collectAsState()
    var isPressing by remember { mutableStateOf(false) }
    val fingerColor by animateColorAsState(if (result == true) Color.Green else if (result == false) Color.Red else if (isPressing) Color.Yellow else Color.Gray)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.hot_mg_lie_title), color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(stringResource(R.string.hot_mg_lie_desc), color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(fingerColor.copy(0.2f)).border(3.dp, fingerColor, CircleShape)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val down = event.changes.any { it.pressed }
                            if (down && !isPressing) { isPressing = true; viewModel.startLieAnalysis() } else if (!down) { isPressing = false }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, tint = fingerColor, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = when (result) {
                true -> stringResource(R.string.hot_mg_lie_true)
                false -> stringResource(R.string.hot_mg_lie_false)
                else -> if (isPressing) stringResource(R.string.hot_mg_lie_analyzing) else stringResource(R.string.hot_mg_lie_place_finger)
            },
            fontSize = 24.sp, fontWeight = FontWeight.Black, color = fingerColor
        )
    }
}

@Composable
fun HapticGameDisplay(viewModel: HotGameViewModel) {
    val status by viewModel.hapticStatus.collectAsState()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.hot_mg_haptic_title), color = Color(0xFF03A9F4), fontWeight = FontWeight.Black, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.hot_mg_haptic_desc), color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.size(220.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFF03A9F4).copy(0.2f), Color.Transparent))).border(3.dp, Color(0xFF03A9F4).copy(0.6f), CircleShape).clickable { viewModel.triggerVibration() }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFF03A9F4), modifier = Modifier.size(48.dp))
                Text(stringResource(R.string.hot_mg_haptic_btn), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(status, color = Color(0xFF03A9F4), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GyroCupDisplay() {
    val context = LocalContext.current
    var tiltX by remember { mutableStateOf(0f) }
    var spilled by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) { event?.let { if (!spilled) { tiltX = it.values[0]; if (abs(tiltX) > 7) spilled = true } } }
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.hot_mg_gyro_title), color = Color(0xFF03A9F4), fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.hot_mg_gyro_desc), color = Color.Gray)
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.size(150.dp, 200.dp).clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)).background(Color.White.copy(0.1f)).border(2.dp, Color.White, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))) {
            if (!spilled) Box(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = tiltX * -5f }.background(Brush.verticalGradient(listOf(Color(0xFF03A9F4).copy(0.5f), Color(0xFF03A9F4)))).align(Alignment.BottomCenter).fillMaxHeight(0.7f))
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (spilled) Text(stringResource(R.string.hot_mg_gyro_fail), color = Color.Red, fontWeight = FontWeight.Black, fontSize = 24.sp)
    }
}

@Composable
fun ScratchGameDisplay(onFinish: () -> Unit) {
    var scratchedCount by remember { mutableStateOf(0) }
    LaunchedEffect(scratchedCount) { if (scratchedCount > 15) onFinish() }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.hot_mg_scratch_title), color = Gold, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) { Text("💋", fontSize = 80.sp) }
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(5) { Row(modifier = Modifier.weight(1f).fillMaxWidth()) { repeat(5) { var isScratched by remember { mutableStateOf(false) }; Box(modifier = Modifier.weight(1f).fillMaxHeight().background(if (isScratched) Color.Transparent else Gold).pointerInput(Unit) { detectDragGestures { _, _ -> if (!isScratched) { isScratched = true; scratchedCount++ } } }) } } }
            }
        }
    }
}

@Composable
fun AudioGameDisplay(isBlow: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val randomLevel by infiniteTransition.animateFloat(initialValue = 0.2f, targetValue = 0.8f, animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "lvl")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(if (isBlow) R.string.hot_mg_blow_title else R.string.hot_mg_whisper_title), color = Color(0xFFE91E63), fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(100.dp)) {
            repeat(10) { val height = (randomLevel * Random.nextFloat()).coerceIn(0.1f, 1f); Box(modifier = Modifier.width(10.dp).fillMaxHeight(height).background(Color(0xFFE91E63), RoundedCornerShape(50))) }
        }
    }
}

@Composable
fun RouletteWheelDisplay() {
    val infiniteTransition = rememberInfiniteTransition(label = "wheel")
    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "rot")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.hot_mg_roulette_title), color = DiabloColor, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(30.dp))
        Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
                val colors = listOf(Color.Red, Color.Black, Color.Red, Color.Black)
                colors.forEachIndexed { i, color -> drawArc(color, startAngle = i * 90f, sweepAngle = 90f, useCenter = true) }
            }
            Icon(Icons.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.align(Alignment.CenterEnd).size(40.dp))
        }
    }
}

@Composable
fun HeartbeatGameDisplay() {
    val infiniteTransition = rememberInfiniteTransition(label = "heart")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.3f, animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.hot_mg_heart_title), color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(40.dp))
        Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFE91E63), modifier = Modifier.size(100.dp).scale(scale))
    }
}

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), shape = RoundedCornerShape(28.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔥", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.hot_tutorial_title), color = Gold, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                TutorialStep(stringResource(R.string.hot_tutorial_step_1))
                TutorialStep(stringResource(R.string.hot_tutorial_step_2))
                TutorialStep(stringResource(R.string.hot_tutorial_step_3))
                TutorialStep(stringResource(R.string.hot_tutorial_step_4), isWarning = true)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Gold), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(stringResource(R.string.hot_btn_understood), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun TutorialStep(text: String, isWarning: Boolean = false) {
    Row(modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(if (isWarning) "⚠️" else "•", color = if (isWarning) DiabloColor else Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = if (isWarning) Color.White else Color.LightGray, fontSize = 15.sp, lineHeight = 22.sp)
    }
}

@Composable
fun SafetyTrafficLight(onGreen: () -> Unit, onYellow: () -> Unit, onRed: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        SafetyButton(Color(0xFFEF5350), "🛑", onRed, label = stringResource(R.string.hot_safety_no))
        SafetyButton(Color(0xFFFFCA28), "🍺", onYellow, label = stringResource(R.string.hot_safety_drink))
        SafetyButton(Color(0xFF66BB6A), "🔥", onGreen, isBig = true)
    }
}

@Composable
fun SafetyButton(color: Color, icon: String, onClick: () -> Unit, isBig: Boolean = false, label: String = "") {
    val size = if (isBig) 80.dp else 60.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f)), border = BorderStroke(2.dp, color), shape = CircleShape, modifier = Modifier.size(size), contentPadding = PaddingValues(0.dp)) {
            Text(icon, fontSize = if (isBig) 32.sp else 24.sp)
        }
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HotBackground(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.1f, targetValue = 0.25f, animationSpec = infiniteRepeatable(animation = tween(2500, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "bgAlpha")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.1f, animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "bgScale")
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color.Black, color.copy(alpha = 0.15f), Color.Black))))
        Box(modifier = Modifier.align(Alignment.Center).size(350.dp).scale(scale).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(color.copy(alpha = alpha), Color.Transparent))))
    }
}

private fun getIntensityColor(intensity: HotIntensity): Color {
    return when (intensity) { HotIntensity.SOFT -> ColorSoft; HotIntensity.MEDIUM -> ColorMedium; HotIntensity.HOT -> ColorHot; HotIntensity.EXTREME -> ColorExtreme }
}