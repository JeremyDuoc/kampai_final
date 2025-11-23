package com.example.kampai.ui.theme.kingscup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.ui.theme.AccentAmber
import com.example.kampai.ui.theme.AccentRed
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun KingsCupGameScreen(
    viewModel: KingsCupViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val showRulesDialog by viewModel.showRulesDialog.collectAsState()
    val currentCard by viewModel.currentCard.collectAsState()
    val kingsDrawn by viewModel.kingsDrawn.collectAsState()
    val cardsRemaining by viewModel.cardsRemaining.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val currentPlayer = viewModel.getCurrentPlayer()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(players) {
        viewModel.setPlayers(players)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KingsCupBackground(kingsDrawn = kingsDrawn)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. NUEVO HEADER UNIFICADO
            GameStatsHeader(
                onBack = onBack,
                onReset = { viewModel.reset() },
                cardsRemaining = cardsRemaining,
                kingsDrawn = kingsDrawn,
                screenWidth = screenWidth
            )

            // 2. ÁREA DE JUEGO
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (gameState) {
                    is KingsCupViewModel.GameState.Idle -> {
                        IdleContent(
                            currentPlayer = currentPlayer,
                            onDrawCard = { viewModel.drawCard() },
                            onShowRules = { viewModel.showRules() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is KingsCupViewModel.GameState.Drawing -> {
                        DrawingContent(screenWidth = screenWidth)
                    }
                    is KingsCupViewModel.GameState.ShowingCard -> {
                        val card = (gameState as KingsCupViewModel.GameState.ShowingCard).card
                        ShowingCardContent(
                            card = card,
                            currentPlayer = currentPlayer,
                            onNext = { viewModel.nextTurn() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is KingsCupViewModel.GameState.Finished -> {
                        FinishedContent(
                            kingsDrawn = kingsDrawn,
                            onReset = { viewModel.reset() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                }
            }
        }

        // DIÁLOGO DE REGLAS
        if (showRulesDialog) {
            KingsCupRulesDialog(
                onDismiss = { viewModel.hideRules() },
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
fun KingsCupBackground(kingsDrawn: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val bgColor by animateColorAsState(
        targetValue = when (kingsDrawn) {
            4 -> AccentRed.copy(alpha = 0.2f)
            3 -> AccentRed.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(350.dp)
                .rotate(offset1)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentAmber.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun GameStatsHeader(
    onBack: () -> Unit,
    onReset: () -> Unit,
    cardsRemaining: Int,
    kingsDrawn: Int,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val iconSize = 44.dp
    val barGradient = Brush.horizontalGradient(
        colors = listOf(AccentAmber, Color(0xFFFF8C00), AccentRed)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(bottom = 12.dp)
    ) {
        // Fila Superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(iconSize)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Filled.ArrowBack, "Atrás", tint = MaterialTheme.colorScheme.onSurface)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "KING'S CUP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = AccentAmber
                )
                Text(
                    text = "$cardsRemaining cartas restantes",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(iconSize)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Filled.Refresh, "Reiniciar", tint = AccentAmber)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila Inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Nivel del Vaso Central",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$kingsDrawn",
                        color = if (kingsDrawn == 3) AccentRed else AccentAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "/4 👑",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(kingsDrawn / 4f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(barGradient)
                )
            }
        }
    }
}

@Composable
fun IdleContent(
    currentPlayer: com.example.kampai.domain.models.PlayerModel?,
    onDrawCard: () -> Unit,
    onShowRules: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.06f).coerceIn(20.dp, 32.dp)
    val buttonHeight = (screenHeight * 0.09f).coerceIn(60.dp, 80.dp)

    // Usamos Box para centrar si el contenido es pequeño, pero permitimos scroll si es grande
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() // Cambiado de fillMaxSize para que Box controle la altura en pantallas grandes
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.scale(scale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentPlayer != null) {
                    CurrentPlayerCard(
                        player = currentPlayer,
                        screenWidth = screenWidth
                    )
                }

                Spacer(modifier = Modifier.height(contentPadding * 1.5f))

                AnimatedDeck3D(screenWidth = screenWidth)

                Spacer(modifier = Modifier.height(contentPadding * 2f))

                DrawCardButton(
                    onClick = onDrawCard,
                    height = buttonHeight,
                    screenWidth = screenWidth
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onShowRules,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ver Guía de Cartas",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun ShowingCardContent(
    card: KingsCupViewModel.Card,
    currentPlayer: com.example.kampai.domain.models.PlayerModel?,
    onNext: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }

    val rotationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "flip"
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    LaunchedEffect(card) {
        isVisible = false
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 28.dp)
    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)

    // Corrección: Box centrado + Scroll. NO usamos weight aquí.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Espaciado automático
        ) {
            if (currentPlayer != null) {
                CurrentPlayerCard(player = currentPlayer, screenWidth = screenWidth)
            }

            Column(
                modifier = Modifier.scale(scale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayingCard(
                    card = card,
                    rotationY = rotationY,
                    screenWidth = screenWidth
                )

                Spacer(modifier = Modifier.height(24.dp))

                RuleCard(card = card, screenWidth = screenWidth)
            }

            // Espaciador final para asegurar que el botón no quede pegado en pantallas pequeñas
            Spacer(modifier = Modifier.height(16.dp))

            ResponsiveButton(
                text = "Siguiente Turno →",
                onClick = onNext,
                height = buttonHeight,
                color = card.suit.color
            )
        }
    }
}

// ... (CurrentPlayerCard, AnimatedDeck3D, DrawCardButton, DrawingContent, PlayingCard - se mantienen igual)
@Composable
fun CurrentPlayerCard(
    player: com.example.kampai.domain.models.PlayerModel,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val fontSize = (screenWidth * 0.045f).value.coerceIn(16f, 22f).sp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        border = BorderStroke(1.dp, player.getAvatarColor().copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            player.getAvatarColor().copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(player.getAvatarColor().copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.getDisplayEmoji(), fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Es el turno de",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = player.name,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AnimatedDeck3D(screenWidth: androidx.compose.ui.unit.Dp) {
    val cardWidth = (screenWidth * 0.5f).coerceIn(140.dp, 200.dp)
    val cardHeight = cardWidth * 1.4f

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.offset(y = floatOffset.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = 16.dp)
                .scale(0.9f)
                .width(cardWidth)
                .height(cardHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF8B4513).copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .offset(y = 8.dp)
                .scale(0.95f)
                .width(cardWidth)
                .height(cardHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFB45309).copy(alpha = 0.7f))
        )
        Card(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .shadow(20.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AccentAmber, Color(0xFFD97706)),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .border(4.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👑", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "KING'S\nCUP",
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DrawCardButton(onClick: () -> Unit, height: androidx.compose.ui.unit.Dp, screenWidth: androidx.compose.ui.unit.Dp) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "buttonScale")
    val fontSize = (screenWidth * 0.05f).value.coerceIn(18f, 24f).sp

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().height(height).scale(scale)
            .shadow(20.dp, RoundedCornerShape(20.dp), ambientColor = AccentAmber, spotColor = AccentAmber)
            .background(Brush.horizontalGradient(colors = listOf(AccentAmber, Color(0xFFF59E0B))), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🃏", fontSize = fontSize * 1.3f)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Sacar Carta", fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun DrawingContent(screenWidth: androidx.compose.ui.unit.Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "drawing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation"
    )
    val fontSize = (screenWidth * 0.06f).value.coerceIn(20f, 32f).sp

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🃏", fontSize = (screenWidth * 0.25f).value.coerceIn(80f, 140f).sp, modifier = Modifier.rotate(rotation))
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Sacando carta...", fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun PlayingCard(card: KingsCupViewModel.Card, rotationY: Float, screenWidth: androidx.compose.ui.unit.Dp) {
    val cardWidth = (screenWidth * 0.55f).coerceIn(160.dp, 220.dp)
    val cardHeight = cardWidth * 1.4f

    Card(
        modifier = Modifier.width(cardWidth).height(cardHeight).graphicsLayer { this.rotationY = rotationY }
            .shadow(32.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = card.value.display, fontSize = (cardWidth.value * 0.25f).sp, fontWeight = FontWeight.Black, color = card.suit.color)
                    Text(text = card.suit.symbol, fontSize = (cardWidth.value * 0.25f).sp, color = card.suit.color)
                }
                Text(text = card.suit.symbol, fontSize = (cardWidth.value * 0.5f).sp, color = card.suit.color, modifier = Modifier.align(Alignment.CenterHorizontally))
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth().rotate(180f)) {
                    Text(text = card.value.display, fontSize = (cardWidth.value * 0.25f).sp, fontWeight = FontWeight.Black, color = card.suit.color)
                    Text(text = card.suit.symbol, fontSize = (cardWidth.value * 0.25f).sp, color = card.suit.color)
                }
            }
        }
    }
}

@Composable
fun RuleCard(
    card: KingsCupViewModel.Card,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 26f).sp
    val bodySize = (screenWidth * 0.04f).value.coerceIn(14f, 18f).sp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = card.suit.color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = card.value.emoji,
                fontSize = (screenWidth * 0.15f).value.coerceIn(48f, 72f).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = card.value.rule,
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = card.customDescription ?: card.value.description,
                fontSize = bodySize,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                lineHeight = (bodySize.value * 1.4f).sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FinishedContent(
    kingsDrawn: Int,
    onReset: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.5f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.06f).coerceIn(20.dp, 32.dp)
    val emojiSize = (screenWidth * 0.3f).value.coerceIn(100f, 160f).sp
    val titleSize = (screenWidth * 0.07f).value.coerceIn(24f, 40f).sp
    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)

    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(modifier = Modifier.scale(scale), horizontalAlignment = Alignment.CenterHorizontally) {
            val infiniteTransition = rememberInfiniteTransition(label = "trophy")
            val trophyScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.15f, animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse), label = "trophy")

            Text(text = if (kingsDrawn == 4) "🏆" else "👑", fontSize = emojiSize, modifier = Modifier.scale(trophyScale))
            Spacer(modifier = Modifier.height(contentPadding))
            Text(
                text = if (kingsDrawn == 4) "¡Cuarto Rey!\n¡BEBE EL VASO!" else "¡Juego\nTerminado!",
                fontSize = titleSize, fontWeight = FontWeight.Black,
                color = if (kingsDrawn == 4) AccentRed else Color.White,
                textAlign = TextAlign.Center, lineHeight = (titleSize.value * 1.2f).sp
            )
            Spacer(modifier = Modifier.height(contentPadding * 2f))
            ResponsiveButton(text = "🔄 Nueva Partida", onClick = onReset, height = buttonHeight, color = AccentAmber)
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

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val fontSize = (screenWidth * 0.045f).value.coerceIn(16f, 22f).sp

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
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

@Composable
fun KingsCupRulesDialog(
    onDismiss: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val dialogPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
        val titleSize = (screenWidth * 0.06f).value.coerceIn(20f, 28f).sp
        val bodySize = (screenWidth * 0.04f).value.coerceIn(14f, 16f).sp

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .padding(dialogPadding)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = AccentAmber.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(dialogPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 Guía de Cartas",
                        fontSize = titleSize,
                        fontWeight = FontWeight.Bold,
                        color = AccentAmber
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Gray
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )

                // Contenido scrolleable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Objetivo: Evitar sacar el 4º Rey.",
                        fontSize = bodySize,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    KingsCupRuleItem("🅰️", "As - Cascada", "Todos beben hasta que el que sacó la carta, se detenga.", bodySize)
                    KingsCupRuleItem("2️⃣", "Dos - Tú", "Elige a una persona para que beba.", bodySize)
                    KingsCupRuleItem("3️⃣", "Tres - Yo", "Tú bebes.", bodySize)
                    KingsCupRuleItem("4️⃣", "Cuatro - Chicas", "Todas las mujeres beben.", bodySize)
                    KingsCupRuleItem("5️⃣", "Cinco - Pulgar", "Pon tu pulgar en la mesa. El último en hacerlo bebe.", bodySize)
                    KingsCupRuleItem("6️⃣", "Seis - Chicos", "Todos los hombres beben.", bodySize)
                    KingsCupRuleItem("7️⃣", "Siete - Cielo", "Levanta la mano. El último en hacerlo bebe.", bodySize)
                    KingsCupRuleItem("8️⃣", "Ocho - Compañero", "Elige a alguien. Cada vez que tú bebas, él/ella también bebe.", bodySize)
                    KingsCupRuleItem("9️⃣", "Nueve - Rima", "Di una palabra. Todos deben decir una rima. El que falle o repita, bebe.", bodySize)
                    KingsCupRuleItem("🔟", "Diez - Categoría", "Ej: Marcas de coches. El que se quede en blanco, bebe.", bodySize)
                    KingsCupRuleItem("🤴", "Jota - Regla", "Inventa una regla (ej: beber con la zurda). Quien la rompa, bebe.", bodySize)
                    KingsCupRuleItem("👸", "Reina - Pregunta", "Haz preguntas a otros. Si responden 'sí' o 'no', o no responden con otra pregunta, beben.", bodySize)

                    // REGLAS DE REY ACTUALIZADAS EN EL DIÁLOGO
                    KingsCupRuleItem("👑", "1º Rey", "Elige QUÉ lleva el vaso central. Es decir, prepara el trago.", bodySize)
                    KingsCupRuleItem("👑", "2º Rey", "Elige DÓNDE se va a beber (ej: en la mesa).", bodySize)
                    KingsCupRuleItem("👑", "3º Rey", "Elige CÓMO se va a beber (ej: sin manos).", bodySize)
                    KingsCupRuleItem("☠️", "4º Rey", "Bebe el vaso cumpliendo todo lo anterior.", bodySize)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentAmber
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "¡Entendido!",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun KingsCupRuleItem(emoji: String, title: String, description: String, fontSize: androidx.compose.ui.unit.TextUnit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Text(text = emoji, fontSize = fontSize * 1.2f, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = fontSize, fontWeight = FontWeight.Bold, color = AccentAmber)
            Text(text = description, fontSize = fontSize * 0.9f, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = fontSize * 1.3f)
        }
    }
}