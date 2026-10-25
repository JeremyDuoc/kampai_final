package com.jeremy.kampai.ui.theme.partymanager

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.R
import com.jeremy.kampai.domain.models.*
import com.jeremy.kampai.ui.theme.*

@Composable
fun PartyManagerScreen(
    viewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val players by viewModel.players.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val context = LocalContext.current

    var shuffleDegrees by remember { mutableStateOf(0f) }
    val animatedShuffleRotation by animateFloatAsState(
        targetValue = shuffleDegrees,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "shuffleRotation"
    )

    NeoAtmosphereBackground(activeColor = KColor.Amber) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. TOP BAR
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallIconButton(
                            glyph = "‹",
                            description = stringResource(R.string.back),
                            onClick = onBack
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Overline(text = "SALA VIP", color = KColor.Secondary)
                        Spacer(modifier = Modifier.weight(1f))
                        if (players.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearAllPlayers() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(KColor.Danger.copy(alpha = 0.15f))
                                    .border(1.dp, KColor.Danger.copy(alpha = 0.35f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = KColor.Danger,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(42.dp))
                        }
                    }
                }

                // 2. HERO TITLE BLOCK
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Overline(text = "✦ TU GRUPO, TUS REGLAS", color = KColor.Amber)
                        Text(
                            text = "La crew está\ncasi lista.",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 37.sp,
                            letterSpacing = (-1).sp,
                            color = KColor.Foreground
                        )
                        Text(
                            text = "Arma tu equipo una vez. Se queda contigo durante toda la sesión.",
                            color = KColor.Secondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }

                // 3. PILLS COUNT
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Pill(
                            text = "${players.size} JUGADORES",
                            tint = if (players.size >= 2) KColor.Green else KColor.Amber
                        )
                        Pill(
                            text = "${players.count { it.isReady }} LISTOS",
                            tint = KColor.Purple
                        )
                    }
                }

                // 4. ROSTER CONTAINER
                item {
                    if (players.isEmpty()) {
                        EmptyPartyState(
                            onAddClick = { viewModel.toggleAddDialog() },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    } else {
                        GlassPanel(accent = KColor.Amber) {
                            players.forEachIndexed { index, player ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PlayerAvatarBadge(
                                        emoji = player.getDisplayEmoji(),
                                        color = player.getAvatarColor(),
                                        size = 44.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = player.name,
                                            color = KColor.Foreground,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "JUGADOR ${(index + 1).toString().padStart(2, '0')}",
                                            color = KColor.Muted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Ready toggle pill
                                    val readyTint = if (player.isReady) KColor.Green else KColor.Secondary
                                    Box(
                                        modifier = Modifier
                                            .heightIn(min = 40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(readyTint.copy(alpha = 0.12f))
                                            .border(1.dp, readyTint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                            .clickable { viewModel.togglePlayerReady(player.id) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (player.isReady) "✓ LISTO" else "○ PENDIENTE",
                                            color = readyTint,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Delete button
                                    IconButton(
                                        onClick = { viewModel.removePlayer(player.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.delete),
                                            tint = KColor.Muted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (index < players.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color.White.copy(alpha = 0.08f))
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. TOOLBAR BUTTONS
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KampaiButton(
                            label = "＋ AÑADIR",
                            onClick = { viewModel.toggleAddDialog() },
                            modifier = Modifier.weight(1f),
                            primary = false,
                            enabled = players.size < 12
                        )
                        KampaiButton(
                            label = "⤨ MEZCLAR",
                            onClick = {
                                if (players.size > 1) {
                                    shuffleDegrees += 360f
                                    viewModel.shufflePlayers()
                                    Toast.makeText(context, context.getString(R.string.party_shuffle_toast), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .rotate(animatedShuffleRotation),
                            primary = false,
                            strongHaptic = true,
                            enabled = players.size > 1
                        )
                    }
                }

                // 6. ORDER OF PLAY PANEL
                if (players.size >= 2) {
                    item {
                        Overline(text = "ORDEN DE JUEGO", color = KColor.Amber)
                        GlassPanel(accent = KColor.Amber) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "♛", color = KColor.Amber, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Turnos persistentes",
                                        fontWeight = FontWeight.Bold,
                                        color = KColor.Foreground,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = players.joinToString(" → ") { it.name },
                                        color = KColor.Secondary,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM CTA
            KampaiButton(
                label = if (players.size >= 2) "INICIAR LA FIESTA →" else "FALTAN ${2 - players.size} JUGADORES",
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                primary = players.size >= 2,
                enabled = players.size >= 2,
                strongHaptic = true
            )
        }

        if (showAddDialog) {
            AddVipPlayerDialog(
                onDismiss = { viewModel.toggleAddDialog() },
                onConfirm = { name, gender, emoji ->
                    viewModel.addPlayer(name, gender, emoji)
                }
            )
        }
    }
}

// ============================================================================
// LISTA DE JUGADORES VIP
// ============================================================================

@Composable
fun VipPlayersList(
    players: List<PlayerModel>,
    onRemovePlayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(players, key = { it.id }) { player ->
            VipPlayerCard(player = player, onRemove = { onRemovePlayer(player.id) })
        }
    }
}

@Composable
fun VipPlayerCard(player: PlayerModel, onRemove: () -> Unit) {
    val partyRoleRes = remember(player.colorIndex) {
        val roles = listOf(
            R.string.party_role_legend,
            R.string.party_role_tequila,
            R.string.party_role_immune,
            R.string.party_role_party_animal,
            R.string.party_role_judge,
            R.string.party_role_saint
        )
        roles[player.colorIndex % roles.size]
    }

    NeoGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = player.getAvatarColor().copy(alpha = 0.5f),
        glowColor = player.getAvatarColor().copy(alpha = 0.2f),
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con resplandor
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(player.getAvatarColor().copy(alpha = 0.45f), CyberSurfaceElevated)
                        )
                    )
                    .border(2.dp, player.getAvatarColor(), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.getDisplayEmoji(), fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NeonBadge(
                        text = stringResource(partyRoleRes),
                        color = player.getAvatarColor()
                    )
                    Text(
                        text = "${player.gender.emoji} " + stringResource(player.gender.nameRes),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AccentRed.copy(alpha = 0.12f))
                    .border(1.dp, AccentRed.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.delete),
                    tint = AccentRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ============================================================================
// ESTADO VACÍO
// ============================================================================

@Composable
fun EmptyPartyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(NeonPurple.copy(alpha = 0.15f))
                .border(2.dp, NeonPurple.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎭", fontSize = 54.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.party_empty_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.party_empty_subtitle),
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 30.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedButton(
            onClick = onAddClick,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f))
        ) {
            Text(
                text = stringResource(R.string.party_btn_add_player),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// ============================================================================
// DIÁLOGO CREAR JUGADOR VIP
// ============================================================================

@Composable
fun AddVipPlayerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Gender, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }
    var selectedEmoji by remember { mutableStateOf(AvatarEmojis.getRandomEmoji()) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val categories = listOf(
        Pair(R.string.avatar_cat_animals, AvatarEmojis.animals),
        Pair(R.string.avatar_cat_faces, AvatarEmojis.faces),
        Pair(R.string.avatar_cat_fantasy, AvatarEmojis.fantasy),
        Pair(R.string.avatar_cat_sports, AvatarEmojis.sports)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        NeoGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(2.dp),
            borderColor = NeonPurple,
            glowColor = NeonPurple.copy(alpha = 0.4f),
            cornerRadius = 28.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨ " + stringResource(R.string.party_title_new_player),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Avatar preview destacado con aura
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(NeonPurple.copy(alpha = 0.25f))
                        .border(2.dp, NeonPurple, CircleShape)
                        .shadow(16.dp, CircleShape, spotColor = NeonPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedEmoji, fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input de nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.party_input_name), color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = NeonPurple,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Selector de Género
                Text(
                    text = stringResource(R.string.party_label_gender),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Gender.values().forEach { gender ->
                        val isSelected = selectedGender == gender
                        val bg by animateColorAsState(if (isSelected) NeonPurple else CyberSurfaceElevated, label = "gBg")

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(bg)
                                .border(1.dp, if (isSelected) NeonPurple else Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                .clickable { selectedGender = gender },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = gender.emoji, fontSize = 16.sp)
                                Text(
                                    text = stringResource(gender.nameRes),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Selector de categoría de emojis
                Text(
                    text = stringResource(R.string.party_label_avatar),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.size) { index ->
                        val isCatSelected = selectedCategoryIndex == index
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isCatSelected) CyberCyan else CyberSurfaceElevated)
                                .clickable { selectedCategoryIndex = index }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = stringResource(categories[index].first),
                                fontSize = 10.sp,
                                fontWeight = if (isCatSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isCatSelected) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cuadrícula de Emojis
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val currentEmojis = categories[selectedCategoryIndex].second
                    items(currentEmojis) { emoji ->
                        val isEmojiSelected = emoji == selectedEmoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isEmojiSelected) NeonPurple.copy(alpha = 0.4f) else CyberSurfaceElevated)
                                .border(1.2.dp, if (isEmojiSelected) NeonPurple else Color.Transparent, CircleShape)
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = stringResource(R.string.party_btn_cancel),
                            color = Color.White
                        )
                    }

                    TactileButton(
                        onClick = {
                            if (name.isNotBlank()) onConfirm(name, selectedGender, selectedEmoji)
                        },
                        containerColor = NeonPurple,
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.party_btn_add),
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}