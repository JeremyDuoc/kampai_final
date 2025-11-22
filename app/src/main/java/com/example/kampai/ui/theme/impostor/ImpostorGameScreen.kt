package com.example.kampai.ui.theme.impostor

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.domain.models.PlayerModel
import com.example.kampai.ui.theme.AccentAmber
import com.example.kampai.ui.theme.AccentRed
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import com.example.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun ImpostorGameScreen(
    viewModel: ImpostorViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val currentPlayerIndex by viewModel.currentPlayerIndex.collectAsState()

    LaunchedEffect(players) {
        viewModel.setPlayers(players)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ImpostorBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImpostorHeader(onBack = onBack)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (val state = gameState) {
                    is ImpostorViewModel.GameState.Setup -> {
                        SetupContent(
                            playerCount = players.size,
                            onStart = { viewModel.startGame() }
                        )
                    }
                    is ImpostorViewModel.GameState.ShowingWord -> {
                        ShowingWordContent(
                            player = players.getOrNull(currentPlayerIndex),
                            word = state.word,
                            isImpostor = state.isImpostor,
                            currentIndex = currentPlayerIndex,
                            totalPlayers = players.size,
                            onNext = { viewModel.nextPlayer() }
                        )
                    }
                    is ImpostorViewModel.GameState.GivingClues -> {
                        GivingCluesContent(
                            onStartVoting = { viewModel.startVoting() }
                        )
                    }
                    is ImpostorViewModel.GameState.Voting -> {
                        VotingContent(
                            players = players,
                            votedPlayers = viewModel.votedPlayers.collectAsState().value,
                            onVote = { viewModel.votePlayer(it) },
                            onShowResults = { viewModel.showResults() }
                        )
                    }
                    is ImpostorViewModel.GameState.Results -> {
                        ResultsContent(
                            impostorWon = state.impostorWon,
                            impostor = state.impostor,
                            realWord = state.realWord,
                            onReset = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImpostorBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentRed.copy(alpha = 0.25f),
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
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryViolet.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun ImpostorHeader(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🕵️ El Impostor",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = AccentRed
            )
            Text(
                text = "Encuentra al impostor",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SetupContent(playerCount: Int, onStart: () -> Unit) {
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
        Text(text = "🕵️", fontSize = 120.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Quién es el Impostor?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "• Todos reciben una palabra secreta\n• Excepto el impostor que no la conoce\n• Den pistas sin decir la palabra\n• El impostor debe fingir que sabe\n• Voten quién es el impostor",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Start,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (playerCount < 3) {
                    Text(
                        text = "⚠️ Se necesitan al menos 3 jugadores",
                        color = AccentRed,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "✓ $playerCount jugadores listos",
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            enabled = playerCount >= 3,
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Iniciar Juego",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ShowingWordContent(
    player: PlayerModel?,
    word: String,
    isImpostor: Boolean,
    currentIndex: Int,
    totalPlayers: Int,
    onNext: () -> Unit
) {
    var showWord by remember { mutableStateOf(false) }

    LaunchedEffect(player) {
        showWord = false
    }

    val scale by animateFloatAsState(
        targetValue = if (showWord) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Jugador ${currentIndex + 1} de $totalPlayers",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        player?.let {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                it.getAvatarColor().copy(alpha = 0.2f)
                            )
                        )
                    )
                    .border(2.dp, it.getAvatarColor().copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    it.getAvatarColor().copy(alpha = 0.6f),
                                    it.getAvatarColor().copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = it.gender.getEmoji(), fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = it.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!showWord) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .shadow(20.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.DarkGray
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔒", fontSize = 80.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Toca para ver\ntu palabra",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showWord = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver mi palabra", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .scale(scale)
                    .shadow(20.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    if (isImpostor) AccentRed.copy(alpha = 0.2f)
                                    else PrimaryViolet.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            color = if (isImpostor) AccentRed else PrimaryViolet,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isImpostor) {
                            Text(
                                text = "🕵️",
                                fontSize = 80.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "¡ERES EL IMPOSTOR!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = AccentRed,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No conoces la palabra\n¡Finge que sí!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = word,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 48.sp
                                ),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Esta es la palabra secreta",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (currentIndex < totalPlayers - 1) "Siguiente Jugador" else "Comenzar Ronda",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GivingCluesContent(onStartVoting: () -> Unit) {
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
        Text(text = "💭", fontSize = 120.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ronda de Pistas",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "• Den pistas por turnos\n• No digan la palabra directamente\n• El impostor debe fingir conocerla\n• Observen quién parece sospechoso",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Start,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartVoting,
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryPink),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Iniciar Votación",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VotingContent(
    players: List<PlayerModel>,
    votedPlayers: Map<String, Int>,
    onVote: (String) -> Unit,
    onShowResults: () -> Unit
) {
    val totalVotes = votedPlayers.values.sum()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "¿Quién es el impostor?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Votos totales: $totalVotes",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(players) { player ->
                PlayerVoteCard(
                    player = player,
                    votes = votedPlayers[player.id] ?: 0,
                    onVote = { onVote(player.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onShowResults,
            enabled = totalVotes > 0,
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Ver Resultados",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlayerVoteCard(
    player: PlayerModel,
    votes: Int,
    onVote: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVote),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            player.getAvatarColor().copy(alpha = 0.15f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = player.getAvatarColor().copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                player.getAvatarColor().copy(alpha = 0.6f),
                                player.getAvatarColor().copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.gender.getEmoji(), fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "$votes voto${if (votes != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SecondaryPink.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryPink
                )
            }
        }
    }
}

@Composable
fun ResultsContent(
    impostorWon: Boolean,
    impostor: PlayerModel,
    realWord: String,
    onReset: () -> Unit
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Text(
            text = if (impostorWon) "😈" else "🎉",
            fontSize = 140.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (impostorWon) "¡El Impostor Ganó!" else "¡Atraparon al Impostor!",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black
            ),
            color = if (impostorWon) AccentRed else Color.Green,
            textAlign = TextAlign.Center,
            lineHeight = 48.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                    text = "El impostor era:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        impostor.getAvatarColor().copy(alpha = 0.6f),
                                        impostor.getAvatarColor().copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = impostor.gender.getEmoji(), fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = impostor.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "La palabra era:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = realWord,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = PrimaryViolet
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (impostorWon) {
                    Text(
                        text = "¡Todos beben excepto el impostor!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AccentRed,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "¡El impostor bebe!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Green,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Jugar de Nuevo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}