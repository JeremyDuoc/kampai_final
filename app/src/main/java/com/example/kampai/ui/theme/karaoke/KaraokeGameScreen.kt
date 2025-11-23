package com.example.kampai.ui.theme.karaoke

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
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
import com.example.kampai.domain.models.PlayerModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.ui.theme.AccentAmber
import com.example.kampai.ui.theme.AccentCyan
import com.example.kampai.ui.theme.AccentRed
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import com.example.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun KaraokeGameScreen(
    viewModel: KaraokeViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val votes by viewModel.votes.collectAsState()
    val players by partyViewModel.players.collectAsState()

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
        KaraokeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            KaraokeHeader(
                onBack = onBack,
                onReset = { viewModel.reset() },
                screenWidth = screenWidth
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val state = gameState) {
                    is KaraokeViewModel.GameState.Idle -> {
                        IdleContent(
                            hasPlayers = players.isNotEmpty(),
                            onStart = { viewModel.startRound() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is KaraokeViewModel.GameState.Singing -> {
                        SingingContent(
                            song = state.song,
                            part = state.part,
                            singer = state.singer,
                            timeLeft = timeLeft,
                            onSkipToVoting = { viewModel.skipToVoting() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is KaraokeViewModel.GameState.Voting -> {
                        VotingContent(
                            song = state.song,
                            part = state.part,
                            singer = state.singer,
                            players = players,
                            votes = votes,
                            onVote = { playerId, approved -> viewModel.vote(playerId, approved) },
                            onShowResults = { viewModel.showResults() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                    is KaraokeViewModel.GameState.Results -> {
                        ResultsContent(
                            singer = state.singer,
                            approved = state.approved,
                            yesVotes = state.yesVotes,
                            noVotes = state.noVotes,
                            onNextRound = { viewModel.reset() },
                            screenHeight = screenHeight,
                            screenWidth = screenWidth
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KaraokeBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(320.dp)
                .rotate(offset1)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SecondaryPink.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(380.dp)
                .rotate(offset2)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun KaraokeHeader(
    onBack: () -> Unit,
    onReset: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val headerPadding = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp)
    val iconSize = (screenWidth * 0.12f).coerceIn(40.dp, 56.dp)
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 26f).sp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = headerPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(iconSize)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(iconSize * 0.5f)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎤 Karaoke Roulette",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Black,
                    color = SecondaryPink
                )
                Text(
                    text = "¡Canta o bebe!",
                    fontSize = (titleSize.value * 0.5f).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(iconSize)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Reiniciar",
                    tint = SecondaryPink,
                    modifier = Modifier.size(iconSize * 0.5f)
                )
            }
        }
    }
}

@Composable
fun IdleContent(
    hasPlayers: Boolean,
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
    val emojiSize = (screenWidth * 0.28f).value.coerceIn(90f, 150f).sp
    val titleSize = (screenWidth * 0.065f).value.coerceIn(22f, 34f).sp
    val bodySize = (screenWidth * 0.04f).value.coerceIn(14f, 18f).sp
    val buttonHeight = (screenHeight * 0.09f).coerceIn(60.dp, 78.dp)

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
            // Micrófono animado
            val infiniteTransition = rememberInfiniteTransition(label = "mic")
            val micRotation by infiniteTransition.animateFloat(
                initialValue = -15f,
                targetValue = 15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation"
            )

            Text(
                text = "🎤",
                fontSize = emojiSize,
                modifier = Modifier.rotate(micRotation)
            )

            Spacer(modifier = Modifier.height(contentPadding * 1.5f))

            // Tarjeta de información
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(contentPadding * 1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Karaoke Roulette",
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(contentPadding * 0.6f))

                    Text(
                        text = "Un jugador aleatorio cantará una canción. El grupo votará si lo hizo bien.",
                        fontSize = bodySize,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = (bodySize.value * 1.5f).sp
                    )

                    Spacer(modifier = Modifier.height(contentPadding))

                    RuleItem("🎵", "30 segundos para cantar", bodySize)
                    Spacer(modifier = Modifier.height(8.dp))
                    RuleItem("👍", "Si aprueba: reparte 3 tragos", bodySize)
                    Spacer(modifier = Modifier.height(8.dp))
                    RuleItem("👎", "Si falla: bebe 2 tragos", bodySize)
                }
            }

            Spacer(modifier = Modifier.height(contentPadding * 2f))

            if (!hasPlayers) {
                Text(
                    text = "⚠️ Agrega jugadores primero",
                    color = AccentRed,
                    fontSize = bodySize,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(contentPadding))
            }

            ResponsiveButton(
                text = "🎤 Comenzar",
                onClick = onStart,
                height = buttonHeight,
                color = SecondaryPink,
                enabled = hasPlayers
            )
        }
    }
}

@Composable
fun RuleItem(emoji: String, text: String, textSize: androidx.compose.ui.unit.TextUnit) {
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
fun SingingContent(
    song: KaraokeViewModel.Song,
    part: String,
    singer: com.example.kampai.domain.models.PlayerModel,
    timeLeft: Int,
    onSkipToVoting: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    LaunchedEffect(song) {
        isVisible = false
        delay(100)
        isVisible = true
    }

    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 28.dp)
    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Jugador destacado
        SingerCard(singer = singer, screenWidth = screenWidth)

        Spacer(modifier = Modifier.weight(0.3f))

        Column(
            modifier = Modifier.scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer circular
            CircularTimer(
                timeLeft = timeLeft,
                totalTime = 30,
                screenWidth = screenWidth
            )

            Spacer(modifier = Modifier.height(contentPadding * 1.5f))

            // Tarjeta de canción
            SongCard(
                song = song,
                part = part,
                screenWidth = screenWidth
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))

        ResponsiveButton(
            text = "Ir a Votación →",
            onClick = onSkipToVoting,
            height = buttonHeight,
            color = AccentCyan
        )
    }
}

@Composable
fun SingerCard(
    singer: com.example.kampai.domain.models.PlayerModel,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val fontSize = (screenWidth * 0.045f).value.coerceIn(16f, 22f).sp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = singer.getAvatarColor().copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(singer.getAvatarColor().copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = singer.getDisplayEmoji(), fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "¡Es tu turno!",
                    fontSize = (fontSize.value * 0.7f).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = singer.name,
                    fontSize = fontSize * 1.1f,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(text = "🎤", fontSize = 36.sp)
        }
    }
}

@Composable
fun CircularTimer(
    timeLeft: Int,
    totalTime: Int,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timer")

    val scale by infiniteTransition.animateFloat(
        initialValue = if (timeLeft <= 10) 0.95f else 1f,
        targetValue = if (timeLeft <= 10) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (timeLeft <= 5) 200 else 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val color by animateColorAsState(
        targetValue = when {
            timeLeft <= 5 -> AccentRed
            timeLeft <= 15 -> AccentAmber
            else -> Color(0xFF10B981)
        },
        label = "color"
    )

    val timerSize = (screenWidth * 0.45f).coerceIn(140.dp, 200.dp)

    Box(
        modifier = Modifier
            .size(timerSize)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Círculos decorativos
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(timerSize - (index * 30).dp)
                    .clip(CircleShape)
                    .border(
                        width = (3 - index).dp,
                        color = color.copy(alpha = 0.3f - index * 0.1f),
                        shape = CircleShape
                    )
            )
        }

        // Círculo central
        Box(
            modifier = Modifier
                .size(timerSize * 0.7f)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    ambientColor = color,
                    spotColor = color
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.4f),
                            color.copy(alpha = 0.2f)
                        )
                    )
                )
                .border(3.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$timeLeft",
                fontSize = (timerSize.value * 0.4f).sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun SongCard(
    song: KaraokeViewModel.Song,
    part: String,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 28f).sp
    val bodySize = (screenWidth * 0.04f).value.coerceIn(14f, 18f).sp

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
                            SecondaryPink.copy(alpha = 0.2f)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    color = SecondaryPink.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = song.emoji,
                    fontSize = (screenWidth * 0.15f).value.coerceIn(48f, 72f).sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = song.title,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = (titleSize.value * 1.2f).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = song.artist,
                    fontSize = bodySize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Badge de parte
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SecondaryPink.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "Cantar: $part",
                        fontSize = (bodySize.value * 0.9f).sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryPink,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VotingContent(
    song: KaraokeViewModel.Song,
    part: String,
    singer: com.example.kampai.domain.models.PlayerModel,
    players: List<com.example.kampai.domain.models.PlayerModel>,
    votes: Map<String, Boolean>,
    onVote: (String, Boolean) -> Unit,
    onShowResults: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 28.dp)
    val titleSize = (screenWidth * 0.055f).value.coerceIn(18f, 26f).sp
    val bodySize = (screenWidth * 0.04f).value.coerceIn(14f, 18f).sp
    val buttonHeight = (screenHeight * 0.08f).coerceIn(56.dp, 72.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Votación",
            fontSize = titleSize,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿${singer.name} lo hizo bien?",
            fontSize = bodySize,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(contentPadding * 1.5f))

        // Grid de votantes
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.5f),
            userScrollEnabled = false
        ) {
            items(players.filter { it.id != singer.id }) { player ->
                VoterCard(
                    player = player,
                    vote = votes[player.id],
                    onVote = { approved -> onVote(player.id, approved) },
                    screenWidth = screenWidth
                )
            }
        }

        Spacer(modifier = Modifier.height(contentPadding * 1.5f))

        // Indicador de votos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VoteCounter(
                emoji = "👍",
                count = votes.values.count { it },
                color = Color(0xFF10B981)
            )
            VoteCounter(
                emoji = "👎",
                count = votes.values.count { !it },
                color = AccentRed
            )
        }

        Spacer(modifier = Modifier.height(contentPadding * 1.5f))

        ResponsiveButton(
            text = "Ver Resultado",
            onClick = onShowResults,
            height = buttonHeight,
            color = PrimaryViolet,
            enabled = votes.isNotEmpty()
        )
    }
}

