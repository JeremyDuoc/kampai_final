package com.jeremy.kampai.ui.theme.never

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

private val NeverBase = Color(0xFF07070A)
private val NeverInk = Color(0xFFF4F2F6)
private val NeverMuted = Color(0xFFA7A6B0)
private val NeverMint = Color(0xFFA5ECB4)

/**
 * YO NUNCA / ESSENTIAL
 * A group game, not a player-by-player form.
 * Exactly one primary action; optional left swipe; existing string-array catalog.
 */
@Composable
fun NeverGameScreen(
    viewModel: NeverViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val game by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }
    var confirmRestart by remember { mutableStateOf(false) }
    val compactHeight = LocalConfiguration.current.screenHeightDp < 740
    var advancing by remember { mutableStateOf(false) }

    LaunchedEffect(advancing, game.number) {
        if (advancing) {
            delay(315)
            advancing = false
        }
    }

    val next: () -> Unit = {
        if (game.canAdvance && !advancing) {
            advancing = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.nextQuestion()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(NeverBase)
    ) {
        // Ambient background glow (matching prototype)
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF639471).copy(alpha = 0.12f), Color.Transparent),
                        radius = 600f
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = NeverInk)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "KAMPAI / JUEGOS",
                        color = Color(0xFF92929B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "YO NUNCA",
                        color = NeverInk,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones", tint = NeverInk)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF191B21))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cómo jugar", color = NeverInk, fontSize = 14.sp) },
                            onClick = {
                                showMenu = false
                                showGuide = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Empezar de nuevo", color = NeverInk, fontSize = 14.sp) },
                            onClick = {
                                showMenu = false
                                confirmRestart = true
                            }
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = if (compactHeight) 8.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Intro
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "CONFESIONES DE LA MESA",
                        color = NeverMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(11.dp))
                    Text(
                        "Alguien tiene\nalgo que contar.",
                        textAlign = TextAlign.Center,
                        color = NeverInk,
                        fontSize = if (compactHeight) 23.sp else 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 31.sp
                    )
                    if (!compactHeight) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Si alguna vez lo hiciste, te toca beber.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = NeverMuted
                        )
                    }
                }

                Spacer(Modifier.height(if (compactHeight) 14.dp else 20.dp))

                // Card Stack with Animation
                AnimatedContent(
                    targetState = game,
                    transitionSpec = {
                        (slideInHorizontally(animationSpec = tween(260)) { it / 2 } +
                            fadeIn(animationSpec = tween(240)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = tween(190)) { -it / 2 } +
                                    fadeOut(animationSpec = tween(150))
                            )
                    },
                    label = "NeverNextStatement"
                ) { shown ->
                    NeverStatementCard(
                        question = shown.question ?: "No hay frases disponibles.",
                        number = shown.number,
                        enabled = shown.canAdvance && !advancing,
                        compact = compactHeight,
                        onSwipeLeft = next
                    )
                }

                Spacer(Modifier.height(if (compactHeight) 12.dp else 18.dp))

                Text(
                    "Desliza a la izquierda o pulsa siguiente.",
                    color = NeverMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom CTA
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)))
                    .padding(horizontal = 23.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = next,
                    enabled = game.canAdvance && !advancing,
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeverMint,
                        contentColor = Color(0xFF101913),
                        disabledContainerColor = NeverMint.copy(alpha = 0.55f),
                        disabledContentColor = Color(0xFF101913).copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    Text(
                        "SIGUIENTE FRASE  →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    "Hablen entre ustedes. La app no necesita registrar nada.",
                    fontSize = 11.sp,
                    color = NeverMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showGuide) {
        AlertDialog(
            onDismissRequest = { showGuide = false },
            containerColor = Color(0xFF181A20),
            titleContentColor = NeverInk,
            textContentColor = NeverMuted,
            title = { Text("Cómo jugar", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Lean la frase en voz alta. Si alguien del grupo ha hecho lo que dice, bebe. Después, sigan con otra frase. Pueden contar la historia si quieren.",
                        color = Color(0xFFC0C0C9),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No hay turnos individuales, votos ni respuestas que registrar: lo importante ocurre entre ustedes.",
                        color = Color(0xFFC0C0C9),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Todas las personas pueden pasar de una frase o beber algo sin alcohol.",
                        color = Color(0xFF898D92),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showGuide = false }) {
                    Text("Entendido", color = NeverMint, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            containerColor = Color(0xFF191B21),
            titleContentColor = NeverInk,
            textContentColor = NeverMuted,
            title = { Text("¿Empezar de nuevo?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se mezclan las frases y se reinicia el contador. Las preguntas vistas pueden aparecer de nuevo.",
                    color = Color(0xFFB0B2B9),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { confirmRestart = false }) {
                    Text("Cancelar", color = NeverMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmRestart = false
                        advancing = false
                        viewModel.reset()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeverMint,
                        contentColor = Color(0xFF101913)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reiniciar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun NeverStatementCard(
    question: String,
    number: Int,
    enabled: Boolean,
    compact: Boolean,
    onSwipeLeft: () -> Unit
) {
    var dragOffset by remember(number) { mutableFloatStateOf(0f) }
    val smoothOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "NeverCardDrag"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(if (compact) 290.dp else 344.dp),
        contentAlignment = Alignment.Center
    ) {
        // Shadow/Background rotated card
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(if (compact) 276.dp else 332.dp)
                .graphicsLayer { rotationZ = 2f }
                .background(Color(0xFF171A1C), RoundedCornerShape(25.dp))
                .border(1.dp, NeverMint.copy(alpha = 0.12f), RoundedCornerShape(25.dp))
        )

        // Main Card
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = smoothOffset
                    rotationZ = smoothOffset * 0.025f
                }
                .shadow(16.dp, RoundedCornerShape(23.dp))
                .clip(RoundedCornerShape(23.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1B2823), Color(0xFF151D1C), Color(0xFF12171A))
                    )
                )
                .border(1.dp, NeverMint.copy(alpha = 0.34f), RoundedCornerShape(23.dp))
                .pointerInput(number, enabled) {
                    if (enabled) detectHorizontalDragGestures(
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            dragOffset = (dragOffset + amount).coerceIn(-200f, 70f)
                        },
                        onDragEnd = {
                            val shouldAdvance = dragOffset < -110f
                            dragOffset = 0f
                            if (shouldAdvance) onSwipeLeft()
                        },
                        onDragCancel = { dragOffset = 0f }
                    )
                }
        ) {
            // Subtle top-right decorative accent ring and radial glow (matching mockup)
            Box(
                Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(NeverMint.copy(alpha = 0.10f), Color.Transparent)
                        )
                    )
            )
            Box(
                Modifier
                    .size(115.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = (-24).dp)
                    .border(1.dp, Color(0xFFACE8BC).copy(alpha = 0.13f), CircleShape)
            )

            // Card Inner Content
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(23.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Series & Serial Number
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "YO NUNCA / KAMPAI",
                        color = Color(0xFFB1D7BD),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                    Text(
                        "#${number.toString().padStart(3, '0')}",
                        color = Color(0xFF8FAA98),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Middle Question
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val size = when {
                        compact && question.length > 100 -> 21.sp
                        maxWidth < 295.dp -> 22.sp
                        question.length > 150 -> 22.sp
                        question.length > 100 -> 25.sp
                        else -> 28.sp
                    }
                    Text(
                        text = question,
                        color = Color(0xFFF6FAF6),
                        fontSize = size,
                        lineHeight = (size.value * 1.22f).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Bottom Footer
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFDBF3DF).copy(alpha = 0.15f))
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "DI LA VERDAD.",
                            color = Color(0xFFA7C4AD),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.25.sp
                        )
                        Text(
                            "↗",
                            color = NeverMint,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}