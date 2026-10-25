package com.jeremy.kampai.ui.theme.warmup

import android.net.Uri
import com.jeremy.kampai.R
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import com.jeremy.kampai.domain.models.PlayerModel
import com.jeremy.kampai.ui.theme.*
import com.jeremy.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun WarmupGameScreen(
    viewModel: WarmupViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onShowAd: (() -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentRound = (gameState as? WarmupViewModel.GameState.ShowingAction)?.number ?: 0
    val showRulesDialog by viewModel.showRulesDialog.collectAsState()
    val showStatsDialog by viewModel.showStatsDialog.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val drinkStats by viewModel.drinkStats.collectAsState()
    val selectedRounds by viewModel.selectedRounds.collectAsState()
    val selectedEventFrequency by viewModel.selectedEventFrequency.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Color reactivo según la frase o evento
    val currentColor = remember(gameState) {
        when (val state = gameState) {
            is WarmupViewModel.GameState.ShowingAction -> {
                if (state.action is WarmupViewModel.WarmupAction.Phrase) state.action.color else SolarAmber
            }
            is WarmupViewModel.GameState.ShowingEvent -> state.event.color
            else -> SolarAmber
        }
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "warmupAccentColor"
    )

    var statsButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var helpButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var mainContentBounds by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(players) { viewModel.setPlayers(players) }
    LaunchedEffect(Unit) { viewModel.maybeShowTutorialAutomatically() }

    Box(modifier = Modifier.fillMaxSize().background(OledBlack)) {
        // Video de fondo en bucle con ExoPlayer + viñeta atmosférica reactiva
        WarmupBackground(activeColor = animatedAccentColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            ResponsiveHeader(
                onBack = {
                    if (currentRound > 20) {
                        onShowAd { onBack() }
                    } else {
                        onBack()
                    }
                },
                accentColor = animatedAccentColor,
                totalDrinks = drinkStats.values.sum(),
                onShowStats = { viewModel.toggleStats() },
                onShowHelp = { viewModel.showTutorialManually() },
                onStatsBoundsChanged = { statsButtonBounds = it },
                onHelpBoundsChanged = { helpButtonBounds = it }
            )

            AnimatedContent(
                targetState = gameState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(450, delayMillis = 60)) +
                            scaleIn(initialScale = 0.93f, animationSpec = tween(450, delayMillis = 60)))
                        .togetherWith(fadeOut(animationSpec = tween(300)))
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { mainContentBounds = it.boundsInRoot() },
                label = "WarmupStateTransition"
            ) { targetState ->
                when (targetState) {
                    is WarmupViewModel.GameState.Idle -> {
                        IdleContent(
                            players = players,
                            selectedRounds = selectedRounds,
                            selectedEventFrequency = selectedEventFrequency,
                            onRoundsChanged = { viewModel.setSelectedRounds(it) },
                            onEventFrequencyChanged = { viewModel.setSelectedEventFrequency(it) },
                            onStart = { viewModel.startWarmup() },
                            onShowRules = { viewModel.showRules() },
                            screenWidth = screenWidth
                        )
                    }
                    is WarmupViewModel.GameState.ShowingAction -> {
                        if (targetState.action is WarmupViewModel.WarmupAction.Phrase) {
                            PhraseContent(
                                phraseRes = targetState.action.textRes,
                                emoji = targetState.action.emoji,
                                color = targetState.action.color,
                                currentRound = targetState.number,
                                totalRounds = targetState.total,
                                onNext = { viewModel.nextAction() },
                                screenWidth = screenWidth
                            )
                        }
                    }
                    is WarmupViewModel.GameState.ShowingEvent -> {
                        EventDialog(
                            event = targetState.event,
                            onAccept = { viewModel.acceptChallenge() },
                            onReject = { viewModel.rejectChallenge() },
                            onReveal = { viewModel.revealGift() }
                        )
                    }
                    is WarmupViewModel.GameState.Finished -> {
                        FinishedContent(
                            stats = targetState.stats,
                            onReset = {
                                onShowAd { viewModel.reset() }
                            }
                        )
                    }
                }
            }
        }

        if (showRulesDialog) {
            RulesDialog(onDismiss = { viewModel.hideRules() })
        }
        if (showStatsDialog) {
            StatsDialog(
                players = players,
                stats = drinkStats,
                onDismiss = { viewModel.toggleStats() },
                onAddDrink = { playerId -> viewModel.addManualDrink(playerId) }
            )
        }
        if (showTutorial) {
            TutorialOverlay(
                onDismiss = { viewModel.dismissTutorial() },
                statsButtonBounds = statsButtonBounds,
                helpButtonBounds = helpButtonBounds,
                mainContentBounds = mainContentBounds
            )
        }
    }
}

