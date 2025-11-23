package com.example.kampai.ui.theme.staring

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.domain.models.PlayerModel
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import com.example.kampai.ui.theme.AccentCyan
import com.example.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun StaringGameScreen(
    viewModel: StaringViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val count by viewModel.count.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val selectedPlayers by viewModel.selectedPlayers.collectAsState()

    // Obtener dimensiones de pantalla para responsividad
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Pasar jugadores al ViewModel
    LaunchedEffect(players) {
        viewModel.setPlayers(players)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fondo animado
        StaringBackground(state = state)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            StaringHeader(
                onBack = onBack,
                onReset = { viewModel.reset() },
                screenWidth = screenWidth
            )

            // Contenedor Principal con Peso y Centrado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is StaringViewModel.GameState.Idle -> {
                        IdleContent(
                            players = selectedPlayers,
                            hasPlayers = players.isNotEmpty(),
                            onStart = { viewModel.startDuel() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is StaringViewModel.GameState.Counting -> {
                        CountingContent(
                            count = count,
                            players = selectedPlayers,
                            screenWidth = screenWidth
                        )
                    }
                    is StaringViewModel.GameState.Fight -> {
                        FightContent(
                            players = selectedPlayers,
                            hasPlayers = players.isNotEmpty(),
                            screenWidth = screenWidth
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StaringBackground(state: StaringViewModel.GameState) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            is StaringViewModel.GameState.Fight -> AccentCyan.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .rotate(offset)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryViolet.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(350.dp)
                .rotate(-offset)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun StaringHeader(
    onBack: () -> Unit,
    onReset: () -> Unit,
    screenWidth: Dp
) {
    val headerPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 24f).sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = headerPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onSurface)
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👁️ Duelo de Miradas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = titleSize
                ),
                color = AccentCyan
            )
            Text(
                text = "No parpadees",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }

        IconButton(
            onClick = onReset,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Reiniciar",
                tint = AccentCyan
            )
        }
    }
}

@Composable
fun IdleContent(
    players: List<PlayerModel>,
    hasPlayers: Boolean,
    onStart: () -> Unit,
    screenHeight: Dp,
    screenWidth: Dp
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

    val emojiSize = (screenWidth * 0.25f).value.coerceIn(60f, 100f).sp
    val contentPadding = (screenWidth * 0.06f).coerceIn(20.dp, 32.dp)
    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)

    // Box para centrar contenido si es poco, pero permitir scroll si es mucho
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()) // Scroll para pantallas pequeñas
                .padding(contentPadding)
                .scale(scale)
        ) {
            // Emoji animado con efecto de ojos
            val infiniteTransition = rememberInfiniteTransition(label = "eyes")
            val eyeScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "eyeScale"
            )

            Text(
                text = "👁️👁️",
                fontSize = emojiSize,
                modifier = Modifier.scale(eyeScale)
            )

            Spacer(modifier = Modifier.height(contentPadding))

            // Mostrar jugadores seleccionados o mensaje general
            if (hasPlayers && players.size == 2) {
                PlayersDuelCard(players = players)
            } else {
                GeneralDuelCard()
            }

            Spacer(modifier = Modifier.height(contentPadding))

            // Instrucciones
            InstructionsCard()

            Spacer(modifier = Modifier.height(contentPadding * 1.5f))

            // Botón de inicio
            StartDuelButton(
                onClick = onStart,
                height = buttonHeight,
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
fun PlayersDuelCard(players: List<PlayerModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DUELISTAS",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black
                ),
                color = AccentCyan
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Jugador 1
            PlayerDuelChip(player = players[0])

            Spacer(modifier = Modifier.height(8.dp))

            // VS animado
            VsAnimatedSeparator()

            Spacer(modifier = Modifier.height(8.dp))

            // Jugador 2
            PlayerDuelChip(player = players[1])
        }
    }
}

@Composable
fun PlayerDuelChip(player: PlayerModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        player.getAvatarColor().copy(alpha = 0.3f),
                        player.getAvatarColor().copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = player.getAvatarColor().copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            player.getAvatarColor().copy(alpha = 0.8f),
                            player.getAvatarColor().copy(alpha = 0.4f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.getDisplayEmoji(),
                fontSize = 28.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nombre
        Text(
            text = player.name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black
            ),
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        // Emoji de ojo
        Text(text = "👁️", fontSize = 24.sp)
    }
}

