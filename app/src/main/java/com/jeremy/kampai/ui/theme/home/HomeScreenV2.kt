package com.jeremy.kampai.ui.theme.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.R
import com.jeremy.kampai.domain.models.FeaturedGameSource
import com.jeremy.kampai.domain.models.GameIntensity
import com.jeremy.kampai.domain.models.GameModel
import com.jeremy.kampai.domain.models.GameVibe
import com.jeremy.kampai.ui.theme.partymanager.PartyManagerViewModel

private val FeaturedLilac = Color(0xFFD9A0EF)
private val FeaturedPink = Color(0xFFE798C4)
private val FeaturedAmber = Color(0xFFFFAD50)
private val FeaturedCoral = Color(0xFFF48F83)
private val HomeBase = Color(0xFF09080D)

@Composable
fun HomeScreenV2(
    viewModel: HomeViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onGameSelected: (String) -> Unit,
    onPartyManager: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val featuredGame by viewModel.featuredGame.collectAsState()
    val featuredSource by viewModel.featuredSource.collectAsState()
    val games by viewModel.vibeGames.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val showDisclaimer by viewModel.showDisclaimer.collectAsState()
    val showDiceModal by viewModel.showDiceModal.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val rolledGame by viewModel.rolledGame.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshFeatured()
    }

    val motion = rememberInfiniteTransition(label = "kampai_home_v6_motion")
    val featuredPhase by motion.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "featured_steam_0_400_0"
    )
    val sheenProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9_500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "game_card_sheen"
    )
    val backgroundDrift by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background_drift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBase)
    ) {
        KampaiV6Background(backgroundDrift)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 29.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                HomeHeaderV2(
                    onSettings = onNavigateToSettings,
                    modifier = Modifier.padding(horizontal = 19.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 19.dp, end = 19.dp, top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_v2_eyebrow),
                        color = Color(0xFF65DEE9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.7.sp
                    )
                    Text(
                        text = stringResource(R.string.home_v2_headline),
                        color = Color(0xFFF9F6FB),
                        fontSize = 35.sp,
                        lineHeight = 37.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.6).sp
                    )
                }
            }

            featuredGame?.let { game ->
                item {
                    FeaturedGameCardV2(
                        game = game,
                        source = featuredSource,
                        phase = featuredPhase,
                        modifier = Modifier.padding(start = 19.dp, end = 19.dp, top = 22.dp),
                        onClick = { onGameSelected(game.route) }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 19.dp, end = 19.dp, top = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionHeaderV2(
                        title = stringResource(R.string.home_v2_random_title),
                        aside = stringResource(R.string.home_v2_random_aside)
                    )
                    RandomGameCardV2(
                        lastGame = rolledGame,
                        onClick = viewModel::openDiceModal
                    )
                }
            }

            item {
                SectionHeaderV2(
                    title = stringResource(R.string.home_v2_games_title),
                    aside = stringResource(R.string.home_v2_games_aside, games.size),
                    modifier = Modifier.padding(start = 19.dp, end = 19.dp, top = 24.dp, bottom = 14.dp)
                )
            }

            items(games.chunked(2)) { pair ->
                val startIndex = pair.firstOrNull()?.let { games.indexOf(it) } ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 19.dp, end = 19.dp, bottom = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    pair.forEachIndexed { index, game ->
                        GameTileV2(
                            game = game,
                            index = (startIndex + index).coerceAtLeast(0),
                            sheenProgress = sheenProgress,
                            modifier = Modifier.weight(1f),
                            onClick = { onGameSelected(game.route) }
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 19.dp, end = 19.dp, top = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionHeaderV2(
                        title = stringResource(R.string.home_v2_coming_title),
                        aside = stringResource(R.string.home_v2_feedback)
                    )
                    ComingSoonCardV2(
                        onSuggest = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:game.kampai@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.home_v2_suggest_subject))
                            }
                            runCatching { context.startActivity(intent) }
                        }
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.home_v2_footer),
                    color = Color(0xFF8F8495),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 19.dp, end = 19.dp, top = 18.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            item {
                Box(
                    modifier = Modifier.padding(start = 19.dp, end = 19.dp, top = 18.dp)
                ) {
                    NeoAdBanner()
                }
            }
        }

        BottomDockV2(
            playerCount = players.size,
            onPartyManager = onPartyManager,
            onDice = viewModel::openDiceModal,
            onSettings = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )
    }

    if (showDiceModal) {
        DiceOfDestinyDialog(
            isRolling = isRolling,
            selectedGame = rolledGame,
            onDismiss = viewModel::closeDiceModal,
            onReroll = viewModel::rollDice,
            onPlay = {
                rolledGame?.let { onGameSelected(it.route) }
                viewModel.closeDiceModal()
            }
        )
    }

    if (showDisclaimer) {
        NeoLegalDisclaimerDialog(onAccept = viewModel::acceptDisclaimer)
    }
}

