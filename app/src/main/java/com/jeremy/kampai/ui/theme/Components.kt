package com.jeremy.kampai.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// 1. FONDO DE FIESTA / NEO-ATMOSPHERE
// ============================================================================

@Composable
fun NeoAtmosphereBackground(
    activeColor: Color = NeonPurple,
    parallaxOffset: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "atmosphere")

    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb1Angle"
    )

    val angle2 by infiniteTransition.animateFloat(
        initialValue = (2f * Math.PI).toFloat(),
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb2Angle"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "atmospherePulse"
    )

    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    val particles = remember {
        listOf(
            Offset(0.12f, 0.08f), Offset(0.38f, 0.05f), Offset(0.72f, 0.11f),
            Offset(0.91f, 0.07f), Offset(0.09f, 0.24f), Offset(0.32f, 0.33f),
            Offset(0.82f, 0.27f), Offset(0.95f, 0.38f), Offset(0.18f, 0.48f),
            Offset(0.58f, 0.52f), Offset(0.86f, 0.58f), Offset(0.06f, 0.65f),
            Offset(0.42f, 0.70f), Offset(0.74f, 0.74f), Offset(0.93f, 0.82f),
            Offset(0.20f, 0.88f), Offset(0.52f, 0.92f), Offset(0.81f, 0.95f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        // Capa 1: Estrellas / polvo festivo
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = -parallaxOffset * 0.2f }
        ) {
            val w = size.width
            val h = size.height

            particles.forEachIndexed { index, p ->
                val phase = (index % 5) * 0.2f
                val localTwinkle = ((twinkle + phase) % 1f).let { if (it < 0.5f) it * 2f else (1f - it) * 2f }
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f + localTwinkle * 0.45f),
                    radius = if (index % 4 == 0) 2.2f else 1.3f,
                    center = Offset(w * p.x, h * p.y)
                )
            }
        }

        // Capa 2: Orbes de luz líquida difuminados
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = -parallaxOffset * 0.35f }
                .blur(76.dp)
        ) {
            val w = size.width
            val h = size.height

            // Orbe Superior - reactivo al color
            val x1 = w * 0.25f + w * 0.22f * kotlin.math.cos(angle1)
            val y1 = h * 0.22f + h * 0.16f * kotlin.math.sin(angle1)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(activeColor.copy(alpha = 0.48f), Color.Transparent),
                    center = Offset(x1, y1),
                    radius = w * 0.70f * pulse
                ),
                center = Offset(x1, y1),
                radius = w * 0.70f * pulse
            )

            // Orbe Inferior - contraste cálido/frío
            val x2 = w * 0.75f + w * 0.20f * kotlin.math.cos(angle2)
            val y2 = h * 0.72f + h * 0.14f * kotlin.math.sin(angle2)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPink.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(x2, y2),
                    radius = w * 0.80f
                ),
                center = Offset(x2, y2),
                radius = w * 0.80f
            )

            // Orbe Central - brillo tenue
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyberCyan.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.48f),
                    radius = w * 0.55f
                ),
                center = Offset(w * 0.5f, h * 0.48f),
                radius = w * 0.55f
            )
        }

        content()
    }
}

// Fondo de compatibilidad para juegos antiguos
@Composable
fun PartyBackground(content: @Composable BoxScope.() -> Unit) {
    NeoAtmosphereBackground(activeColor = PrimaryViolet, content = content)
}

// ============================================================================
// 2. NEO-GLASS CARD (Contenedor translúcido con borde luminoso)
// ============================================================================

@Composable
fun NeoGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberBorder,
    glowColor: Color = Color.Transparent,
    cornerRadius: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .scale(scale)
            .then(
                if (glowColor != Color.Transparent) {
                    Modifier.shadow(16.dp, RoundedCornerShape(cornerRadius), spotColor = glowColor)
                } else Modifier
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        CyberSurface.copy(alpha = 0.88f),
                        CyberSurfaceElevated.copy(alpha = 0.65f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.8f),
                        borderColor.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .then(clickableModifier)
    ) {
        content()
    }
}

// Compatibilidad con código previo
@Composable
fun KampaiCard(
    modifier: Modifier = Modifier,
    borderColor: Color = PrimaryViolet,
    content: @Composable BoxScope.() -> Unit
) {
    NeoGlassCard(
        modifier = modifier.padding(0.dp),
        borderColor = borderColor,
        cornerRadius = 24.dp
    ) {
        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

// ============================================================================
// 3. MEDIDOR DE INTENSIDAD (Cápsulas iluminadas de combustible)
// ============================================================================

@Composable
fun IntensityGauge(
    level: Int,
    maxLevel: Int = 4,
    activeColor: Color = SolarAmber,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLevel) { index ->
            val isActive = index < level
            val color by animateColorAsState(
                targetValue = if (isActive) activeColor else Color.White.copy(alpha = 0.12f),
                animationSpec = tween(350, delayMillis = index * 60),
                label = "intensityColor$index"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isActive) {
                            Modifier.shadow(4.dp, CircleShape, spotColor = activeColor)
                        } else Modifier
                    )
            )
        }
    }
}

