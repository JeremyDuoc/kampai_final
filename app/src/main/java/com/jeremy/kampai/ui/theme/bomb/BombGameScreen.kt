package com.jeremy.kampai.ui.theme.bomb

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.R
import com.jeremy.kampai.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BombGameScreen(
    viewModel: BombViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val category by viewModel.category.collectAsState()

    val activeAtmosphere = when {
        uiState is BombViewModel.GameState.Exploded -> KColor.Danger
        timeLeft <= 5 -> KColor.Danger
        timeLeft <= 10 -> KColor.Amber
        else -> KColor.Purple
    }

    val animatedAtmosphere by animateColorAsState(
        targetValue = activeAtmosphere,
        animationSpec = tween(400),
        label = "bombAtmosphere"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(KColor.Base)
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val isLandscape = maxWidth > maxHeight || maxWidth > 600.dp
        val effectiveWidth = if (isLandscape) maxWidth / 2 else maxWidth

        NeoAtmosphereBackground(activeColor = animatedAtmosphere) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // HEADER RESPONSIVO
                BombResponsiveHeader(
                    onBack = onBack,
                    onReset = { viewModel.resetGame() },
                    accentColor = animatedAtmosphere
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    when (uiState) {
                        is BombViewModel.GameState.Idle -> {
                            IdleContentResponsive(
                                onStart = { viewModel.startGame() },
                                screenHeight = maxHeight,
                                effectiveWidth = effectiveWidth,
                                isLandscape = isLandscape,
                                accentColor = animatedAtmosphere
                            )
                        }
                        is BombViewModel.GameState.Playing -> {
                            PlayingContentResponsive(
                                category = category,
                                timeLeft = timeLeft,
                                screenHeight = maxHeight,
                                effectiveWidth = effectiveWidth,
                                isLandscape = isLandscape,
                                accentColor = animatedAtmosphere
                            )
                        }
                        is BombViewModel.GameState.Exploded -> {
                            ExplodedContentResponsive(
                                onReset = { viewModel.resetGame() },
                                screenHeight = maxHeight,
                                effectiveWidth = effectiveWidth,
                                isLandscape = isLandscape,
                                accentColor = animatedAtmosphere
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BombResponsiveHeader(
    onBack: () -> Unit,
    onReset: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallIconButton(
            icon = Icons.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            onClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Overline(
                text = "MODO BOMBA / TIEMPO LÍMITE",
                color = accentColor
            )
            Text(
                text = stringResource(R.string.bomb_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = KColor.Foreground
            )
        }

        SmallIconButton(
            icon = Icons.Filled.Refresh,
            contentDescription = stringResource(R.string.party_cancel),
            onClick = onReset
        )
    }
}

@Composable
fun IdleContentResponsive(
    onStart: () -> Unit,
    screenHeight: Dp,
    effectiveWidth: Dp,
    isLandscape: Boolean,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        ChallengeReticle(
            emoji = "💣",
            accent = accentColor
        )

        GlassPanel(
            accent = accentColor,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionHeading(
                    title = stringResource(R.string.bomb_ready_title),
                    aside = "REGLAS"
                )

                BombRuleItem("🎯", stringResource(R.string.bomb_rule_1))
                BombRuleItem("⏱️", stringResource(R.string.bomb_rule_2))
                BombRuleItem("📱", stringResource(R.string.bomb_rule_3))
                BombRuleItem("💥", stringResource(R.string.bomb_rule_4), isDanger = true)
            }
        }

        KampaiButton(
            label = "💣 " + stringResource(R.string.bomb_btn_start).uppercase(),
            onClick = onStart,
            accent = KColor.Danger,
            primary = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PlayingContentResponsive(
    category: String,
    timeLeft: Int,
    screenHeight: Dp,
    effectiveWidth: Dp,
    isLandscape: Boolean,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shakeTransition")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (timeLeft <= 4) 8f else 0f,
        animationSpec = infiniteRepeatable(animation = tween(40), repeatMode = RepeatMode.Reverse),
        label = "shake"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = shakeOffset }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // CATEGORÍA ACTIVA
        GlassPanel(
            accent = accentColor,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Pill(
                    text = stringResource(R.string.bomb_badge_label),
                    tint = accentColor
                )

                Text(
                    text = category,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = KColor.Foreground,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
            }
        }

        // TIMER CIRCULAR NEON
        CircularBombTimer(
            timeLeft = timeLeft,
            accentColor = accentColor
        )

        // MENSAJE DE URGENCIA
        if (timeLeft <= 10) {
            val text = when {
                timeLeft <= 3 -> stringResource(R.string.bomb_urgent_high)
                timeLeft <= 5 -> stringResource(R.string.bomb_urgent_med)
                else -> stringResource(R.string.bomb_urgent_low)
            }
            Pill(
                text = "⚡ $text",
                tint = accentColor
            )
        }
    }
}

@Composable
fun CircularBombTimer(
    timeLeft: Int,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bombPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (timeLeft <= 4) 1.15f else if (timeLeft <= 8) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (timeLeft <= 4) 120 else 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bombPulse"
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .scale(pulseScale)
            .shadow(32.dp, CircleShape, spotColor = accentColor)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(accentColor.copy(alpha = 0.35f), KColor.Strong)
                )
            )
            .border(4.dp, accentColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$timeLeft",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = KColor.Foreground,
                letterSpacing = (-2).sp
            )
            Overline(
                text = "SEGUNDOS",
                color = accentColor
            )
        }
    }
}

@Composable
fun ExplodedContentResponsive(
    onReset: () -> Unit,
    screenHeight: Dp,
    effectiveWidth: Dp,
    isLandscape: Boolean,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        ChallengeReticle(
            emoji = "💥",
            accent = KColor.Danger
        )

        Text(
            text = stringResource(R.string.bomb_boom_title),
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = KColor.Danger,
            letterSpacing = (-1).sp
        )

        GlassPanel(
            accent = KColor.Danger,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.bomb_exploded_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KColor.Foreground,
                    textAlign = TextAlign.Center
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🍺", fontSize = 28.sp)
                    Text(
                        text = stringResource(R.string.bomb_drink_command),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = KColor.Danger
                    )
                    Text(text = "🍺", fontSize = 28.sp)
                }

                Pill(
                    text = stringResource(R.string.bomb_failed_msg),
                    tint = KColor.Danger
                )
            }
        }

        KampaiButton(
            label = "🔄 " + stringResource(R.string.bomb_btn_retry).uppercase(),
            onClick = onReset,
            accent = KColor.Danger,
            primary = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BombRuleItem(
    icon: String,
    text: String,
    isDanger: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isDanger) KColor.Danger.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isDanger) KColor.Danger else KColor.Secondary,
            fontWeight = if (isDanger) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}