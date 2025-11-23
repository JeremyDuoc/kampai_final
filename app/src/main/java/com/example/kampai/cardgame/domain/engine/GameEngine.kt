package com.example.kampai.cardgame.domain.engine

import com.example.kampai.cardgame.domain.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor principal del juego - Solo debe ejecutarse en el HOST
 * Gestiona toda la lógica de reglas, validaciones y transiciones de estado
 */
@Singleton
class GameEngine @Inject constructor() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val playerHands = mutableMapOf<String, MutableList<CardModel>>()
    private val drawPile = mutableListOf<CardModel>()
    private val discardPile = mutableListOf<CardModel>()

    // ==================== INICIALIZACIÓN ====================

    fun initializeGame(players: List<PlayerInfo>, ruleConfig: RuleConfig) {
        // Generate and shuffle deck
        val deck = CardModel.generateDeck().toMutableList()
        drawPile.clear()
        drawPile.addAll(deck)
        discardPile.clear()

        // Deal initial hands
        playerHands.clear()
        players.forEach { player ->
            val hand = mutableListOf<CardModel>()
            repeat(ruleConfig.initialHandSize) {
                if (drawPile.isNotEmpty()) {
                    hand.add(drawPile.removeFirst())
                }
            }
            playerHands[player.id] = hand
        }

        // Place first card (ensure it's not a wild or +4)
        var startCard: CardModel
        do {
            startCard = drawPile.removeFirstOrNull() ?: CardModel.generateDeck().first()
        } while (startCard.value.isWild() || startCard.value == CardValue.WILD_DRAW_FOUR)

        discardPile.add(startCard)

        // Initialize game state
        _gameState.value = GameState(
            phase = GamePhase.PLAYING,
            players = players,
            currentPlayerIndex = 0,
            direction = TurnDirection.CLOCKWISE,
            topCard = startCard,
            drawPileSize = drawPile.size,
            discardPileSize = discardPile.size,
            ruleConfig = ruleConfig
        )
    }

    // ==================== VALIDACIÓN DE ACCIONES ====================

    fun validateCardPlay(playerId: String, card: CardModel): CardPlayValidation {
        val state = _gameState.value

        // Check if it's player's turn
        if (getCurrentPlayer()?.id != playerId) {
            return CardPlayValidation(false, "No es tu turno")
        }

        // Check if player has the card
        val playerHand = playerHands[playerId] ?: return CardPlayValidation(false, "Mano no encontrada")
        if (card !in playerHand) {
            return CardPlayValidation(false, "No tienes esa carta")
        }

        val topCard = state.topCard ?: return CardPlayValidation(false, "No hay carta superior")

        // Check if there are stacked plus cards
        if (state.stackedPlusCards > 0) {
            // Can only play another plus card or draw
            if (!card.value.isPlusCard() && state.ruleConfig.allowStackingPlusCards) {
                return CardPlayValidation(false, "Debes jugar una carta +2/+4 o robar")
            }
        }

        // Check if card can be played on top card
        if (!card.canPlayOn(topCard)) {
            return CardPlayValidation(false, "Esta carta no se puede jugar sobre la carta superior")
        }

        // Check if trying to finish with special card
        if (playerHand.size == 1 && card.value.isSpecial() && state.ruleConfig.cantFinishWithSpecial) {
            return CardPlayValidation(false, "No puedes terminar con una carta especial")
        }

        return CardPlayValidation(true)
    }

    // ==================== PROCESAMIENTO DE ACCIONES ====================

    fun processAction(action: GameAction): ValidationResult {
        return when (action) {
            is GameAction.PlayCard -> processPlayCard(action)
            is GameAction.DrawCard -> processDrawCard(action)
            is GameAction.PressKampai -> processPressKampai(action)
            is GameAction.PressPenalty -> processPressPenalty(action)
            is GameAction.EndTurn -> processEndTurn(action)
        }
    }

    private fun processPlayCard(action: GameAction.PlayCard): ValidationResult {
        val validation = validateCardPlay(action.playerId, action.card)
        if (!validation.canPlay) {
            return ValidationResult.Error(validation.reason ?: "No se puede jugar la carta")
        }

        val playerHand = playerHands[action.playerId] ?: return ValidationResult.Error("Mano no encontrada")

        // Remove card from hand
        playerHand.remove(action.card)

        // Process card effect
        var cardToDiscard = action.card

        // If it's a wild card, change its color
        if (action.card.value.isWild() && action.chosenColor != null) {
            cardToDiscard = action.card.copy(color = action.chosenColor)
        }

        // Add to discard pile
        discardPile.add(cardToDiscard)

        // Update state
        val currentState = _gameState.value
        var newState = currentState.copy(
            topCard = cardToDiscard,
            discardPileSize = discardPile.size,
            lastAction = action
        )

        // Process card effects
        when (action.card.value) {
            CardValue.SKIP -> {
                newState = advanceTurn(newState, skipNext = true)
            }
            CardValue.REVERSE -> {
                newState = newState.copy(direction = newState.direction.reverse())
                newState = advanceTurn(newState)
            }
            CardValue.DRAW_TWO -> {
                if (currentState.ruleConfig.allowStackingPlusCards) {
                    newState = newState.copy(stackedPlusCards = currentState.stackedPlusCards + 2)
                } else {
                    val nextPlayer = getNextPlayer(newState)
                    if (nextPlayer != null) {
                        drawCards(nextPlayer.id, 2)
                        newState = advanceTurn(newState, skipNext = true)
                    }
                }
            }
            CardValue.WILD_DRAW_FOUR -> {
                if (currentState.ruleConfig.allowStackingPlusCards) {
                    newState = newState.copy(stackedPlusCards = currentState.stackedPlusCards + 4)
                } else {
                    val nextPlayer = getNextPlayer(newState)
                    if (nextPlayer != null) {
                        drawCards(nextPlayer.id, 4)
                        newState = advanceTurn(newState, skipNext = true)
                    }
                }
            }
            else -> {
                // Normal card - just advance turn
                newState = advanceTurn(newState)
            }
        }

        // Check if player has one card left (trigger KAMPAI window)
        if (playerHand.size == 1) {
            newState = newState.copy(
                phase = GamePhase.KAMPAI_WINDOW,
                kampaiWindowActive = true,
                kampaiWindowStartTime = System.currentTimeMillis()
            )
        }

        // Check win condition
        if (playerHand.isEmpty()) {
            newState = newState.copy(
                phase = GamePhase.GAME_OVER,
                winnerId = action.playerId
            )
        }

        _gameState.value = newState
        return ValidationResult.Success
    }

    private fun processDrawCard(action: GameAction.DrawCard): ValidationResult {
        val currentState = _gameState.value

        // If there are stacked plus cards, draw them all
        val cardsToDraw = if (currentState.stackedPlusCards > 0) {
            currentState.stackedPlusCards
        } else {
            1
        }

        drawCards(action.playerId, cardsToDraw)

        // Reset stacked cards and advance turn
        val newState = advanceTurn(currentState.copy(stackedPlusCards = 0))
        _gameState.value = newState

        return ValidationResult.Success
    }

    private fun processPressKampai(action: GameAction.PressKampai): ValidationResult {
        val currentState = _gameState.value

        if (!currentState.kampaiWindowActive) {
            return ValidationResult.Error("No hay ventana KAMPAI activa")
        }

        if (action.playerId != getCurrentPlayer()?.id) {
            return ValidationResult.Error("Solo el jugador activo puede presionar KAMPAI")
        }

        // Success - close window and continue
        _gameState.value = currentState.copy(
            phase = GamePhase.PLAYING,
            kampaiWindowActive = false
        )

        return ValidationResult.Success
    }

    private fun processPressPenalty(action: GameAction.PressPenalty): ValidationResult {
        val currentState = _gameState.value

        if (!currentState.kampaiWindowActive) {
            return ValidationResult.Error("No hay ventana de penalidad activa")
        }

        // Check if penalty window is still open
        val elapsed = System.currentTimeMillis() - currentState.kampaiWindowStartTime
        val windowDuration = currentState.ruleConfig.kampaiPenaltySeconds * 1000L

        if (elapsed > windowDuration) {
            return ValidationResult.Error("La ventana de penalidad ya expiró")
        }

        // Apply penalty - target player draws 2 cards
        drawCards(action.targetId, 2)

        // Close window
        _gameState.value = currentState.copy(
            phase = GamePhase.PLAYING,
            kampaiWindowActive = false
        )

        return ValidationResult.Success
    }

    private fun processEndTurn(action: GameAction.EndTurn): ValidationResult {
        val newState = advanceTurn(_gameState.value)
        _gameState.value = newState
        return ValidationResult.Success
    }

    // ==================== UTILIDADES ====================

    private fun drawCards(playerId: String, count: Int) {
        val playerHand = playerHands[playerId] ?: return

        repeat(count) {
            if (drawPile.isEmpty()) {
                reshuffleDiscardPile()
            }

            if (drawPile.isNotEmpty()) {
                playerHand.add(drawPile.removeFirst())
            }
        }

        _gameState.value = _gameState.value.copy(
            drawPileSize = drawPile.size
        )
    }

    private fun reshuffleDiscardPile() {
        if (discardPile.size <= 1) return

        // Keep top card, shuffle rest back into draw pile
        val topCard = discardPile.removeLast()
        drawPile.addAll(discardPile.shuffled())
        discardPile.clear()
        discardPile.add(topCard)
    }

    private fun advanceTurn(state: GameState, skipNext: Boolean = false): GameState {
        val nextIndex = calculateNextPlayerIndex(state, skipNext)
        return state.copy(
            currentPlayerIndex = nextIndex,
            phase = GamePhase.TURN_TRANSITION
        )
    }

    private fun calculateNextPlayerIndex(state: GameState, skipOne: Boolean): Int {
        val increment = if (state.direction == TurnDirection.CLOCKWISE) 1 else -1
        val skip = if (skipOne) 2 else 1

        var nextIndex = state.currentPlayerIndex + (increment * skip)
        val playerCount = state.players.size

        // Handle wrap-around
        while (nextIndex < 0) nextIndex += playerCount
        while (nextIndex >= playerCount) nextIndex -= playerCount

        return nextIndex
    }

    fun getCurrentPlayer(): PlayerInfo? {
        val state = _gameState.value
        return state.players.getOrNull(state.currentPlayerIndex)
    }

    fun getNextPlayer(state: GameState): PlayerInfo? {
        val nextIndex = calculateNextPlayerIndex(state, false)
        return state.players.getOrNull(nextIndex)
    }

    fun getPlayerHand(playerId: String): PlayerHand {
        val cards = playerHands[playerId] ?: emptyList()
        return PlayerHand(playerId, cards, cards.size)
    }

    fun getPlayerHandSize(playerId: String): Int {
        return playerHands[playerId]?.size ?: 0
    }

    // ==================== NETWORK SYNC ====================

    fun getFullHandForPlayer(playerId: String): PlayerHand {
        return getPlayerHand(playerId)
    }

    fun getPublicHandsForNonActivePlayer(): Map<String, PlayerHand> {
        val currentPlayerId = getCurrentPlayer()?.id
        return playerHands.mapValues { (playerId, cards) ->
            if (playerId == currentPlayerId) {
                PlayerHand(playerId, cards, cards.size)
            } else {
                PlayerHand(playerId, emptyList(), cards.size)
            }
        }
    }
}