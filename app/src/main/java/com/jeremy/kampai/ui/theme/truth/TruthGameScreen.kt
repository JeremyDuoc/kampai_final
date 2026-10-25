package com.jeremy.kampai.ui.theme.truth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.ui.theme.partymanager.PartyManagerViewModel

private val TBase = Color(0xFF07070A)
private val TPanel = Color(0xFF13131A)
private val TInk = Color(0xFFF6F4FA)
private val TMuted = Color(0xFFA9A5B7)
private val TTruth = Color(0xFF79DBE5)
private val TDare = Color(0xFFF28E9F)
private val TGold = Color(0xFFFFC46B)

/** Navigation API is unchanged; pass a graph-scoped PartyManagerViewModel for a shared party. */
@Composable
fun TruthGameScreen(
    viewModel: TruthViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val party by partyViewModel.players.collectAsState()
    val state by viewModel.state.collectAsState()
    val error by viewModel.error.collectAsState()
    val partyChanged by viewModel.partyChanged.collectAsState()
    val haptics = LocalHapticFeedback.current
    var menuOpen by remember { mutableStateOf(false) }
    var confirmRestart by remember { mutableStateOf(false) }

    LaunchedEffect(party) { viewModel.syncParty(party) }

    Box(
        Modifier
            .fillMaxSize()
            .background(TBase)
    ) {
        // Subtle ambient glow at the top
        Box(
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8258BF).copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Top Bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Volver", tint = TInk)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KAMPAI / JUEGOS", color = TMuted, fontSize = 10.sp, letterSpacing = 1.7.sp)
                    Text(
                        "VERDAD O RETO",
                        color = TInk,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.3.sp
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, "Opciones", tint = TInk)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Nueva partida") },
                            onClick = {
                                menuOpen = false
                                confirmRestart = true
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("⚡ Verdad directa (Test)") },
                            onClick = {
                                menuOpen = false
                                viewModel.debugForceTruth()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚡ Reto directo (Test)") },
                            onClick = {
                                menuOpen = false
                                viewModel.debugForceDare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚡ Penalización (Test)") },
                            onClick = {
                                menuOpen = false
                                viewModel.debugForcePenalty()
                            }
                        )
                    }
                }
            }
            Divider(color = Color.White.copy(alpha = 0.075f))

            if (state.phase == TruthRoundEngine.Phase.EMPTY) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("SIN PARTICIPANTES", color = TGold, fontSize = 11.sp, letterSpacing = 2.sp)
                        Text(
                            error ?: "Agrega jugadores a tu party desde el menú principal.",
                            color = TInk,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )
                        TextButton(onClick = onBack) { Text("Volver a la party", color = TGold) }
                    }
                }
            } else {
                val player = state.currentPlayer
                // Turn Bar
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 23.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF24222E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(player?.emoji ?: "✦", fontSize = 23.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("LE TOCA A", fontSize = 10.sp, color = TMuted, letterSpacing = 1.8.sp)
                        Text(player?.name ?: "—", fontSize = 22.sp, color = TInk, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TURNO", fontSize = 10.sp, color = TMuted, letterSpacing = 1.6.sp)
                        Text(
                            state.turn.toString().padStart(2, '0'),
                            color = TInk,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Divider(Modifier.padding(horizontal = 24.dp), color = Color.White.copy(alpha = 0.065f))

                // Phase Animated Transition Area
                AnimatedContent(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    targetState = state.phase,
                    transitionSpec = {
                        (fadeIn(tween(230)) + slideInVertically(tween(230)) { it / 14 }) togetherWith fadeOut(tween(110))
                    },
                    label = "truthPhase"
                ) { phase ->
                    when (phase) {
                        TruthRoundEngine.Phase.PICK -> TruthChoiceContent(
                            onTruth = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.choose(TruthRoundEngine.Kind.TRUTH)
                            },
                            onDare = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.choose(TruthRoundEngine.Kind.DARE)
                            },
                            onRandom = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.randomPick()
                            },
                            onSkip = { viewModel.skipPlayer() }
                        )
                        TruthRoundEngine.Phase.ROLLING -> TruthRollingContent()
                        TruthRoundEngine.Phase.CHALLENGE -> {
                            val prompt = state.prompt
                            if (prompt != null) {
                                TruthPromptContent(
                                    resourceId = prompt.resourceId,
                                    kind = prompt.kind,
                                    canRedraw = state.redrawsUsed == 0,
                                    onRedraw = { viewModel.redraw() }
                                )
                            }
                        }
                        TruthRoundEngine.Phase.PENALTY -> TruthPenaltyContent(
                            name = player?.name ?: "Jugador",
                            amount = state.penaltyDrinks
                        )
                        TruthRoundEngine.Phase.EMPTY -> Unit
                    }
                }

                if (partyChanged) {
                    Text(
                        "La party cambió. Los cambios se aplican al iniciar una partida nueva.",
                        color = TMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }

                // Action area
                when (state.phase) {
                    TruthRoundEngine.Phase.CHALLENGE -> {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TruthActionButton("Paso", false, TDare, Modifier.weight(0.42f)) {
                                    viewModel.refuse()
                                }
                                TruthActionButton("Hecho · siguiente", true, TGold, Modifier.weight(0.58f)) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.complete()
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Si pasas, la penalización es de 2 tragos.",
                                color = Color(0xFF7C7889),
                                fontSize = 11.sp
                            )
                        }
                    }
                    TruthRoundEngine.Phase.PENALTY -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 12.dp)
                        ) {
                            TruthActionButton("Siguiente jugador", true, TGold, Modifier.fillMaxWidth()) {
                                viewModel.acknowledgePenalty()
                            }
                        }
                    }
                    else -> Unit
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            title = { Text("¿Empezar otra partida?") },
            text = { Text("Se conserva la party. Se reinicia el orden de turnos y el historial de preguntas.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestart = false
                    viewModel.startFresh()
                }) { Text("Empezar de nuevo") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestart = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TruthChoiceContent(
    onTruth: () -> Unit,
    onDare: () -> Unit,
    onRandom: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))
        Text(
            "¿QUÉ ELIGES?",
            color = TInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
        Text(
            "Tú decides. O deja que el azar hable.",
            color = TMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
        )
        TruthModeCard("01", "VERDAD", "Una pregunta. Una respuesta sincera.", "?", TTruth, onTruth)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
            TextButton(onClick = onRandom) {
                Text(
                    "✦  QUE DECIDA LA SUERTE",
                    color = TGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
            }
            Divider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
        }
        TruthModeCard("02", "RETO", "Atrévete a cumplir lo que salga.", "!", TDare, onDare)
        Spacer(Modifier.height(14.dp))
        TextButton(onClick = onSkip) {
            Text("Saltar jugador →", color = TMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TruthModeCard(
    number: String,
    title: String,
    description: String,
    glyph: String,
    accent: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.975f else 1f,
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "modePress"
    )
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(TPanel, accent.copy(alpha = 0.10f))))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(20.dp)
    ) {
        Text(
            glyph,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 10.dp),
            color = accent.copy(alpha = 0.16f),
            fontFamily = FontFamily.Serif,
            fontSize = 124.sp,
            lineHeight = 110.sp
        )
        Column(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.78f)
        ) {
            Text(
                "$number / ELIGE",
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.8.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                color = TInk,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(description, color = TMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TruthRollingContent() {
    val motion = rememberInfiniteTransition(label = "randomRoll")
    val rotation by motion.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(480, easing = LinearEasing)),
        label = "roll"
    )
    Box(
        Modifier
            .fillMaxSize()
            .padding(25.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(115.dp)
                    .border(1.dp, TGold.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✦",
                    modifier = Modifier.rotate(rotation),
                    fontSize = 72.sp,
                    color = TGold
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                "EL AZAR DECIDE",
                color = TInk,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
private fun TruthPromptContent(
    resourceId: Int,
    kind: TruthRoundEngine.Kind,
    canRedraw: Boolean,
    onRedraw: () -> Unit
) {
    val truth = kind == TruthRoundEngine.Kind.TRUTH
    val accent = if (truth) TTruth else TDare
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(15.dp))
        Text(
            if (truth) "VERDAD" else "RETO",
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.8.sp
        )
        Spacer(Modifier.height(18.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 245.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TPanel)
                .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                .padding(horizontal = 25.dp, vertical = 29.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .size(width = 29.dp, height = 3.dp)
                    .background(accent, RoundedCornerShape(2.dp))
            )
            AnimatedContent(
                targetState = resourceId,
                transitionSpec = { fadeIn(tween(230)) togetherWith fadeOut(tween(90)) },
                label = "nextPrompt"
            ) { id ->
                if (id != 0) {
                    Text(
                        stringResource(id),
                        color = TInk,
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.height(15.dp))
        if (canRedraw) {
            TextButton(onClick = onRedraw) {
                Text("↻  Cambiar una vez", color = TMuted, fontSize = 13.sp)
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun TruthPenaltyContent(name: String, amount: Int) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "PASÓ SU TURNO",
            color = TDare,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(14.dp))
        Text(name, color = TInk, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(22.dp))
        Text(
            "$amount",
            color = TGold,
            fontFamily = FontFamily.Serif,
            fontSize = 96.sp,
            lineHeight = 100.sp
        )
        Text(
            if (amount == 1) "TRAGO" else "TRAGOS",
            color = TGold,
            fontSize = 21.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Penalización de la ronda. Continúen cuando estén listos.",
            color = TMuted,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun TruthActionButton(
    label: String,
    primary: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 58.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) tint else Color(0xFF201923),
            contentColor = if (primary) TBase else TInk
        ),
        border = if (primary) null else BorderStroke(1.dp, tint.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}