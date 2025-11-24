package com.example.kampai.ui.theme.partymanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.domain.models.*
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import kotlinx.coroutines.delay

@Composable
fun PartyManagerScreen(
    viewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit,
    // VARIABLE DE CONTROL: Pásala como 'true' desde MainActivity si el usuario pagó
    isPremiumUnlocked: Boolean = true
) {
    val players by viewModel.players.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PartyBackground()

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            PartyHeader(
                playerCount = players.size,
                onBack = onBack,
                onClearAll = { if (players.isNotEmpty()) viewModel.clearAllPlayers() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (players.isEmpty()) {
                EmptyPartyState(modifier = Modifier.weight(1f))
            } else {
                PlayersList(
                    players = players,
                    onRemovePlayer = { viewModel.removePlayer(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.toggleAddDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().height(64.dp).background(brush = Brush.horizontalGradient(colors = listOf(PrimaryViolet, SecondaryPink)), shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.party_btn_add_player), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (showAddDialog) {
            AddPlayerDialog(
                onDismiss = { viewModel.toggleAddDialog() },
                showAdvancedOptions = isPremiumUnlocked, // <--- AQUÍ ACTIVAMOS LA MAGIA
                onConfirm = { name, gender, emoji, attraction, vibe ->
                    viewModel.addPlayer(name, gender, emoji, attraction, vibe)
                }
            )
        }
    }
}

@Composable
fun AddPlayerDialog(
    onDismiss: () -> Unit,
    showAdvancedOptions: Boolean,
    onConfirm: (String, Gender, String, Attraction, Vibe) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }
    var selectedEmoji by remember { mutableStateOf(AvatarEmojis.getRandomEmoji()) }

    // Nuevos campos (con valores por defecto seguros)
    var selectedAttraction by remember { mutableStateOf(Attraction.BOTH) }
    var selectedVibe by remember { mutableStateOf(Vibe.BOLD) }

    var showEmojiPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth(0.95f)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.party_dialog_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.party_input_name)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryViolet, focusedLabelColor = PrimaryViolet, cursorColor = PrimaryViolet, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(R.string.party_label_gender), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Gender.values().forEach { gender ->
                        GenderChip(gender = gender, isSelected = selectedGender == gender, onClick = { selectedGender = gender }, modifier = Modifier.weight(1f))
                    }
                }

                // --- SECCIÓN PREMIUM: OPCIONES AVANZADAS ---
                if (showAdvancedOptions) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = stringResource(R.string.party_label_attraction), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Attraction.values().forEach { attr ->
                            SelectableChip(
                                text = stringResource(attr.nameRes),
                                isSelected = selectedAttraction == attr,
                                onClick = { selectedAttraction = attr },
                                color = PrimaryViolet
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = stringResource(R.string.party_label_vibe), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Vibe.values().forEach { vibe ->
                            SelectableChip(
                                text = stringResource(vibe.nameRes),
                                isSelected = selectedVibe == vibe,
                                onClick = { selectedVibe = vibe },
                                color = SecondaryPink
                            )
                        }
                    }
                }
                // ------------------------------------------

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = stringResource(R.string.party_label_avatar), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                AvatarSelector(selectedEmoji = selectedEmoji, onEmojiSelected = { selectedEmoji = it }, showPicker = showEmojiPicker, onTogglePicker = { showEmojiPicker = !showEmojiPicker })

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                        Text(stringResource(R.string.party_btn_cancel))
                    }
                    Button(onClick = { if (name.isNotBlank()) onConfirm(name, selectedGender, selectedEmoji, selectedAttraction, selectedVibe) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet), enabled = name.isNotBlank()) {
                        Text(stringResource(R.string.party_btn_add))
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableChip(text: String, isSelected: Boolean, onClick: () -> Unit, color: Color) {
    val bgColor by animateColorAsState(if (isSelected) color else Color.Transparent)
    val textColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.2f)

    Box(
        modifier = Modifier.height(36.dp).clip(RoundedCornerShape(50)).background(bgColor).border(1.dp, borderColor, RoundedCornerShape(50)).clickable { onClick() }.padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 12.sp, color = textColor, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PartyBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val offset by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset")
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.align(Alignment.TopStart).offset(x = (-100).dp, y = (-100).dp).size(300.dp).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(PrimaryViolet.copy(alpha = 0.3f), Color.Transparent))))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp).size(350.dp).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(SecondaryPink.copy(alpha = 0.25f), Color.Transparent))))
    }
}