// ============================================================================
// 1. FONDO DE VIDEO (EXOPLAYER) + VIÑETA REACTIVA
// ============================================================================

@OptIn(UnstableApi::class)
@Composable
fun WarmupBackground(activeColor: Color) {
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
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay cinematográfico oscuro y resplandor de neón reactivo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.88f),
                            activeColor.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )
    }
}

// ============================================================================
// 2. HEADER RESPONSIVO
// ============================================================================

@Composable
fun ResponsiveHeader(
    onBack: () -> Unit,
    accentColor: Color,
    totalDrinks: Int,
    onShowStats: () -> Unit,
    onShowHelp: () -> Unit,
    onStatsBoundsChanged: (Rect) -> Unit,
    onHelpBoundsChanged: (Rect) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(CyberSurface.copy(alpha = 0.85f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.warmup_screen_title),
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.warmup_screen_subtitle),
                fontSize = 11.sp,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onShowHelp,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurface.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .onGloballyPositioned { onHelpBoundsChanged(it.boundsInRoot()) }
            ) {
                Text(text = "❓", fontSize = 16.sp)
            }

            Surface(
                onClick = onShowStats,
                shape = CircleShape,
                color = CyberSurface.copy(alpha = 0.85f),
                border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.6f)),
                modifier = Modifier
                    .shadow(8.dp, CircleShape, spotColor = accentColor)
                    .onGloballyPositioned { onStatsBoundsChanged(it.boundsInRoot()) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = stringResource(R.string.warmup_stats_btn),
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$totalDrinks 🍺",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3. PANTALLA IDLE (CONFIGURACIÓN Y LANZADOR)
// ============================================================================

@Composable
fun IdleContent(
    players: List<PlayerModel>,
    selectedRounds: Int,
    selectedEventFrequency: Int,
    onRoundsChanged: (Int) -> Unit,
    onEventFrequencyChanged: (Int) -> Unit,
    onStart: () -> Unit,
    onShowRules: () -> Unit,
    screenWidth: Dp
) {
    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placa del cóctel con resplandor pulsante
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(28.dp, CircleShape, spotColor = SolarAmber)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(SolarAmber.copy(alpha = 0.4f), CyberSurfaceElevated)
                    )
                )
                .border(2.dp, SolarAmber, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AnimatedPulseEmoji(emoji = "🍹", baseSize = 56)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Tarjeta de Crew activo
        NeoGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = SolarAmber.copy(alpha = 0.4f),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.warmup_crew_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolarAmber,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (players.isNotEmpty()) {
                    AvatarStack(emojis = players.map { it.avatarEmoji }, maxVisible = 6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${players.size} bebedores listos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = stringResource(R.string.warmup_feature_1),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // HUD de configuración de rondas y eventos
        NeoGameConfigCard(
            selectedRounds = selectedRounds,
            selectedEventFrequency = selectedEventFrequency,
            onRoundsChanged = onRoundsChanged,
            onEventFrequencyChanged = onEventFrequencyChanged
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Botón de Inicio con Resorte
        TactileButton(
            onClick = onStart,
            containerColor = SolarAmber,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.warmup_start_btn).uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onShowRules) {
            Text(
                text = stringResource(R.string.warmup_rules_btn),
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// ============================================================================
// HUD DE CONFIGURACIÓN
// ============================================================================

@Composable
fun NeoGameConfigCard(
    selectedRounds: Int,
    selectedEventFrequency: Int,
    onRoundsChanged: (Int) -> Unit,
    onEventFrequencyChanged: (Int) -> Unit
) {
    NeoGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = Color.White.copy(alpha = 0.15f),
        cornerRadius = 24.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.warmup_config_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            NeoConfigSliderRow(
                emoji = "🎲",
                labelRes = R.string.warmup_config_rounds_label,
                value = selectedRounds,
                valueSuffix = stringResource(R.string.warmup_config_rounds_unit),
                range = WarmupViewModel.MIN_ROUNDS.toFloat()..WarmupViewModel.MAX_ROUNDS.toFloat(),
                onValueChange = { onRoundsChanged(it.toInt()) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            NeoConfigSliderRow(
                emoji = "⚡",
                labelRes = R.string.warmup_config_frequency_label,
                value = selectedEventFrequency,
                valueSuffix = stringResource(R.string.warmup_config_frequency_unit),
                range = WarmupViewModel.MIN_EVENT_FREQUENCY.toFloat()..WarmupViewModel.MAX_EVENT_FREQUENCY.toFloat(),
                onValueChange = { onEventFrequencyChanged(it.toInt()) }
            )
        }
    }
}

@Composable
fun NeoConfigSliderRow(
    emoji: String,
    labelRes: Int,
    value: Int,
    valueSuffix: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(labelRes),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SolarAmber.copy(alpha = 0.22f))
                    .border(1.dp, SolarAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$value $valueSuffix",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = SolarAmber
                )
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = SolarAmber,
                activeTrackColor = SolarAmber,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}

// ============================================================================
// 4. PHRASE CONTENT (TERMINAL HOLOGRÁFICO DE CONSIGNA NEO-ARCADE)
// ============================================================================

@Composable
fun LiveBeatEqualizer(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(390, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq4"
    )

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(h1, h2, h3, h4).forEach { heightFraction ->
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
        }
    }
}

fun getGameVectorForEmoji(emoji: String): Int {
    return when (emoji) {
        "🥃", "🍺", "🍻", "🍹" -> R.drawable.ic_game_beer
        "🍷", "🍸", "🥂", "🍳", "🍣" -> R.drawable.ic_game_chalice
        "💀", "☠️" -> R.drawable.ic_game_skull
        "💣", "💥" -> R.drawable.ic_game_bomb
        "🔥", "⚡", "🌞", "🦁", "♈", "♐" -> R.drawable.ic_game_fire
        "👑", "🏛️", "🗳️", "🪑", "🏠" -> R.drawable.ic_game_crown
        "🏆", "🎁", "🎂", "👴", "👵", "👶" -> R.drawable.ic_game_trophy
        "🧠", "💡", "🔤", "📝", "💭", "📚", "🌍" -> R.drawable.ic_game_brain
        "🎭", "🎪", "😂", "🥺", "😊", "😬", "😳", "🥱", "😐", "🤪", "😡", "👗", "👙", "👕" -> R.drawable.ic_game_theater
        "🎲", "🎰" -> R.drawable.ic_game_dice
        "⚫", "⚪", "🟢", "🌀", "🔄" -> R.drawable.ic_game_roulette
        "😈", "👹", "🐺", "🐶" -> R.drawable.ic_game_dare
        "🤫", "🤐", "🚪", "🗣️" -> R.drawable.ic_game_truth
        "🧪", "🚬", "💉", "🔞" -> R.drawable.ic_game_potion
        "🔍", "👁️", "👀", "🕵️", "👓", "👃", "👅", "💇" -> R.drawable.ic_game_search
        "🃏" -> R.drawable.ic_game_cards
        "📱", "🪫", "📲", "🔋" -> R.drawable.ic_game_phone
        "📸" -> R.drawable.ic_game_camera
        "🎵", "🎶", "🎤", "📻" -> R.drawable.ic_game_music
        "🦶", "👟", "👞", "🏃", "👇", "📏", "🦒" -> R.drawable.ic_game_foot
        "❤️‍🔥", "💋", "💍", "❤️", "💖", "💘" -> R.drawable.ic_game_heart
        "💔", "🤡" -> R.drawable.ic_game_broken_heart
        "💪", "🏋️" -> R.drawable.ic_game_muscle
        "✋", "👉", "👍", "✂️", "⚔️" -> R.drawable.ic_game_fist
        "⏱️", "⏰", "⏳", "🐢", "🔟", "🚗", "🏍️", "✈️", "✔️" -> R.drawable.ic_game_timer
        "🎉", "🥳", "🎈" -> R.drawable.ic_game_party
        "💰", "🤑", "💵" -> R.drawable.ic_game_coins
        "🎮", "🕹️", "🤓" -> R.drawable.ic_game_gamepad
        "🤗", "👫", "👨‍👩‍👧‍👦", "🤝", "💆‍♂️", "💆‍♀️", "💆" -> R.drawable.ic_game_handshake
        else -> R.drawable.ic_game_cheers
    }
}

@Composable
fun PhraseContent(
    phraseRes: Int,
    emoji: String,
    color: Color,
    currentRound: Int,
    totalRounds: Int,
    onNext: () -> Unit,
    screenWidth: Dp
) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "PhraseScale"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTip")
    val tipAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tipAlpha"
    )

    LaunchedEffect(phraseRes) {
        isVisible = false
        delay(35)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val progress = (currentRound.toFloat() / totalRounds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(400), label = "ProgressBar")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. HUD SUPERIOR DE STAGE & PROGRESO LÁSER
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurfaceElevated.copy(alpha = 0.85f))
                    .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiveBeatEqualizer(accentColor = color)

                Text(
                    text = stringResource(R.string.warmup_round_fmt, currentRound, totalRounds).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(color)
                        .shadow(4.dp, CircleShape, spotColor = color)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra Láser de Progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(color.copy(alpha = 0.4f), color)
                            )
                        )
                        .shadow(8.dp, CircleShape, spotColor = color)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. HERO CHALLENGE CARD (CENTRAL & PROTAGONISTA)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .scale(scale)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(32.dp, RoundedCornerShape(32.dp), spotColor = color)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CyberSurfaceElevated.copy(alpha = 0.96f),
                                CyberSurface.copy(alpha = 0.94f)
                            )
                        )
                    )
                    .border(1.5.dp, color.copy(alpha = 0.65f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Orbe del Emoji Integrado en la cabecera
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(24.dp, CircleShape, spotColor = color)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(color.copy(alpha = 0.45f), CyberSurfaceElevated)
                                )
                            )
                            .border(2.dp, color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(getGameVectorForEmoji(emoji)),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Badge de Modo PartyMix
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.18f))
                            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.warmup_screen_title).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = color,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Texto de la Consigna (Grande, legible, protagonista)
                    Text(
                        text = stringResource(phraseRes),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        lineHeight = 36.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Indicador Táctil
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.graphicsLayer { alpha = tipAlpha }
                    ) {
                        Text(
                            text = "►►",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            text = stringResource(R.string.warmup_quick_tip),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "◄◄",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. BOTÓN ARCADE TÁCTIL (SIN FLECHA DUPLICADA)
        TactileButton(
            onClick = onNext,
            containerColor = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.warmup_next_btn).replace("→", "").trim().uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============================================================================
// 5. EVENT DIALOG (MODO BOSS ENCOUNTER / HAZARD EVENT OVERHAUL)
// ============================================================================

@Composable
fun HazardStripesBanner(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stripeWidth = 22.dp.toPx()
        val totalWidth = size.width
        val totalHeight = size.height

        var startX = -totalHeight
        var isAccent = true
        while (startX < totalWidth + totalHeight) {
            val path = Path().apply {
                moveTo(startX, 0f)
                lineTo(startX + stripeWidth, 0f)
                lineTo(startX + stripeWidth - totalHeight, totalHeight)
                lineTo(startX - totalHeight, totalHeight)
                close()
            }
            drawPath(
                path = path,
                color = if (isAccent) accentColor.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.65f)
            )
            startX += stripeWidth
            isAccent = !isAccent
        }
    }
}

