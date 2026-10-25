package com.jeremy.kampai.ui.theme.kingscup

import androidx.lifecycle.ViewModel
import com.jeremy.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class KingsCupViewModel @Inject constructor() : ViewModel() {

    private val _partyPlayers = MutableStateFlow<List<PlayerModel>>(emptyList())
    val partyPlayers: StateFlow<List<PlayerModel>> = _partyPlayers.asStateFlow()

    private val _state = MutableStateFlow(KingsCupEngine.State(phase = KingsCupEngine.Phase.SETUP))
    val state: StateFlow<KingsCupEngine.State> = _state.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var deck: List<KingsCupEngine.Card> = emptyList()
    private var currentPlayerIndex: Int = 0
    private var kingsCount: Int = 0

    init {
        deck = KingsCupEngine.createFreshDeck()
    }

    fun syncParty(partyRoster: List<PlayerModel>) {
        _partyPlayers.value = partyRoster
        if (_state.value.phase == KingsCupEngine.Phase.SETUP) {
            _state.update {
                it.copy(
                    currentPlayer = partyRoster.firstOrNull(),
                    remaining = if (deck.isNotEmpty()) deck.size else 52
                )
            }
        } else if (_state.value.currentPlayer == null && partyRoster.isNotEmpty()) {
            val safeIndex = currentPlayerIndex % partyRoster.size
            _state.update { it.copy(currentPlayer = partyRoster.getOrNull(safeIndex)) }
        }
    }

    fun start() {
        deck = KingsCupEngine.createFreshDeck()
        kingsCount = 0
        currentPlayerIndex = 0
        val player = _partyPlayers.value.firstOrNull()
        _state.value = KingsCupEngine.State(
            phase = KingsCupEngine.Phase.READY,
            kings = 0,
            remaining = deck.size,
            currentPlayer = player,
            card = null
        )
    }

    fun draw() {
        if (deck.isEmpty()) {
            _state.update { it.copy(phase = KingsCupEngine.Phase.FINISHED) }
            return
        }

        val drawnCard = deck.first()
        deck = deck.drop(1)
        if (drawnCard.rank == KingsCupEngine.Rank.KING) {
            kingsCount++
        }

        _state.update {
            it.copy(
                phase = KingsCupEngine.Phase.REVEALED,
                kings = kingsCount,
                remaining = deck.size,
                card = drawnCard
            )
        }
    }

    fun nextTurn() {
        val currentCard = _state.value.card
        if (kingsCount >= 4 && currentCard?.rank == KingsCupEngine.Rank.KING) {
            _state.update { it.copy(phase = KingsCupEngine.Phase.FINISHED) }
            return
        }

        if (deck.isEmpty()) {
            _state.update { it.copy(phase = KingsCupEngine.Phase.FINISHED) }
            return
        }

        val party = _partyPlayers.value
        if (party.isNotEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % party.size
        }
        val nextPlayer = party.getOrNull(currentPlayerIndex)

        _state.update {
            it.copy(
                phase = KingsCupEngine.Phase.READY,
                currentPlayer = nextPlayer,
                card = null
            )
        }
    }

    fun newGame() {
        start()
    }

    fun reset() {
        deck = KingsCupEngine.createFreshDeck()
        kingsCount = 0
        currentPlayerIndex = 0
        val party = _partyPlayers.value
        val phase = if (party.size >= 2) KingsCupEngine.Phase.READY else KingsCupEngine.Phase.SETUP
        _state.value = KingsCupEngine.State(
            phase = phase,
            kings = 0,
            remaining = deck.size,
            currentPlayer = party.firstOrNull(),
            card = null
        )
    }

    fun debugForceKing() {
        val kingIndex = deck.indexOfFirst { it.rank == KingsCupEngine.Rank.KING }
        if (kingIndex >= 0) {
            val king = deck[kingIndex]
            deck = listOf(king) + deck.filterIndexed { i, _ -> i != kingIndex }
        }
    }
}