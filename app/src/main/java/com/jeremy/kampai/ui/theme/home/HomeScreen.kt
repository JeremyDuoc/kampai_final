package com.jeremy.kampai.ui.theme.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.jeremy.kampai.R
import com.jeremy.kampai.domain.models.GameModel
import com.jeremy.kampai.domain.models.GameVibe
import com.jeremy.kampai.ui.theme.*
import com.jeremy.kampai.ui.theme.partymanager.PartyManagerViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onGameSelected: (String) -> Unit,
    onPartyManager: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val mostPlayedGames by viewModel.mostPlayedGames.collectAsState()
    val selectedVibe by viewModel.selectedVibe.collectAsState()
    val vibeGames by viewModel.vibeGames.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val showDisclaimer by viewModel.showDisclaimer.collectAsState()
    val showDiceModal by viewModel.showDiceModal.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val rolledGame by viewModel.rolledGame.collectAsState()
    val context = LocalContext.current

    // Juego actualmente destacado en el Hero card
    var customFeaturedGame by remember { mutableStateOf<GameModel?>(null) }
    val featuredGame = customFeaturedGame ?: mostPlayedGames.firstOrNull() ?: vibeGames.firstOrNull()

    // Color activo que tiñe dinámicamente la atmósfera
    val targetAtmosphereColor = featuredGame?.color ?: KColor.Purple
    val animatedAtmosphereColor by animateColorAsState(
        targetValue = targetAtmosphereColor,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "homeAtmosphereColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KColor.Base)
    ) {
        // Fondo atmosférico inmersivo con resplandor neón
        NeoAtmosphereBackground(activeColor = animatedAtmosphereColor) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 150.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. TOP HEADER: KAMPAI 乾杯 + PARTY STATUS PILL + AJUSTES
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KAMPAI✦",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = KColor.Foreground,
                                letterSpacing = (-1).sp
                            )
                            Overline(
                                text = "乾杯 / GAME NIGHT",
                                color = animatedAtmosphereColor
                            )
                        }

                        // Pill de Fiesta Activa / Crear Fiesta
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onPartyManager)
                        ) {
                            Pill(
                                text = if (players.isNotEmpty()) "● PARTY (${players.size})" else "● CREAR FIESTA",
                                tint = if (players.isNotEmpty()) KColor.Green else KColor.Cyan
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botón de Ajustes
                        SmallIconButton(
                            icon = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                            onClick = onNavigateToSettings
                        )
                    }
                }

                // 2. HERO HEADLINE
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Overline(
                            text = "LA NOCHE RECIÉN EMPIEZA",
                            color = KColor.Cyan
                        )
                        Text(
                            text = "¿Qué se juega\nesta noche?",
                            fontSize = 34.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.2).sp,
                            color = KColor.Foreground
                        )
                        Text(
                            text = "Diez juegos. Una sola fiesta. Cero vueltas.",
                            color = KColor.Secondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // 3. MOOD FILTER BAR (VIBE CHIPS - FULL BLEED EDGE-TO-EDGE)
                item {
                    VibeFilterBar(
                        selectedVibe = selectedVibe,
                        onVibeSelected = { vibe ->
                            viewModel.selectVibe(vibe)
                            val matches = if (vibe == GameVibe.ALL) mostPlayedGames else vibeGames.filter { it.vibe == vibe }
                            if (matches.isNotEmpty()) {
                                customFeaturedGame = matches.first()
                            }
                        }
                    )
                }

                // 4. HERO FEATURED GAME CARD (3D TILTED DECK)
                if (featuredGame != null) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            HeroGameCard(
                                game = featuredGame,
                                onClick = { onGameSelected(featuredGame.route) }
                            )
                        }
                    }
                }

                // 5. BANNER RÁPIDO: DADO DEL DESTINO
                item {
                    SectionHeading(
                        title = "¿No pueden decidir?",
                        aside = "RANDOM",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        GlassPanel(accent = KColor.Amber) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(KColor.Amber, Color(0xFFFF887C))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⚄",
                                        color = KColor.InkOnAccent,
                                        fontSize = 30.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.dice_title),
                                        fontWeight = FontWeight.Bold,
                                        color = KColor.Foreground,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = rolledGame?.let { "Último: " + stringResource(it.title) }
                                            ?: stringResource(R.string.dice_subtitle),
                                        color = KColor.Secondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                                        .clickable { viewModel.openDiceModal() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🎲",
                                        fontSize = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. BENTO GRID: EXPLORAR JUEGOS (2 COLUMNAS)
                item {
                    SectionHeading(
                        title = stringResource(R.string.home_categories),
                        aside = "${vibeGames.size} MODOS ↗",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                items(vibeGames.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { game ->
                            GameTile(
                                game = game,
                                modifier = Modifier.weight(1f),
                                onClick = { onGameSelected(game.route) }
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 7. PRÓXIMAMENTE / SUGERIR JUEGO
                item {
                    SectionHeading(
                        title = stringResource(R.string.home_coming_soon_eyebrow),
                        aside = "FEEDBACK",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        GlassPanel(accent = KColor.Cyan) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "💡",
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.home_coming_soon_card_title),
                                            fontWeight = FontWeight.Bold,
                                            color = KColor.Foreground,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "¿Tienes una idea o regla que debería estar en Kampai?",
                                            color = KColor.Secondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                KampaiButton(
                                    label = stringResource(R.string.home_coming_soon_card_cta).uppercase() + " ✉",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:game.kampai@gmail.com")
                                            putExtra(Intent.EXTRA_SUBJECT, "Sugerencia de juego para Kampai")
                                        }
                                        context.startActivity(intent)
                                    },
                                    accent = KColor.Cyan,
                                    primary = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // 8. ADMOB BANNER
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        NeoAdBanner()
                    }
                }
            }

            // DOCK FLOTANTE INFERIOR
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                FloatingControlDock(
                    playerCount = players.size,
                    accentColor = animatedAtmosphereColor,
                    onPartyClick = onPartyManager,
                    onDiceClick = { viewModel.openDiceModal() },
                    onSettingsClick = onNavigateToSettings
                )
            }
        }

        // MODAL SORTEO: DADO DEL DESTINO
        if (showDiceModal) {
            DiceOfDestinyDialog(
                isRolling = isRolling,
                selectedGame = rolledGame,
                onDismiss = { viewModel.closeDiceModal() },
                onReroll = { viewModel.rollDice() },
                onPlay = {
                    rolledGame?.let {
                        viewModel.closeDiceModal()
                        onGameSelected(it.route)
                    }
                }
            )
        }

        // DISCLAIMER LEGAL
        if (showDisclaimer) {
            NeoLegalDisclaimerDialog(onAccept = { viewModel.acceptDisclaimer() })
        }
    }
}

