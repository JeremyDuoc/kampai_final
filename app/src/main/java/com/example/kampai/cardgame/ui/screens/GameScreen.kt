package com.example.kampai.cardgame.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.cardgame.domain.models.*
import com.example.kampai.cardgame.ui.components.*
import com.example.kampai.cardgame.ui.viewmodel.GameViewModel

/**
 * Pantalla principal del juego en orientación HORIZONTAL
 * Layout optimizado para landscape mode
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val localHand by viewModel.localPlayerHand.collectAsState()
    val turnTimeRemaining by viewModel.turnTimeRemaining.collectAsState()
    val kampaiTimeRemaining by viewModel.kampaiTimeRemaining.collectAsState()
    val showColorPicker by viewModel.showColorPicker.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !isLandscape -> {
                // Force landscape message
                ForceLandscapeMessage()
            }

            uiState.showTurnTransition -> {
                TurnTransitionScreen(
                    nextPlayerName = uiState.nextPlayerName,
                    onAcknowledge = { viewModel.acknowledgeTurnTransition() }
                )
            }

            uiState.gameWinner != null -> {
                GameOverScreen(
                    winner = uiState.gameWinner!!,
                    onBack = onBack
                )
            }

            else -> {
                GamePlayScreen(
                    gameState = gameState,
                    uiState = uiState,
                    localHand = localHand,
                    turnTimeRemaining = turnTimeRemaining,
                    kampaiTimeRemaining = kampaiTimeRemaining,
                    onPlayCard = { viewModel.playCard(it) },
                    onDrawCard = { viewModel.drawCard() },
                    onPressKampai = { viewModel.pressKampai() },
                    onPressPenalty = { viewModel.pressPenalty() }
                )
            }
        }

        // Color picker dialog
        if (showColorPicker) {
            ColorPickerDialog(
                onColorSelected = { viewModel.selectWildColor(it) },
                onDismiss = { /* Can't dismiss - must choose */ }
            )
        }

        // Error snackbar
        uiState.errorMessage?.let { error ->
            ErrorSnackbar(
                message = error,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

// ==================== MAIN GAMEPLAY SCREEN ====================

@Composable
private fun GamePlayScreen(
    gameState: GameState,
    uiState: com.example.kampai.cardgame.ui.viewmodel.GameUiState,
    localHand: List<CardModel>,
    turnTimeRemaining: Int,
    kampaiTimeRemaining: Int,
    onPlayCard: (CardModel) -> Unit,
    onDrawCard: () -> Unit,
    onPressKampai: () -> Unit,
    onPressPenalty: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A0B2E),
                        Color(0xFF0D0519)
                    )
                )
            )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT SIDE - Opponent hands (minimized)
            OpponentsSidebar(
                players = gameState.players,
                currentPlayerId = gameState.players.getOrNull(gameState.currentPlayerIndex)?.id,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(120.dp)
            )

            // CENTER - Main game area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Top info bar
                TopInfoBar(
                    turnTimeRemaining = turnTimeRemaining,
                    currentPlayer = gameState.players.getOrNull(gameState.currentPlayerIndex),
                    isMyTurn = uiState.isMyTurn,
                    direction = gameState.direction,
                    modifier = Modifier.fillMaxWidth()
                )

                // Main play area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Draw pile
                        DrawPile(
                            cardCount = gameState.drawPileSize,
                            onDraw = onDrawCard,
                            enabled = uiState.canDrawCard,
                            modifier = Modifier.weight(1f)
                        )

                        // Discard pile
                        DiscardPile(
                            topCard = gameState.topCard,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Stacked cards indicator
                    if (gameState.stackedPlusCards > 0) {
                        StackedCardsIndicator(
                            count = gameState.stackedPlusCards,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        )
                    }
                }

                // Bottom - Player's hand
                PlayerHandArea(
                    cards = localHand,
                    topCard = gameState.topCard,
                    canPlay = uiState.canPlayCard,
                    onCardClick = onPlayCard,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // RIGHT SIDE - Actions and status
            ActionsSidebar(
                showKampaiButton = uiState.showKampaiButton,
                canPressPenalty = uiState.canPressPenalty,
                kampaiTimeRemaining = kampaiTimeRemaining,
                mustDrawCards = uiState.mustDrawCards,
                cardsToDrawCount = uiState.cardsToDrawCount,
                onPressKampai = onPressKampai,
                onPressPenalty = onPressPenalty,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(140.dp)
            )
        }
    }
}

// ==================== TOP INFO BAR ====================

@Composable
private fun TopInfoBar(
    turnTimeRemaining: Int,
    currentPlayer: PlayerInfo?,
    isMyTurn: Boolean,
    direction: TurnDirection,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current player indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isMyTurn) "🎯 TU TURNO" else "Turno de:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMyTurn) Color(0xFFF59E0B) else Color.White
                )

                if (!isMyTurn && currentPlayer != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF6A1B9A).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = currentPlayer.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Direction indicator
            DirectionIndicator(direction)

            // Timer
            if (isMyTurn && turnTimeRemaining > 0) {
                TurnTimer(timeRemaining = turnTimeRemaining)
            }
        }
    }
}

