package com.jeremy.kampai.ui.theme.culture

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeremy.kampai.R
import com.jeremy.kampai.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CultureSelectionScreen(
    onNavigateToBomb: () -> Unit,
    onNavigateToClassic: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(KColor.Base)
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val useWideLayout = maxWidth > 600.dp || maxWidth > maxHeight

        NeoAtmosphereBackground(activeColor = KColor.Purple) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SmallIconButton(
                        glyph = "‹",
                        description = stringResource(R.string.back),
                        onClick = onBack
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.culture_title).uppercase() + " ✺",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = KColor.Purple
                        )
                        Overline(
                            text = "ELIGE EL MODO DE JUEGO",
                            color = KColor.Secondary
                        )
                    }

                    Spacer(modifier = Modifier.size(46.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Cómo quieren jugar?",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = KColor.Foreground,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.culture_selection_subtitle),
                        fontSize = 13.sp,
                        color = KColor.Secondary
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (useWideLayout) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ModeCardClassic(onNavigateToClassic)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ModeCardBomb(onNavigateToBomb)
                        }
                    }
                } else {
                    ModeCardClassic(onNavigateToClassic)
                    Spacer(modifier = Modifier.height(18.dp))
                    ModeCardBomb(onNavigateToBomb)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ModeCardClassic(onClick: () -> Unit) {
    NeonModeCard(
        title = stringResource(R.string.culture_mode_classic),
        emoji = "🤓",
        description = stringResource(R.string.culture_desc_classic),
        bulletPoints = listOf(
            stringResource(R.string.culture_feat_classic_1),
            stringResource(R.string.culture_feat_classic_2),
            stringResource(R.string.culture_feat_classic_3)
        ),
        accentColor = KColor.Purple,
        onClick = onClick
    )
}

@Composable
fun ModeCardBomb(onClick: () -> Unit) {
    NeonModeCard(
        title = stringResource(R.string.culture_mode_bomb),
        emoji = "💣",
        description = stringResource(R.string.culture_desc_bomb),
        bulletPoints = listOf(
            stringResource(R.string.culture_feat_bomb_1),
            stringResource(R.string.culture_feat_bomb_2),
            stringResource(R.string.culture_feat_bomb_3)
        ),
        accentColor = KColor.Danger,
        onClick = onClick
    )
}

@Composable
fun NeonModeCard(
    title: String,
    emoji: String,
    description: String,
    bulletPoints: List<String>,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "modeCardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        GlassPanel(
            accent = accentColor,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.22f))
                            .border(1.2.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 30.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Pill(
                            text = if (emoji == "💣") "MODO TENSIÓN" else "MODO CHILL",
                            tint = accentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = KColor.Foreground
                        )
                    }
                }

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = KColor.Secondary,
                    lineHeight = 18.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    bulletPoints.forEach { point ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = point,
                                fontSize = 12.sp,
                                color = KColor.Foreground.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                KampaiButton(
                    label = "SELECCIONAR MODO →",
                    onClick = onClick,
                    accent = accentColor,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                )
            }
        }
    }
}