@Composable
fun VsAnimatedSeparator() {
    val infiniteTransition = rememberInfiniteTransition(label = "vs")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .rotate(rotation)
                .clip(CircleShape)
                .background(AccentCyan.copy(alpha = 0.2f))
                .border(1.dp, AccentCyan.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "VS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black
                ),
                color = AccentCyan,
                modifier = Modifier.rotate(-rotation)
            )
        }
    }
}

@Composable
fun GeneralDuelCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Elijan dos oponentes",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Dos personas deben mirarse fijamente sin pestañear",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InstructionsCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📋 Reglas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            RuleItem(emoji = "👁️", text = "Mírense fijamente")
            Spacer(modifier = Modifier.height(4.dp))
            RuleItem(emoji = "🚫", text = "Sin pestañear ni reír")
            Spacer(modifier = Modifier.height(4.dp))
            RuleItem(emoji = "🍺", text = "El primero que falle bebe")
        }
    }
}

@Composable
fun RuleItem(emoji: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun StartDuelButton(
    onClick: () -> Unit,
    height: Dp,
    screenWidth: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    val fontSize = (screenWidth * 0.05f).value.coerceIn(16f, 22f).sp

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
                ambientColor = AccentCyan,
                spotColor = AccentCyan
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AccentCyan,
                        Color(0xFF0891B2)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚔️", fontSize = fontSize * 1.2f)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "¡Iniciar Duelo!",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun CountingContent(
    count: Int,
    players: List<PlayerModel>,
    screenWidth: Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "count")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "countScale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "countRot"
    )

    // Dimensiones responsivas para el círculo
    val circleSize = (screenWidth * 0.55f).coerceIn(180.dp, 280.dp)
    val fontSize = (circleSize.value * 0.5f).sp

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Mostrar jugadores en cuenta regresiva
            if (players.size == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerCountdownChip(player = players[0])
                    Text(text = "⚔️", fontSize = 32.sp)
                    PlayerCountdownChip(player = players[1])
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = "Prepárense...",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Círculo de cuenta regresiva RESPONSIVO
            Box(
                modifier = Modifier.size(circleSize),
                contentAlignment = Alignment.Center
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(circleSize - (index * 30).dp)
                            .clip(CircleShape)
                            .border(
                                width = (3 - index).dp,
                                color = AccentCyan.copy(alpha = 0.3f - index * 0.1f),
                                shape = CircleShape
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(circleSize * 0.7f)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AccentCyan.copy(alpha = 0.4f),
                                    AccentCyan.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(3.dp, AccentCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = fontSize
                        ),
                        color = AccentCyan,
                        modifier = Modifier
                            .scale(scale)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¡MÍRENSE FIJAMENTE!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = AccentCyan.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PlayerCountdownChip(player: PlayerModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(player.getAvatarColor().copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = player.getDisplayEmoji(), fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = player.name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
fun FightContent(
    players: List<PlayerModel>,
    hasPlayers: Boolean,
    screenWidth: Dp
) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 180f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Tamaño de fuente dinámico
    val yaSize = (screenWidth * 0.2f).value.coerceIn(48f, 80f).sp

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .scale(scale)
                .graphicsLayer { rotationX = rotation }
        ) {
            // Lightning effect
            val infiniteTransition = rememberInfiniteTransition(label = "lightning")
            val lightningScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "lightningScale"
            )

            Text(
                text = "⚡ ¡YA! ⚡",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = yaSize
                ),
                color = AccentCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(lightningScale)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mostrar vs de jugadores
            if (hasPlayers && players.size == 2) {
                FightPlayersCard(players = players)
            } else {
                FightGeneralCard()
            }
        }
    }
}

@Composable
fun FightPlayersCard(players: List<PlayerModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = AccentCyan.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = players[0].getDisplayEmoji(), fontSize = 40.sp)
                    Text(text = players[0].name, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(text = "VS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = AccentCyan)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = players[1].getDisplayEmoji(), fontSize = 40.sp)
                    Text(text = players[1].name, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¡El primero en pestañear o reír BEBE! 🍺",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AccentCyan,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FightGeneralCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = AccentCyan.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Mírense a los ojos!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "El primero en pestañear o reír: ¡BEBE! 🍺",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black
                ),
                color = AccentCyan,
                textAlign = TextAlign.Center
            )
        }
    }
}