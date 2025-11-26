package com.example.kampai.ui.theme.culture

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kampai.R
import com.example.kampai.ui.theme.AccentRed
import com.example.kampai.ui.theme.PrimaryViolet
import kotlinx.coroutines.delay

@Composable
fun CultureSelectionScreen(
    onNavigateToBomb: () -> Unit,
    onNavigateToClassic: () -> Unit,
    onBack: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val useWideLayout = maxWidth > 600.dp || maxWidth > maxHeight

        AnimatedSelectionBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedHeader(onBack = onBack, showContent = showContent)

            val topSpacerHeight = if (maxHeight < 600.dp) 20.dp else 60.dp
            Spacer(modifier = Modifier.height(topSpacerHeight))

            Text(
                text = stringResource(R.string.culture_selection_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (maxHeight < 600.dp) 24.sp else 28.sp // Texto responsive
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.graphicsLayer { alpha = if (showContent) 1f else 0f }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.culture_selection_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { alpha = if (showContent) 1f else 0f }
            )

            Spacer(modifier = Modifier.height(if (maxHeight < 600.dp) 20.dp else 40.dp))

            if (useWideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ModeCardClassic(onNavigateToClassic, showContent, 200)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ModeCardBomb(onNavigateToBomb, showContent, 400)
                    }
                }
            } else {
                ModeCardClassic(onNavigateToClassic, showContent, 200)
                Spacer(modifier = Modifier.height(24.dp))
                ModeCardBomb(onNavigateToBomb, showContent, 400)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ModeCardClassic(onClick: () -> Unit, showContent: Boolean, delay: Long) {
    AnimatedModeCard(
        title = stringResource(R.string.culture_mode_classic),
        emoji = "🎯",
        description = stringResource(R.string.culture_desc_classic),
        bulletPoints = listOf(
            stringResource(R.string.culture_feat_classic_1),
            stringResource(R.string.culture_feat_classic_2),
            stringResource(R.string.culture_feat_classic_3)
        ),
        color = PrimaryViolet,
        onClick = onClick,
        delay = delay,
        showContent = showContent
    )
}

@Composable
fun ModeCardBomb(onClick: () -> Unit, showContent: Boolean, delay: Long) {
    AnimatedModeCard(
        title = stringResource(R.string.culture_mode_bomb),
        emoji = "💣",
        description = stringResource(R.string.culture_desc_bomb),
        bulletPoints = listOf(
            stringResource(R.string.culture_feat_bomb_1),
            stringResource(R.string.culture_feat_bomb_2),
            stringResource(R.string.culture_feat_bomb_3)
        ),
        color = AccentRed,
        onClick = onClick,
        delay = delay,
        showContent = showContent
    )
}

@Composable
fun AnimatedSelectionBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(25000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.align(Alignment.TopStart).offset(x = (-100).dp, y = (-100).dp).size(300.dp).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(PrimaryViolet.copy(alpha = 0.25f), Color.Transparent))))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp).size(350.dp).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(AccentRed.copy(alpha = 0.2f), Color.Transparent))))
    }
}

@Composable
fun AnimatedHeader(onBack: () -> Unit, showContent: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "headerScale"
    )

    Box(modifier = Modifier.fillMaxWidth().scale(scale)) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart).size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
        }

        Text(
            text = stringResource(R.string.culture_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 22.sp),
            color = PrimaryViolet,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun AnimatedModeCard(
    title: String,
    emoji: String,
    description: String,
    bulletPoints: List<String>,
    color: Color,
    onClick: () -> Unit,
    delay: Long,
    showContent: Boolean
) {
    var isVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateFloatAsState(targetValue = if (isVisible) 0f else 50f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), label = "offsetY")
    val alpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, animationSpec = tween(600), label = "alpha")
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")

    // Animación de pulsación para el emoji de la bomba
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (title.contains("Bomb") || title.contains("Bomba")) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "emojiScale"
    )

    LaunchedEffect(showContent) {
        if (showContent) {
            delay(delay)
            isVisible = true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer { translationY = offsetY; this.alpha = alpha; scaleX = scale; scaleY = scale }
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp), ambientColor = color.copy(alpha = 0.4f), spotColor = color.copy(alpha = 0.4f))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(brush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.surface, color.copy(alpha = 0.12f))))
                .border(width = 2.dp, brush = Brush.linearGradient(colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0.2f))), shape = RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.size(64.dp).scale(emojiScale).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.1f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    // Hacemos que el título se encoja un poco si es muy largo en pantallas pequeñas
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f) // Permite que el texto fluya sin empujar el emoji fuera
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp), color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                bulletPoints.forEach { point ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = point, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}