@Composable
fun EventDialog(
    event: WarmupViewModel.WarmupAction.Event,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onReveal: () -> Unit
) {
    val isGiftEvent = event.eventType == WarmupViewModel.EventType.GIFT
    val giftPhase = event.giftPhase

    // Animación de pulso de alarma estroboscópica
    val infiniteTransition = rememberInfiniteTransition(label = "strobeAlert")
    val strobeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "strobeAlpha"
    )
    val targetPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "targetPulseScale"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .shadow(32.dp, RoundedCornerShape(28.dp), spotColor = event.color)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF160E26), Color(0xFF0C0816))
                    )
                )
                .border(2.dp, event.color.copy(alpha = strobeAlpha), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. BANNER SUPERIOR DE FRANJAS DE PELIGRO (HAZARD STRIPES)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    HazardStripesBanner(
                        accentColor = event.color,
                        modifier = Modifier.fillMaxSize()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.event_hazard_alert),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 2. ORBE DE ICONO CENTRAL
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .shadow(24.dp, CircleShape, spotColor = event.color)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(event.color.copy(alpha = 0.45f), CyberSurfaceElevated)
                                )
                            )
                            .border(2.dp, event.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(getGameVectorForEmoji(event.emoji)),
                            contentDescription = null,
                            tint = event.color,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge de Misión
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(event.color.copy(alpha = 0.20f))
                            .border(1.dp, event.color.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isGiftEvent && giftPhase == WarmupViewModel.GiftPhase.RAISE_HAND)
                                stringResource(R.string.event_label_prep)
                            else
                                stringResource(R.string.event_rules_header),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = event.color,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(event.titleRes),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // 3. TARGET ACQUIRED (RETÍCULA DE BLANCO CIBERNÉTICO)
                    if (event.selectedPlayer != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1B1429))
                                .border(1.2.dp, event.color.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val crossLen = 10.dp.toPx()
                                val crossColor = event.color.copy(alpha = 0.7f)
                                drawLine(crossColor, Offset(8f, 8f), Offset(8f + crossLen, 8f), 2f)
                                drawLine(crossColor, Offset(8f, 8f), Offset(8f, 8f + crossLen), 2f)
                                drawLine(crossColor, Offset(size.width - 8f, size.height - 8f), Offset(size.width - 8f - crossLen, size.height - 8f), 2f)
                                drawLine(crossColor, Offset(size.width - 8f, size.height - 8f), Offset(size.width - 8f, size.height - 8f - crossLen), 2f)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .scale(targetPulseScale)
                                        .clip(CircleShape)
                                        .background(event.color.copy(alpha = 0.25f))
                                        .border(2.dp, event.color, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = event.selectedPlayer.avatarEmoji, fontSize = 28.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(event.color)
                                        )
                                        Text(
                                            text = stringResource(R.string.event_target_acquired),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = event.color,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = event.selectedPlayer.name,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = stringResource(R.string.event_target_label),
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. DOSSIER DE LA MISIÓN
                    val descriptionText = if (event.descriptionArgs.isNotEmpty()) {
                        stringResource(event.descriptionRes, *event.descriptionArgs.toTypedArray())
                    } else {
                        stringResource(event.descriptionRes)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = descriptionText,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = stringResource(event.instructionRes),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.72f),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. CAJA DE PENITENCIA INTEGRADA AL COLOR TEMÁTICO
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(event.color.copy(alpha = 0.12f))
                            .border(1.dp, event.color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "💀 " + stringResource(R.string.warmup_penalty_fmt, event.penaltyDrinks),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 6. ESTACIÓN DE ACCIONES ARCADE SIMÉTRICAS
                    if (isGiftEvent && giftPhase == WarmupViewModel.GiftPhase.RAISE_HAND) {
                        TactileButton(
                            onClick = onReveal,
                            containerColor = event.color,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.event_btn_reveal).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    } else if (isGiftEvent && giftPhase == WarmupViewModel.GiftPhase.REVEAL) {
                        TactileButton(
                            onClick = onAccept,
                            containerColor = event.color,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.event_btn_continue).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Botón de Rendición / Castigo (Simétrico weight 1f, alto contraste)
                            Button(
                                onClick = onReject,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF38121A),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.5.dp, AccentRed.copy(alpha = 0.75f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_game_skull),
                                        contentDescription = null,
                                        tint = AccentRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = stringResource(R.string.event_btn_surrender),
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.event_btn_reject_sub),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentRed
                                        )
                                    }
                                }
                            }

                            // Botón de Victoria / Reto Aceptado (Simétrico weight 1f)
                            TactileButton(
                                onClick = onAccept,
                                containerColor = event.color,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_game_cheers),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = stringResource(R.string.event_btn_victory),
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.event_btn_accept_sub),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 6. FINISHED CONTENT (PODIO DE LA FIESTA)
// ============================================================================

