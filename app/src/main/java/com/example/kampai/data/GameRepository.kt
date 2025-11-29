package com.example.kampai.data

import androidx.compose.ui.graphics.Color
import com.example.kampai.domain.models.GameModel
import com.example.kampai.ui.theme.*
import com.example.kampai.R
import javax.inject.Inject

class GameRepository @Inject constructor() {

    private val allGames = listOf(
        // --- JUEGOS PRINCIPALES ---
        GameModel(
            id = "warmup",
            title = R.string.game_title_warmup,
            description = R.string.game_desc_warmup,
            iconEmoji = "🍹", // Cóctel (PartyMix)
            color = Color(0xFFF59E0B),
            route = "game_warmup"
        ),
        GameModel(
            id = "culture",
            title = R.string.game_title_culture,
            description = R.string.game_desc_culture,
            iconEmoji = "🤓", // Nerd (Cultura)
            color = PrimaryViolet,
            route = "culture_selection"
        ),
        GameModel(
            id = "kingscup",
            title = R.string.game_title_kingscup,
            description = R.string.game_desc_kingscup,
            iconEmoji = "👑", // Corona (King's Cup)
            color = Color(0xFF1E1E1E),
            route = "game_kingscup"
        ),

        // --- JUEGOS CLÁSICOS Y OTROS ---
        GameModel(
            id = "impostor",
            title = R.string.game_title_impostor,
            description = R.string.game_desc_impostor,
            iconEmoji = "👺", // Máscara (Impostor)
            color = AccentRed,
            route = "game_impostor"
        ),
        GameModel(
            id = "never_have_i_ever",
            title = R.string.game_title_never,
            description = R.string.game_desc_never,
            iconEmoji = "🙊", // Mono tapándose boca (Yo Nunca)
            color = AccentCyan,
            route = "game_never"
        ),
        GameModel(
            id = "truth_or_dare",
            title = R.string.game_title_truth,
            description = R.string.game_desc_truth,
            iconEmoji = "🎭",
            color = AccentAmber,
            route = "game_truth"
        ),
        GameModel(
            id = "high_low",
            title = R.string.game_title_highlow,
            description = R.string.game_desc_highlow,
            iconEmoji = "🃏",
            color = SecondaryPink,
            route = "game_highlow"
        ),
        GameModel(
            id = "charades",
            title = R.string.game_title_charades,
            description = R.string.game_desc_charades,
            iconEmoji = "🤸",
            color = AccentAmber,
            route = "game_charades"
        ),
        GameModel(
            id = "roulette",
            title = R.string.game_title_roulette,
            description = R.string.game_desc_roulette,
            iconEmoji = "🧨",
            color = AccentRed,
            route = "game_roulette"
        ),
        GameModel(
            id = "most_likely",
            title = R.string.game_title_likely,
            description = R.string.game_desc_likely,
            iconEmoji = "🫵",
            color = SecondaryPink,
            route = "game_likely"
        )

    )

    fun getMostPlayedGames(): List<GameModel> {
        return allGames.filter {
            it.id == "warmup" || it.id == "kingscup" || it.id == "impostor"  || it.id == "culture"
        }
    }

    fun getClassicGames(): List<GameModel> {
        return allGames.filter {
            it.id != "warmup" &&
                    it.id != "kingscup" &&
                    it.id != "impostor" &&
                    it.id != "culture"
        }
    }

}