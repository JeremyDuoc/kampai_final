package com.example.kampai.domain.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class HotChallenge(
    val id: String,
    val textRes: Int? = null,        // ID si viene del XML (Traducible)
    val textString: String? = null,  // Texto si viene de la BD (Personalizado)
    val intensity: HotIntensity,
    val target: HotTarget,
    val isCustom: Boolean = false,   // Para ponerle borde dorado en la UI
    val isSecret: Boolean = false    // Para mecánicas de "leer en silencio"
) {
    // Función helper para obtener el texto real en la UI sin importar de dónde venga
    @Composable
    fun getContent(): String {
        return textString ?: textRes?.let { stringResource(it) } ?: "Error loading card"
    }
}