@Composable
fun VoterCard(
    player: com.example.kampai.domain.models.PlayerModel,
    vote: Boolean?,
    onVote: (Boolean) -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val fontSize = (screenWidth * 0.035f).value.coerceIn(12f, 16f).sp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (vote) {
                true -> Color(0xFF10B981).copy(alpha = 0.2f)
                false -> AccentRed.copy(alpha = 0.2f)
                null -> Color.White.copy(alpha = 0.08f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(player.getAvatarColor().copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.getDisplayEmoji(), fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = player.name,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (vote == null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onVote(false) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentRed.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.ThumbDown,
                            contentDescription = "No",
                            tint = AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onVote(true) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.ThumbUp,
                            contentDescription = "Sí",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = if (vote) "👍" else "👎",
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
fun VoteCounter(emoji: String, count: Int, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$count",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun ResultsContent(
    singer: PlayerModel,
    approved: Boolean,
    yesVotes: Int,
    noVotes: Int,
    onNextRound: () -> Unit,
    screenHeight: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
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

    val contentPadding = (screenWidth * 0.05f).coerceIn(16.dp, 28.dp)
    val emojiSize = (screenWidth * 0.28f).value.coerceIn(90f, 150f).sp
    val titleSize = (screenWidth * 0.065f).value.coerceIn(22f, 34f).sp
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
            modifier = Modifier
                .scale(scale)
                .graphicsLayer { rotationY = rotation },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji animado
            val infiniteTransition = rememberInfiniteTransition(label = "result")
            val resultScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "resultScale"
            )

            Text(
                text = if (approved) "🎉" else "😢",
                fontSize = emojiSize,
                modifier = Modifier.scale(resultScale)
            )

            Spacer(modifier = Modifier.height(contentPadding))

            // Tarjeta del cantante
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = singer.getAvatarColor().copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(singer.getAvatarColor().copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = singer.getDisplayEmoji(), fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = singer.name,
                            fontSize = titleSize,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(contentPadding * 1.5f))

            // Resultado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (approved)
                        Color(0xFF10B981).copy(alpha = 0.2f)
                    else
                        AccentRed.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding * 1.5f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (approved) "¡APROBADO!" else "¡RECHAZADO!",
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        color = if (approved) Color(0xFF10B981) else AccentRed,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(contentPadding))

                    // Contadores de votos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        VoteResultCounter(
                            emoji = "👍",
                            count = yesVotes,
                            color = Color(0xFF10B981),
                            label = "A favor"
                        )

                        VoteResultCounter(
                            emoji = "👎",
                            count = noVotes,
                            color = AccentRed,
                            label = "En contra"
                        )
                    }

                    Spacer(modifier = Modifier.height(contentPadding * 1.5f))

                    // Castigo/Recompensa
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (approved)
                                "¡Reparte 3 tragos! 🍻"
                            else
                                "¡Bebe 2 tragos! 😅",
                            fontSize = bodySize,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(contentPadding * 2f))

            // Botón siguiente
            ResponsiveButton(
                text = "Siguiente Ronda →",
                onClick = onNextRound,
                height = buttonHeight,
                color = SecondaryPink
            )
        }
    }
}

@Composable
fun VoteResultCounter(
    emoji: String,
    count: Int,
    color: Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.4f),
                            color.copy(alpha = 0.2f)
                        )
                    )
                )
                .border(3.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = emoji, fontSize = 28.sp)
                Text(
                    text = "$count",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ResponsiveButton(
    text: String,
    onClick: () -> Unit,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 16.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (enabled) color else Color.Gray,
                spotColor = if (enabled) color else Color.Gray
            )
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.8f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Gray.copy(alpha = 0.3f),
                            Color.Gray.copy(alpha = 0.2f)
                        )
                    )
                },
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            fontSize = (height.value * 0.25f).sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.Gray
        )
    }
}