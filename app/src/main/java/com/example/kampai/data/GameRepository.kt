package com.example.kampai.data

import androidx.compose.ui.graphics.Color
import com.example.kampai.domain.models.GameModel
import com.example.kampai.ui.theme.*
import com.example.kampai.R
import javax.inject.Inject

class GameRepository @Inject constructor() {

    // Lista maestra de todos los juegos
    private val allGames = listOf(
        // --- MÁS JUGADOS (TOP 3) ---
        GameModel(
            id = "warmup",
            title = "PartyMix",
            description = "Eventos aleatorios y retos rápidos.",
            iconRes = R.drawable.warmup,
            color = Color(0xFFF59E0B),
            route = "game_warmup"
        ),
        GameModel(
            id = "culture",
            title = "Cultura Chupística",
            description = "Modo Clásico o Bomba.",
            iconRes = R.drawable.culture,
            color = PrimaryViolet,
            route = "culture_selection"
        ),
        GameModel(
            id = "impostor",
            title = "El Impostor",
            description = "Encuentra quién miente.",
            iconRes = R.drawable.mimic, // Usamos mimic o truth si no hay icono específico cargado
            color = AccentRed,
            route = "game_impostor"
        ),

        // --- CLÁSICOS (EL RESTO) ---
        GameModel(
            id = "never_have_i_ever",
            title = "Yo Nunca Nunca",
            description = "Si lo has hecho, bebes.",
            iconRes = R.drawable.never,
            color = AccentCyan,
            route = "game_never"
        ),
        GameModel(
            id = "truth_or_dare",
            title = "Verdad o Reto",
            description = "¿Te atreves o confiesas?",
            iconRes = R.drawable.truth,
            color = AccentAmber,
            route = "game_truth"
        ),
        GameModel(
            id = "high_low",
            title = "Mayor o Menor",
            description = "Adivina la carta.",
            iconRes = R.drawable.highlow,
            color = SecondaryPink,
            route = "game_highlow"
        ),
        GameModel(
            id = "medusa",
            title = "La Medusa",
            description = "No cruces miradas.",
            iconRes = R.drawable.medusa,
            color = AccentCyan,
            route = "game_medusa"
        ),
        GameModel(
            id = "charades",
            title = "Mímica Borracha",
            description = "Actúa sin hablar.",
            iconRes = R.drawable.mimic,
            color = AccentAmber,
            route = "game_charades"
        ),
        GameModel(
            id = "roulette",
            title = "Ruleta Rusa",
            description = "La suerte decide.",
            iconRes = R.drawable.ruleta,
            color = AccentRed,
            route = "game_roulette"
        ),
        GameModel(
            id = "judge",
            title = "El Juez",
            description = "Nuevas reglas.",
            iconRes = R.drawable.juez,
            color = AccentAmber,
            route = "game_judge"
        ),
        GameModel(
            id = "staring",
            title = "Duelo de Miradas",
            description = "No parpadees.",
            iconRes = R.drawable.duelo,
            color = PrimaryViolet,
            route = "game_staring"
        ),
        GameModel(
            id = "most_likely",
            title = "¿Quién es más probable?",
            description = "Votación grupal.",
            iconRes = R.drawable.truth,
            color = SecondaryPink,
            route = "game_likely"
        )
    )

    // Retorna SOLO los 3 más jugados para el Home
    fun getMostPlayedGames(): List<GameModel> {
        return allGames.filter { it.id == "warmup" || it.id == "culture" || it.id == "impostor" }
    }

    // Retorna EL RESTO para la pantalla de Clásicos
    fun getClassicGames(): List<GameModel> {
        return allGames.filter { it.id != "warmup" && it.id != "culture" && it.id != "impostor" }
    }
}