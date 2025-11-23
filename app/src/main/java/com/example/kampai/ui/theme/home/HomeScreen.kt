package com.example.kampai.ui.theme.home

import android.widget.Toast // Faltaba este import
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock // Faltaba este
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WineBar // Faltaba este
import androidx.compose.material3.Card // Faltaba este
import androidx.compose.material3.CardDefaults // Faltaba este
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Faltaba este
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.domain.models.GameModel
import com.example.kampai.ui.theme.AccentAmber // Asegúrate de que este color exista en Color.kt
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import com.example.kampai.ui.theme.TextGray
import com.example.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onGameSelected: (String) -> Unit,
    onNavigateToClassics: () -> Unit,
    onPartyManager: () -> Unit,
    onNavigateToSettings: () -> Unit  // ← NUEVO
) {
    val mostPlayedGames by viewModel.mostPlayedGames.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            HeaderSection(
                playerCount = players.size,
                onSettingsClick = onNavigateToSettings  // ← PASA EL CALLBACK
            )

            Spacer(modifier = Modifier.height(32.dp))

            // SECCIÓN 1: MÁS JUGADOS
            Text(
                text = "🔥 Más Jugados",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lista de los 3 juegos principales
            mostPlayedGames.forEachIndexed { index, game ->
                AnimatedGameCard(
                    game = game,
                    onClick = onGameSelected,
                    index = index
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SECCIÓN 2: CATEGORÍAS
            Text(
                text = "📂 Categorías",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón Clásicos
                CategoryCard(
                    title = "Clásicos",
                    icon = Icons.Filled.WineBar,
                    color1 = PrimaryViolet,
                    color2 = SecondaryPink,
                    onClick = onNavigateToClassics,
                    modifier = Modifier.weight(1f)
                )

                // Botón +18 (Premium)
                CategoryCard(
                    title = "+18 Hot",
                    subtitle = "(Próximamente)",
                    icon = Icons.Filled.Lock,
                    color1 = Color(0xFF1F1F1F),
                    color2 = Color(0xFF2D2D2D),
                    borderColor = AccentAmber,
                    onClick = {
                        Toast.makeText(context, "Versión Premium próximamente 🔥", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Espacio extra para que el FAB no tape contenido
            Spacer(modifier = Modifier.height(80.dp))
        }

        // FAB para Party Manager
        FloatingActionButton(
            onClick = onPartyManager,
            containerColor = PrimaryViolet,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(72.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = "Gestionar Jugadores",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
                if (players.isNotEmpty()) {
                    Text(
                        text = "${players.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color1: Color,
    color2: Color,
    borderColor: Color = Color.Transparent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(color1, color2)))
                .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if(borderColor != Color.Transparent) borderColor else Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(borderColor != Color.Transparent) borderColor else Color.White
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-80).dp)
                .size(250.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryViolet.copy(alpha = 0.3f),
                            PrimaryViolet.copy(alpha = 0.0f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SecondaryPink.copy(alpha = 0.25f),
                            SecondaryPink.copy(alpha = 0.0f)
                        )
                    )
                )
        )
    }
}

@Composable
fun HeaderSection(playerCount: Int, onSettingsClick: () -> Unit) {
    var isLogoVisible by remember { mutableStateOf(false) }

    val logoScale = animateFloatAsState(
        targetValue = if (isLogoVisible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val logoAlpha = animateFloatAsState(
        targetValue = if (isLogoVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isLogoVisible = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header con Logo y Botón Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Espacio vacío (balance visual)
            Spacer(modifier = Modifier.size(48.dp))

            // Logo en el centro
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryViolet.copy(alpha = 0.15f),
                                SecondaryPink.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_kampai),
                    contentDescription = "Logo Kampai",
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .scale(logoScale.value)
                        .graphicsLayer { alpha = logoAlpha.value },
                    contentScale = ContentScale.Fit
                )
            }

            // Botón Configuración (derecha)
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryViolet.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, PrimaryViolet.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Configuración",
                    tint = PrimaryViolet,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Elige tu juego favorito",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.graphicsLayer { alpha = logoAlpha.value }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (playerCount > 0) {
                "🍻 $playerCount jugador${if (playerCount != 1) "es" else ""} en la party"
            } else {
                "🍻 La fiesta comienza ahora"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (playerCount > 0) PrimaryViolet else TextGray,
            modifier = Modifier.graphicsLayer { alpha = logoAlpha.value }
        )
    }
}

// Esta función estaba sin usar en el código anterior, pero la dejo por si la necesitas luego
@Composable
fun GamesList(games: List<GameModel>, onGameSelected: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        itemsIndexed(games) { index, game ->
            AnimatedGameCard(
                game = game,
                onClick = onGameSelected,
                index = index
            )
        }
    }
}

@Composable
fun AnimatedGameCard(
    game: GameModel,
    onClick: (String) -> Unit,
    index: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetX by animateFloatAsState(
        targetValue = if (isVisible) 0f else 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "offsetX"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(index * 80L)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = offsetX
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = game.color.copy(alpha = 0.3f),
                    spotColor = game.color.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            game.color.copy(alpha = 0.08f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            game.color.copy(alpha = 0.4f),
                            game.color.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onClick(game.route)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = game.color,
                        spotColor = game.color
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                game.color.copy(alpha = 0.4f),
                                game.color.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = game.color.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = game.iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                game.color.copy(alpha = 0.8f),
                                game.color.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}