// ============================================================================
// BARRA DE FILTRO DE VIBRA (MOOD CHIPS)
// ============================================================================

@Composable
fun VibeFilterBar(
    selectedVibe: GameVibe,
    onVibeSelected: (GameVibe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(GameVibe.values()) { vibe ->
            val isSelected = vibe == selectedVibe
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) KColor.Purple.copy(alpha = 0.25f) else KColor.Raised.copy(alpha = 0.85f),
                animationSpec = tween(250),
                label = "vibeBg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) KColor.Purple else Color.White.copy(alpha = 0.14f),
                animationSpec = tween(250),
                label = "vibeBorder"
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(1.dp, borderColor, CircleShape)
                    .clickable { onVibeSelected(vibe) }
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(vibe.labelRes),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) KColor.Foreground else KColor.Secondary
                )
            }
        }
    }
}

// ============================================================================
// HERO FEATURED GAME CARD (3D TILTED DECK AESTHETIC)
// ============================================================================

@Composable
fun HeroGameCard(
    game: GameModel,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        game.color.copy(alpha = 0.42f),
                        Color(0xFF19132D),
                        KColor.Strong
                    )
                )
            )
            .border(1.dp, game.color.copy(alpha = 0.52f), shape)
    ) {
        // Pseudo-3D deck rotado en capas en la parte derecha
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-22).dp, y = (-8).dp)
                .size(width = 105.dp, height = 140.dp)
                .graphicsLayer { rotationZ = -8f }
                .clip(RoundedCornerShape(18.dp))
                .background(KColor.Cyan.copy(alpha = 0.28f))
                .border(1.dp, KColor.Cyan.copy(alpha = 0.52f), RoundedCornerShape(18.dp))
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-12).dp, y = 4.dp)
                .size(width = 106.dp, height = 142.dp)
                .graphicsLayer { rotationZ = 13f }
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(game.color, Color(0xFF40226A))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.44f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = game.iconEmoji,
                fontSize = 58.sp
            )
        }

        // Columna de información y CTA en la parte izquierda
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(18.dp)
                .fillMaxWidth(0.62f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Overline(
                text = "✦ DESTACADO",
                color = Color(0xFFDFC3FF)
            )

            Text(
                text = stringResource(game.title),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = (-0.8).sp,
                color = KColor.Foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stringResource(game.description),
                fontSize = 12.sp,
                color = KColor.Secondary,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(
                    text = "👥 " + game.playerRange,
                    tint = game.color
                )
                Pill(
                    text = game.intensity.label,
                    tint = game.intensity.color
                )
            }

            KampaiButton(
                label = "JUGAR AHORA →",
                onClick = onClick,
                accent = game.color,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============================================================================
// GAME TILE (BENTO GRID TILE)
// ============================================================================

@Composable
fun GameTile(
    game: GameModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(21.dp)

    Column(
        modifier = modifier
            .heightIn(min = 126.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(game.color.copy(alpha = 0.30f), KColor.Strong)
                )
            )
            .border(1.dp, game.color.copy(alpha = 0.27f), shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = game.iconEmoji,
                fontSize = 28.sp
            )

            if (game.isNew) {
                NeonBadge(
                    text = "NUEVO",
                    color = KColor.Pink
                )
            } else {
                Text(
                    text = game.playerRange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = KColor.Secondary
                )
            }
        }

        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = stringResource(game.title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 17.sp,
                color = KColor.Foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = game.intensity.label + " • " + stringResource(game.vibe.labelRes),
                fontSize = 11.sp,
                color = KColor.Secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// DOCK FLOTANTE INFERIOR (CÁPSULA UNIFICADA)
// ============================================================================

@Composable
fun FloatingControlDock(
    playerCount: Int,
    accentColor: Color,
    onPartyClick: () -> Unit,
    onDiceClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = KColor.Strong.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        modifier = Modifier
            .shadow(24.dp, CircleShape, spotColor = accentColor)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Acceso rápido al Party Manager
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onPartyClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = stringResource(R.string.party_manager),
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$playerCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = KColor.Foreground
                )
            }

            // Botón central del Dado
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(KColor.Amber, Color(0xFFFF887C))
                        )
                    )
                    .clickable(onClick = onDiceClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎲",
                    fontSize = 20.sp
                )
            }

            // Acceso rápido a Ajustes
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============================================================================
// MODAL SORTEO: DADO DEL DESTINO
// ============================================================================

