package com.example.kampai.ui.theme.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.ui.theme.AccentAmber
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val showSuggestionsDialog by viewModel.showSuggestionsDialog.collectAsState()
    val appVersion = "1.0.0"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fondo decorativo
        SettingsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            // Header
            SettingsHeader(onBack = onBack)

            // Contenido scrolleable
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
                        title = "Idioma",
                        subtitle = "Selecciona tu idioma preferido",
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
                    SectionTitle("ℹ️ INFORMACIÓN")
                }

                item {
                    InfoItem(
                        icon = "🎮",
                        title = "Versión",
                        value = appVersion
                    )
                }

                item {
                    InfoItem(
                        icon = "📱",
                        title = "Plataforma",
                        value = "Android"
                    )
                }

                // SECCIÓN: SOPORTE
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle("💬 SOPORTE Y FEEDBACK")
                }

                item {
                    ActionItem(
                        icon = "💡",
                        title = "Enviar Sugerencia",
                        subtitle = "Comparte tus ideas para mejorar",
                        onClick = { viewModel.showSuggestionsDialog() }
                    )
                }

                item {
                    ActionItem(
                        icon = "🐛",
                        title = "Reportar Error",
                        subtitle = "Si encuentras un problema, cuéntanos",
                        onClick = { viewModel.showBugReportDialog() }
                    )
                }

                item {
                    ActionItem(
                        icon = "⭐",
                        title = "Calificar App",
                        subtitle = "Ayuda a otros a descubrirnos",
                        onClick = { /* Abrir Play Store */ }
                    )
                }

                // SECCIÓN: LEGAL
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle("⚖️ LEGAL")
                }

                item {
                    ActionItem(
                        icon = "📜",
                        title = "Términos de Servicio",
                        subtitle = "Lee nuestros términos",
                        onClick = { /* Abrir URL */ }
                    )
                }

                item {
                    ActionItem(
                        icon = "🔒",
                        title = "Privacidad",
                        subtitle = "Cómo protegemos tus datos",
                        onClick = { /* Abrir URL */ }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Diálogos
        if (showSuggestionsDialog) {
            SuggestionsDialog(
                onDismiss = { viewModel.hideSuggestionsDialog() },
                onSend = { suggestion ->
                    viewModel.sendSuggestion(suggestion)
                }
            )
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
                        text = "Tema",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkMode) "Modo Oscuro" else "Modo Claro",
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
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onSurface)
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚙️ Configuración",
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
                        text = "Sonido",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Efectos de sonido del juego",
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
    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        Dialog(
            onDismissRequest = {
                isVisible = false
                onDismiss()
            }
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
                        text = "💡 Sugerencias",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cuéntanos tu idea para mejorar Kampai",
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
                        placeholder = { Text("Escribe tu sugerencia aquí...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                            onClick = {
                                isVisible = false
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (suggestion.isNotBlank()) {
                                    onSend(suggestion)
                                    isVisible = false
                                    onDismiss()
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
}