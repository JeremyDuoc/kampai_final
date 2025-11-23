package com.example.kampai.cardgame.domain.models

import androidx.compose.ui.graphics.Color

// ==================== ENUMS Y CONSTANTES ====================

enum class CardColor {
    RED, BLUE, GREEN, YELLOW, WILD;

    fun toComposeColor(): Color = when(this) {
        RED -> Color(0xFFE53935)
        BLUE -> Color(0xFF1E88E5)
        GREEN -> Color(0xFF43A047)
        YELLOW -> Color(0xFFFDD835)
        WILD -> Color(0xFF424242)
    }
}

enum class CardValue {
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
    SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR;

    fun isSpecial(): Boolean = this in listOf(SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR)
    fun isWild(): Boolean = this in listOf(WILD, WILD_DRAW_FOUR)
    fun isPlusCard(): Boolean = this in listOf(DRAW_TWO, WILD_DRAW_FOUR)

    fun getSymbol(): String = when(this) {
        ZERO -> "0"
        ONE -> "1"
        TWO -> "2"
        THREE -> "3"
        FOUR -> "4"
        FIVE -> "5"
        SIX -> "6"
        SEVEN -> "7"
        EIGHT -> "8"
        NINE -> "9"
        SKIP -> "🚫"
        REVERSE -> "🔄"
        DRAW_TWO -> "+2"
        WILD -> "🌈"
        WILD_DRAW_FOUR -> "+4"
    }
}

enum class GameDesign {
    CLASSIC,
    NEON,
    RETRO,
    MINIMALIST,
    CYBERPUNK;

    fun getDesignId(): Int = ordinal
}

// ==================== MODELOS DE CARTA ====================

data class CardModel(
    val id: String,
    val value: CardValue,
    val color: CardColor,
    val designId: Int = GameDesign.CLASSIC.getDesignId()
) {
    fun canPlayOn(topCard: CardModel): Boolean {
        // Wild cards can always be played
        if (value.isWild()) return true

        // Same color or same value
        if (color == topCard.color) return true
        if (value == topCard.value) return true

        // Wild cards on pile can accept any card
        if (topCard.value.isWild()) return true

        return false
    }

    companion object {
        fun generateDeck(designId: Int = 0): List<CardModel> {
            val deck = mutableListOf<CardModel>()
            var idCounter = 0

            // Number cards (0: 1 per color, 1-9: 2 per color)
            CardColor.values().filter { it != CardColor.WILD }.forEach { color ->
                // One zero per color
                deck.add(CardModel("card_${idCounter++}", CardValue.ZERO, color, designId))

                // Two of each number 1-9
                CardValue.values().filter {
                    it in listOf(
                        CardValue.ONE, CardValue.TWO, CardValue.THREE, CardValue.FOUR,
                        CardValue.FIVE, CardValue.SIX, CardValue.SEVEN, CardValue.EIGHT, CardValue.NINE
                    )
                }.forEach { value ->
                    repeat(2) {
                        deck.add(CardModel("card_${idCounter++}", value, color, designId))
                    }
                }

                // Two of each action card per color
                repeat(2) {
                    deck.add(CardModel("card_${idCounter++}", CardValue.SKIP, color, designId))
                    deck.add(CardModel("card_${idCounter++}", CardValue.REVERSE, color, designId))
                    deck.add(CardModel("card_${idCounter++}", CardValue.DRAW_TWO, color, designId))
                }
            }

            // Wild cards (4 of each)
            repeat(4) {
                deck.add(CardModel("card_${idCounter++}", CardValue.WILD, CardColor.WILD, designId))
                deck.add(CardModel("card_${idCounter++}", CardValue.WILD_DRAW_FOUR, CardColor.WILD, designId))
            }

            return deck.shuffled()
        }
    }
}

// ==================== CONFIGURACIÓN DE REGLAS ====================