@Composable
fun DiceOfDestinyDialog(
    isRolling: Boolean,
    selectedGame: GameModel?,
    onDismiss: () -> Unit,
    onReroll: () -> Unit,
    onPlay: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        GlassPanel(
            accent = KColor.Amber,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎲 " + stringResource(R.string.dice_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = KColor.Foreground
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Exhibidor de juego sorteado con resplandor neón
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(
                            selectedGame?.color?.copy(alpha = 0.25f) ?: KColor.Raised
                        )
                        .border(
                            2.dp,
                            selectedGame?.color ?: KColor.Amber,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedGame?.iconEmoji ?: "🎲",
                        fontSize = 52.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isRolling) stringResource(R.string.dice_rolling)
                    else (selectedGame?.let { stringResource(it.title) } ?: ""),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isRolling) KColor.Amber else KColor.Foreground,
                    textAlign = TextAlign.Center
                )

                if (!isRolling && selectedGame != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(selectedGame.description),
                        fontSize = 12.sp,
                        color = KColor.Secondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KampaiButton(
                        label = stringResource(R.string.dice_reroll),
                        onClick = onReroll,
                        primary = false,
                        accent = KColor.Amber,
                        modifier = Modifier.weight(1f)
                    )

                    KampaiButton(
                        label = stringResource(R.string.dice_play_now),
                        onClick = onPlay,
                        accent = selectedGame?.color ?: KColor.Amber,
                        enabled = !isRolling && selectedGame != null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ============================================================================
// ADMOB BANNER
// ============================================================================

@Composable
fun NeoAdBanner() {
    GlassPanel(
        accent = KColor.Purple.copy(alpha = 0.3f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.ad_label).uppercase(),
                fontSize = 9.sp,
                color = KColor.Muted,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            AndroidView(
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = "ca-app-pub-5499036778442602/1480575304"
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}

// ============================================================================
// DIÁLOGO LEGAL
// ============================================================================

@Composable
fun NeoLegalDisclaimerDialog(onAccept: () -> Unit) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        GlassPanel(
            accent = KColor.Danger,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.disclaimer_title),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = KColor.Danger
                )

                Text(
                    text = stringResource(R.string.disclaimer_text_1),
                    color = KColor.Foreground,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• " + stringResource(R.string.disclaimer_bullet_1), fontSize = 11.sp, color = KColor.Secondary)
                    Text("• " + stringResource(R.string.disclaimer_bullet_2), fontSize = 11.sp, color = KColor.Secondary)
                    Text("• " + stringResource(R.string.disclaimer_bullet_3), fontSize = 11.sp, color = KColor.Secondary)
                }

                Text(
                    text = stringResource(R.string.disclaimer_text_2),
                    fontWeight = FontWeight.Bold,
                    color = KColor.Foreground,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                KampaiButton(
                    label = stringResource(R.string.disclaimer_btn_accept),
                    onClick = onAccept,
                    accent = KColor.Danger,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}