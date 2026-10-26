package com.jeremy.kampai.ui.theme.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

    val motion = rememberInfiniteTransition(label = "kampai_home_v2_motion")
    val featuredShift by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "featured_steam"
    )
    val sheenProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "game_sheen"
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
            contentPadding = PaddingValues(top = 18.dp, bottom = 142.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                HomeHeaderV2(
                    onSettings = onNavigateToSettings,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_v2_eyebrow),
                        color = Color(0xFF65DEE9),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.7.sp
                    )
                    Text(
                        text = stringResource(R.string.home_v2_headline),
                        color = Color(0xFFF9F6FB),
                        fontSize = 35.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.45).sp
                    )
                }
            }

            featuredGame?.let { game ->
                item {
                    FeaturedGameCardV2(
                        game = game,
                        source = featuredSource,
                        shift = featuredShift,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        onClick = { onGameSelected(game.route) }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            items(games.chunked(2)) { pair ->
                val startIndex = pair.firstOrNull()?.let { games.indexOf(it) } ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
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
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
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
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .blur(74.dp)
        ) {
            val w = size.width
            val h = size.height
            val dx = (drift - .5f) * w * .09f
            val dy = (drift - .5f) * h * .035f

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFB97918).copy(alpha = .40f), Color.Transparent),
                    center = Offset(-w * .03f + dx, h * .18f + dy),
                    radius = w * .72f
                ),
                center = Offset(-w * .03f + dx, h * .18f + dy),
                radius = w * .72f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFA42B82).copy(alpha = .27f), Color.Transparent),
                    center = Offset(w * 1.03f - dx, h * .73f - dy),
                    radius = w * .78f
                ),
                center = Offset(w * 1.03f - dx, h * .73f - dy),
                radius = w * .78f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF425FAE).copy(alpha = .13f), Color.Transparent),
                    center = Offset(w * .78f, h * .08f),
                    radius = w * .58f
                ),
                center = Offset(w * .78f, h * .08f),
                radius = w * .58f
            )
        }

        val stars = remember {
            listOf(
                Offset(.38f, .08f), Offset(.72f, .16f), Offset(.82f, .30f), Offset(.43f, .47f),
                Offset(.95f, .57f), Offset(.31f, .79f), Offset(.75f, .84f), Offset(.09f, .69f)
            )
        }
        Canvas(Modifier.matchParentSize()) {
            stars.forEachIndexed { index, p ->
                val phase = (drift + index * .13f) % 1f
                val alpha = .18f + kotlin.math.abs(.5f - phase) * .65f
                drawCircle(
                    color = Color.White.copy(alpha = alpha.coerceIn(.16f, .50f)),
                    radius = if (index % 3 == 0) 1.8f else 1.15f,
                    center = Offset(size.width * p.x, size.height * p.y)
                )
            }
        }
    }
}

@Composable
private fun HomeHeaderV2(onSettings: () -> Unit, modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "kampai_brand")
    val breathe by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brand_breathe"
    )
    val burstRotation by motion.animateFloat(
        initialValue = 5f,
        targetValue = 17f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brand_burst"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    translationY = -1.5f * breathe
                    scaleX = 1f + .006f * breathe
                    scaleY = 1f + .006f * breathe
                }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.app_name).uppercase(),
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.45).sp
                )
                Text(
                    text = stringResource(R.string.home_v2_logo_burst),
                    color = Color.White.copy(alpha = .90f + .10f * breathe),
                    fontSize = 25.sp,
                    modifier = Modifier
                        .offset(x = (-1).dp)
                        .graphicsLayer {
                            rotationZ = burstRotation
                            scaleX = 1f + .08f * breathe
                            scaleY = 1f + .08f * breathe
                            shadowElevation = 3f * breathe
                        }
                )
            }
            Text(
                text = stringResource(R.string.home_v2_brand_subtitle),
                color = FeaturedAmber.copy(alpha = .84f + .16f * breathe),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        GlassIconButtonV2(
            icon = Icons.Filled.Settings,
            contentDescription = stringResource(R.string.settings),
            onClick = onSettings
        )
    }
}