@Composable
private fun KampaiV6Background(drift: Float) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0B090F),
                        Color(0xFF090811),
                        Color(0xFF100914),
                        Color(0xFF08070D)
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f)
                )
            )

            val dx = (drift - .5f) * size.width * .024f
            val dy = (drift - .5f) * size.height * .015f

            fun glow(
                center: Offset,
                radiusX: Float,
                radiusY: Float,
                colors: List<Color>
            ) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = colors,
                        center = center,
                        radius = maxOf(radiusX, radiusY)
                    ),
                    topLeft = Offset(center.x - radiusX, center.y - radiusY),
                    size = Size(radiusX * 2f, radiusY * 2f)
                )
            }

            glow(
                center = Offset(size.width * -.08f + dx, size.height * .18f + dy),
                radiusX = size.width * .74f,
                radiusY = size.height * .47f,
                colors = listOf(
                    Color(0xFFD38418).copy(alpha = .50f),
                    Color(0xFF8D500A).copy(alpha = .22f),
                    Color.Transparent
                )
            )
            glow(
                center = Offset(size.width * 1.08f - dx, size.height * .73f - dy),
                radiusX = size.width * .72f,
                radiusY = size.height * .57f,
                colors = listOf(
                    Color(0xFFAE2A83).copy(alpha = .32f),
                    Color(0xFF6B1F5B).copy(alpha = .18f),
                    Color.Transparent
                )
            )
            glow(
                center = Offset(size.width * .75f, size.height * .09f + dy),
                radiusX = size.width * .57f,
                radiusY = size.height * .40f,
                colors = listOf(Color(0xFF415FAE).copy(alpha = .17f), Color.Transparent)
            )
            glow(
                center = Offset(size.width * .18f + dx, size.height * .83f),
                radiusX = size.width * .56f,
                radiusY = size.height * .34f,
                colors = listOf(Color(0xFF1F8C91).copy(alpha = .13f), Color.Transparent)
            )
            glow(
                center = Offset(size.width * .50f, size.height * 1.08f),
                radiusX = size.width * .85f,
                radiusY = size.height * .46f,
                colors = listOf(Color(0xFF7B2870).copy(alpha = .16f), Color.Transparent)
            )

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color(0xFF040308).copy(alpha = .17f),
                        Color(0xFF030207).copy(alpha = .38f)
                    ),
                    center = Offset(size.width * .5f, size.height * .5f),
                    radius = maxOf(size.width, size.height) * .72f
                )
            )
        }

        val stars = remember {
            listOf(
                Offset(.38f, .08f), Offset(.72f, .16f), Offset(.82f, .30f), Offset(.43f, .47f),
                Offset(.95f, .57f), Offset(.31f, .79f), Offset(.75f, .84f), Offset(.09f, .69f)
            )
        }
        Canvas(Modifier.fillMaxSize()) {
            val starDrift = -9.dp.toPx() * drift
            stars.forEachIndexed { index, p ->
                val phase = (drift + index * .17f) % 1f
                val alpha = .11f + kotlin.math.abs(.5f - phase) * .48f
                drawCircle(
                    color = Color.White.copy(alpha = alpha.coerceIn(.11f, .46f)),
                    radius = if (index % 3 == 0) 1.7f else 1.05f,
                    center = Offset(size.width * p.x, size.height * p.y + starDrift)
                )
            }
        }

        val grain = remember {
            List(140) { i ->
                Offset(
                    (((i * 37) % 101) / 100f).coerceIn(0f, 1f),
                    (((i * 61 + 17) % 103) / 102f).coerceIn(0f, 1f)
                )
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            grain.forEachIndexed { index, p ->
                drawCircle(
                    color = Color.White.copy(alpha = if (index % 4 == 0) .025f else .013f),
                    radius = if (index % 5 == 0) 1.1f else .65f,
                    center = Offset(size.width * p.x, size.height * p.y)
                )
            }
        }
    }
}