@Composable
private fun DirectionIndicator(direction: TurnDirection) {
    val infiniteTransition = rememberInfiniteTransition(label = "direction")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFF06B6D4).copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (direction == TurnDirection.CLOCKWISE) "↻" else "↺",
            fontSize = 24.sp,
            color = Color(0xFF06B6D4),
            modifier = Modifier.scale(
                scaleX = if (direction == TurnDirection.CLOCKWISE) 1f else -1f,
                scaleY = 1f
            )
        )
    }
}

@Composable
private fun TurnTimer(timeRemaining: Int) {
    val color = when {
        timeRemaining <= 5 -> Color(0xFFEF4444)
        timeRemaining <= 10 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val scale by animateFloatAsState(
        targetValue = if (timeRemaining <= 5) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "timerScale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "⏱️", fontSize = 16.sp)
            Text(
                text = "$timeRemaining",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

// ==================== OPPONENTS SIDEBAR ====================

@Composable
private fun OpponentsSidebar(
    players: List<PlayerInfo>,
    currentPlayerId: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Jugadores",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            players.forEach { player ->
                OpponentHandMinimized(
                    playerName = player.name,
                    cardCount = 7, // Would come from game state
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ==================== ACTIONS SIDEBAR ====================

@Composable
private fun ActionsSidebar(
    showKampaiButton: Boolean,
    canPressPenalty: Boolean,
    kampaiTimeRemaining: Int,
    mustDrawCards: Boolean,
    cardsToDrawCount: Int,
    onPressKampai: () -> Unit,
    onPressPenalty: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // KAMPAI Button
            AnimatedVisibility(
                visible = showKampaiButton,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                KampaiButton(
                    onClick = onPressKampai,
                    timeRemaining = kampaiTimeRemaining
                )
            }

            // Penalty Button
            AnimatedVisibility(
                visible = canPressPenalty,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                PenaltyButton(
                    onClick = onPressPenalty,
                    timeRemaining = kampaiTimeRemaining
                )
            }

            // Draw cards warning
            if (mustDrawCards) {
                DrawCardsWarning(count = cardsToDrawCount)
            }
        }
    }
}

@Composable
private fun KampaiButton(
    onClick: () -> Unit,
    timeRemaining: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "kampai")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .scale(scale),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🍻",
                    fontSize = 32.sp
                )
                Text(
                    text = "KAMPAI!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        if (timeRemaining > 0) {
            Text(
                text = "$timeRemaining",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
private fun PenaltyButton(
    onClick: () -> Unit,
    timeRemaining: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "penalty")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .scale(scale),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 28.sp
                )
                Text(
                    text = "PENALIDAD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        if (timeRemaining > 0) {
            Text(
                text = "$timeRemaining",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
private fun DrawCardsWarning(count: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF59E0B).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Debes robar\n$count cartas",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StackedCardsIndicator(
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEF4444).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "📚", fontSize = 24.sp)
            Text(
                text = "+$count acumuladas",
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

// ==================== PLAYER HAND AREA ====================

@Composable
private fun PlayerHandArea(
    cards: List<CardModel>,
    topCard: CardModel?,
    canPlay: Boolean,
    onCardClick: (CardModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cards.isEmpty()) {
                Text(
                    text = "No tienes cartas",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            } else {
                CardHand(
                    cards = cards,
                    onCardClick = { card ->
                        if (canPlay && topCard != null && card.canPlayOn(topCard)) {
                            onCardClick(card)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }
    }
}

// ==================== DIALOGS AND OVERLAYS ====================

@Composable
private fun ColorPickerDialog(
    onColorSelected: (CardColor) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Elige un color",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf(
                        CardColor.RED,
                        CardColor.BLUE,
                        CardColor.GREEN,
                        CardColor.YELLOW
                    ).forEach { color ->
                        ColorOptionButton(
                            color = color,
                            onClick = { onColorSelected(color) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOptionButton(
    color: CardColor,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color.toComposeColor()
        ),
        modifier = Modifier.size(80.dp),
        shape = CircleShape
    ) {
        // Empty - just the color
    }
}

@Composable
private fun TurnTransitionScreen(
    nextPlayerName: String,
    onAcknowledge: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable { onAcknowledge() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🔄",
                fontSize = 80.sp
            )

            Text(
                text = "Turno de",
                fontSize = 24.sp,
                color = Color.Gray
            )

            Text(
                text = nextPlayerName,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Toca para continuar",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun GameOverScreen(
    winner: PlayerInfo,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6A1B9A),
                        Color(0xFF1A0B2E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🏆",
                fontSize = 120.sp
            )

            Text(
                text = "¡Victoria!",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF59E0B)
            )

            Text(
                text = winner.name,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A1B9A)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Volver al Menú",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ForceLandscapeMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📱",
                fontSize = 80.sp
            )
            Text(
                text = "Por favor, gira tu dispositivo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Este juego requiere orientación horizontal",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEF4444)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color.White
                )
                Text(
                    text = message,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}