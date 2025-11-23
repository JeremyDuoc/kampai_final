package com.example.kampai.data

import androidx.compose.ui.graphics.Color
import com.example.kampai.domain.models.GameModel
import com.example.kampai.ui.theme.*
import com.example.kampai.R
import javax.inject.Inject

class GameRepository @Inject constructor() {

    private val allGames = listOf(

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
            id = "kingscup",
            title = "King's Cup",
            description = "El clásico juego de cartas.",
            iconRes = R.drawable.culture,
            color = Color(0xFFF59E0B),
            route = "game_kingscup"
        ),

        GameModel(
            id = "karaoke",
            title = "Karaoke",
            description = "Modo Karaoke",
            iconRes = R.drawable.culture,
            color = Color(0xFFF59E0B),
            route = "game_karaoke"
        ),
        GameModel(
            id = "card_game",
            title = "Juego de Cartas",
            description = "Multijugador local tipo UNO",
            iconRes = R.drawable.culture, // Necesitas este drawable
            color = Color(0xFFE53935),
            route = "card_game"
        ),


        GameModel(
            id = "impostor",
            title = "El Impostor",
            description = "Encuentra quién miente.",
            iconRes = R.drawable.mimic,
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
            description = "Adivina si la carta que viene es mayor o menor.",
            iconRes = R.drawable.highlow,
            color = SecondaryPink,
            route = "game_highlow"
        ),
        GameModel(
            id = "charades",
            title = "Mímica Borracha",
            description = "Actúa sin hablar y los demás tienen que adivinar.",
            iconRes = R.drawable.mimic,
            color = AccentAmber,
            route = "game_charades"
        ),
        GameModel(
            id = "roulette",
            title = "Ruleta Rusa",
            description = "La suerte decide. Presiona una cámara para decidir tu destino",
            iconRes = R.drawable.ruleta,
            color = AccentRed,
            route = "game_roulette"
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

    // Retorna los más jugados
    fun getMostPlayedGames(): List<GameModel> {
        return allGames.filter { it.id == "warmup" || it.id == "culture" || it.id == "impostor" || it.id == "kingscup" }
    }

    // Retorna EL RESTO para la pantalla de Clásicos
    fun getClassicGames(): List<GameModel> {
        return allGames.filter { it.id != "warmup" && it.id != "culture" && it.id != "impostor" && it.id != "kingscup"}
    }
}