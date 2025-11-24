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
            title = R.string.game_title_warmup,
            description = R.string.game_desc_warmup,
            iconRes = R.drawable.warmup,
            color = Color(0xFFF59E0B),
            route = "game_warmup"
        ),
        GameModel(
            id = "culture",
            title = R.string.game_title_culture,
            description = R.string.game_desc_culture,
            iconRes = R.drawable.culture,
            color = PrimaryViolet,
            route = "culture_selection"
        ),
        GameModel(
            id = "kingscup",
            title = R.string.game_title_kingscup,
            description = R.string.game_desc_kingscup,
            iconRes = R.drawable.culture,
            color = Color(0xFFF59E0B),
            route = "game_kingscup"
        ),
        GameModel(
            id = "karaoke",
            title = R.string.game_title_karaoke,
            description = R.string.game_desc_karaoke,
            iconRes = R.drawable.culture,
            color = Color(0xFFF59E0B),
            route = "game_karaoke"
        ),

        GameModel(
            id = "hot",
            title = R.string.game_title_hot,
            description = R.string.game_desc_hot,
            iconRes = R.drawable.truth,
            color = Color(0xFFD32F2F),
            route = "game_hot"
        ),

        GameModel(
            id = "card_game",
            title = R.string.game_title_cardgame,
            description = R.string.game_desc_cardgame,
            iconRes = R.drawable.culture, // Asegúrate que este icono exista o cámbialo
            color = Color(0xFFE53935),
            route = "card_game"
        ),
        GameModel(
            id = "impostor",
            title = R.string.game_title_impostor,
            description = R.string.game_desc_impostor,
            iconRes = R.drawable.mimic,
            color = AccentRed,
            route = "game_impostor"
        ),

        // --- CLÁSICOS ---
        GameModel(
            id = "never_have_i_ever",
            title = R.string.game_title_never,
            description = R.string.game_desc_never,
            iconRes = R.drawable.never,
            color = AccentCyan,
            route = "game_never"
        ),
        GameModel(
            id = "truth_or_dare",
            title = R.string.game_title_truth,
            description = R.string.game_desc_truth,
            iconRes = R.drawable.truth,
            color = AccentAmber,
            route = "game_truth"
        ),
        GameModel(
            id = "high_low",
            title = R.string.game_title_highlow,
            description = R.string.game_desc_highlow,
            iconRes = R.drawable.highlow,
            color = SecondaryPink,
            route = "game_highlow"
        ),
        GameModel(
            id = "charades",
            title = R.string.game_title_charades,
            description = R.string.game_desc_charades,
            iconRes = R.drawable.mimic,
            color = AccentAmber,
            route = "game_charades"
        ),
        GameModel(
            id = "roulette",
            title = R.string.game_title_roulette,
            description = R.string.game_desc_roulette,
            iconRes = R.drawable.ruleta,
            color = AccentRed,
            route = "game_roulette"
        ),
        GameModel(
            id = "most_likely",
            title = R.string.game_title_likely,
            description = R.string.game_desc_likely,
            iconRes = R.drawable.truth,
            color = SecondaryPink,
            route = "game_likely"
        )
    )

    fun getMostPlayedGames(): List<GameModel> {
        return allGames.filter { it.id == "warmup" || it.id == "culture" || it.id == "impostor" || it.id == "kingscup" }
    }

    fun getClassicGames(): List<GameModel> {
        return allGames.filter { it.id != "warmup" && it.id != "culture" && it.id != "impostor" && it.id != "kingscup"}
    }
}