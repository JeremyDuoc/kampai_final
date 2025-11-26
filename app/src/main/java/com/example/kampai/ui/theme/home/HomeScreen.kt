package com.example.kampai.ui.theme.home

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.domain.models.GameModel
import com.example.kampai.ui.theme.*
import com.example.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onGameSelected: (String) -> Unit,
    onNavigateToClassics: () -> Unit,
    onNavigateToHot: () -> Unit,
    onPartyManager: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val mostPlayedGames by viewModel.mostPlayedGames.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val showDisclaimer by viewModel.showDisclaimer.collectAsState()

    // Lógica Premium (Activada para pruebas)
    val isPremiumUnlocked = false
    val context = LocalContext.current
    val hotMessage = stringResource(R.string.home_hot_message)


    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Scroll para la pantalla completa
    val scrollState = rememberScrollState()

    // Estado del Carrusel
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { mostPlayedGames.size }
    )

    val targetColor = remember(pagerState.currentPage, mostPlayedGames) {
        if (mostPlayedGames.isNotEmpty()) {
            mostPlayedGames[pagerState.currentPage].color
        } else {
            PrimaryViolet
        }
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A)) // Fondo base oscuro
    ) {
        // 1. Fondo Líquido
        PremiumLiquidBackground(activeColor = animatedBackgroundColor)

        // Contenido Principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp), // Espacio para el FAB
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            HomeHeaderSection(
                playerCount = players.size,
                onSettingsClick = onNavigateToSettings,
                onProfileClick = onPartyManager,
                accentColor = animatedBackgroundColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Título: Más Jugados
            SectionTitle(title = stringResource(R.string.home_most_played))

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Carrusel de Juegos
            if (mostPlayedGames.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 70.dp), // Muestra parte de las cartas laterales
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Altura fija para el carrusel
                ) { page ->
                    val game = mostPlayedGames[page]

                    // Efectos visuales al deslizar (Escala y Opacidad)
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absoluteOffset = pageOffset.absoluteValue
                    val scale = lerp(0.85f, 1f, 1f - absoluteOffset.coerceIn(0f, 1f))
                    val alpha = lerp(0.5f, 1f, 1f - absoluteOffset.coerceIn(0f, 1f))

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                rotationY = pageOffset * 5f // Rotación 3D sutil
                            }
                    ) {
                        // Usamos la nueva tarjeta con Emojis
                        CarouselGameCard(
                            game = game,
                            onClick = { onGameSelected(game.route) },
                            isFocused = page == pagerState.currentPage
                        )
                    }
                }
            } else {
                // Loading o estado vacío
                Box(modifier = Modifier.height(300.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryViolet)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Título: Categorías
            SectionTitle(title = stringResource(R.string.home_categories))

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Botones de Categorías
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón Clásicos
                GlassCategoryCard(
                    title = stringResource(R.string.home_classics),
                    icon = Icons.Filled.WineBar,
                    accentColor = PrimaryViolet,
                    onClick = onNavigateToClassics,
                    modifier = Modifier.weight(1f)
                )

                // Botón AfterDark / Premium
                if (isPremiumUnlocked) {
                    GlassCategoryCard(
                        title = "AfterDark",
                        subtitle = "🔥",
                        icon = Icons.Filled.LocalFireDepartment,
                        accentColor = Color(0xFFD32F2F),
                        isSpecial = true,
                        onClick = onNavigateToHot,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    GlassCategoryCard(
                        title = stringResource(R.string.home_hot_plus18),
                        subtitle = stringResource(R.string.home_hot_coming_soon),
                        icon = Icons.Filled.Lock,
                        accentColor = TextGray,
                        onClick = { Toast.makeText(context, hotMessage, Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. FAB (Botón Flotante de Jugadores)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            HomeFloatingButton(
                playerCount = players.size,
                onClick = onPartyManager,
                accentColor = animatedBackgroundColor

            )
        }

        // Disclaimer Legal
        if (showDisclaimer) {
            LegalDisclaimerDialog(onAccept = { viewModel.acceptDisclaimer() })
        }
    }
}

// =================================================================
// COMPONENTES UI
// =================================================================

@Composable
fun PremiumLiquidBackground(activeColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse), label = "o1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1000f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse), label = "o2"
    )
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "s1"
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
        drawRect(Color(0xFF0F0F1A)) // Base oscura

        // Orbe Principal
        drawCircle(
            color = activeColor.copy(alpha = 0.5f),
            radius = size.minDimension * 0.7f * scalePulse,
            center = center.copy(x = size.width * 0.2f + offset1 * 0.3f, y = size.height * 0.3f)
        )

        // Orbe Secundario
        drawCircle(
            color = activeColor.copy(alpha = 0.25f),
            radius = size.minDimension * 0.8f,
            center = center.copy(x = size.width * 0.8f - offset2 * 0.2f, y = size.height * 0.7f)
        )
    }
}

@Composable
fun CarouselGameCard(
    game: GameModel,
    onClick: () -> Unit,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 16.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo de la carta
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                game.color.copy(alpha = 0.8f),
                                Color(0xFF1E1E2C)
                            )
                        )
                    )
            )

            // Borde brillante
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Brush.verticalGradient(listOf(Color.White.copy(0.5f), Color.Transparent)), RoundedCornerShape(32.dp))
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- EMOJI GIGANTE ---
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(24.dp, CircleShape, spotColor = game.color)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = game.iconEmoji, fontSize = 72.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Títulos
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = game.title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 26.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = game.description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Jugar
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.content_desc_play).uppercase(),
                        color = game.color,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GlassCategoryCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color,
    isSpecial: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF252530).copy(alpha = 0.6f))
            .border(1.dp, Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.5f), Color.Transparent)), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        // Brillo de fondo
        Box(modifier = Modifier.size(80.dp).offset((-20).dp, (-20).dp).alpha(0.2f).background(accentColor, CircleShape).blur(30.dp))

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isSpecial) Color(0xFFFFD700) else Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
            }
        }
    }
}

