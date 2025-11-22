package com.example.kampai.ui.theme.kingscup

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Modificado: Ahora acepta una descripción personalizada opcional
    data class Card(
        val value: CardValue,
        val suit: CardSuit,
        val id: String = "${value.name}_${suit.name}",
        val customDescription: String? = null // Para las reglas dinámicas del Rey
    )

    enum class CardValue(val display: String, val rule: String, val description: String, val emoji: String) {
        ACE("A", "Cascada", "Todos beben hasta que el que sacó pare", "🌊"),
        TWO("2", "Tú", "Elige a alguien para beber", "👉"),
        THREE("3", "Yo", "Quien sacó la carta bebe", "🍺"),
        FOUR("4", "Chicas", "Todas las mujeres beben", "👩"),
        FIVE("5", "Pulgar", "Último en poner pulgar en mesa bebe", "👍"),
        SIX("6", "Chicos", "Todos los hombres beben", "👨"),
        SEVEN("7", "Cielo", "Último en levantar la mano bebe", "✋"),
        EIGHT("8", "Compañero", "Elige un compañero de bebida", "🤝"),
        NINE("9", "Rima", "Di una palabra, rimen hasta fallar", "🎵"),
        TEN("10", "Categoría", "Di una categoría, nombren hasta fallar", "📋"),
        JACK("J", "Regla", "Crea una regla nueva", "⚖️"),
        QUEEN("Q", "Pregunta", "Haces preguntas, quien responda bebe", "❓"),
        // La descripción por defecto del Rey es genérica, la cambiaremos dinámicamente
        KING("K", "Rey", "Ver instrucciones especiales", "👑")
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

            // --- LÓGICA DE LOS REYES ---
            if (drawnCard.value == CardValue.KING) {
                val currentKings = _kingsDrawn.value + 1 // Este será el rey número X
                _kingsDrawn.value = currentKings

                // Asignar descripción según el número de Rey
                val kingDescription = when (currentKings) {
                    1 -> "1º Rey: Elige QUÉ lleva el vaso.\nPrepara la mezcla del trago central."
                    2 -> "2º Rey: Elige DÓNDE se bebe.\nEj: Parado en la mesa, bajo la mesa, en el baño..."
                    3 -> "3º Rey: Elige CÓMO se bebe.\nEj: Agachado, sin manos, haciendo el pino..."
                    4 -> "4º Rey: ¡MALA SUERTE!\nDebes beberte todo el vaso central cumpliendo las reglas anteriores."
                    else -> "Rey Extra: Bebe un trago."
                }

                // Crear una copia de la carta con la nueva descripción
                drawnCard = drawnCard.copy(customDescription = kingDescription)
            }

            _currentCard.value = drawnCard
            _deck.value = _deck.value.drop(1)
            _cardsRemaining.value = _deck.value.size

            _gameState.value = GameState.ShowingCard(drawnCard)

            if (_players.value.isNotEmpty()) {
                _currentPlayerIndex.value = (_currentPlayerIndex.value + 1) % _players.value.size
            }

            if (_kingsDrawn.value == 4 && drawnCard.value == CardValue.KING) {
                // Esperamos un poco más para que lean la penitencia final
                // El estado Finished se manejará después de que el usuario le de a "Siguiente" o tras un delay
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