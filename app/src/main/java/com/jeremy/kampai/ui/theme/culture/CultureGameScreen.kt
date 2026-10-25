package com.jeremy.kampai.ui.theme.culture

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.R
import com.jeremy.kampai.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CultureGameScreen(
    viewModel: CultureViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val category by viewModel.currentCategory.collectAsState()
    var showCategory by remember { mutableStateOf(false) }

    LaunchedEffect(category) {
        showCategory = false
        delay(60)
        showCategory = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KColor.Base)
    ) {
        NeoAtmosphereBackground(activeColor = KColor.Purple) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HEADER
                CultureHeader(onBack = onBack)

                Spacer(modifier = Modifier.height(16.dp))

                // ARENA CONTENT SCROLLABLE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Pill(
                        text = "👥 " + stringResource(R.string.culture_instruction),
                        tint = KColor.Cyan
                    )

                    // CATEGORÍA PRINCIPAL
                    AnimatedCategoryCard(
                        category = category,
                        showCategory = showCategory
                    )

                    // SECCIÓN DE REGLAS
                    RulesSection()

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // CTA INFERIOR
                KampaiButton(
                    label = "🔄 " + stringResource(R.string.culture_btn_next).uppercase(),
                    onClick = { viewModel.nextCategory() },
                    accent = KColor.Purple,
                    primary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CultureHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                text = "CULTURA CHUPÍSTICA / CLÁSICO",
                color = KColor.Purple
            )
            Text(
                text = stringResource(R.string.culture_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = KColor.Foreground
            )
        }

        Spacer(modifier = Modifier.size(46.dp))
    }
}

@Composable
fun AnimatedCategoryCard(category: String, showCategory: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (showCategory) 1f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (showCategory) 0f else -6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotation"
    )

    GlassPanel(
        accent = KColor.Purple,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { rotationZ = rotation }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(KColor.Purple, Color(0xFF6B21A8))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎯", fontSize = 30.sp)
            }

            Text(
                text = category,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = KColor.Foreground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Overline(
                text = "✦ POR TURNO SIN REPETIR ✦",
                color = KColor.Purple.copy(alpha = 0.8f)
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
                title = stringResource(R.string.culture_rules_title),
                aside = "REGLAS"
            )

            CultureRuleItem(number = "1", text = stringResource(R.string.culture_rule_1))
            CultureRuleItem(number = "2", text = stringResource(R.string.culture_rule_2))
            CultureRuleItem(number = "3", text = stringResource(R.string.culture_rule_3), isPenalty = true)
        }
    }
}

@Composable
fun CultureRuleItem(
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
                    if (isPenalty) KColor.Danger.copy(alpha = 0.25f) else KColor.Purple.copy(alpha = 0.2f)
                )
                .border(
                    1.dp,
                    if (isPenalty) KColor.Danger.copy(alpha = 0.6f) else KColor.Purple.copy(alpha = 0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (isPenalty) KColor.Danger else KColor.Purple
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