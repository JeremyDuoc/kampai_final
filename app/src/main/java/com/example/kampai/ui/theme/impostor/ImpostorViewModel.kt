package com.example.kampai.ui.theme.impostor

import androidx.lifecycle.ViewModel
import com.example.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ImpostorViewModel @Inject constructor() : ViewModel() {

    sealed class GameState {
        object Setup : GameState()
        data class ShowingWord(val word: String, val isImpostor: Boolean) : GameState()
        object GivingClues : GameState()
        object Voting : GameState()
        data class Results(val impostorWon: Boolean, val impostor: PlayerModel, val realWord: String) : GameState()
    }

    private val words = listOf(
        "Pizza", "Playa", "Guitarra", "Fútbol", "Cerveza",
        "Montaña", "Café", "Libro", "Celular", "Coche",
        "Perro", "Sol", "Avión", "Zapato", "Concierto",
        "Fiesta", "Restaurante", "Cine", "Bicicleta", "Reloj",
        "Botella", "Micrófono", "Computadora", "Cama", "Lluvia"
    )

    private val _gameState = MutableStateFlow<GameState>(GameState.Setup)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _players = MutableStateFlow<List<PlayerModel>>(emptyList())
    val players: StateFlow<List<PlayerModel>> = _players.asStateFlow()

    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex.asStateFlow()

    private val _selectedWord = MutableStateFlow("")
    private val _impostorIndex = MutableStateFlow(-1)

    private val _votedPlayers = MutableStateFlow<Map<String, Int>>(emptyMap())
    val votedPlayers: StateFlow<Map<String, Int>> = _votedPlayers.asStateFlow()

    fun setPlayers(players: List<PlayerModel>) {
        _players.value = players
    }

    fun startGame() {
        if (_players.value.size < 3) {
            // No se puede jugar con menos de 3 jugadores
            return
        }

        _selectedWord.value = words.random()
        _impostorIndex.value = (0 until _players.value.size).random()
        _currentPlayerIndex.value = 0
        _votedPlayers.value = emptyMap()

        showCurrentPlayerWord()
    }

    private fun showCurrentPlayerWord() {
        val isImpostor = _currentPlayerIndex.value == _impostorIndex.value
        _gameState.value = GameState.ShowingWord(
            word = if (isImpostor) "???" else _selectedWord.value,
            isImpostor = isImpostor
        )
    }

    fun nextPlayer() {
        if (_currentPlayerIndex.value < _players.value.size - 1) {
            _currentPlayerIndex.value += 1
            showCurrentPlayerWord()
        } else {
            _gameState.value = GameState.GivingClues
        }
    }

    fun startVoting() {
        _gameState.value = GameState.Voting
        _votedPlayers.value = _players.value.associate { it.id to 0 }
    }

    fun votePlayer(playerId: String) {
        val current = _votedPlayers.value.toMutableMap()
        current[playerId] = (current[playerId] ?: 0) + 1
        _votedPlayers.value = current
    }

    fun showResults() {
        val mostVotedId = _votedPlayers.value.maxByOrNull { it.value }?.key
        val impostorId = _players.value[_impostorIndex.value].id
        val impostorWon = mostVotedId != impostorId

        _gameState.value = GameState.Results(
            impostorWon = impostorWon,
            impostor = _players.value[_impostorIndex.value],
            realWord = _selectedWord.value
        )
    }

    fun reset() {
        _gameState.value = GameState.Setup
        _currentPlayerIndex.value = 0
        _votedPlayers.value = emptyMap()
    }
}