package com.example.kampai.ui.theme.warmup

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // OBLIGATORIO
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.kampai.R
import com.example.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun WarmupGameScreen(
    viewModel: WarmupViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val showRulesDialog by viewModel.showRulesDialog.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(players) { viewModel.setPlayers(players) }

    Box(modifier = Modifier.fillMaxSize()) {
        WarmupBackground()
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            ResponsiveHeader(onBack = onBack, screenWidth = screenWidth)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = gameState) {
                    is WarmupViewModel.GameState.Idle -> {
                        IdleContent(
                            onStart = { viewModel.startWarmup() },
                            onShowRules = { viewModel.showRules() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is WarmupViewModel.GameState.ShowingAction -> {
                        when (val action = state.action) {
                            is WarmupViewModel.WarmupAction.Phrase -> {
                                PhraseContent(
                                    phraseRes = action.textRes, // Pasamos el ID
                                    emoji = action.emoji,
                                    color = action.color,
                                    currentRound = state.number,
                                    totalRounds = state.total,
                                    onNext = { viewModel.nextAction() },
                                    screenHeight = screenHeight,
                                    screenWidth = screenWidth
                                )
                            }
                            else -> {}
                        }
                    }
                    is WarmupViewModel.GameState.ShowingEvent -> {
                        EventDialog(
                            event = state.event,
                            onAccept = { viewModel.acceptChallenge() },
                            onReject = { viewModel.rejectChallenge() },
                            onReveal = { viewModel.revealGift() }, // Necesario para regalos
                            screenWidth = screenWidth
                        )
                    }
                    is WarmupViewModel.GameState.Finished -> {
                        FinishedContent(
                            onReset = { viewModel.reset() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                }
            }
        }
        if (showRulesDialog) {
            RulesDialog(onDismiss = { viewModel.hideRules() }, screenWidth = screenWidth)
        }
    }
}

// ... (WarmupBackground se mantiene igual) ...
@OptIn(UnstableApi::class)
@Composable
fun WarmupBackground() {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.background_video}")
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM } }, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)))
    }
}

@Composable
fun ResponsiveHeader(onBack: () -> Unit, screenWidth: androidx.compose.ui.unit.Dp) {
    val headerPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.Black.copy(alpha = 0.4f)) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = headerPadding, vertical = 12.dp)) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart).size(40.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
            }
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.warmup_screen_title), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                Text(text = stringResource(R.string.warmup_screen_subtitle), fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun IdleContent(onStart: () -> Unit, onShowRules: () -> Unit, screenHeight: androidx.compose.ui.unit.Dp, screenWidth: androidx.compose.ui.unit.Dp) {
    // ... (Animaciones se mantienen) ...
    val contentPadding = (screenWidth * 0.06f).coerceIn(20.dp, 32.dp)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎯", fontSize = 80.sp) // Emoji simple
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(contentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.warmup_mode_title), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(R.string.warmup_mode_desc), fontSize = 14.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                ResponsiveFeature("✨", stringResource(R.string.warmup_feature_1))
                ResponsiveFeature("🎲", stringResource(R.string.warmup_feature_2))
                ResponsiveFeature("🔥", stringResource(R.string.warmup_feature_3))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        ResponsiveButton(text = stringResource(R.string.warmup_start_btn), onClick = onStart, height = 56.dp, color = Color(0xFFF59E0B))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onShowRules, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.warmup_rules_btn), color = Color(0xFFF59E0B))
        }
    }
}

@Composable
fun ResponsiveFeature(emoji: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
fun PhraseContent(
    phraseRes: Int, // Recibimos Int
    emoji: String,
    color: Color,
    currentRound: Int,
    totalRounds: Int,
    onNext: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    // ... (Animaciones y padding igual) ...
    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 28.dp)
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.15f)) {
            // FORMATO: "Ronda 1/50"
            Text(
                text = stringResource(R.string.warmup_round_fmt, currentRound, totalRounds),
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(text = emoji, fontSize = 100.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
            Box(
                modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.15f), color.copy(alpha = 0.25f))))
                    .border(2.dp, color.copy(alpha = 0.6f), RoundedCornerShape(28.dp)).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // AQUÍ SE TRADUCE LA FRASE
                Text(
                    text = stringResource(phraseRes),
                    fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        ResponsiveButton(text = stringResource(R.string.warmup_next_btn), onClick = onNext, height = 56.dp, color = color)
    }
}

