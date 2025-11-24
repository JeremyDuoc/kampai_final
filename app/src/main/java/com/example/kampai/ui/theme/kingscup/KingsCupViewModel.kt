package com.example.kampai.ui.theme.kingscup

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.R // Importar R
import com.example.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KingsCupViewModel @Inject constructor() : ViewModel() {

    // Modificado: customDescriptionRes ahora es Int? (nullable resource ID)
    data class Card(
        val value: CardValue,
        val suit: CardSuit,
        val id: String = "${value.name}_${suit.name}",
        val customDescriptionRes: Int? = null
    )

    // Enum con Resources IDs
    enum class CardValue(val display: String, val ruleRes: Int, val descriptionRes: Int, val emoji: String) {
        ACE("A", R.string.card_ace_title, R.string.card_ace_desc, "🌊"),
        TWO("2", R.string.card_2_title, R.string.card_2_desc, "👉"),
        THREE("3", R.string.card_3_title, R.string.card_3_desc, "🍺"),
        FOUR("4", R.string.card_4_title, R.string.card_4_desc, "👩"),
        FIVE("5", R.string.card_5_title, R.string.card_5_desc, "👍"),
        SIX("6", R.string.card_6_title, R.string.card_6_desc, "👨"),
        SEVEN("7", R.string.card_7_title, R.string.card_7_desc, "✋"),
        EIGHT("8", R.string.card_8_title, R.string.card_8_desc, "🤝"),
        NINE("9", R.string.card_9_title, R.string.card_9_desc, "🎵"),
        TEN("10", R.string.card_10_title, R.string.card_10_desc, "📋"),
        JACK("J", R.string.card_j_title, R.string.card_j_desc, "⚖️"),
        QUEEN("Q", R.string.card_q_title, R.string.card_q_desc, "❓"),
        KING("K", R.string.card_k_title, R.string.card_k_desc, "👑")
    }

    enum class CardSuit(val symbol: String, val color: Color) {
        HEARTS("♥", Color(0xFFDC2626)),
        DIAMONDS("♦", Color(0xFFDC2626)),
        CLUBS("♣", Color.Black),
        SPADES("♠", Color.Black)
    }

    sealed class GameState {
        object Idle : GameState()
        object Drawing : GameState()
        data class ShowingCard(val card: Card) : GameState()
        object Finished : GameState()
    }

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _deck = MutableStateFlow<List<Card>>(emptyList())
    val deck: StateFlow<List<Card>> = _deck.asStateFlow()

    private val _currentCard = MutableStateFlow<Card?>(null)
    val currentCard: StateFlow<Card?> = _currentCard.asStateFlow()

    private val _kingsDrawn = MutableStateFlow(0)
    val kingsDrawn: StateFlow<Int> = _kingsDrawn.asStateFlow()

    private val _players = MutableStateFlow<List<PlayerModel>>(emptyList())

    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex.asStateFlow()

    private val _cardsRemaining = MutableStateFlow(52)
    val cardsRemaining: StateFlow<Int> = _cardsRemaining.asStateFlow()

    private val _showRulesDialog = MutableStateFlow(false)
    val showRulesDialog: StateFlow<Boolean> = _showRulesDialog.asStateFlow()

    init {
        initializeDeck()
    }

    fun setPlayers(players: List<PlayerModel>) {
        _players.value = players
        _currentPlayerIndex.value = 0
    }

    fun getCurrentPlayer(): PlayerModel? {
        val players = _players.value
        return if (players.isNotEmpty()) {
            players[_currentPlayerIndex.value % players.size]
        } else null
    }

    fun showRules() { _showRulesDialog.value = true }
    fun hideRules() { _showRulesDialog.value = false }

    private fun initializeDeck() {
        val newDeck = mutableListOf<Card>()
        CardValue.values().forEach { value ->
            CardSuit.values().forEach { suit ->
                newDeck.add(Card(value, suit))
            }
        }
        _deck.value = newDeck.shuffled()
        _cardsRemaining.value = newDeck.size
    }

    fun drawCard() {
        if (_deck.value.isEmpty()) {
            _gameState.value = GameState.Finished
            return
        }

        viewModelScope.launch {
            _gameState.value = GameState.Drawing
            delay(600)

            var drawnCard = _deck.value.first()

            // --- LÓGICA DE LOS REYES (Usando IDs) ---
            if (drawnCard.value == CardValue.KING) {
                val currentKings = _kingsDrawn.value + 1
                _kingsDrawn.value = currentKings

                // Asignar ID de descripción según el número de Rey
                val kingDescRes = when (currentKings) {
                    1 -> R.string.king_1_desc
                    2 -> R.string.king_2_desc
                    3 -> R.string.king_3_desc
                    4 -> R.string.king_4_desc
                    else -> R.string.king_extra_desc
                }

                drawnCard = drawnCard.copy(customDescriptionRes = kingDescRes)
            }

            _currentCard.value = drawnCard
            _deck.value = _deck.value.drop(1)
            _cardsRemaining.value = _deck.value.size

            _gameState.value = GameState.ShowingCard(drawnCard)

            if (_players.value.isNotEmpty()) {
                _currentPlayerIndex.value = (_currentPlayerIndex.value + 1) % _players.value.size
            }
        }
    }

    fun reset() {
        viewModelScope.launch {
            _gameState.value = GameState.Idle
            _kingsDrawn.value = 0
            _currentPlayerIndex.value = 0
            initializeDeck()
            delay(300)
        }
    }

    fun nextTurn() {
        if (_kingsDrawn.value >= 4 && _currentCard.value?.value == CardValue.KING) {
            _gameState.value = GameState.Finished
        } else {
            _gameState.value = GameState.Idle
        }
    }
}