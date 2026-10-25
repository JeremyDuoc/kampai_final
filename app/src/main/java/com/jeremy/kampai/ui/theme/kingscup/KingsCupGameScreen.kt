package com.jeremy.kampai.ui.theme.kingscup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.R
import com.jeremy.kampai.ui.theme.partymanager.PartyManagerViewModel

/**
 * KING'S CUP / ESSENTIAL — One primary action per phase.
 * No house-rule forms, buddy selection screens, audit log, or artificial pauses.
 * Retains the original R.string.card_* / guide_* / king_* texts and original 13 ranks.
 * IMPORTANT: pass nav-graph-scoped PartyManagerViewModel from the party graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KingsCupGameScreen(
    viewModel: KingsCupViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val partyRoster by partyViewModel.players.collectAsState()
    val party by viewModel.partyPlayers.collectAsState()
    val game by viewModel.state.collectAsState()
    val error by viewModel.error.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showGuide by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var askReset by remember { mutableStateOf(false) }

    LaunchedEffect(partyRoster) { viewModel.syncParty(partyRoster) }

    Column(
        Modifier
            .fillMaxSize()
            .background(KcBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top App Bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = KcText)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("KAMPAI / CARTAS", color = KcMuted, fontSize = 10.sp, letterSpacing = 1.7.sp)
                Text(
                    "KING'S CUP",
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    letterSpacing = 0.5.sp,
                    color = KcText
                )
            }
            IconButton(onClick = { showGuide = true }) {
                Icon(Icons.Filled.Info, contentDescription = "Reglas de las cartas", tint = KcText)
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = KcText)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Sacar Rey (Prueba)") },
                        onClick = {
                            showMenu = false
                            viewModel.debugForceKing()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reiniciar partida") },
                        onClick = {
                            showMenu = false
                            askReset = true
                        }
                    )
                }
            }
        }

        // Progress header
        KcProgress(
            kings = game.kings,
            remaining = if (game.phase == KingsCupEngine.Phase.SETUP) 52 else game.remaining
        )

        // Main Stage
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val player = game.currentPlayer
                if (game.phase == KingsCupEngine.Phase.SETUP) {
                    Text("LISTOS PARA LA PARTIDA", color = KcMuted, fontSize = 11.sp, letterSpacing = 1.3.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (party.size >= 2) "${party.size} jugadores en tu party" else "Prepara tu party",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = KcText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                } else {
                    val overlineText = if (game.phase == KingsCupEngine.Phase.FINISHED) "EL ÚLTIMO REY" else "LE TOCA A"
                    Text(overlineText, color = KcMuted, fontSize = 10.sp, letterSpacing = 1.8.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        player?.name.orEmpty(),
                        color = KcText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Card with subtle ambient shimmer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    // Shimmer warm glow
                    Box(
                        Modifier
                            .size(width = 190.dp, height = 250.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(KcAmber.copy(alpha = 0.12f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(90.dp)
                            )
                    )

                    // Fade/scale card transition
                    AnimatedContent(
                        targetState = game.phase to game.card?.id,
                        transitionSpec = {
                            (fadeIn(tween(240)) + scaleIn(tween(320, easing = FastOutSlowInEasing), initialScale = 0.94f))
                                .togetherWith(fadeOut(tween(140)))
                        },
                        label = "card-transition"
                    ) { target ->
                        when (target.first) {
                            KingsCupEngine.Phase.SETUP, KingsCupEngine.Phase.READY -> KcCardBack()
                            KingsCupEngine.Phase.REVEALED, KingsCupEngine.Phase.FINISHED -> {
                                val card = game.card
                                if (card != null) KcCardFace(card)
                                else KcCardBack()
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                when (game.phase) {
                    KingsCupEngine.Phase.SETUP -> {
                        Text(
                            "52 cartas · 4 reyes · 1 mesa",
                            color = KcMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        if (party.size < 2) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Agrega al menos dos jugadores desde Party Manager.",
                                color = KcAmber,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    KingsCupEngine.Phase.READY -> {
                        Text(
                            "Saca una carta para descubrir la regla.",
                            color = KcMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    KingsCupEngine.Phase.REVEALED -> {
                        game.card?.let { KcRule(it, game.kings) }
                    }
                    KingsCupEngine.Phase.FINISHED -> {
                        Text(
                            stringResource(R.string.kings_game_over_king),
                            color = KcAmber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "El cuarto rey ha terminado la partida.",
                            color = KcMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom CTA
        Column(
            Modifier
                .fillMaxWidth()
                .background(KcBackground)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                onClick = {
                    when (game.phase) {
                        KingsCupEngine.Phase.SETUP -> viewModel.start()
                        KingsCupEngine.Phase.READY -> {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.draw()
                        }
                        KingsCupEngine.Phase.REVEALED -> viewModel.nextTurn()
                        KingsCupEngine.Phase.FINISHED -> viewModel.newGame()
                    }
                },
                enabled = game.phase != KingsCupEngine.Phase.SETUP || party.size >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KcAmber,
                    contentColor = KcBackground,
                    disabledContainerColor = KcPanel,
                    disabledContentColor = KcMuted
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    when (game.phase) {
                        KingsCupEngine.Phase.SETUP -> "COMENZAR"
                        KingsCupEngine.Phase.READY -> "SACAR CARTA"
                        KingsCupEngine.Phase.REVEALED -> "SIGUIENTE TURNO"
                        KingsCupEngine.Phase.FINISHED -> "JUGAR DE NUEVO"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error.orEmpty(), fontSize = 12.sp, color = Color(0xFFFF8492), textAlign = TextAlign.Center)
            }
        }
    }

    if (showGuide) {
        ModalBottomSheet(
            onDismissRequest = { showGuide = false },
            containerColor = KcPanel,
            contentColor = KcText
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
            ) {
                Text(
                    "GUÍA DE CARTAS",
                    color = KcAmber,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.kings_guide_objective), color = KcMuted, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                KingsCupEngine.Rank.values().forEach { rank ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            rank.label,
                            modifier = Modifier.width(32.dp),
                            color = KcAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(rank.ruleTitle()),
                                color = KcText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (rank == KingsCupEngine.Rank.KING) {
                                listOf(
                                    R.string.guide_k1,
                                    R.string.guide_k2,
                                    R.string.guide_k3,
                                    R.string.guide_k4
                                ).forEachIndexed { index, guideId ->
                                    Text(
                                        "Rey ${index + 1}: " + stringResource(guideId),
                                        color = KcMuted,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                    if (index != 3) Spacer(Modifier.height(3.dp))
                                }
                            } else {
                                Text(
                                    stringResource(rank.guideDescription()),
                                    color = KcMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                    Divider(color = Color.White.copy(alpha = 0.07f))
                }
                Spacer(Modifier.height(26.dp))
            }
        }
    }

    if (askReset) {
        AlertDialog(
            onDismissRequest = { askReset = false },
            title = { Text("¿Reiniciar la partida?") },
            text = { Text("Se barajarán de nuevo las cartas. Los jugadores de tu party se conservan.") },
            confirmButton = {
                TextButton(onClick = { viewModel.reset(); askReset = false }) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { askReset = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private val KcBackground = Color(0xFF07070A)
private val KcPanel = Color(0xFF111118)
private val KcText = Color(0xFFF7F6FF)
private val KcMuted = Color(0xFFAAA7BD)
private val KcAmber = Color(0xFFFFC46B)
private val KcCardPaper = Color(0xFFF7F4EC)

@Composable
private fun KcProgress(kings: Int, remaining: Int) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("LOS CUATRO REYES", color = KcMuted, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(4) { index ->
                        Box(
                            Modifier
                                .size(width = 22.dp, height = 5.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (index < kings) KcAmber else Color(0xFF282733))
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$kings / 4 REYES", color = KcText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("$remaining cartas restantes", color = KcMuted, fontSize = 11.sp)
            }
        }
        Divider(
            color = Color.White.copy(alpha = 0.08f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun KcCardBack() {
    Box(
        Modifier
            .width(207.dp)
            .height(271.dp)
            .border(BorderStroke(1.dp, KcAmber.copy(alpha = 0.45f)), RoundedCornerShape(17.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF22202B), Color(0xFF12121B), Color(0xFF15101D))
                ),
                RoundedCornerShape(17.dp)
            )
            .padding(9.dp)
            .border(BorderStroke(1.dp, KcAmber.copy(alpha = 0.20f)), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.ic_game_crown),
                contentDescription = null,
                tint = KcAmber,
                modifier = Modifier.size(54.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "KAMPAI",
                color = KcText,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Text("KING'S CUP", color = KcMuted, letterSpacing = 2.sp, fontSize = 10.sp)
        }
    }
}

@Composable
private fun KcCardFace(card: KingsCupEngine.Card) {
    val ink = if (card.suit == KingsCupEngine.Suit.HEARTS || card.suit == KingsCupEngine.Suit.DIAMONDS)
        Color(0xFFB63747) else Color(0xFF22232A)
    Box(
        Modifier
            .width(207.dp)
            .height(271.dp)
            .background(KcCardPaper, RoundedCornerShape(17.dp))
            .border(1.dp, Color(0xFFD7D2CA), RoundedCornerShape(17.dp))
            .padding(17.dp)
    ) {
        Column(Modifier.align(Alignment.TopStart)) {
            Text(card.rank.label, fontWeight = FontWeight.Black, color = ink, fontSize = 32.sp, lineHeight = 32.sp)
            Text(card.suit.symbol, color = ink, fontSize = 22.sp)
        }
        if (card.rank == KingsCupEngine.Rank.KING) {
            KcKingCore(suitSymbol = card.suit.symbol, ink = ink)
        } else {
            Text(
                card.suit.symbol,
                modifier = Modifier.align(Alignment.Center),
                fontFamily = FontFamily.Serif,
                fontSize = 100.sp,
                color = ink
            )
        }
        Text(
            "${card.rank.label}${card.suit.symbol}",
            modifier = Modifier.align(Alignment.BottomEnd),
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = ink
        )
    }
}

@Composable
private fun BoxScope.KcKingCore(suitSymbol: String, ink: Color) {
    Box(
        Modifier
            .align(Alignment.Center)
            .size(width = 134.dp, height = 162.dp)
            .border(1.5.dp, ink.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.10f))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(12.dp)
    ) {
        Box(
            Modifier
                .matchParentSize()
                .border(1.dp, ink.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        )
        Icon(
            painter = painterResource(R.drawable.ic_game_crown),
            contentDescription = null,
            tint = ink,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(22.dp)
        )
        Text(
            "KING",
            color = ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            letterSpacing = 2.3.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
        )
        Text(
            suitSymbol,
            color = ink.copy(alpha = 0.85f),
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 5.dp, top = 38.dp)
        )
        Text(
            suitSymbol,
            color = ink.copy(alpha = 0.85f),
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 5.dp, top = 38.dp)
        )
        Text(
            suitSymbol,
            color = ink.copy(alpha = 0.85f),
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 5.dp, bottom = 38.dp)
        )
        Text(
            suitSymbol,
            color = ink.copy(alpha = 0.85f),
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 5.dp, bottom = 38.dp)
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .size(76.dp)
                .border(2.dp, ink, CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.72f), Color.White.copy(alpha = 0.12f))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("K", color = ink, fontWeight = FontWeight.Black, fontSize = 36.sp)
        }
        Text(
            "KING'S CUP",
            color = ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun KcRule(card: KingsCupEngine.Card, kings: Int) {
    val descriptionRes = if (card.rank == KingsCupEngine.Rank.KING) {
        when (kings) {
            1 -> R.string.king_1_desc
            2 -> R.string.king_2_desc
            3 -> R.string.king_3_desc
            else -> R.string.king_4_desc
        }
    } else card.rank.ruleDescription()
    val isFourthKing = (kings == 4 && card.rank == KingsCupEngine.Rank.KING)

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(card.rank.ruleTitle()),
            color = if (isFourthKing) KcAmber else KcText,
            fontWeight = FontWeight.Black,
            fontSize = 23.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(descriptionRes),
            color = KcMuted,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun KingsCupEngine.Rank.ruleTitle(): Int = when (this) {
    KingsCupEngine.Rank.ACE -> R.string.card_ace_title
    KingsCupEngine.Rank.TWO -> R.string.card_2_title
    KingsCupEngine.Rank.THREE -> R.string.card_3_title
    KingsCupEngine.Rank.FOUR -> R.string.card_4_title
    KingsCupEngine.Rank.FIVE -> R.string.card_5_title
    KingsCupEngine.Rank.SIX -> R.string.card_6_title
    KingsCupEngine.Rank.SEVEN -> R.string.card_7_title
    KingsCupEngine.Rank.EIGHT -> R.string.card_8_title
    KingsCupEngine.Rank.NINE -> R.string.card_9_title
    KingsCupEngine.Rank.TEN -> R.string.card_10_title
    KingsCupEngine.Rank.JACK -> R.string.card_j_title
    KingsCupEngine.Rank.QUEEN -> R.string.card_q_title
    KingsCupEngine.Rank.KING -> R.string.card_k_title
}

private fun KingsCupEngine.Rank.ruleDescription(): Int = when (this) {
    KingsCupEngine.Rank.ACE -> R.string.card_ace_desc
    KingsCupEngine.Rank.TWO -> R.string.card_2_desc
    KingsCupEngine.Rank.THREE -> R.string.card_3_desc
    KingsCupEngine.Rank.FOUR -> R.string.card_4_desc
    KingsCupEngine.Rank.FIVE -> R.string.card_5_desc
    KingsCupEngine.Rank.SIX -> R.string.card_6_desc
    KingsCupEngine.Rank.SEVEN -> R.string.card_7_desc
    KingsCupEngine.Rank.EIGHT -> R.string.card_8_desc
    KingsCupEngine.Rank.NINE -> R.string.card_9_desc
    KingsCupEngine.Rank.TEN -> R.string.card_10_desc
    KingsCupEngine.Rank.JACK -> R.string.card_j_desc
    KingsCupEngine.Rank.QUEEN -> R.string.card_q_desc
    KingsCupEngine.Rank.KING -> R.string.card_k_desc
}

private fun KingsCupEngine.Rank.guideDescription(): Int = when (this) {
    KingsCupEngine.Rank.ACE -> R.string.guide_ace
    KingsCupEngine.Rank.TWO -> R.string.guide_2
    KingsCupEngine.Rank.THREE -> R.string.guide_3
    KingsCupEngine.Rank.FOUR -> R.string.guide_4
    KingsCupEngine.Rank.FIVE -> R.string.guide_5
    KingsCupEngine.Rank.SIX -> R.string.guide_6
    KingsCupEngine.Rank.SEVEN -> R.string.guide_7
    KingsCupEngine.Rank.EIGHT -> R.string.guide_8
    KingsCupEngine.Rank.NINE -> R.string.guide_9
    KingsCupEngine.Rank.TEN -> R.string.guide_10
    KingsCupEngine.Rank.JACK -> R.string.guide_j
    KingsCupEngine.Rank.QUEEN -> R.string.guide_q
    KingsCupEngine.Rank.KING -> R.string.guide_k1
}