@Composable
private fun HomeHeaderV2(onSettings: () -> Unit, modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "kampai_brand_v6")
    val breathe by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brand_breathe_6_8s"
    )
    val burstRotation by motion.animateFloat(
        initialValue = 9f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4_800
                9f at 0
                18f at 2_304
                5f at 2_784
                11f at 3_456
                9f at 4_800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "brand_burst_rotation_4_8s"
    )
    val burstScale by motion.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4_800
                1f at 0
                1.08f at 2_304
                .98f at 2_784
                1.03f at 3_456
                1f at 4_800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "brand_burst_scale_4_8s"
    )
    val shimmer by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "brand_shimmer"
    )
    val shimmerPulse = when {
        shimmer < .58f -> 0f
        shimmer < .66f -> (shimmer - .58f) / .08f
        shimmer < .76f -> 1f - (shimmer - .66f) / .10f
        else -> 0f
    }.coerceIn(0f, 1f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .offset(x = (-7).dp, y = (-8).dp)
                    .size(width = 116.dp, height = 58.dp)
                    .blur(10.dp)
            ) {
                drawOval(
                    brush = Brush.radialGradient(
                        listOf(
                            FeaturedPink.copy(alpha = .08f + .08f * breathe),
                            FeaturedAmber.copy(alpha = .045f + .045f * breathe),
                            Color.Transparent
                        )
                    ),
                    size = size
                )
            }

            Column(
                modifier = Modifier
                    .offset(y = (-1.5f * breathe).dp)
                    .graphicsLayer {
                        scaleX = 1f + .006f * breathe
                        scaleY = 1f + .006f * breathe
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Text(
                            text = stringResource(R.string.app_name).uppercase(),
                            color = Color.White,
                            fontSize = 25.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.65).sp
                        )
                        if (shimmerPulse > 0f) {
                            Text(
                                text = stringResource(R.string.app_name).uppercase(),
                                color = Color.White.copy(alpha = .22f * shimmerPulse),
                                fontSize = 25.sp,
                                lineHeight = 25.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1.65).sp,
                                modifier = Modifier.blur(.8.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .offset(x = (-1).dp)
                            .graphicsLayer {
                                rotationZ = burstRotation
                                scaleX = burstScale
                                scaleY = burstScale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (breathe > .35f) {
                            Text(
                                text = stringResource(R.string.home_v2_logo_burst),
                                color = FeaturedPink.copy(alpha = .13f * breathe),
                                fontSize = 29.sp,
                                modifier = Modifier.blur(3.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.home_v2_logo_burst),
                            color = Color.White,
                            fontSize = 27.sp,
                            lineHeight = 27.sp
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.home_v2_brand_subtitle),
                    color = Color(
                        red = 1f,
                        green = .69f + .07f * breathe,
                        blue = .18f + .10f * breathe,
                        alpha = 1f
                    ),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.2.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        GlassIconButtonV2(
            icon = Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.settings),
            onClick = onSettings
        )
    }
}

@Composable
private fun FeaturedGameCardV2(
    game: GameModel,
    source: FeaturedGameSource,
    phase: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val borderColors = listOf(
        FeaturedLilac, FeaturedPink, FeaturedAmber, FeaturedCoral,
        FeaturedLilac, FeaturedPink, FeaturedAmber, FeaturedCoral, FeaturedLilac
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .shadow(21.dp, shape, spotColor = Color.Black.copy(alpha = .34f))
    ) {
        AnimatedFeaturedStrokeV2(
            colors = borderColors,
            phase = phase,
            blur = 22.dp,
            alpha = .30f
        )
        AnimatedFeaturedStrokeV2(
            colors = borderColors,
            phase = phase,
            blur = 0.dp,
            alpha = 1f
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(1.6.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF2E1F16),
                            Color(0xFF2B1B28),
                            Color(0xFF18152A),
                            Color(0xFF14131D)
                        )
                    )
                )
                .clickable(onClick = onClick)
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawOval(
                    brush = Brush.radialGradient(
                        listOf(FeaturedAmber.copy(alpha = .15f), Color.Transparent),
                        center = Offset(size.width * .07f, size.height * .04f),
                        radius = size.width * .72f
                    ),
                    topLeft = Offset(-size.width * .15f, -size.height * .20f),
                    size = Size(size.width * .88f, size.height * .88f)
                )
                drawOval(
                    brush = Brush.radialGradient(
                        listOf(FeaturedPink.copy(alpha = .13f), Color.Transparent),
                        center = Offset(size.width * .97f, size.height * .91f),
                        radius = size.width * .62f
                    ),
                    topLeft = Offset(size.width * .49f, size.height * .43f),
                    size = Size(size.width * .70f, size.height * .72f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(.71f)
                    .padding(start = 17.dp, top = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = FeaturedLilac,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = stringResource(
                            if (source == FeaturedGameSource.COMMUNITY) {
                                R.string.home_v2_featured_community
                            } else {
                                R.string.home_v2_featured_default
                            }
                        ),
                        color = Color(0xFFECD7F4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.3.sp
                    )
                }

                Spacer(Modifier.height(17.dp))
                Text(
                    text = stringResource(game.title),
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-.9).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(game.description),
                    color = Color(0xFFC5BAC7),
                    fontSize = 12.sp,
                    lineHeight = 17.8.sp,
                    modifier = Modifier.widthIn(max = 220.dp),
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    MetaPillV2(game.playerRange, FeaturedAmber)
                    MetaPillV2(stringResource(intensityLabelRes(game.intensity)), FeaturedLilac)
                }
            }

            FeaturedDeckV2(
                game = game,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-15).dp, y = 49.dp)
            )

            AnimatedPlayButtonV2(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 17.dp, end = 17.dp, bottom = 21.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.AnimatedFeaturedStrokeV2(
    colors: List<Color>,
    phase: Float,
    blur: androidx.compose.ui.unit.Dp,
    alpha: Float
) {
    val blurModifier = if (blur > 0.dp) Modifier.blur(blur) else Modifier
    Canvas(
        modifier = Modifier
            .matchParentSize()
            .then(blurModifier)
            .graphicsLayer {
                this.alpha = alpha
                if (blur > 0.dp) {
                    scaleX = 1.015f
                    scaleY = 1.02f
                }
            }
    ) {
        val span = size.width * 1.16f
        val travel = phase * size.width
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(-span + travel, -span * .35f),
            end = Offset(travel, span * .65f),
            tileMode = TileMode.Repeated
        )
        drawRoundRect(
            brush = brush,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
private fun FeaturedDeckV2(game: GameModel, modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "featured_deck_float")
    val floatPhase by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "featured_deck_float_6s"
    )

    val backRotation = -10f + 2f * floatPhase
    val backY = 2f + 5f * floatPhase
    val faceRotation = 11f - 2f * floatPhase
    val faceX = 6f + 1f * floatPhase
    val faceY = 12f - 5f * floatPhase

    Box(modifier = modifier.size(width = 110.dp, height = 149.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .rotate(backRotation)
                .offset(x = (-8).dp, y = backY.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = .42f))
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF285970), Color(0xFF203E57), Color(0xFF492B50))))
                .border(1.dp, Color(0xFF4AA0B1), RoundedCornerShape(18.dp))
        ) {
            Box(
                Modifier
                    .padding(12.dp)
                    .fillMaxSize()
                    .border(1.dp, Color(0xFF77DCEB).copy(alpha = .22f), RoundedCornerShape(12.dp))
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .rotate(faceRotation)
                .offset(x = faceX.dp, y = faceY.dp)
                .shadow(13.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = .46f))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFD98A18), Color(0xFF94555D), Color(0xFF4B2A64))
                    )
                )
                .border(1.dp, Color(0xFFFFE1B8).copy(alpha = .72f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = gameIconV2(game.id),
                contentDescription = null,
                tint = Color(0xFFFFE9D0),
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
private fun AnimatedPlayButtonV2(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "featured_cta_v6")
    val drift by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "featured_cta_drift"
    )
    val shine by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "featured_cta_shine"
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .98f else 1f,
        animationSpec = spring(),
        label = "featured_cta_press"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(51.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(9.dp, RoundedCornerShape(15.dp), spotColor = FeaturedPink.copy(alpha = .16f))
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        FeaturedLilac,
                        FeaturedPink,
                        FeaturedAmber,
                        FeaturedCoral,
                        FeaturedLilac
                    ),
                    start = Offset(-220f + drift * 320f, 0f),
                    end = Offset(720f + drift * 320f, 180f),
                    tileMode = TileMode.Repeated
                )
            )
            .border(1.dp, Color(0xFFFFE1BA).copy(alpha = .24f), RoundedCornerShape(15.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Canvas(Modifier.matchParentSize()) {
            if (shine in .60f..82f) {
                val local = ((shine - .60f) / .22f).coerceIn(0f, 1f)
                val alpha = kotlin.math.sin(local * Math.PI).toFloat() * .26f
                val x = -size.width * .30f + local * size.width * 1.60f
                rotate(8f, pivot = Offset(size.width / 2f, size.height / 2f)) {
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = alpha), Color.Transparent),
                            start = Offset(x - size.width * .16f, 0f),
                            end = Offset(x + size.width * .16f, 0f)
                        ),
                        topLeft = Offset(0f, -size.height),
                        size = Size(size.width, size.height * 3f)
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.home_v2_play_now),
            color = Color(0xFF1E1424),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = .15.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun MetaPillV2(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = .10f))
            .border(1.dp, color.copy(alpha = .34f), CircleShape)
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = color, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun GameTileV2(
    game: GameModel,
    index: Int,
    sheenProgress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = gamePaletteV2(game.id)
    val shape = RoundedCornerShape(22.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .976f else 1f,
        animationSpec = spring(dampingRatio = .78f, stiffness = 430f),
        label = "game_tile_press"
    )
    val phase = (sheenProgress + index * .17f) % 1f

    Box(
        modifier = modifier
            .height(142.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 15.dp,
                shape = shape,
                ambientColor = palette.edge.copy(alpha = .08f),
                spotColor = Color.Black.copy(alpha = .90f)
            )
            .clip(shape)
            .background(Brush.linearGradient(listOf(palette.start, palette.end)))
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            palette.edge.copy(alpha = .78f),
                            palette.edge.copy(alpha = .32f),
                            Color.White.copy(alpha = .13f),
                            palette.accent.copy(alpha = .58f)
                        )
                    )
                ),
                shape
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawOval(
                brush = Brush.radialGradient(
                    listOf(palette.edge.copy(alpha = .24f), Color.Transparent),
                    center = Offset(size.width * .04f, -size.height * .18f),
                    radius = size.width * .95f
                ),
                topLeft = Offset(-size.width * .45f, -size.height * .58f),
                size = Size(size.width * 1.4f, size.height * 1.15f)
            )
            drawOval(
                brush = Brush.radialGradient(
                    listOf(palette.accent.copy(alpha = .14f), Color.Transparent),
                    center = Offset(size.width * 1.07f, size.height * 1.11f),
                    radius = size.width * .80f
                ),
                topLeft = Offset(size.width * .48f, size.height * .46f),
                size = Size(size.width * .92f, size.height * .92f)
            )

            if (phase >= .62f) {
                val local = ((phase - .62f) / .38f).coerceIn(0f, 1f)
                val alpha = kotlin.math.sin(local * Math.PI).toFloat() * .075f
                val x = -size.width * .95f + local * size.width * 2.40f
                rotate(13f, pivot = Offset(size.width / 2f, size.height / 2f)) {
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = alpha), Color.Transparent),
                            start = Offset(x - size.width * .29f, 0f),
                            end = Offset(x + size.width * .29f, 0f)
                        ),
                        topLeft = Offset(0f, -size.height * .40f),
                        size = Size(size.width, size.height * 1.8f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = 13.dp, bottom = 13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(7.dp, RoundedCornerShape(13.dp), spotColor = Color.Black.copy(alpha = .28f))
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(palette.edge.copy(alpha = .18f), Color.White.copy(alpha = .035f))
                            )
                        )
                        .border(1.dp, palette.edge.copy(alpha = .34f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = gameIconV2(game.id),
                        contentDescription = null,
                        tint = palette.edge.copy(alpha = .96f),
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = game.playerRange,
                    color = Color(0xFFF8EFFA).copy(alpha = .88f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = .15.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Column {
                Text(
                    text = stringResource(game.title),
                    color = Color.White,
                    fontSize = 13.5.sp,
                    lineHeight = 15.3.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-.35).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        gameVibeIconV2(game.vibe),
                        contentDescription = null,
                        tint = Color(0xFFC7BACA),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(intensityLabelRes(game.intensity)) + " · " +
                            stringResource(gameVibeLabelRes(game.vibe)),
                        color = Color(0xFFC7BACA),
                        fontSize = 9.7.sp,
                        letterSpacing = (-.06).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun RandomGameCardV2(lastGame: GameModel?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 85.dp)
            .shadow(10.dp, shape, spotColor = Color.Black.copy(alpha = .34f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = .075f), Color.White.copy(alpha = .035f))
                )
            )
            .border(1.dp, Color(0xFFE7BEE5).copy(alpha = .20f), shape)
            .clickable(onClick = onClick)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFEEAE5E).copy(alpha = .14f), Color.Transparent),
                    center = Offset(-size.width * .05f, size.height * .5f),
                    radius = size.width * .55f
                ),
                topLeft = Offset(-size.width * .55f, -size.height * .10f),
                size = Size(size.width * .70f, size.height * 1.20f)
            )
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFB95FCC).copy(alpha = .14f), Color.Transparent),
                    center = Offset(size.width * 1.05f, size.height * .5f),
                    radius = size.width * .58f
                ),
                topLeft = Offset(size.width * .76f, -size.height * .10f),
                size = Size(size.width * .56f, size.height * 1.20f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 16.dp, top = 13.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(49.dp)
                    .shadow(9.dp, RoundedCornerShape(15.dp), spotColor = Color(0xFFE37E84).copy(alpha = .16f))
                    .clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFC16A), Color(0xFFEE8CAD), Color(0xFFB887E8)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Casino, contentDescription = null, tint = Color(0xFF1A1623), modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dice_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = (-.2).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = lastGame?.let { stringResource(R.string.home_v2_last_random, stringResource(it.title)) }
                        ?: stringResource(R.string.dice_subtitle),
                    color = Color(0xFFBFB1C2),
                    fontSize = 11.sp,
                    lineHeight = 15.9.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F182B).copy(alpha = .68f))
                    .border(1.dp, Color(0xFFE4BBEE).copy(alpha = .20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Casino, contentDescription = null, tint = Color(0xFFF3D9FF), modifier = Modifier.size(23.dp))
            }
        }
    }
}