data class RuleConfig(
    val turnDurationSeconds: Int = 30,
    val allowStackingPlusCards: Boolean = true,
    val cantFinishWithSpecial: Boolean = true,
    val kampaiPenaltySeconds: Int = 4,
    val initialHandSize: Int = 7
) {
    companion object {
        val DEFAULT = RuleConfig()
        val QUICK = RuleConfig(
            turnDurationSeconds = 15,
            allowStackingPlusCards = false,
            kampaiPenaltySeconds = 3
        )
        val CLASSIC = RuleConfig(
            turnDurationSeconds = 45,
            allowStackingPlusCards = true,
            cantFinishWithSpecial = false,
            kampaiPenaltySeconds = 5
        )
    }
}

// ==================== MODELOS DE JUGADOR ====================

data class PlayerInfo(
    val id: String,
    val name: String,
    val designId: Int = GameDesign.CLASSIC.getDesignId(),
    val isHost: Boolean = false,
    val isConnected: Boolean = true
)

data class PlayerHand(
    val playerId: String,
    val cards: List<CardModel> = emptyList(),
    val handSize: Int = 0 // For network sync - only send size to non-active players
) {
    fun withoutCards(): PlayerHand = copy(cards = emptyList(), handSize = cards.size)
}

// ==================== ESTADO DEL JUEGO ====================

enum class GamePhase {
    LOBBY,
    STARTING,
    PLAYING,
    TURN_TRANSITION,
    KAMPAI_WINDOW,
    GAME_OVER
}

enum class TurnDirection {
    CLOCKWISE,
    COUNTER_CLOCKWISE;

    fun reverse(): TurnDirection = when(this) {
        CLOCKWISE -> COUNTER_CLOCKWISE
        COUNTER_CLOCKWISE -> CLOCKWISE
    }
}

data class GameState(
    val phase: GamePhase = GamePhase.LOBBY,
    val players: List<PlayerInfo> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val direction: TurnDirection = TurnDirection.CLOCKWISE,
    val topCard: CardModel? = null,
    val drawPileSize: Int = 0,
    val discardPileSize: Int = 0,
    val stackedPlusCards: Int = 0, // For stacking +2 and +4
    val ruleConfig: RuleConfig = RuleConfig.DEFAULT,
    val kampaiWindowActive: Boolean = false,
    val kampaiWindowStartTime: Long = 0L,
    val winnerId: String? = null,
    val lastAction: GameAction? = null
)

// ==================== ACCIONES DEL JUEGO ====================

sealed class GameAction {
    data class PlayCard(
        val playerId: String,
        val card: CardModel,
        val chosenColor: CardColor? = null // For wild cards
    ) : GameAction()

    data class DrawCard(val playerId: String) : GameAction()

    data class PressKampai(val playerId: String) : GameAction()

    data class PressPenalty(
        val accuserId: String,
        val targetId: String
    ) : GameAction()

    data class EndTurn(val playerId: String) : GameAction()
}

// ==================== MENSAJES DE RED ====================

sealed class NetworkMessage {
    data class Connect(val playerInfo: PlayerInfo) : NetworkMessage()
    data class Disconnect(val playerId: String) : NetworkMessage()

    data class GameStateSync(val gameState: GameState) : NetworkMessage()
    data class HandSync(val hand: PlayerHand) : NetworkMessage()

    data class ActionRequest(val action: GameAction) : NetworkMessage()
    data class ActionResult(
        val success: Boolean,
        val message: String? = null,
        val updatedState: GameState? = null
    ) : NetworkMessage()

    data class KampaiWindowOpened(
        val targetPlayerId: String,
        val expiresAt: Long
    ) : NetworkMessage()

    data class TurnTransition(val nextPlayerId: String) : NetworkMessage()
}

// ==================== RESULTADO DE VALIDACIÓN ====================

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

data class CardPlayValidation(
    val canPlay: Boolean,
    val reason: String? = null
)