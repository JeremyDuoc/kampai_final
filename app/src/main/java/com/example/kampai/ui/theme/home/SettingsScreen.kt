package com.example.kampai.ui.theme.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.ui.theme.AccentAmber
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val showSuggestionsDialog by viewModel.showSuggestionsDialog.collectAsState()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsState()

    val appVersion = stringResource(R.string.splash_version)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SettingsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            SettingsHeader(onBack = onBack)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SECCIÓN: PREFERENCIAS
                item {
                    SettingItem(
                        icon = "🌐",
                        title = stringResource(R.string.settings_language),
                        subtitle = stringResource(R.string.settings_language_select),
                        value = language.getDisplayName(),
                        onClick = { viewModel.showLanguageDialog() }
                    )
                }

                item {
                    SoundSettingItem(
                        isEnabled = soundEnabled,
                        onToggle = { viewModel.toggleSound() }
                    )
                }

                item {
                    ThemeSettingItem(
                        isDarkMode = isDarkMode,
                        onToggle = { viewModel.toggleDarkMode(it) }
                    )
                }

                // SECCIÓN: INFORMACIÓN
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(stringResource(R.string.settings_information))
                }

                item {
                    InfoItem(
                        icon = "🎮",
                        title = stringResource(R.string.settings_version),
                        value = appVersion
                    )
                }

                item {
                    InfoItem(
                        icon = "📱",
                        title = stringResource(R.string.settings_platform),
                        value = stringResource(R.string.settings_android)
                    )
                }

                // SECCIÓN: SOPORTE
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(stringResource(R.string.settings_support))
                }

                item {
                    ActionItem(
                        icon = "💡",
                        title = stringResource(R.string.settings_suggest),
                        subtitle = stringResource(R.string.settings_suggest_desc),
                        onClick = { viewModel.showSuggestionsDialog() }
                    )
                }

                item {
                    ActionItem(
                        icon = "🐛",
                        title = stringResource(R.string.settings_bug),
                        subtitle = stringResource(R.string.settings_bug_desc),
                        onClick = { viewModel.showBugReportDialog() }
                    )
                }

                item {
                    ActionItem(
                        icon = "⭐",
                        title = stringResource(R.string.settings_rate),
                        subtitle = stringResource(R.string.settings_rate_desc),
                        onClick = { viewModel.openPlayStore() }
                    )
                }

                // SECCIÓN: LEGAL
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(stringResource(R.string.settings_legal))
                }

                item {
                    ActionItem(
                        icon = "📜",
                        title = stringResource(R.string.settings_terms),
                        subtitle = stringResource(R.string.settings_terms_desc),
                        onClick = { viewModel.openTermsOfService() }
                    )
                }

                item {
                    ActionItem(
                        icon = "🔒",
                        title = stringResource(R.string.settings_privacy),
                        subtitle = stringResource(R.string.settings_privacy_desc),
                        onClick = { viewModel.openPrivacyPolicy() }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // --- DIÁLOGOS ---

        if (showSuggestionsDialog) {
            SuggestionsDialog(
                onDismiss = { viewModel.hideSuggestionsDialog() },
                onSend = { suggestion ->
                    viewModel.sendSuggestion(suggestion)
                    viewModel.hideSuggestionsDialog()
                }
            )
        }

        if (showLanguageDialog) {
            LanguageDialog(
                currentLanguage = language,
                onDismiss = { viewModel.hideLanguageDialog() },
                onLanguageSelected = { viewModel.changeLanguage(it) }
            )
        }
    }
}

// --- COMPOSABLES AUXILIARES ---

@Composable
fun LanguageDialog(
    currentLanguage: SettingsViewModel.Language,
    onDismiss: () -> Unit,
    onLanguageSelected: (SettingsViewModel.Language) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                PrimaryViolet.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(2.dp, PrimaryViolet.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                SettingsViewModel.Language.values().forEach { language ->
                    val isSelected = language == currentLanguage

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) PrimaryViolet.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable { onLanguageSelected(language) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = language.getDisplayName(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) PrimaryViolet else MaterialTheme.colorScheme.onSurface
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = PrimaryViolet
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.party_cancel))
                }
            }
        }
    }
}

@Composable
fun ThemeSettingItem(
    isDarkMode: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            Color(0xFF7C3AED).copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF7C3AED).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = if (isDarkMode) "🌙" else "☀️", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_theme),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkMode) stringResource(R.string.settings_dark_mode) else stringResource(R.string.settings_light_mode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggle(it) },
                modifier = Modifier.scale(1.2f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF7C3AED),
                    checkedTrackColor = Color(0xFF7C3AED).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color(0xFFF59E0B),
                    uncheckedTrackColor = Color(0xFFF59E0B).copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun SettingsBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryViolet.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SecondaryPink.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun SettingsHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    ),
                    color = PrimaryViolet
                )
            }

            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        ),
        color = AccentAmber,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingItem(
    icon: String,
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            PrimaryViolet.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = PrimaryViolet.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = icon, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentAmber
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = PrimaryViolet,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SoundSettingItem(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            SecondaryPink.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = SecondaryPink.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = if (isEnabled) "🔊" else "🔇", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_sound),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_sound_effects),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(1.2f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SecondaryPink,
                    checkedTrackColor = SecondaryPink.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: String,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActionItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            AccentAmber.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = AccentAmber.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = icon, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AccentAmber,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SuggestionsDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var suggestion by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(32.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                PrimaryViolet.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = PrimaryViolet.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💡 " + stringResource(R.string.settings_suggest),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.settings_suggest_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = suggestion,
                    onValueChange = { suggestion = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text(stringResource(R.string.settings_suggest_desc), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryViolet,
                        unfocusedBorderColor = PrimaryViolet.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.party_cancel), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (suggestion.isNotBlank()) {
                                onSend(suggestion)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryViolet
                        ),
                        enabled = suggestion.isNotBlank()
                    ) {
                        Text("Enviar 📧", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}