@Composable
private fun ComingSoonCardV2(onSuggest: () -> Unit) {
    val shape = RoundedCornerShape(23.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, spotColor = Color.Black.copy(alpha = .34f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = .12f), Color(0xFFECAAca).copy(alpha = .115f))
                )
            )
            .border(1.dp, Color(0xFFF8DCEB).copy(alpha = .24f), shape)
            .padding(start = 15.dp, end = 15.dp, top = 17.dp, bottom = 15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(FeaturedAmber.copy(alpha = .17f), FeaturedLilac.copy(alpha = .12f))
                        )
                    )
                    .border(1.dp, Color(0xFFFFD284).copy(alpha = .18f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color(0xFFFFC36A), modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_v2_suggest_title),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_v2_suggest_body),
                    color = Color(0xFFBDACBD),
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = .10f))
                .border(1.dp, Color.White.copy(alpha = .22f), RoundedCornerShape(16.dp))
                .clickable(onClick = onSuggest),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.home_v2_suggest_action),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(7.dp))
            Icon(Icons.Outlined.Send, contentDescription = null, tint = Color.White.copy(alpha = .90f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun BottomDockV2(
    playerCount: Int,
    onPartyManager: () -> Unit,
    onDice: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diceInteraction = remember { MutableInteractionSource() }
    val dicePressed by diceInteraction.collectIsPressedAsState()
    val diceScale by animateFloatAsState(
        targetValue = if (dicePressed) .92f else 1f,
        animationSpec = spring(dampingRatio = .72f, stiffness = 520f),
        label = "dock_dice_scale"
    )
    val diceRotation by animateFloatAsState(
        targetValue = if (dicePressed) -16f else 0f,
        animationSpec = spring(dampingRatio = .75f, stiffness = 500f),
        label = "dock_dice_rotation"
    )

    Row(
        modifier = modifier
            .shadow(18.dp, CircleShape, spotColor = Color.Black.copy(alpha = .78f))
            .clip(CircleShape)
            .background(Color(0xFF181423).copy(alpha = .94f))
            .border(1.dp, Color(0xFFE1C7E8).copy(alpha = .23f), CircleShape)
            .widthIn(min = 194.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DockActionV2(onClick = onPartyManager) {
            Icon(Icons.Filled.Groups, contentDescription = stringResource(R.string.home_v2_party), tint = Color(0xFFFFAE2C), modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(6.dp))
            Text(playerCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .graphicsLayer {
                    scaleX = diceScale
                    scaleY = diceScale
                    rotationZ = diceRotation
                }
                .shadow(8.dp, CircleShape, spotColor = FeaturedCoral.copy(alpha = .22f))
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFFBC76), Color(0xFFF88D88))))
                .clickable(
                    interactionSource = diceInteraction,
                    indication = null,
                    onClick = onDice
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Casino, contentDescription = stringResource(R.string.dice_title), tint = Color(0xFF111016), modifier = Modifier.size(23.dp))
        }

        DockActionV2(onClick = onSettings) {
            Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings), tint = Color.White.copy(alpha = .90f), modifier = Modifier.size(23.dp))
        }
    }
}