@Composable
fun FinishedContent(
    stats: Map<PlayerModel, Int>,
    onReset: () -> Unit
) {
    val maxDrinks = stats.values.maxOrNull() ?: 0
    val minDrinks = stats.values.minOrNull() ?: 0

    val mostDrunk = stats.filterValues { it == maxDrinks }.keys.firstOrNull()
    val leastDrunk = stats.filterValues { it == minDrinks }.keys.firstOrNull()
    val isDraw = mostDrunk == leastDrunk || stats.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(28.dp, CircleShape, spotColor = SolarAmber)
                .clip(CircleShape)
                .background(SolarAmber.copy(alpha = 0.25f))
                .border(2.dp, SolarAmber, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AnimatedPulseEmoji(emoji = "🏆", baseSize = 52)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.end_summary_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))

        if (!isDraw && mostDrunk != null && leastDrunk != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NeoWinnerPodiumCard(
                    player = mostDrunk,
                    count = stats[mostDrunk] ?: 0,
                    label = stringResource(R.string.end_most_drunk),
                    badgeEmoji = "👑",
                    color = AccentRed
                )
                NeoWinnerPodiumCard(
                    player = leastDrunk,
                    count = stats[leastDrunk] ?: 0,
                    label = stringResource(R.string.end_least_drunk),
                    badgeEmoji = "😇",
                    color = ToxicGreen
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            NeoGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SolarAmber,
                glowColor = SolarAmber.copy(alpha = 0.35f),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.end_punishment_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = SolarAmber,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.end_punishment_desc, mostDrunk.name, leastDrunk.name),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            NeoGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = Color.White.copy(alpha = 0.2f),
                cornerRadius = 20.dp
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.end_punishment_draw),
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(34.dp))

        TactileButton(
            onClick = onReset,
            containerColor = SolarAmber,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.warmup_back_home_btn),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun NeoWinnerPodiumCard(
    player: PlayerModel,
    count: Int,
    label: String,
    badgeEmoji: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(16.dp, CircleShape, spotColor = color)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = player.avatarEmoji, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$badgeEmoji $label",
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
        Text(
            text = player.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = "$count 🍺",
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}

// ============================================================================
// 7. DIÁLOGO DE REGLAS
// ============================================================================

@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        NeoGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(4.dp),
            borderColor = SolarAmber,
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.rules_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = SolarAmber
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    NeoRuleItem("1️⃣", stringResource(R.string.rule_1_title), stringResource(R.string.rule_1_desc))
                    NeoRuleItem("2️⃣", stringResource(R.string.rule_2_title), stringResource(R.string.rule_2_desc))
                    NeoRuleItem("3️⃣", stringResource(R.string.rule_3_title), stringResource(R.string.rule_3_desc))
                    NeoRuleItem("4️⃣", stringResource(R.string.rule_4_title), stringResource(R.string.rule_4_desc))
                }

                Spacer(modifier = Modifier.height(20.dp))

                TactileButton(
                    onClick = onDismiss,
                    containerColor = SolarAmber,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.rules_understood),
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun NeoRuleItem(emoji: String, title: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Text(text = description, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
        }
    }
}

