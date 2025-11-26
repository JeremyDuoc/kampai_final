package com.example.kampai.ui.theme.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.domain.models.GameModel
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import com.example.kampai.ui.theme.TextGray
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassicsScreen(
    viewModel: ClassicsViewModel = hiltViewModel(),
    onGameSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val games by viewModel.classicGames.collectAsState()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp


    val pagerState = rememberPagerState(
        pageCount = { games.size },
        initialPage = 0
    )

    val targetColor = remember(pagerState.currentPage, games) {
        if (games.isNotEmpty()) {
            games[pagerState.currentPage].color
        } else {
            PrimaryViolet
        }
    }


    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 800),
        label = "bgColor"
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F13))
    ) {

        AuroraBackground(activeColor = animatedBackgroundColor)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            ClassicsHeader(onBack = onBack)


            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (games.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 64.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.65f)
                    ) { page ->
                        val game = games[page]

                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val absoluteOffset = pageOffset.absoluteValue

                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - absoluteOffset.coerceIn(0f, 1f)
                        )

                        val alpha = lerp(
                            start = 0.6f,
                            stop = 1f,
                            fraction = 1f - absoluteOffset.coerceIn(0f, 1f)
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    // Rotación ligera para efecto 3D
                                    rotationY = pageOffset * 10f
                                }
                        ) {
                            CarouselGameCard(
                                game = game,
                                onClick = onGameSelected,
                                isFocused = (page == pagerState.currentPage)
                            )
                        }
                    }
                } else {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(games.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CarouselGameCard(
    game: GameModel,
    onClick: (String) -> Unit,
    isFocused: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "pressScale"
    )

    Card(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)   // Ancho fijo
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(game.route) },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 16.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fondo con gradiente vertical
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                game.color.copy(alpha = 0.8f),
                                Color(0xFF1A1A1A)
                            )
                        )
                    )
            )

            // Borde brillante si está enfocado
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(0.6f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                )
            }

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )

                    Text(
                        text = game.iconEmoji,
                        fontSize = 72.sp // Emoji Grande
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = game.title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(id = game.description),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onClick(game.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = game.color
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("JUGAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------
// FONDO "AURORA"
// -----------------------------------------------------------
@Composable
private fun AuroraBackground(activeColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val t by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) { // Blur alto para efecto líquido
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF120E1F), Color(0xFF000000))
            )
        )

        val x1 = width * 0.5f + (width * 0.3f) * kotlin.math.cos(t)
        val y1 = height * 0.3f + (height * 0.2f) * kotlin.math.sin(t)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(activeColor.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(x1, y1),
                radius = width * 0.8f * pulse
            ),
            center = Offset(x1, y1),
            radius = width * 0.8f * pulse
        )

        val x2 = width * 0.5f + (width * 0.3f) * kotlin.math.cos(t + 2f)
        val y2 = height * 0.7f + (height * 0.2f) * kotlin.math.sin(t + 2f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(activeColor.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(x2, y2),
                radius = width * 0.9f
            ),
            center = Offset(x2, y2),
            radius = width * 0.9f
        )
    }
}

@Composable
private fun ClassicsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(50.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = stringResource(R.string.classics_screen_title),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            ),
            color = Color.White
        )
    }
}


private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}