@Composable
fun HomeHeaderSection(playerCount: Int, onSettingsClick: () -> Unit, onProfileClick: () -> Unit, accentColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            // Botón Perfil
            Surface(onClick = onProfileClick, shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.1f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.People, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "$playerCount", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Logo Texto
            Text(text = "KAMPAI", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp), color = Color.White)

            // Botón Settings
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(44.dp).background(Color.White.copy(0.1f), CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape)) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtítulo
        val resources = LocalContext.current.resources
        val subtitleText = if (playerCount > 0) resources.getQuantityString(R.plurals.home_player_count_plural, playerCount, playerCount) else stringResource(R.string.home_party_subtitle)
        Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(accentColor.copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(text = subtitleText, style = MaterialTheme.typography.labelMedium, color = TextWhite.copy(alpha = 1f))
        }
    }
}

@Composable
fun HomeFloatingButton(playerCount: Int, onClick: () -> Unit, accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.05f, animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "fabScale")

    FloatingActionButton(onClick = onClick, containerColor = accentColor, modifier = Modifier.size(72.dp).scale(scale).border(2.dp, Color.White.copy(0.3f), CircleShape).shadow(16.dp, CircleShape, spotColor = PrimaryViolet), shape = CircleShape) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = Icons.Filled.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            if (playerCount > 0) Text(text = "$playerCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 22.sp), color = Color.White, modifier = Modifier.padding(horizontal = 24.dp))
}

@Composable
fun LegalDisclaimerDialog(onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(text = stringResource(R.string.disclaimer_title), fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
        text = {
            Column {
                Text(stringResource(R.string.disclaimer_text_1), color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Text("• " + stringResource(R.string.disclaimer_bullet_1), fontWeight = FontWeight.Medium, color = Color.Black)
                Text("• " + stringResource(R.string.disclaimer_bullet_2), fontWeight = FontWeight.Medium, color = Color.Black)
                Text("• " + stringResource(R.string.disclaimer_bullet_3), fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.disclaimer_text_2), fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        confirmButton = {
            Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.disclaimer_btn_accept), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// Función auxiliar para interpolación lineal
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}