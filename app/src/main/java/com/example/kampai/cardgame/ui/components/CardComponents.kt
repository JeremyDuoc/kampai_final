package com.example.kampai.cardgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.kampai.R
import com.example.kampai.cardgame.domain.models.CardModel
import com.example.kampai.cardgame.domain.models.CardValue

/**
 * Componente de carta con sistema de capas para skins personalizados
 * Estructura de 3 capas:
 * 1. Capa Base (Color lógico de la carta)
 * 2. Capa de Diseño (Skin/textura con transparencia)
 * 3. Capa Superior (Valor/Símbolo de la carta)
 */
@Composable
fun GameCard(
    card: CardModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isSelectable: Boolean = true,
    showBack: Boolean = false,
    elevation: Dp = 4.dp
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .aspectRatio(0.7f) // Standard card ratio
            .then(
                if (onClick != null && isSelectable) {
                    Modifier.clickable {
                        isPressed = true
                        onClick()
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (showBack) {
                CardBack()
            } else {
                CardFront(card)
            }
        }
    }
}

@Composable
private fun CardBack() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A0B2E),
                        Color(0xFF2D1B4E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Logo Kampai
        Image(
            painter = painterResource(id = R.drawable.logo_kampai),
            contentDescription = "Kampai Logo",
            modifier = Modifier
                .fillMaxSize(0.6f)
                .graphicsLayer { alpha = 0.8f },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun CardFront(card: CardModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        // CAPA 1: Color Base (Siempre visible)
        ColorBaseLayer(card)

        // CAPA 2: Diseño/Skin (Con transparencia)
        DesignLayer(card.designId)

        // CAPA 3: Valor/Símbolo (Siempre opaco y contrastante)
        ValueLayer(card)
    }
}

@Composable
private fun ColorBaseLayer(card: CardModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        card.color.toComposeColor(),
                        card.color.toComposeColor().copy(alpha = 0.85f)
                    )
                )
            )
    )
}

@Composable
private fun DesignLayer(designId: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 0.3f // 30% opacity to show base color through
            }
    ) {
        when (designId) {
            0 -> ClassicDesign()
            1 -> NeonDesign()
            2 -> RetroDesign()
            3 -> MinimalistDesign()
            4 -> CyberpunkDesign()
            else -> ClassicDesign()
        }
    }
}

@Composable
private fun ClassicDesign() {
    // Simple gradient overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun NeonDesign() {
    // Neon glow effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Cyan.copy(alpha = 0.4f),
                        Color.Magenta.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun RetroDesign() {
    // Retro stripes
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(20) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        if (it % 2 == 0) Color.White.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
            )
        }
    }
}

@Composable
private fun MinimalistDesign() {
    // Clean minimal design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.1f))
    )
}

@Composable
private fun CyberpunkDesign() {
    // Cyberpunk grid
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00FFFF).copy(alpha = 0.3f),
                        Color(0xFFFF00FF).copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun ValueLayer(card: CardModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top-left corner
        Text(
            text = card.value.getSymbol(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Center (large)
        Text(
            text = card.value.getSymbol(),
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )

        // Bottom-right corner (rotated)
        Text(
            text = card.value.getSymbol(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer { rotationZ = 180f }
        )
    }
}

// ==================== CARD HAND DISPLAY ====================

@Composable
fun CardHand(
    cards: List<CardModel>,
    onCardClick: (CardModel) -> Unit,
    modifier: Modifier = Modifier,
    maxVisibleCards: Int = 7
) {
    val spacing = remember(cards.size) {
        if (cards.size > maxVisibleCards) -60.dp else -40.dp
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        cards.forEachIndexed { index, card ->
            Box(
                modifier = Modifier
                    .offset(x = spacing * index)
                    .zIndex(index.toFloat())
            ) {
                GameCard(
                    card = card,
                    onClick = { onCardClick(card) },
                    modifier = Modifier
                        .height(180.dp)
                        .graphicsLayer {
                            // Fan effect
                            val centerIndex = cards.size / 2f
                            val offset = index - centerIndex
                            rotationZ = offset * 3f
                            translationY = kotlin.math.abs(offset) * 10f
                        }
                )
            }
        }
    }
}

// ==================== OPPONENT HAND MINIMIZED ====================

@Composable
fun OpponentHandMinimized(
    playerName: String,
    cardCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = playerName,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Surface(
                color = Color(0xFFF59E0B),
                shape = CircleShape
            ) {
                Text(
                    text = "$cardCount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ==================== DISCARD AND DRAW PILES ====================

@Composable
fun DiscardPile(
    topCard: CardModel?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (topCard != null) {
            GameCard(
                card = topCard,
                isSelectable = false,
                modifier = Modifier.height(200.dp)
            )
        } else {
            EmptyPile("Descarte")
        }
    }
}

@Composable
fun DrawPile(
    cardCount: Int,
    onDraw: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(modifier = modifier) {
        if (cardCount > 0) {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .clickable(enabled = enabled) { onDraw() }
            ) {
                // Stack effect - show multiple cards
                repeat(minOf(cardCount, 3)) { index ->
                    GameCard(
                        card = CardModel(
                            "",
                            CardValue.ZERO,
                            com.example.kampai.cardgame.domain.models.CardColor.RED
                        ),
                        showBack = true,
                        isSelectable = false,
                        modifier = Modifier
                            .offset(x = (index * 2).dp, y = (index * 2).dp)
                            .zIndex(-index.toFloat())
                    )
                }

                // Card count badge - usando Surface en lugar de Badge
                Surface(
                    color = Color(0xFF2563EB),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "$cardCount",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            EmptyPile("Mazo Vacío")
        }
    }
}

@Composable
private fun EmptyPile(label: String) {
    Card(
        modifier = Modifier
            .height(200.dp)
            .aspectRatio(0.7f),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}