// ============================================================================
// 4. PILA DE AVATARES (Muestra quiénes están en la fiesta)
// ============================================================================

@Composable
fun AvatarStack(
    emojis: List<String>,
    maxVisible: Int = 4,
    modifier: Modifier = Modifier
) {
    if (emojis.isEmpty()) return

    val visibleEmojis = emojis.take(maxVisible)
    val remaining = emojis.size - maxVisible

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleEmojis.forEachIndexed { index, emoji ->
            Box(
                modifier = Modifier
                    .offset(x = (-index * 10).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceElevated)
                    .border(1.5.dp, NeonPurple.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 14.sp)
            }
        }
        if (remaining > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-visibleEmojis.size * 10).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeonPurple)
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remaining",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

// ============================================================================
// 5. BOTÓN TÁCTIL CON RESORTE
// ============================================================================

@Composable
fun TactileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NeonPurple,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "btnScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f)
        ),
        shape = shape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 1.dp
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .scale(scale)
            .shadow(12.dp, shape, spotColor = containerColor)
    ) {
        content()
    }
}

// ============================================================================
// 6. INSIGNIA / BADGE DE NEÓN
// ============================================================================

@Composable
fun NeonBadge(
    text: String,
    color: Color = NeonPink,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 0.8.sp
        )
    }
}

// ============================================================================
// 7. GAME SCAFFOLD ESTÁNDAR
// ============================================================================

@Composable
fun GameScaffold(
    title: String,
    color: Color,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    NeoAtmosphereBackground(activeColor = color) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            content()
        }
    }
}

// ============================================================================
// 8. NEON AFTERHOURS DESIGN SYSTEM COMPONENTS
// ============================================================================

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    accent: Color = KColor.Purple,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val surface = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = .095f), Color.White.copy(alpha = .035f))
    )
    val border = Brush.linearGradient(
        listOf(Color.White.copy(alpha = .18f), accent.copy(alpha = .25f), Color.White.copy(alpha = .08f))
    )
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Column(
        modifier = modifier
            .clip(shape)
            .then(clickModifier)
            .background(surface, shape)
            .border(BorderStroke(1.dp, border), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun KampaiButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = KColor.Purple,
    primary: Boolean = true,
    enabled: Boolean = true,
    strongHaptic: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "kampai_btn_press"
    )
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(16.dp)
    val brush = if (primary) {
        Brush.linearGradient(listOf(Color(0xFFD3A0FF), accent))
    } else {
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = .12f), Color.White.copy(alpha = .06f))
        )
    }
    Box(
        modifier = modifier
            .heightIn(min = 54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else .45f
            }
            .clip(shape)
            .background(brush, shape)
            .border(1.dp, Color.White.copy(alpha = if (primary) .18f else .14f), shape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled) {
                if (strongHaptic) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (primary) KColor.InkOnAccent else KColor.Foreground,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun Overline(
    text: String,
    color: Color = KColor.Secondary,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.6.sp,
        modifier = modifier
    )
}

@Composable
fun SectionHeading(
    title: String,
    aside: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = KColor.Foreground,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Overline(text = aside)
    }
}

@Composable
fun Pill(
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(tint.copy(alpha = .12f))
            .border(1.dp, tint.copy(alpha = .36f), CircleShape)
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = tint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SmallIconButton(
    glyph: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = .08f))
            .border(1.dp, Color.White.copy(alpha = .14f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            color = KColor.Foreground,
            fontSize = 22.sp
        )
    }
}

@Composable
fun SmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = .08f))
            .border(1.dp, Color.White.copy(alpha = .14f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = KColor.Foreground,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun PlayerAvatarBadge(
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.linearGradient(listOf(color, KColor.Strong)))
            .border(1.dp, Color.White.copy(alpha = .22f), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (size.value * 0.52f).sp)
    }
}

@Composable
fun ChallengeReticle(
    emoji: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(130.dp)
            .border(1.dp, accent.copy(alpha = .48f), CircleShape)
            .padding(12.dp)
            .border(2.dp, accent.copy(alpha = .82f), CircleShape)
            .shadow(24.dp, CircleShape, spotColor = accent),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 56.sp)
    }
}

@Composable
fun StepperButton(
    symbol: String,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = .10f))
            .border(1.dp, Color.White.copy(alpha = .18f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = KColor.Foreground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AmbientLights() {
    Canvas(Modifier.fillMaxSize()) {
        val minDim = size.minDimension
        val purpleCenter = Offset(size.width * .96f, size.height * .20f)
        val cyanCenter = Offset(size.width * .04f, size.height * .70f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(KColor.Purple.copy(alpha = .24f), Color.Transparent),
                center = purpleCenter,
                radius = minDim * .77f
            ),
            radius = minDim * .77f,
            center = purpleCenter
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(KColor.Cyan.copy(alpha = .10f), Color.Transparent),
                center = cyanCenter,
                radius = minDim * .66f
            ),
            radius = minDim * .66f,
            center = cyanCenter
        )
    }
}