@Composable
fun PartyHeader(playerCount: Int, onBack: () -> Unit, onClearAll: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.party_manager_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 24.sp), color = PrimaryViolet)
            val countText = if (playerCount == 1) stringResource(R.string.party_players_count_one, playerCount) else stringResource(R.string.party_players_count, playerCount)
            Text(text = countText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (playerCount > 0) {
            IconButton(onClick = onClearAll, modifier = Modifier.size(48.dp).background(Color.Red.copy(alpha = 0.15f), CircleShape)) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = Color.Red)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun EmptyPartyState(modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")
    LaunchedEffect(Unit) { delay(100); isVisible = true }
    Column(modifier = modifier.fillMaxWidth().scale(scale), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "🎭", fontSize = 120.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(R.string.party_empty_title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.party_empty_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun PlayersList(players: List<PlayerModel>, onRemovePlayer: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(players, key = { it.id }) { player -> PlayerCard(player = player, onRemove = { onRemovePlayer(player.id) }) }
    }
}

@Composable
fun PlayerCard(player: PlayerModel, onRemove: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    val offsetX by animateFloatAsState(targetValue = if (isVisible) 0f else 100f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "offsetX")
    LaunchedEffect(Unit) { delay(50); isVisible = true }
    Card(modifier = Modifier.fillMaxWidth().offset(x = offsetX.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Row(modifier = Modifier.fillMaxWidth().background(brush = Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.surface, player.getAvatarColor().copy(alpha = 0.1f)))).border(width = 2.dp, color = player.getAvatarColor().copy(alpha = 0.4f), shape = RoundedCornerShape(20.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(brush = Brush.radialGradient(colors = listOf(player.getAvatarColor().copy(alpha = 0.6f), player.getAvatarColor().copy(alpha = 0.3f)))), contentAlignment = Alignment.Center) {
                Text(text = player.getDisplayEmoji(), fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = player.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(text = stringResource(player.gender.nameRes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(40.dp).background(Color.Red.copy(alpha = 0.15f), CircleShape)) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.delete), tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AvatarSelector(selectedEmoji: String, onEmojiSelected: (String) -> Unit, showPicker: Boolean, onTogglePicker: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onTogglePicker), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedEmoji, fontSize = 40.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = stringResource(R.string.party_avatar_selected), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = stringResource(R.string.party_avatar_change), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    }
                }
                Text(text = if (showPicker) "▲" else "▼", color = Color.White)
            }
        }
        AnimatedVisibility(visible = showPicker, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                EmojiCategory(title = stringResource(R.string.avatar_cat_animals), emojis = AvatarEmojis.animals, selectedEmoji = selectedEmoji, onEmojiSelected = { onEmojiSelected(it); onTogglePicker() })
                EmojiCategory(title = stringResource(R.string.avatar_cat_faces), emojis = AvatarEmojis.faces, selectedEmoji = selectedEmoji, onEmojiSelected = { onEmojiSelected(it); onTogglePicker() })
                EmojiCategory(title = stringResource(R.string.avatar_cat_fantasy), emojis = AvatarEmojis.fantasy, selectedEmoji = selectedEmoji, onEmojiSelected = { onEmojiSelected(it); onTogglePicker() })
                EmojiCategory(title = stringResource(R.string.avatar_cat_sports), emojis = AvatarEmojis.sports, selectedEmoji = selectedEmoji, onEmojiSelected = { onEmojiSelected(it); onTogglePicker() })
            }
        }
    }
}

@Composable
fun EmojiCategory(title: String, emojis: List<String>, selectedEmoji: String, onEmojiSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryViolet, modifier = Modifier.padding(vertical = 8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(emojis) { emoji -> EmojiButton(emoji = emoji, isSelected = emoji == selectedEmoji, onClick = { onEmojiSelected(emoji) }) }
        }
    }
}

@Composable
fun EmojiButton(emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isSelected) PrimaryViolet.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)).border(width = if (isSelected) 2.dp else 0.dp, color = PrimaryViolet, shape = CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text = emoji, fontSize = 28.sp)
    }
}

@Composable
fun GenderChip(gender: Gender, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(targetValue = if (isSelected) PrimaryViolet else Color.White.copy(alpha = 0.1f), label = "bg")
    Box(modifier = modifier.height(56.dp).clip(RoundedCornerShape(16.dp)).background(backgroundColor).border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) PrimaryViolet else Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = gender.emoji, fontSize = 20.sp)
            Text(text = stringResource(gender.nameRes), fontSize = 11.sp, color = if (isSelected) Color.White else Color.Gray)
        }
    }
}