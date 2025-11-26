package com.example.kampai.ui.theme.hot

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kampai.R
import com.example.kampai.domain.models.HotIntensity
import com.example.kampai.domain.models.HotTarget

private val DarkBg = Color(0xFF121212)
private val Gold = Color(0xFFFFD700)
private val RedWine = Color(0xFF4A0E0E)

@Composable
fun CreateChallengeScreen(
    viewModel: CreateChallengeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val text by viewModel.text.collectAsState()
    val intensity by viewModel.intensity.collectAsState()
    val target by viewModel.target.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect {
            Toast.makeText(context, context.getString(R.string.create_msg_success), Toast.LENGTH_SHORT).show()
            onBack() // Volver atrás al guardar
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBg, RedWine)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Gold)
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = stringResource(R.string.create_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.create_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // VISTA PREVIA DE LA CARTA
            Text(
                text = stringResource(R.string.create_preview),
                style = MaterialTheme.typography.labelSmall,
                color = Gold.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Usamos la función local renombrada para evitar conflictos
            val currentColor = getLocalIntensityColor(intensity)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                border = BorderStroke(2.dp, currentColor)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Tu reto aparecerá aquí...",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            text = text,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Badge de intensidad
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd),
                        color = currentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = intensity.name,
                            color = currentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CAMPO DE TEXTO
            OutlinedTextField(
                value = text,
                onValueChange = { viewModel.onTextChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.create_hint), color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Gold
                ),
                shape = RoundedCornerShape(16.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SELECTOR INTENSIDAD
            Text(
                text = stringResource(R.string.create_label_intensity),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HotIntensity.values().forEach { item ->
                    IntensityChip(
                        intensity = item,
                        isSelected = intensity == item,
                        onClick = { viewModel.onIntensityChanged(item) },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SELECTOR TARGET
            Text(
                text = stringResource(R.string.create_label_target),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HotTarget.values().forEach { item ->
                    TargetChip(
                        target = item,
                        isSelected = target == item,
                        onClick = { viewModel.onTargetChanged(item) },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // BOTÓN GUARDAR
            Button(
                onClick = { viewModel.saveCard() },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    disabledContainerColor = Gold.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_btn_save),
                    color = if (text.isNotBlank()) DarkBg else Color.White.copy(alpha = 0.5f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun IntensityChip(
    intensity: HotIntensity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getLocalIntensityColor(intensity)
    val bgColor by animateColorAsState(if (isSelected) color else Color.Transparent)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, if (isSelected) Color.Transparent else color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when(intensity) {
                HotIntensity.SOFT -> "Soft"
                HotIntensity.MEDIUM -> "Med"
                HotIntensity.HOT -> "Hot"
                HotIntensity.EXTREME -> "X"
            },
            color = if (isSelected) Color.Black else color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TargetChip(
    target: HotTarget,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(if (isSelected) Color.White else Color.Transparent)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when(target) {
                HotTarget.SOLO -> "Solo"
                HotTarget.COUPLE -> "Pareja"
                HotTarget.GROUP -> "Grupo"
            },
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

private fun getLocalIntensityColor(intensity: HotIntensity): Color {
    return when (intensity) {
        HotIntensity.SOFT -> Color(0xFF4CAF50) // Verde
        HotIntensity.MEDIUM -> Color(0xFFF59E0B) // Naranja
        HotIntensity.HOT -> Color(0xFFFF4444) // Rojo
        HotIntensity.EXTREME -> Color(0xFF9C27B0) // Morado
    }
}