package com.jeremy.kampai.ui.theme.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.R
import com.jeremy.kampai.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSupportClick: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val showSuggestionsDialog by viewModel.showSuggestionsDialog.collectAsState()
    val showBugReportDialog by viewModel.showBugReportDialog.collectAsState()

    val appVersion = stringResource(R.string.splash_version)

    NeoAtmosphereBackground(activeColor = SolarAmber) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            // Header estilo Centro de Control
            NeoSettingsHeader(onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // PANEL 1: AUDIO
                item {
                    SettingsSectionLabel(title = stringResource(R.string.settings_sound))
                    Spacer(modifier = Modifier.height(8.dp))
                    NeoSoundControlCard(
                        isEnabled = soundEnabled,
                        onToggle = { viewModel.toggleSound() }
                    )
                }

                // PANEL 2: IDIOMA
                item {
                    SettingsSectionLabel(title = stringResource(R.string.settings_language))
                    Spacer(modifier = Modifier.height(8.dp))
                    NeoLanguageSelectorRow(
                        currentLanguage = language,
                        onSelectLanguage = { viewModel.changeLanguage(it) }
                    )
                }

                // PANEL 3: APOYO AL CREADOR (GOLD CARD)
                item {
                    NeoSupportCreatorCard(onSupportClick = onSupportClick)
                }

                // PANEL 4: FEEDBACK & COMUNIDAD
                item {
                    SettingsSectionLabel(title = stringResource(R.string.settings_support))
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        NeoActionCard(
                            emoji = "💡",
                            title = stringResource(R.string.settings_suggest),
                            subtitle = stringResource(R.string.settings_suggest_desc),
                            accentColor = SolarAmber,
                            onClick = { viewModel.showSuggestionsDialog() }
                        )

                        NeoActionCard(
                            emoji = "🐛",
                            title = stringResource(R.string.settings_bug),
                            subtitle = stringResource(R.string.settings_bug_desc),
                            accentColor = NeonPink,
                            onClick = { viewModel.showBugReportDialog() }
                        )

                        NeoActionCard(
                            emoji = "⭐",
                            title = stringResource(R.string.settings_rate),
                            subtitle = stringResource(R.string.settings_rate_desc),
                            accentColor = ToxicGreen,
                            onClick = { viewModel.openPlayStore() }
                        )
                    }
                }

                // PANEL 5: LEGAL & INFORMACIÓN
                item {
                    SettingsSectionLabel(title = stringResource(R.string.settings_legal))
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        NeoInfoRow(
                            label = stringResource(R.string.settings_version),
                            value = "$appVersion (Android)"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openTermsOfService() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_terms),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.openPrivacyPolicy() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_privacy),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Diálogo de sugerencias
        if (showSuggestionsDialog) {
            NeoInputDialog(
                title = stringResource(R.string.settings_suggest),
                emoji = "💡",
                accentColor = SolarAmber,
                onDismiss = { viewModel.hideSuggestionsDialog() },
                onSend = {
                    viewModel.sendSuggestion(it)
                    viewModel.hideSuggestionsDialog()
                }
            )
        }

        // Diálogo de reporte de errores
        if (showBugReportDialog) {
            NeoInputDialog(
                title = stringResource(R.string.settings_bug),
                emoji = "🐛",
                accentColor = NeonPink,
                onDismiss = { viewModel.hideBugReportDialog() },
                onSend = {
                    viewModel.sendBugReport(it)
                    viewModel.hideBugReportDialog()
                }
            )
        }
    }
}

// ============================================================================
// HEADER DE CONFIGURACIÓN
// ============================================================================

@Composable
fun NeoSettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(CyberSurface.copy(alpha = 0.9f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.size(42.dp))
    }
}

// ============================================================================
// ETIQUETA DE SECCIÓN
// ============================================================================

@Composable
fun SettingsSectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        color = Color.White.copy(alpha = 0.45f),
        modifier = Modifier.padding(start = 4.dp)
    )
}

// ============================================================================
// CONTROL DE AUDIO
// ============================================================================

@Composable
fun NeoSoundControlCard(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    NeoGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isEnabled) CyberCyan else CyberBorder,
        glowColor = if (isEnabled) CyberCyan.copy(alpha = 0.2f) else Color.Transparent,
        cornerRadius = 20.dp,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isEnabled) CyberCyan.copy(alpha = 0.2f) else CyberSurfaceElevated)
                        .border(1.dp, if (isEnabled) CyberCyan else Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isEnabled) "🔊" else "🔇", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = stringResource(R.string.settings_sound),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.settings_sound_effects),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberCyan,
                    checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

// ============================================================================
// SELECTOR DIRECTO DE IDIOMA CON BANDERAS
// ============================================================================

@Composable
fun NeoLanguageSelectorRow(
    currentLanguage: SettingsViewModel.Language,
    onSelectLanguage: (SettingsViewModel.Language) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingsViewModel.Language.values().forEach { lang ->
            val isSelected = lang == currentLanguage
            val flagEmoji = when (lang) {
                SettingsViewModel.Language.SPANISH -> "🇪🇸"
                SettingsViewModel.Language.ENGLISH -> "🇺🇸"
                SettingsViewModel.Language.PORTUGUESE -> "🇧🇷"
            }
            val shortName = when (lang) {
                SettingsViewModel.Language.SPANISH -> "Español"
                SettingsViewModel.Language.ENGLISH -> "English"
                SettingsViewModel.Language.PORTUGUESE -> "Português"
            }

            NeoGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                borderColor = if (isSelected) NeonPurple else CyberBorder,
                glowColor = if (isSelected) NeonPurple.copy(alpha = 0.35f) else Color.Transparent,
                cornerRadius = 16.dp,
                onClick = { onSelectLanguage(lang) }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = flagEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shortName,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ============================================================================
// TARJETA DE APOYO AL CREADOR (GOLD CARD)
// ============================================================================

@Composable
fun NeoSupportCreatorCard(onSupportClick: () -> Unit) {
    NeoGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = SolarAmber,
        glowColor = SolarAmber.copy(alpha = 0.4f),
        cornerRadius = 22.dp,
        onClick = onSupportClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(SolarAmber.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SolarAmber.copy(alpha = 0.25f))
                            .border(1.5.dp, SolarAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "❤️", fontSize = 26.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.settings_support_creator),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_support_creator_desc),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                NeonBadge(text = "AD 🎬", color = SolarAmber)
            }
        }
    }
}

// ============================================================================
// TARJETA DE ACCIÓN SIMPLE
// ============================================================================

@Composable
fun NeoActionCard(
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    NeoGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = accentColor.copy(alpha = 0.4f),
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================================
// FILA DE INFORMACIÓN
// ============================================================================

@Composable
fun NeoInfoRow(label: String, value: String) {
    NeoGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = SolarAmber
            )
        }
    }
}

// ============================================================================
// DIÁLOGO DE ENTRADA PARA SUGERENCIAS / ERRORES
// ============================================================================

@Composable
fun NeoInputDialog(
    title: String,
    emoji: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        NeoGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(4.dp),
            borderColor = accentColor,
            glowColor = accentColor.copy(alpha = 0.4f),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$emoji $title",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(stringResource(R.string.settings_suggest_desc), color = Color.White.copy(alpha = 0.4f)) },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = accentColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            text = stringResource(R.string.close),
                            color = Color.White
                        )
                    }

                    TactileButton(
                        onClick = { if (text.isNotBlank()) onSend(text) },
                        containerColor = accentColor,
                        enabled = text.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.next),
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}