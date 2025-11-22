package com.example.kampai.ui.theme.staring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaringViewModel @Inject constructor() : ViewModel() {

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _count = MutableStateFlow(3)
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _players = MutableStateFlow<List<PlayerModel>>(emptyList())

    private val _selectedPlayers = MutableStateFlow<List<PlayerModel>>(emptyList())
    val selectedPlayers: StateFlow<List<PlayerModel>> = _selectedPlayers.asStateFlow()

    sealed class GameState {
        object Idle : GameState()
        object Counting : GameState()
        object Fight : GameState()
    }

    private var timerJob: Job? = null

    /**
     * Establece la lista de jugadores disponibles
     */
    fun setPlayers(players: List<PlayerModel>) {
        _players.value = players

        // Si hay jugadores suficientes, seleccionar dos aleatorios
        if (players.size >= 2) {
            selectRandomPlayers()
        } else {
            _selectedPlayers.value = emptyList()
        }
    }

    /**
     * Selecciona dos jugadores aleatorios para el duelo
     */
    private fun selectRandomPlayers() {
        val shuffled = _players.value.shuffled()
        _selectedPlayers.value = shuffled.take(2)
    }

    /**
     * Inicia el duelo de miradas
     */
    fun startDuel() {
        // Si hay jugadores suficientes, reseleccionar para variedad
        if (_players.value.size >= 2) {
            selectRandomPlayers()
        }

        _gameState.value = GameState.Counting
        _count.value = 3

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Cuenta regresiva de 3 a 1
            for (i in 3 downTo 1) {
                _count.value = i
                delay(1000)
            }

            // Cambiar a estado de pelea (¡YA!)
            _gameState.value = GameState.Fight
        }
    }

    /**
     * Reinicia el juego a su estado inicial
     */
    fun reset() {
        timerJob?.cancel()
        _gameState.value = GameState.Idle
        _count.value = 3

        // Reseleccionar jugadores si hay suficientes
        if (_players.value.size >= 2) {
            selectRandomPlayers()
        }
    }

    /**
     * Se ejecuta cuando el ViewModel se destruye
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}