@Composable
private fun DockActionV2(onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = { content() }
    )
}

@Composable
private fun GlassIconButtonV2(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = .40f))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = .08f))
            .border(1.dp, Color.White.copy(alpha = .16f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White.copy(alpha = .92f), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SectionHeaderV2(title: String, aside: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Text(aside, color = Color(0xFFABA2B4), fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
    }
}

private data class GamePaletteV2(
    val edge: Color,
    val accent: Color,
    val start: Color,
    val end: Color
)

private fun gamePaletteV2(id: String): GamePaletteV2 = when (id) {
    "warmup" -> GamePaletteV2(Color(0xFFF1B23F), Color(0xFFD982B8), Color(0xC2804F0F), Color(0xF5372031))
    "culture" -> GamePaletteV2(Color(0xFFD85BD8), Color(0xFF937BFF), Color(0xC2671D71), Color(0xF52F1944))
    "kingscup" -> GamePaletteV2(Color(0xFF9D82FF), Color(0xFF5FA9ED), Color(0xC44A347B), Color(0xF7241D41))
    "impostor" -> GamePaletteV2(Color(0xFFFF746D), Color(0xFFD75FA5), Color(0xC27D272B), Color(0xF53C1D32))
    "never_have_i_ever" -> GamePaletteV2(Color(0xFF43D7D5), Color(0xFF678CFF), Color(0xBD026069), Color(0xF71A2840))
    "truth_or_dare" -> GamePaletteV2(Color(0xFFEFB24B), Color(0xFFE98D6B), Color(0xC2875011), Color(0xF73D2530))
    "most_likely" -> GamePaletteV2(Color(0xFFEA50DC), Color(0xFF9C6DFF), Color(0xC777167A), Color(0xF72F1840))
    else -> GamePaletteV2(FeaturedLilac, Color(0xFF6BD8DE), Color(0xFF563462), Color(0xFF1E192D))
}

private fun gameIconV2(id: String): ImageVector = when (id) {
    "warmup" -> Icons.Outlined.LocalBar
    "culture" -> Icons.Outlined.Psychology
    "kingscup" -> Icons.Outlined.EmojiEvents
    "impostor" -> Icons.Outlined.TheaterComedy
    "never_have_i_ever" -> Icons.Outlined.ChatBubbleOutline
    "truth_or_dare" -> Icons.Outlined.HelpOutline
    "most_likely" -> Icons.Outlined.TrackChanges
    else -> Icons.Filled.Casino
}

private fun gameVibeLabelRes(vibe: GameVibe): Int = when (vibe) {
    GameVibe.ALL -> R.string.home_v2_vibe_icebreaker
    GameVibe.ICEBREAKER -> R.string.home_v2_vibe_icebreaker
    GameVibe.DUELS -> R.string.home_v2_vibe_duels
    GameVibe.SPICY -> R.string.home_v2_vibe_spicy
    GameVibe.CHAOS -> R.string.home_v2_vibe_chaos
}

private fun gameVibeIconV2(vibe: GameVibe): ImageVector = when (vibe) {
    GameVibe.ALL, GameVibe.ICEBREAKER -> Icons.Outlined.Bolt
    GameVibe.DUELS -> Icons.Outlined.Style
    GameVibe.SPICY -> Icons.Outlined.LocalFireDepartment
    GameVibe.CHAOS -> Icons.Filled.Casino
}

private fun intensityLabelRes(intensity: GameIntensity): Int = when (intensity) {
    GameIntensity.SUAVE -> R.string.home_v2_intensity_soft
    GameIntensity.INTERMEDIO -> R.string.home_v2_intensity_medium
    GameIntensity.FUERTE -> R.string.home_v2_intensity_strong
    GameIntensity.EXTREMO -> R.string.home_v2_intensity_extreme
}
