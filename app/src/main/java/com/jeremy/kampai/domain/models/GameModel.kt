package com.jeremy.kampai.domain.models

import androidx.compose.ui.graphics.Color
import com.jeremy.kampai.R

/**
 * Nivel de intensidad alcohólica/social del juego.
 * NO mide dificultad mental, sino cuánto empuja el juego a beber
 * (frecuencia de tragos, presión social, shots de golpe, etc).
 */
enum class GameIntensity(val level: Int, val label: String, val color: Color) {
    SUAVE(1, "Suave", Color(0xFF639922)),
    INTERMEDIO(2, "Intermedio", Color(0xFFBA7517)),
    FUERTE(3, "Fuerte", Color(0xFFD85A30)),
    EXTREMO(4, "Extremo", Color(0xFFE24B4A))
}

/**
 * Vibra o categoría de la fiesta para filtrar juegos según el ambiente.
 */
enum class GameVibe(val labelRes: Int) {
    ALL(R.string.filter_all),
    ICEBREAKER(R.string.filter_icebreaker),
    DUELS(R.string.filter_duels),
    SPICY(R.string.filter_spicy),
    CHAOS(R.string.filter_chaos)
}

data class GameModel(
    val id: String,
    val title: Int,
    val description: Int,
    val iconEmoji: String,
    val color: Color,
    val route: String,
    val playerRange: String,
    val intensity: GameIntensity,
    val isNew: Boolean = false,
    val vibe: GameVibe = GameVibe.ICEBREAKER
)