// ============================================================================
// 8. DIÁLOGO DE ESTADÍSTICAS Y RECUENTO DE TRAGOS
// ============================================================================

@Composable
fun StatsDialog(
    players: List<PlayerModel>,
    stats: Map<String, Int>,
    onDismiss: () -> Unit,
    onAddDrink: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        NeoGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(4.dp),
            borderColor = SolarAmber,
            glowColor = SolarAmber.copy(alpha = 0.3f),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.stats_title),
                        color = SolarAmber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.stats_total_fmt, stats.values.sum()),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (players.isEmpty()) {
                    Text(
                        text = stringResource(R.string.stats_no_players),
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(weight = 1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        players.sortedByDescending { stats[it.id] ?: 0 }.forEachIndexed { index, player ->
                            val drinks = stats[player.id] ?: 0
                            val isLeader = index == 0 && drinks > 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isLeader) SolarAmber.copy(alpha = 0.15f) else CyberSurfaceElevated)
                                    .border(1.dp, if (isLeader) SolarAmber.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(text = player.avatarEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = player.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1
                                            )
                                            if (isLeader) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "👑", fontSize = 12.sp)
                                            }
                                        }
                                        if (drinks > 0) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .height(4.dp)
                                                    .width((drinks * 8).coerceAtMost(100).dp)
                                                    .clip(CircleShape)
                                                    .background(SolarAmber)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$drinks 🍺",
                                        color = SolarAmber,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )

                                    IconButton(
                                        onClick = { onAddDrink(player.id) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SolarAmber.copy(alpha = 0.2f))
                                            .border(1.dp, SolarAmber.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = stringResource(R.string.stats_add_drink),
                                            tint = SolarAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TactileButton(
                    onClick = onDismiss,
                    containerColor = SolarAmber,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stats_close),
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

// ============================================================================
// 9. TUTORIAL OVERLAY
// ============================================================================

private data class TutorialStep(
    val targetBounds: Rect?,
    val titleRes: Int,
    val descriptionRes: Int,
    val emoji: String
)

@Composable
fun TutorialOverlay(
    onDismiss: () -> Unit,
    statsButtonBounds: Rect?,
    helpButtonBounds: Rect?,
    mainContentBounds: Rect?
) {
    val density = LocalDensity.current
    var stepIndex by remember { mutableStateOf(0) }

    val steps = remember(statsButtonBounds, helpButtonBounds, mainContentBounds) {
        listOf(
            TutorialStep(
                targetBounds = mainContentBounds,
                titleRes = R.string.tutorial_step1_title,
                descriptionRes = R.string.tutorial_step1_desc,
                emoji = "🎯"
            ),
            TutorialStep(
                targetBounds = mainContentBounds,
                titleRes = R.string.tutorial_step2_title,
                descriptionRes = R.string.tutorial_step2_desc,
                emoji = "👉"
            ),
            TutorialStep(
                targetBounds = statsButtonBounds,
                titleRes = R.string.tutorial_step3_title,
                descriptionRes = R.string.tutorial_step3_desc,
                emoji = "📊"
            ),
            TutorialStep(
                targetBounds = helpButtonBounds,
                titleRes = R.string.tutorial_step4_title,
                descriptionRes = R.string.tutorial_step4_desc,
                emoji = "💡"
            )
        )
    }

    val currentStep = steps.getOrNull(stepIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
    ) {
        currentStep?.targetBounds?.let { bounds ->
            val highlightPadding = 8.dp
            val left = with(density) { bounds.left.toDp() } - highlightPadding
            val top = with(density) { bounds.top.toDp() } - highlightPadding
            val width = with(density) { bounds.width.toDp() } + highlightPadding * 2
            val height = with(density) { bounds.height.toDp() } + highlightPadding * 2

            Box(
                modifier = Modifier
                    .offset(x = left, y = top)
                    .size(width = width, height = height)
                    .border(2.dp, SolarAmber, RoundedCornerShape(20.dp))
                    .background(SolarAmber.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            )
        }

        currentStep?.let { step ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NeoGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = SolarAmber,
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = step.emoji, fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(step.titleRes),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = SolarAmber,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(step.descriptionRes),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == stepIndex) 9.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == stepIndex) SolarAmber else Color.White.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.tutorial_skip),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TactileButton(
                        onClick = {
                            if (stepIndex < steps.lastIndex) stepIndex++ else onDismiss()
                        },
                        containerColor = SolarAmber,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (stepIndex < steps.lastIndex)
                                stringResource(R.string.tutorial_next)
                            else
                                stringResource(R.string.tutorial_finish),
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedPulseEmoji(emoji: String, baseSize: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseEmoji")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    Text(text = emoji, fontSize = (baseSize * scale).sp)
}