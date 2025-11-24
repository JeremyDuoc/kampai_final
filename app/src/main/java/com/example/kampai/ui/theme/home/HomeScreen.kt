package com.example.kampai.ui.theme.home

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WineBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.domain.models.GameModel
import com.example.kampai.ui.theme.AccentAmber
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
    onNavigateToHot: () -> Unit, // <--- NUEVO CALLBACK PARA IR AL MODO HOT
    onPartyManager: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val mostPlayedGames by viewModel.mostPlayedGames.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val showDisclaimer by viewModel.showDisclaimer.collectAsState()

    // --- INTERRUPTOR DE MODO PREMIUM ---
    // Pon esto en FALSE cuando subas a la tienda para bloquearlo.
    // Pon esto en TRUE para que tu amigo (y tú) puedan probarlo gratis.
    val isPremiumUnlocked = true
    // -----------------------------------

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val hotMessage = stringResource(R.string.home_hot_message)

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
                onSettingsClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(32.dp))

            // SECCIÓN 1: MÁS JUGADOS
            Text(
                text = stringResource(R.string.home_most_played),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                text = stringResource(R.string.home_categories),
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
                    title = stringResource(R.string.home_classics),
                    icon = Icons.Filled.WineBar,
                    color1 = PrimaryViolet,
                    color2 = SecondaryPink,
                    onClick = onNavigateToClassics,
                    modifier = Modifier.weight(1f)
                )

                // Botón +18 Hot (Lógica Premium)
                if (isPremiumUnlocked) {
                    // VERSIÓN DESBLOQUEADA (Para tu amigo)
                    CategoryCard(
                        title = "AfterDark", // O usa un string resource
                        subtitle = "Modo Picante 🔥",
                        icon = Icons.Filled.LocalFireDepartment,
                        color1 = Color(0xFFD32F2F), // Rojo Oscuro
                        color2 = Color(0xFF8B0000), // Rojo Vino
                        borderColor = Color(0xFFFFD700), // Dorado
                        onClick = onNavigateToHot, // <--- NAVEGA AL JUEGO
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // VERSIÓN BLOQUEADA (Para la tienda)
                    CategoryCard(
                        title = stringResource(R.string.home_hot_plus18),
                        subtitle = stringResource(R.string.home_hot_coming_soon),
                        icon = Icons.Filled.Lock,
                        color1 = Color(0xFF1F1F1F),
                        color2 = Color(0xFF2D2D2D),
                        borderColor = AccentAmber.copy(alpha = 0.5f),
                        onClick = {
                            // Aquí abrirías el diálogo de compra en el futuro
                            Toast.makeText(context, hotMessage, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

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
                    contentDescription = stringResource(R.string.content_desc_manage_players),
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

        if (showDisclaimer) {
            LegalDisclaimerDialog(
                onAccept = { viewModel.acceptDisclaimer() }
            )
        }
    }
}

// ... (El resto de componentes CategoryCard, AnimatedBackground, etc. se mantienen igual abajo)
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
    val offset1 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset1")
    val offset2 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = -360f, animationSpec = infiniteRepeatable(animation = tween(15000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset2")

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.offset(x = (-80).dp, y = (-80).dp).size(250.dp).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(PrimaryViolet.copy(alpha = 0.3f), PrimaryViolet.copy(alpha = 0.0f)))))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 80.dp, y = 80.dp).size(300.dp).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(SecondaryPink.copy(alpha = 0.25f), SecondaryPink.copy(alpha = 0.0f)))))
    }
}

