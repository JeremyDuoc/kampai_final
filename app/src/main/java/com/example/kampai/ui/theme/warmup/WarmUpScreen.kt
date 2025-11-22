package com.example.kampai.ui.theme.warmup

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Close
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
    val players by partyViewModel.players.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(players) {
        viewModel.setPlayers(players)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WarmupBackground()

        // Contenido principal con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header fijo
            ResponsiveHeader(
                onBack = onBack,
                screenWidth = screenWidth
            )

            // Contenido scrolleable
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val state = gameState) {
                    is WarmupViewModel.GameState.Idle -> {
                        IdleContent(
                            onStart = { viewModel.startWarmup() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is WarmupViewModel.GameState.ShowingAction -> {
                        when (val action = state.action) {
                            is WarmupViewModel.WarmupAction.Phrase -> {
                                PhraseContent(
                                    phrase = action.text,
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
    }
}

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
        )
    }
}

@Composable
fun ResponsiveHeader(onBack: () -> Unit, screenWidth: androidx.compose.ui.unit.Dp) {
    val headerPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val iconSize = (screenWidth * 0.12f).coerceIn(40.dp, 56.dp)
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 26f).sp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.4f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = headerPadding, vertical = 12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(iconSize)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize * 0.5f)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔥 PartyMix",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF59E0B)
                )
                Text(
                    text = "Eventos aleatorios",
                    fontSize = (titleSize.value * 0.5f).sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun IdleContent(
    onStart: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.06f).coerceIn(20.dp, 32.dp)
    val emojiSize = (screenWidth * 0.25f).value.coerceIn(80f, 140f).sp
    val titleSize = (screenWidth * 0.065f).value.coerceIn(20f, 32f).sp
    val bodySize = (screenWidth * 0.04f).value.coerceIn(14f, 18f).sp
    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji animado
            val infiniteTransition = rememberInfiniteTransition(label = "emoji")
            val emojiRotation by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation"
            )

            Text(
                text = "🎯",
                fontSize = emojiSize,
                modifier = Modifier.graphicsLayer { rotationZ = emojiRotation }
            )

            Spacer(modifier = Modifier.height(contentPadding))

            // Tarjeta de información
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(contentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Modo PartyMix",
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(contentPadding * 0.5f))

                    Text(
                        text = "Diferentes rondas con frases y eventos aleatorios. ¡Un jugador será seleccionado para cada reto!",
                        fontSize = bodySize,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = (bodySize.value * 1.5f).sp
                    )

                    Spacer(modifier = Modifier.height(contentPadding * 0.75f))

                    // Características
                    ResponsiveFeature("✨", "Eventos sorpresa", bodySize)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResponsiveFeature("🎲", "Jugadores aleatorios", bodySize)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResponsiveFeature("🔥", "Retos intensos", bodySize)
                }
            }

            Spacer(modifier = Modifier.height(contentPadding * 1.5f))

            // Botón de inicio
            ResponsiveButton(
                text = "🔥 Comenzar Party",
                onClick = onStart,
                height = buttonHeight,
                color = Color(0xFFF59E0B)
            )
        }
    }
}

