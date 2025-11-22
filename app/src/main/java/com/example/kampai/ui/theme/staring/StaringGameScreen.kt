package com.example.kampai.ui.theme.staring

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            StaringHeader(
                onBack = onBack,
                onReset = { viewModel.reset() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is StaringViewModel.GameState.Idle -> {
                        IdleContent(
                            players = selectedPlayers,
                            hasPlayers = players.isNotEmpty(),
                            onStart = { viewModel.startDuel() }
                        )
                    }
                    is StaringViewModel.GameState.Counting -> {
                        CountingContent(
                            count = count,
                            players = selectedPlayers
                        )
                    }
                    is StaringViewModel.GameState.Fight -> {
                        FightContent(
                            players = selectedPlayers,
                            hasPlayers = players.isNotEmpty()
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
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👁️ Duelo de Miradas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
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
    onStart: () -> Unit
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
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
            fontSize = 80.sp,
            modifier = Modifier.scale(eyeScale)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mostrar jugadores seleccionados o mensaje general
        if (hasPlayers && players.size == 2) {
            PlayersDuelCard(players = players)
        } else {
            GeneralDuelCard()
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Instrucciones
        InstructionsCard()

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de inicio
        StartDuelButton(onClick = onStart)
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
                text = "DUELISTAS SELECCIONADOS",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black
                ),
                color = AccentCyan
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Jugador 1
            PlayerDuelChip(player = players[0])

            Spacer(modifier = Modifier.height(16.dp))

            // VS animado
            VsAnimatedSeparator()

            Spacer(modifier = Modifier.height(16.dp))

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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
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
                text = player.gender.getEmoji(),
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Nombre
        Column {
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black
                ),
                color = Color.White
            )
            Text(
                text = "¡Prepárate!",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Emoji de ojo
        Text(text = "👁️", fontSize = 32.sp)
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

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AccentCyan,
                            Color.Transparent
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .rotate(rotation)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.8f),
                            AccentCyan.copy(alpha = 0.4f)
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "VS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black
                ),
                color = Color.White,
                modifier = Modifier.rotate(-rotation)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AccentCyan
                        )
                    )
                )
        )
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
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Elijan dos oponentes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dos personas deben mirarse fijamente sin pestañear",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
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
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📋 Reglas del Duelo",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            RuleItem(
                emoji = "👁️",
                text = "Mírense fijamente a los ojos"
            )
            Spacer(modifier = Modifier.height(8.dp))
            RuleItem(
                emoji = "🚫",
                text = "No pueden pestañear ni reír"
            )
            Spacer(modifier = Modifier.height(8.dp))
            RuleItem(
                emoji = "🍺",
                text = "El primero que falle: ¡BEBE!"
            )
        }
    }
}

@Composable
fun RuleItem(emoji: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.3f),
                            AccentCyan.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun StartDuelButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
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
            Text(text = "⚔️", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "¡Iniciar Duelo!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun CountingContent(count: Int, players: List<PlayerModel>) {
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
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

            Spacer(modifier = Modifier.height(48.dp))
        }

        Text(
            text = "Prepárense...",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Círculo de cuenta regresiva
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size((200 - index * 30).dp)
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
                    .size(140.dp)
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
                        fontSize = 100.sp
                    ),
                    color = AccentCyan,
                    modifier = Modifier
                        .scale(scale)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

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

@Composable
fun PlayerCountdownChip(player: PlayerModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = player.getAvatarColor().copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(player.getAvatarColor().copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.gender.getEmoji(), fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = player.name,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FightContent(players: List<PlayerModel>, hasPlayers: Boolean) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
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
                fontSize = 72.sp
            ),
            color = AccentCyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(lightningScale)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Mostrar vs de jugadores
        if (hasPlayers && players.size == 2) {
            FightPlayersCard(players = players)
        } else {
            FightGeneralCard()
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
            Text(
                text = "${players[0].name} 👁️ VS 👁️ ${players[1].name}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

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