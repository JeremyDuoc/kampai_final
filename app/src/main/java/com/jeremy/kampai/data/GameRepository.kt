package com.jeremy.kampai.data

import androidx.compose.ui.graphics.Color
import com.jeremy.kampai.domain.models.GameIntensity
import com.jeremy.kampai.domain.models.GameModel
import com.jeremy.kampai.domain.models.GameVibe
import com.jeremy.kampai.ui.theme.*
import com.jeremy.kampai.R
import javax.inject.Inject

class GameRepository @Inject constructor() {

    private val allGames = listOf(
        GameModel(
            id = "warmup",
            title = R.string.game_title_warmup,
            description = R.string.game_desc_warmup,
            iconEmoji = "🍹", // Cóctel (PartyMix)
            color = Color(0xFFF59E0B),
            route = "game_warmup",
            playerRange = "2+",
            intensity = GameIntensity.INTERMEDIO,
            isNew = true,
            vibe = GameVibe.ICEBREAKER
        ),
        GameModel(
            id = "culture",
            title = R.string.game_title_culture,
            description = R.string.game_desc_culture,
            iconEmoji = "🤓", // Nerd (Cultura)
            color = PrimaryViolet,
            route = "culture_selection",
            playerRange = "2+",
            intensity = GameIntensity.SUAVE,
            vibe = GameVibe.ICEBREAKER
        ),
        GameModel(
            id = "kingscup",
            title = R.string.game_title_kingscup,
            description = R.string.game_desc_kingscup,
            iconEmoji = "👑", // Corona (King's Cup)
            color = Color(0xFF7C3AED),
            route = "game_kingscup",
            playerRange = "3-8",
            intensity = GameIntensity.FUERTE,
            vibe = GameVibe.DUELS
        ),

        // --- JUEGOS CLÁSICOS Y OTROS ---
        GameModel(
            id = "impostor",
            title = R.string.game_title_impostor,
            description = R.string.game_desc_impostor,
            iconEmoji = "👺", // Máscara (Impostor)
            color = AccentRed,
            route = "game_impostor",
            playerRange = "4+",
            intensity = GameIntensity.FUERTE,
            vibe = GameVibe.SPICY
        ),
        GameModel(
            id = "never_have_i_ever",
            title = R.string.game_title_never,
            description = R.string.game_desc_never,
            iconEmoji = "🙊", // Mono tapándose boca (Yo Nunca)
            color = AccentCyan,
            route = "game_never",
            playerRange = "2+",
            intensity = GameIntensity.INTERMEDIO,
            vibe = GameVibe.SPICY
        ),
        GameModel(
            id = "truth_or_dare",
            title = R.string.game_title_truth,
            description = R.string.game_desc_truth,
            iconEmoji = "🎭",
            color = AccentAmber,
            route = "game_truth",
            playerRange = "2-10",
            intensity = GameIntensity.INTERMEDIO,
            vibe = GameVibe.SPICY
        ),
        GameModel(
            id = "most_likely",
            title = R.string.game_title_likely,
            description = R.string.game_desc_likely,
            iconEmoji = "🫵",
            color = SecondaryPink,
            route = "game_likely",
            playerRange = "3+",
            intensity = GameIntensity.INTERMEDIO,
            vibe = GameVibe.SPICY
        )
    )

    fun getAllGames(): List<GameModel> = allGames

    fun getMostPlayedGames(): List<GameModel> {
        return allGames.filter {
            it.id == "warmup" || it.id == "kingscup" || it.id == "impostor" || it.id == "culture"
        }
    }

    fun getNewGames(): List<GameModel> {
        return allGames.filter { it.isNew }
    }

    fun getClassicGames(): List<GameModel> {
        return allGames.filter {
            it.id != "warmup" &&
                    it.id != "kingscup" &&
                    it.id != "impostor" &&
                    it.id != "culture"
        }
    }

    fun getGamesByVibe(vibe: GameVibe): List<GameModel> {
        return if (vibe == GameVibe.ALL) allGames else allGames.filter { it.vibe == vibe }
    }

    fun getRandomGame(): GameModel {
        return allGames.random()
    }
}