@Composable
fun HeaderSection(playerCount: Int, onSettingsClick: () -> Unit) {
    var isLogoVisible by remember { mutableStateOf(false) }
    val resources = LocalContext.current.resources
    val logoScale = animateFloatAsState(targetValue = if (isLogoVisible) 1f else 0.7f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "logoScale")
    val logoAlpha = animateFloatAsState(targetValue = if (isLogoVisible) 1f else 0f, animationSpec = tween(800), label = "logoAlpha")

    LaunchedEffect(Unit) { delay(100); isLogoVisible = true }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Spacer(modifier = Modifier.size(48.dp))
            Box(
                modifier = Modifier.height(100.dp).weight(1f).clip(RoundedCornerShape(16.dp)).background(brush = Brush.horizontalGradient(colors = listOf(PrimaryViolet.copy(alpha = 0.15f), SecondaryPink.copy(alpha = 0.15f)))).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(id = R.drawable.logo_kampai), contentDescription = stringResource(R.string.content_desc_logo), modifier = Modifier.height(80.dp).fillMaxWidth().scale(logoScale.value).graphicsLayer { alpha = logoAlpha.value }, contentScale = ContentScale.Fit)
            }
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(48.dp).background(PrimaryViolet.copy(alpha = 0.2f), CircleShape).border(2.dp, PrimaryViolet.copy(alpha = 0.5f), CircleShape)) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = stringResource(R.string.content_desc_settings), tint = PrimaryViolet, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.home_welcome), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.graphicsLayer { alpha = logoAlpha.value })
        Spacer(modifier = Modifier.height(4.dp))
        val subtitleText = if (playerCount > 0) resources.getQuantityString(R.plurals.home_player_count_plural, playerCount, playerCount) else stringResource(R.string.home_party_subtitle)
        Text(text = subtitleText, style = MaterialTheme.typography.bodyMedium, color = if (playerCount > 0) PrimaryViolet else TextGray, modifier = Modifier.graphicsLayer { alpha = logoAlpha.value })
    }
}

@Composable
fun AnimatedGameCard(game: GameModel, onClick: (String) -> Unit, index: Int) {
    var isVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetX by animateFloatAsState(targetValue = if (isVisible) 0f else 100f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), label = "offsetX")
    val alpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, animationSpec = tween(500), label = "alpha")
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium), label = "scale")

    LaunchedEffect(Unit) { delay(index * 80L); isVisible = true }

    Box(modifier = Modifier.fillMaxWidth().graphicsLayer { translationX = offsetX; this.alpha = alpha; scaleX = scale; scaleY = scale }) {
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp).shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), ambientColor = game.color.copy(alpha = 0.3f), spotColor = game.color.copy(alpha = 0.3f)).clip(RoundedCornerShape(24.dp)).background(brush = Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.surface, game.color.copy(alpha = 0.08f)))).border(width = 2.dp, brush = Brush.horizontalGradient(colors = listOf(game.color.copy(alpha = 0.4f), game.color.copy(alpha = 0.1f))), shape = RoundedCornerShape(24.dp)).clickable(interactionSource = interactionSource, indication = null) { onClick(game.route) }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(80.dp).shadow(elevation = 16.dp, shape = RoundedCornerShape(20.dp), ambientColor = game.color, spotColor = game.color).clip(RoundedCornerShape(20.dp)).background(brush = Brush.radialGradient(colors = listOf(game.color.copy(alpha = 0.4f), game.color.copy(alpha = 0.15f)))).border(width = 2.dp, color = game.color.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(id = game.iconRes), contentDescription = stringResource(R.string.content_desc_icon), tint = Color.Unspecified, modifier = Modifier.size(70.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(text = stringResource(id = game.title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = stringResource(id = game.description), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp), color = TextGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(game.color.copy(alpha = 0.8f), game.color.copy(alpha = 0.6f)))), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.content_desc_play), tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
            }
        }
    }
}

// ... (LegalDisclaimerDialog y otras funciones auxiliares si las tenías abajo, déjalas aquí) ...
@Composable
fun LegalDisclaimerDialog(onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(text = stringResource(R.string.disclaimer_title), fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
        text = {
            Column {
                Text(stringResource(R.string.disclaimer_text_1))
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.disclaimer_bullet_1), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.disclaimer_bullet_2), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.disclaimer_bullet_3), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.disclaimer_text_2), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        confirmButton = {
            Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.disclaimer_btn_accept), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, shape = RoundedCornerShape(16.dp)
    )
}