@Composable
fun ResponsiveFeature(emoji: String, text: String, textSize: androidx.compose.ui.unit.TextUnit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = textSize * 1.5f)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = textSize,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun PhraseContent(
    phrase: String,
    emoji: String,
    color: Color,
    currentRound: Int,
    totalRounds: Int,
    onNext: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    LaunchedEffect(phrase) {
        isVisible = false
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 28.dp)
    val emojiSize = (screenWidth * 0.22f).value.coerceIn(70f, 120f).sp
    val phraseSize = (screenWidth * 0.065f).value.coerceIn(20f, 36f).sp
    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Contador de rondas
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "Ronda $currentRound/$totalRounds",
                fontSize = (screenWidth * 0.035f).value.coerceIn(12f, 16f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Contenido central
        Column(
            modifier = Modifier.scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji pulsante
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val emojiScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "emojiScale"
            )

            Text(
                text = emoji,
                fontSize = emojiSize,
                modifier = Modifier.scale(emojiScale)
            )

            Spacer(modifier = Modifier.height(contentPadding * 1.5f))

            // Tarjeta con la frase
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    color.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = color.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(contentPadding * 1.2f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phrase,
                        fontSize = phraseSize,
                        fontWeight = FontWeight.Black,
                        lineHeight = (phraseSize.value * 1.3f).sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón siguiente
        ResponsiveButton(
            text = "Siguiente →",
            onClick = onNext,
            height = buttonHeight,
            color = color
        )
    }
}

@Composable
fun EventDialog(
    event: WarmupViewModel.WarmupAction.Event,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            val dialogPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
            val emojiSize = (screenWidth * 0.22f).value.coerceIn(70f, 110f).sp
            val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 26f).sp
            val bodySize = (screenWidth * 0.045f).value.coerceIn(16f, 22f).sp
            val labelSize = (screenWidth * 0.03f).value.coerceIn(11f, 14f).sp

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(dialogPadding)
                    .shadow(32.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A1A),
                                    event.color.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    event.color.copy(alpha = 0.8f),
                                    event.color.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(dialogPadding * 1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Botón cerrar
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = {
                                showDialog = false
                                onReject()
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Emoji pulsante
                    val infiniteTransition = rememberInfiniteTransition(label = "alert")
                    val alertScale by infiniteTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Text(
                        text = event.emoji,
                        fontSize = emojiSize,
                        modifier = Modifier.scale(alertScale)
                    )

                    Spacer(modifier = Modifier.height(dialogPadding))

                    // Etiqueta de evento
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = event.color.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "¡EVENTO ESPECIAL!",
                            fontSize = labelSize,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = event.color,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(dialogPadding * 0.75f))

                    // Título
                    Text(
                        text = event.title,
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(dialogPadding))

                    // Jugador seleccionado
                    if (event.selectedPlayer != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = event.selectedPlayer.getAvatarColor().copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dialogPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            event.selectedPlayer.getAvatarColor().copy(alpha = 0.7f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = event.selectedPlayer.gender.getEmoji(),
                                        fontSize = 28.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Jugador seleccionado:",
                                        fontSize = labelSize,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = event.selectedPlayer.name,
                                        fontSize = (titleSize.value * 0.8f).sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(dialogPadding))
                    }

                    // Descripción
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = event.description,
                            fontSize = bodySize,
                            fontWeight = FontWeight.Bold,
                            lineHeight = (bodySize.value * 1.4f).sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(dialogPadding)
                        )
                    }

                    Spacer(modifier = Modifier.height(dialogPadding * 0.75f))

                    // Instrucción
                    Text(
                        text = event.instruction,
                        fontSize = (bodySize.value * 0.8f).sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = ((bodySize.value * 0.8f) * 1.4f).sp
                    )

                    Spacer(modifier = Modifier.height(dialogPadding * 1.5f))

                    // Botones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                showDialog = false
                                onReject()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height((screenWidth * 0.14f).coerceIn(52.dp, 64.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Rechazar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (bodySize.value * 0.7f).sp
                                )
                                Text(
                                    "-2 tragos",
                                    fontSize = (bodySize.value * 0.55f).sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                showDialog = false
                                onAccept()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = event.color
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height((screenWidth * 0.14f).coerceIn(52.dp, 64.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Aceptar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (bodySize.value * 0.7f).sp
                                )
                                Text(
                                    "Reto",
                                    fontSize = (bodySize.value * 0.55f).sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinishedContent(
    onReset: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.06f).coerceIn(20.dp, 32.dp)
    val emojiSize = (screenWidth * 0.3f).value.coerceIn(100f, 160f).sp
    val titleSize = (screenWidth * 0.08f).value.coerceIn(28f, 48f).sp
    val bodySize = (screenWidth * 0.045f).value.coerceIn(14f, 18f).sp
    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji celebración
            val infiniteTransition = rememberInfiniteTransition(label = "celebration")
            val celebrationScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Text(
                text = "🎉",
                fontSize = emojiSize,
                modifier = Modifier.scale(celebrationScale)
            )

            Spacer(modifier = Modifier.height(contentPadding))

            Text(
                text = "¡PartyMix\nCompletado!",
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = (titleSize.value * 1.2f).sp
            )

            Spacer(modifier = Modifier.height(contentPadding * 0.75f))

            Text(
                text = "¡Esperamos que la hayan pasado increíble!",
                fontSize = bodySize,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                lineHeight = (bodySize.value * 1.5f).sp
            )

            Spacer(modifier = Modifier.height(contentPadding * 2f))

            ResponsiveButton(
                text = "🔄 Volver al Inicio",
                onClick = onReset,
                height = buttonHeight,
                color = Color(0xFFF59E0B)
            )
        }
    }
}

@Composable
fun ResponsiveButton(
    text: String,
    onClick: () -> Unit,
    height: androidx.compose.ui.unit.Dp,
    color: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val fontSize = (screenWidth * 0.045f).value.coerceIn(16f, 22f).sp

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = color,
                spotColor = color
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}