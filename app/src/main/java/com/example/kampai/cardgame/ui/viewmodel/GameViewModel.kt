package com.example.kampai.cardgame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.cardgame.domain.engine.GameEngine
import com.example.kampai.cardgame.domain.models.*
import com.example.kampai.cardgame.network.ConnectionState
import com.example.kampai.cardgame.network.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val networkManager: NetworkManager
) : ViewModel() {

    // ==================== STATE FLOWS ====================

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _localPlayerHand = MutableStateFlow<List<CardModel>>(emptyList())
    val localPlayerHand: StateFlow<List<CardModel>> = _localPlayerHand.asStateFlow()

    val gameState = gameEngine.gameState
    val connectionState = networkManager.connectionState

    private val _showColorPicker = MutableStateFlow(false)
    val showColorPicker: StateFlow<Boolean> = _showColorPicker.asStateFlow()

    private val _pendingWildCard = MutableStateFlow<CardModel?>(null)

    private var turnTimerJob: Job? = null
    private var kampaiTimerJob: Job? = null

    private val _turnTimeRemaining = MutableStateFlow(0)
    val turnTimeRemaining: StateFlow<Int> = _turnTimeRemaining.asStateFlow()

    private val _kampaiTimeRemaining = MutableStateFlow(0)
    val kampaiTimeRemaining: StateFlow<Int> = _kampaiTimeRemaining.asStateFlow()

    // ==================== INITIALIZATION ====================

    init {
        observeNetworkMessages()
        observeGameState()
    }

    private fun observeNetworkMessages() {
        viewModelScope.launch {
            networkManager.receivedMessages.collect { message ->
                handleNetworkMessage(message)
            }
        }
    }

    private fun observeGameState() {
        viewModelScope.launch {
            gameEngine.gameState.collect { state ->
                updateUiForGameState(state)

                // Update local player hand if we're the host
                if (networkManager.isHost()) {
                    val localPlayerId = getLocalPlayerId()
                    if (localPlayerId != null) {
                        _localPlayerHand.value = gameEngine.getPlayerHand(localPlayerId).cards
                    }
                }

                // Manage timers
                if (state.phase == GamePhase.PLAYING) {
                    if (isLocalPlayerTurn()) {
                        startTurnTimer(state.ruleConfig.turnDurationSeconds)
                    } else {
                        stopTurnTimer()
                    }
                }

                if (state.kampaiWindowActive && isLocalPlayerTurn()) {
                    startKampaiTimer(state.ruleConfig.kampaiPenaltySeconds)
                } else if (!state.kampaiWindowActive) {
                    stopKampaiTimer()
                }
            }
        }
    }

    // ==================== GAME ACTIONS ====================

    fun playCard(card: CardModel) {
        viewModelScope.launch {
            // Check if it's a wild card - need color selection
            if (card.value.isWild()) {
                _pendingWildCard.value = card
                _showColorPicker.value = true
                return@launch
            }

            executePlayCard(card, null)
        }
    }

    fun selectWildColor(color: CardColor) {
        viewModelScope.launch {
            val card = _pendingWildCard.value
            if (card != null) {
                executePlayCard(card, color)
            }
            _showColorPicker.value = false
            _pendingWildCard.value = null
        }
    }

    private suspend fun executePlayCard(card: CardModel, chosenColor: CardColor?) {
        val localPlayerId = getLocalPlayerId() ?: return

        val action = GameAction.PlayCard(localPlayerId, card, chosenColor)

        if (networkManager.isHost()) {
            // Process locally
            val result = gameEngine.processAction(action)

            if (result is ValidationResult.Success) {
                // Broadcast updated state
                networkManager.broadcastGameState(gameEngine.gameState.value)

                // Send updated hands
                syncAllPlayerHands()
            } else if (result is ValidationResult.Error) {
                showError(result.message)
            }
        } else {
            // Send to host
            networkManager.sendMessage(NetworkMessage.ActionRequest(action))
        }
    }

    fun drawCard() {
        viewModelScope.launch {
            val localPlayerId = getLocalPlayerId() ?: return@launch

            val action = GameAction.DrawCard(localPlayerId)

            if (networkManager.isHost()) {
                val result = gameEngine.processAction(action)

                if (result is ValidationResult.Success) {
                    networkManager.broadcastGameState(gameEngine.gameState.value)
                    syncAllPlayerHands()
                }
            } else {
                networkManager.sendMessage(NetworkMessage.ActionRequest(action))
            }
        }
    }

    fun pressKampai() {
        viewModelScope.launch {
            val localPlayerId = getLocalPlayerId() ?: return@launch

            val action = GameAction.PressKampai(localPlayerId)

            if (networkManager.isHost()) {
                val result = gameEngine.processAction(action)

                if (result is ValidationResult.Success) {
                    networkManager.broadcastGameState(gameEngine.gameState.value)
                }
            } else {
                networkManager.sendMessage(NetworkMessage.ActionRequest(action))
            }

            stopKampaiTimer()
        }
    }

    fun pressPenalty() {
        viewModelScope.launch {
            val localPlayerId = getLocalPlayerId() ?: return@launch
            val targetPlayerId = gameEngine.getCurrentPlayer()?.id ?: return@launch

            val action = GameAction.PressPenalty(localPlayerId, targetPlayerId)

            if (networkManager.isHost()) {
                val result = gameEngine.processAction(action)

                if (result is ValidationResult.Success) {
                    networkManager.broadcastGameState(gameEngine.gameState.value)
                    syncAllPlayerHands()
                }
            } else {
                networkManager.sendMessage(NetworkMessage.ActionRequest(action))
            }
        }
    }

    fun acknowledgeTurnTransition() {
        viewModelScope.launch {
            _uiState.update { it.copy(showTurnTransition = false) }

            // If host, update phase to PLAYING
            if (networkManager.isHost()) {
                val currentState = gameEngine.gameState.value
                if (currentState.phase == GamePhase.TURN_TRANSITION) {
                    // Update phase back to playing
                    // Note: This would need to be added to GameEngine
                    networkManager.broadcastGameState(
                        currentState.copy(phase = GamePhase.PLAYING)
                    )
                }
            }
        }
    }

    // ==================== NETWORK MESSAGE HANDLING ====================

    private fun handleNetworkMessage(message: NetworkMessage) {
        when (message) {
            is NetworkMessage.GameStateSync -> {
                // Client receives state update from host
                if (!networkManager.isHost()) {
                    // Update UI state based on synced game state
                    updateUiForGameState(message.gameState)
                }
            }

            is NetworkMessage.HandSync -> {
                // Client receives their hand from host
                _localPlayerHand.value = message.hand.cards
            }

            is NetworkMessage.ActionRequest -> {
                // Host receives action from client
                if (networkManager.isHost()) {
                    viewModelScope.launch {
                        val result = gameEngine.processAction(message.action)

                        when (result) {
                            is ValidationResult.Success -> {
                                networkManager.broadcastGameState(gameEngine.gameState.value)
                                syncAllPlayerHands()

                                // Send success to requester
                                val playerId = when (message.action) {
                                    is GameAction.PlayCard -> message.action.playerId
                                    is GameAction.DrawCard -> message.action.playerId
                                    is GameAction.PressKampai -> message.action.playerId
                                    is GameAction.PressPenalty -> message.action.accuserId
                                    is GameAction.EndTurn -> message.action.playerId
                                }

                                networkManager.sendMessage(
                                    NetworkMessage.ActionResult(true, null, gameEngine.gameState.value),
                                    playerId
                                )
                            }

                            is ValidationResult.Error -> {
                                // Send error to requester
                                val playerId = when (message.action) {
                                    is GameAction.PlayCard -> message.action.playerId
                                    is GameAction.DrawCard -> message.action.playerId
                                    is GameAction.PressKampai -> message.action.playerId
                                    is GameAction.PressPenalty -> message.action.accuserId
                                    is GameAction.EndTurn -> message.action.playerId
                                }

                                networkManager.sendMessage(
                                    NetworkMessage.ActionResult(false, result.message),
                                    playerId
                                )
                            }
                        }
                    }
                }
            }

            is NetworkMessage.ActionResult -> {
                // Client receives result of their action
                if (!message.success) {
                    showError(message.message ?: "Acción fallida")
                }
            }

            is NetworkMessage.KampaiWindowOpened -> {
                // All clients notified of KAMPAI window
                if (message.targetPlayerId != getLocalPlayerId()) {
                    // Other players can press penalty
                    _uiState.update { it.copy(canPressPenalty = true) }
                }
            }

            is NetworkMessage.TurnTransition -> {
                // Show turn transition screen
                _uiState.update {
                    it.copy(
                        showTurnTransition = true,
                        nextPlayerName = getPlayerName(message.nextPlayerId)
                    )
                }
            }

            else -> { /* Handle other messages */ }
        }
    }

    // ==================== SYNCHRONIZATION ====================

    private suspend fun syncAllPlayerHands() {
        if (!networkManager.isHost()) return

        val state = gameEngine.gameState.value

        state.players.forEach { player ->
            val hand = if (player.id == gameEngine.getCurrentPlayer()?.id) {
                // Send full hand to active player
                gameEngine.getFullHandForPlayer(player.id)
            } else {
                // Send only hand size to others
                gameEngine.getPlayerHand(player.id).withoutCards()
            }

            networkManager.sendHandToPlayer(player.id, hand)
        }
    }

    // ==================== TIMERS ====================

    private fun startTurnTimer(seconds: Int) {
        stopTurnTimer()
        turnTimerJob = viewModelScope.launch {
            _turnTimeRemaining.value = seconds

            repeat(seconds) {
                delay(1000)
                _turnTimeRemaining.value -= 1
            }

            // Time's up - auto draw card
            if (_turnTimeRemaining.value <= 0) {
                drawCard()
            }
        }
    }

    private fun stopTurnTimer() {
        turnTimerJob?.cancel()
        _turnTimeRemaining.value = 0
    }

    private fun startKampaiTimer(seconds: Int) {
        stopKampaiTimer()
        kampaiTimerJob = viewModelScope.launch {
            _kampaiTimeRemaining.value = seconds

            // Notify other players of penalty window
            if (networkManager.isHost()) {
                val currentPlayer = gameEngine.getCurrentPlayer()
                if (currentPlayer != null) {
                    networkManager.sendMessage(
                        NetworkMessage.KampaiWindowOpened(
                            currentPlayer.id,
                            System.currentTimeMillis() + (seconds * 1000L)
                        )
                    )
                }
            }

            repeat(seconds) {
                delay(1000)
                _kampaiTimeRemaining.value -= 1
            }

            // Time expired - close window
            if (_kampaiTimeRemaining.value <= 0 && networkManager.isHost()) {
                val currentState = gameEngine.gameState.value
                networkManager.broadcastGameState(
                    currentState.copy(
                        kampaiWindowActive = false,
                        phase = GamePhase.PLAYING
                    )
                )
            }
        }
    }

    private fun stopKampaiTimer() {
        kampaiTimerJob?.cancel()
        _kampaiTimeRemaining.value = 0
    }

    // ==================== UI STATE UPDATES ====================

    private fun updateUiForGameState(state: GameState) {
        _uiState.update { currentUi ->
            currentUi.copy(
                currentPhase = state.phase,
                isMyTurn = isLocalPlayerTurn(),
                canPlayCard = isLocalPlayerTurn() && state.phase == GamePhase.PLAYING,
                canDrawCard = isLocalPlayerTurn() && state.phase == GamePhase.PLAYING,
                mustDrawCards = state.stackedPlusCards > 0,
                cardsToDrawCount = state.stackedPlusCards,
                showKampaiButton = state.kampaiWindowActive && isLocalPlayerTurn(),
                canPressPenalty = state.kampaiWindowActive && !isLocalPlayerTurn(),
                gameWinner = state.players.find { it.id == state.winnerId }
            )
        }
    }

    // ==================== UTILITIES ====================

    private fun isLocalPlayerTurn(): Boolean {
        val localPlayerId = getLocalPlayerId() ?: return false
        return gameEngine.getCurrentPlayer()?.id == localPlayerId
    }

    private fun getLocalPlayerId(): String? {
        return when (val state = networkManager.connectionState.value) {
            is ConnectionState.Hosting -> state.hostInfo.id
            is ConnectionState.Connected -> state.playerInfo.id
            else -> null
        }
    }

    private fun getPlayerName(playerId: String): String {
        return gameEngine.gameState.value.players
            .find { it.id == playerId }?.name ?: "Jugador"
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }

        // Clear error after 3 seconds
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(errorMessage = null) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ==================== HOST FUNCTIONS ====================

    fun startGame(players: List<PlayerInfo>, ruleConfig: RuleConfig) {
        if (!networkManager.isHost()) return

        viewModelScope.launch {
            gameEngine.initializeGame(players, ruleConfig)

            // Broadcast initial state
            networkManager.broadcastGameState(gameEngine.gameState.value)

            // Send initial hands
            syncAllPlayerHands()
        }
    }

    // ==================== CLEANUP ====================

    override fun onCleared() {
        super.onCleared()
        stopTurnTimer()
        stopKampaiTimer()
    }
}

// ==================== UI STATE DATA CLASS ====================

data class GameUiState(
    val currentPhase: GamePhase = GamePhase.LOBBY,
    val isMyTurn: Boolean = false,
    val canPlayCard: Boolean = false,
    val canDrawCard: Boolean = false,
    val mustDrawCards: Boolean = false,
    val cardsToDrawCount: Int = 0,
    val showKampaiButton: Boolean = false,
    val canPressPenalty: Boolean = false,
    val showTurnTransition: Boolean = false,
    val nextPlayerName: String = "",
    val gameWinner: PlayerInfo? = null,
    val errorMessage: String? = null
)