@Composable
private fun FeaturedGameCardV2(
    game: GameModel,
    source: FeaturedGameSource,
    shift: Float,
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
            .heightIn(min = 264.dp)
    ) {
        AnimatedFeaturedStrokeV2(
            colors = borderColors,
            shift = shift,
            blur = 30.dp,
            alpha = .25f
        )
        AnimatedFeaturedStrokeV2(
            colors = borderColors,
            shift = shift,
            blur = 0.dp,
            alpha = 1f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF34241D),
                            Color(0xFF302033),
                            Color(0xFF1C1930),
                            Color(0xFF17151F)
                        )
                    )
                )
                .clickable(onClick = onClick)
                .padding(17.dp)
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(FeaturedAmber.copy(alpha = .15f), Color.Transparent),
                        center = Offset(size.width * .08f, size.height * .05f),
                        radius = size.width * .76f
                    ),
                    center = Offset(size.width * .08f, size.height * .05f),
                    radius = size.width * .76f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(FeaturedPink.copy(alpha = .11f), Color.Transparent),
                        center = Offset(size.width * .95f, size.height * .91f),
                        radius = size.width * .63f
                    ),
                    center = Offset(size.width * .95f, size.height * .91f),
                    radius = size.width * .63f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 66.dp)
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
                        color = Color(0xFFEFD7F5),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(game.title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-.75).sp,
                    modifier = Modifier.fillMaxWidth(.66f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(game.description),
                    color = Color(0xFFC7BAC6),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth(.63f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPillV2(game.playerRange, FeaturedAmber)
                    MetaPillV2(stringResource(intensityLabelRes(game.intensity)), FeaturedLilac)
                }
            }

            FeaturedDeckV2(
                game = game,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 4.dp, y = (-2).dp)
            )

            AnimatedPlayButtonV2(
                onClick = onClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun BoxScope.AnimatedFeaturedStrokeV2(
    colors: List<Color>,
    shift: Float,
    blur: androidx.compose.ui.unit.Dp,
    alpha: Float
) {
    Canvas(
        modifier = Modifier
            .matchParentSize()
            .then(if (blur > 0.dp) Modifier.blur(blur) else Modifier)
            .graphicsLayer { this.alpha = alpha }
    ) {
        val travel = size.width * 4f
        val startX = -size.width * 2.0f + travel * shift
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(startX, 0f),
            end = Offset(startX + size.width * 4f, size.height)
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
    Box(modifier = modifier.size(width = 116.dp, height = 150.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .rotate(-10f)
                .offset(x = (-8).dp, y = 2.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = .45f))
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF285970), Color(0xFF203E57), Color(0xFF492B50))))
                .border(1.dp, Color(0xFF4AA0B1).copy(alpha = .75f), RoundedCornerShape(18.dp))
        )
        Box(
            Modifier
                .matchParentSize()
                .rotate(10f)
                .offset(x = 6.dp, y = 10.dp)
                .shadow(14.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = .50f))
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(FeaturedAmber, Color(0xFFA46A44), Color(0xFF51285E))))
                .border(1.dp, Color(0xFFFFDEA8).copy(alpha = .70f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = gameIconV2(game.id),
                contentDescription = null,
                tint = Color(0xFFFFE5D6),
                modifier = Modifier.size(54.dp)
            )
        }
    }
}

