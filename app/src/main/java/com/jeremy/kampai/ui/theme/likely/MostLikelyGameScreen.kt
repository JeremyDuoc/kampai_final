package com.jeremy.kampai.ui.theme.likely

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.R
import com.jeremy.kampai.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MostLikelyScreen(
    viewModel: MostLikelyViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val question by viewModel.question.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KColor.Base)
    ) {
        NeoAtmosphereBackground(activeColor = KColor.Amber) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                MostLikelyHeader(
                    onBack = onBack,
                    onRefresh = { viewModel.nextQuestion() }
                )

                // Arena scrolleable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Badge de instrucción
                    Pill(
                        text = "👉 " + stringResource(R.string.likely_instruction_badge) + " 👈",
                        tint = KColor.Cyan
                    )

                    // Tarjeta de la pregunta (Holográfica)
                    QuestionCard(question = question)

                    // Reglas rápidas de la ronda
                    RulesSection()

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // CTA fijo en la parte inferior
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    KampaiButton(
                        label = stringResource(R.string.likely_btn_next).uppercase() + " →",
                        onClick = { viewModel.nextQuestion() },
                        accent = KColor.Amber,
                        primary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun MostLikelyHeader(
    onBack: () -> Unit,
    onRefresh: () -> Unit
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
                text = "QUIÉN ES MÁS PROBABLE / DEBATE",
                color = KColor.Amber
            )
            Text(
                text = stringResource(R.string.likely_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = KColor.Foreground
            )
        }

        SmallIconButton(
            icon = Icons.Filled.Refresh,
            contentDescription = "Refresh",
            onClick = onRefresh
        )
    }
}

@Composable
fun QuestionCard(question: String) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "questionScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "questionRotation"
    )

    LaunchedEffect(question) {
        isVisible = false
        delay(60)
        isVisible = true
    }

    GlassPanel(
        accent = KColor.Amber,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { rotationZ = rotation }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(KColor.Amber, Color(0xFFFF887C))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤔",
                    fontSize = 32.sp
                )
            }

            Text(
                text = question,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
                color = KColor.Foreground
            )

            Overline(
                text = "✦ APUNTEN SIMULTÁNEAMENTE ✦",
                color = KColor.Amber.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun RulesSection() {
    GlassPanel(
        accent = KColor.Raised,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeading(
                title = stringResource(R.string.likely_rules_title),
                aside = "REGLAS"
            )

            RuleItem(number = "1", text = stringResource(R.string.likely_rule_1))
            RuleItem(number = "2", text = stringResource(R.string.likely_rule_2))
            RuleItem(number = "3", text = stringResource(R.string.likely_rule_3))
            RuleItem(number = "4", text = stringResource(R.string.likely_rule_4), isPenalty = true)
        }
    }
}

@Composable
fun RuleItem(
    number: String,
    text: String,
    isPenalty: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isPenalty) KColor.Danger.copy(alpha = 0.25f) else KColor.Amber.copy(alpha = 0.2f)
                )
                .border(
                    1.dp,
                    if (isPenalty) KColor.Danger.copy(alpha = 0.6f) else KColor.Amber.copy(alpha = 0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (isPenalty) KColor.Danger else KColor.Amber
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isPenalty) KColor.Foreground else KColor.Secondary,
            fontWeight = if (isPenalty) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}