@Composable
fun EventDialog(
    event: WarmupViewModel.WarmupAction.Event,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onReveal: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    // ... (Estado del Dialog) ...
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        Dialog(onDismissRequest = {}) {
            val isGiftEvent = event.eventType == WarmupViewModel.EventType.GIFT
            val giftPhase = event.giftPhase

            Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = event.emoji, fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Etiqueta (Preparación vs Evento)
                    Text(
                        text = if (isGiftEvent && giftPhase == WarmupViewModel.GiftPhase.RAISE_HAND) stringResource(R.string.event_label_prep) else stringResource(R.string.event_label_special),
                        color = event.color, fontWeight = FontWeight.Bold
                    )

                    // Título del evento
                    Text(
                        text = stringResource(event.titleRes),
                        fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center
                    )

                    // Jugador seleccionado
                    if (event.selectedPlayer != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = event.color.copy(alpha = 0.25f))) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                                Text(text = event.selectedPlayer.avatarEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(stringResource(R.string.event_player_selected_label), fontSize = 12.sp, color = Color.Gray)
                                    Text(event.selectedPlayer.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DESCRIPCIÓN (Aquí ocurre la magia de los argumentos)
                    val descriptionText = if (event.descriptionArgs.isNotEmpty()) {
                        // Si hay argumentos (ej: nombres), usamos stringResource con varargs
                        stringResource(event.descriptionRes, *event.descriptionArgs.toTypedArray())
                    } else {
                        // Si no, carga normal
                        stringResource(event.descriptionRes)
                    }

                    Text(
                        text = descriptionText,
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(event.instructionRes),
                        fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones según fase
                    if (isGiftEvent && giftPhase == WarmupViewModel.GiftPhase.RAISE_HAND) {
                        Button(onClick = onReveal, colors = ButtonDefaults.buttonColors(containerColor = event.color), modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.event_btn_reveal))
                        }
                    } else if (isGiftEvent && giftPhase == WarmupViewModel.GiftPhase.REVEAL) {
                        Button(onClick = { showDialog = false; onAccept() }, colors = ButtonDefaults.buttonColors(containerColor = event.color), modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.event_btn_continue))
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { showDialog = false; onReject() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.weight(1f)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.event_btn_reject))
                                    Text(stringResource(R.string.event_btn_reject_sub), fontSize = 10.sp)
                                }
                            }
                            Button(onClick = { showDialog = false; onAccept() }, colors = ButtonDefaults.buttonColors(containerColor = event.color), modifier = Modifier.weight(1f)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.event_btn_accept))
                                    Text(stringResource(R.string.event_btn_accept_sub), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinishedContent(onReset: () -> Unit, screenHeight: androidx.compose.ui.unit.Dp, screenWidth: androidx.compose.ui.unit.Dp) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉", fontSize = 100.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(R.string.warmup_finished_title), fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.warmup_finished_subtitle), fontSize = 16.sp, color = Color.LightGray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(48.dp))
        ResponsiveButton(text = stringResource(R.string.warmup_back_home_btn), onClick = onReset, height = 56.dp, color = Color(0xFFF59E0B))
    }
}

@Composable
fun ResponsiveButton(text: String, onClick: () -> Unit, height: androidx.compose.ui.unit.Dp, color: Color) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.fillMaxWidth().height(height),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun RulesDialog(onDismiss: () -> Unit, screenWidth: androidx.compose.ui.unit.Dp) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.9f), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.rules_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Gray) }
                }
                Divider(color = Color.White.copy(alpha = 0.1f))
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    RuleItem("1️⃣", stringResource(R.string.rule_1_title), stringResource(R.string.rule_1_desc))
                    RuleItem("2️⃣", stringResource(R.string.rule_2_title), stringResource(R.string.rule_2_desc))
                    RuleItem("3️⃣", stringResource(R.string.rule_3_title), stringResource(R.string.rule_3_desc))
                    RuleItem("4️⃣", stringResource(R.string.rule_4_title), stringResource(R.string.rule_4_desc))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.rules_understood))
                }
            }
        }
    }
}

@Composable
fun RuleItem(emoji: String, title: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            Text(description, color = Color.Gray)
        }
    }
}