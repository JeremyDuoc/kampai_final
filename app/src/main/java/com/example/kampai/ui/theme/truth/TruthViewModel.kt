package com.example.kampai.ui.theme.truth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.R // Importar R
import com.example.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TruthViewModel @Inject constructor() : ViewModel() {

    sealed class GameState {
        object Selection : GameState()
        // Cambiamos text: String por textRes: Int
        data class Result(val type: Type, val textRes: Int, val player: PlayerModel?) : GameState()
    }

    enum class Type { TRUTH, DARE }

    private val _uiState = MutableStateFlow<GameState>(GameState.Selection)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _truthCount = MutableStateFlow(0)
    val truthCount: StateFlow<Int> = _truthCount.asStateFlow()

    private val _dareCount = MutableStateFlow(0)
    val dareCount: StateFlow<Int> = _dareCount.asStateFlow()

    private val _players = MutableStateFlow<List<PlayerModel>>(emptyList())

    private val _currentPlayer = MutableStateFlow<PlayerModel?>(null)
    val currentPlayer: StateFlow<PlayerModel?> = _currentPlayer.asStateFlow()

    // LISTAS DE RECURSOS (INT)
    private val truths = listOf(
        R.string.truth_1, R.string.truth_2, R.string.truth_3, R.string.truth_4,
        R.string.truth_5, R.string.truth_6, R.string.truth_7, R.string.truth_8,
        R.string.truth_9, R.string.truth_10, R.string.truth_11, R.string.truth_12,
        R.string.truth_13, R.string.truth_14, R.string.truth_15, R.string.truth_16,
        R.string.truth_17, R.string.truth_18, R.string.truth_19, R.string.truth_20
    ).shuffled().toMutableList()

    private val dares = listOf(
        R.string.dare_1, R.string.dare_2, R.string.dare_3, R.string.dare_4,
        R.string.dare_5, R.string.dare_6, R.string.dare_7, R.string.dare_8,
        R.string.dare_9, R.string.dare_10, R.string.dare_11, R.string.dare_12,
        R.string.dare_13, R.string.dare_14, R.string.dare_15, R.string.dare_16,
        R.string.dare_17, R.string.dare_18, R.string.dare_19, R.string.dare_20,
        R.string.dare_21, R.string.dare_22, R.string.dare_23, R.string.dare_24,
        R.string.dare_25
    ).shuffled().toMutableList()

    private var truthIndex = 0
    private var dareIndex = 0

    fun setPlayers(players: List<PlayerModel>) {
        _players.value = players
        if (players.isNotEmpty() && _currentPlayer.value == null) {
            _currentPlayer.value = players.random()
        }
    }

    fun pickTruth() {
        viewModelScope.launch {
            val truthRes = truths[truthIndex]
            truthIndex = (truthIndex + 1) % truths.size
            if (truthIndex == 0) truths.shuffle()

            _truthCount.value += 1
            _uiState.value = GameState.Result(Type.TRUTH, truthRes, _currentPlayer.value)
        }
    }

    fun pickDare() {
        viewModelScope.launch {
            val dareRes = dares[dareIndex]
            dareIndex = (dareIndex + 1) % dares.size
            if (dareIndex == 0) dares.shuffle()

            _dareCount.value += 1
            _uiState.value = GameState.Result(Type.DARE, dareRes, _currentPlayer.value)
        }
    }

    fun reset() {
        viewModelScope.launch {
            if (_players.value.isNotEmpty()) {
                _currentPlayer.value = _players.value.random()
            }
            _uiState.value = GameState.Selection
        }
    }
}