@Composable
private fun AnimatedPlayButtonV2(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "featured_cta")
    val shift by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "featured_cta_shift"
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .975f else 1f,
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
            .shadow(12.dp, RoundedCornerShape(15.dp), spotColor = FeaturedPink.copy(alpha = .22f))
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(FeaturedLilac, FeaturedPink, FeaturedAmber, FeaturedCoral, FeaturedLilac),
                    start = Offset(-400f + shift * 500f, 0f),
                    end = Offset(700f + shift * 500f, 200f)
                )
            )
            .border(1.dp, Color.White.copy(alpha = .22f), RoundedCornerShape(15.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.home_v2_play_now),
            color = Color(0xFF1E1424),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
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
    val localSheen = (sheenProgress + index * .17f) % 1f

    Box(
        modifier = modifier
            .height(142.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = palette.edge.copy(alpha = .12f),
                spotColor = Color.Black.copy(alpha = .88f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(palette.start, palette.end)
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            palette.edge.copy(alpha = .92f),
                            palette.edge.copy(alpha = .28f),
                            Color.White.copy(alpha = .15f),
                            palette.accent.copy(alpha = .72f)
                        )
                    )
                ),
                shape
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(12.dp)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val x = -size.width * .9f + localSheen * size.width * 2.45f
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = .07f), Color.Transparent),
                    start = Offset(x - size.width * .22f, 0f),
                    end = Offset(x + size.width * .22f, size.height)
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(7.dp, RoundedCornerShape(13.dp), spotColor = Color.Black.copy(alpha = .30f))
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
                    color = Color(0xFFF8EFFA).copy(alpha = .90f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column {
                Text(
                    text = stringResource(game.title),
                    color = Color.White,
                    fontSize = 13.5.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-.3).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFC7BACA),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = stringResource(intensityLabelRes(game.intensity)) + " · " + stringResource(game.vibe.labelRes),
                        color = Color(0xFFC7BACA),
                        fontSize = 9.4.sp,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, spotColor = Color.Black.copy(alpha = .55f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = .09f), Color(0xFFEFAFD1).copy(alpha = .07f))
                )
            )
            .border(1.dp, Color(0xFFE7BEE5).copy(alpha = .20f), shape)
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(49.dp)
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
                fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = lastGame?.let { stringResource(R.string.home_v2_last_random, stringResource(it.title)) }
                    ?: stringResource(R.string.dice_subtitle),
                color = Color(0xFFBFB1C2),
                fontSize = 10.5.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF1F182B).copy(alpha = .72f))
                .border(1.dp, Color(0xFFE4BBEE).copy(alpha = .20f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Casino, contentDescription = null, tint = Color.White.copy(alpha = .88f), modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun ComingSoonCardV2(onSuggest: () -> Unit) {
    val shape = RoundedCornerShape(23.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, spotColor = Color.Black.copy(alpha = .50f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = .09f), FeaturedPink.copy(alpha = .07f))))
            .border(1.dp, Color(0xFFF8DCEB).copy(alpha = .20f), shape)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FeaturedLilac.copy(alpha = .12f))
                    .border(1.dp, FeaturedLilac.copy(alpha = .25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = FeaturedLilac, modifier = Modifier.size(21.dp))
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
        Spacer(Modifier.height(13.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White.copy(alpha = .07f))
                .border(1.dp, Color.White.copy(alpha = .18f), RoundedCornerShape(15.dp))
                .clickable(onClick = onSuggest),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.home_v2_suggest_action),
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = .5.sp
            )
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
    Row(
        modifier = modifier
            .shadow(18.dp, CircleShape, spotColor = Color.Black.copy(alpha = .78f))
            .clip(CircleShape)
            .background(Color(0xFF181423).copy(alpha = .94f))
            .border(1.dp, Color(0xFFE1C7E8).copy(alpha = .23f), CircleShape)
            .padding(horizontal = 13.dp, vertical = 8.dp),
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
                .shadow(8.dp, CircleShape, spotColor = FeaturedCoral.copy(alpha = .22f))
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFFBC76), Color(0xFFF88D88))))
                .clickable(onClick = onDice),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Casino, contentDescription = stringResource(R.string.dice_title), tint = Color(0xFF111016), modifier = Modifier.size(23.dp))
        }

        DockActionV2(onClick = onSettings) {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings), tint = Color.White.copy(alpha = .90f), modifier = Modifier.size(21.dp))
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
    "warmup" -> GamePaletteV2(Color(0xFFF1B23F), Color(0xFFD982B8), Color(0xFF80500F), Color(0xFF372031))
    "culture" -> GamePaletteV2(Color(0xFFD85BD8), Color(0xFF937BFF), Color(0xFF671D71), Color(0xFF2F1944))
    "kingscup" -> GamePaletteV2(Color(0xFF9D82FF), Color(0xFF5FA9ED), Color(0xFF4A347B), Color(0xFF241D41))
    "impostor" -> GamePaletteV2(Color(0xFFFF746D), Color(0xFFD75FA5), Color(0xFF7D272B), Color(0xFF3C1D32))
    "never_have_i_ever" -> GamePaletteV2(Color(0xFF43D7D5), Color(0xFF678CFF), Color(0xFF026069), Color(0xFF1A2840))
    "truth_or_dare" -> GamePaletteV2(Color(0xFFEFB24B), Color(0xFFE98D6B), Color(0xFF875011), Color(0xFF3D2530))
    "most_likely" -> GamePaletteV2(Color(0xFFEA50DC), Color(0xFF9C6DFF), Color(0xFF77167A), Color(0xFF2F1840))
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

private fun intensityLabelRes(intensity: GameIntensity): Int = when (intensity) {
    GameIntensity.SUAVE -> R.string.home_v2_intensity_soft
    GameIntensity.INTERMEDIO -> R.string.home_v2_intensity_medium
    GameIntensity.FUERTE -> R.string.home_v2_intensity_strong
    GameIntensity.EXTREMO -> R.string.home_v2_intensity_extreme
}
