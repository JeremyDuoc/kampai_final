package com.jeremy.kampai.ui.theme.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeremy.kampai.data.GameRepository
import com.jeremy.kampai.domain.models.GameModel
import com.jeremy.kampai.domain.models.GameVibe
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GameRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _mostPlayedGames = MutableStateFlow<List<GameModel>>(emptyList())
    val mostPlayedGames: StateFlow<List<GameModel>> = _mostPlayedGames.asStateFlow()

    private val _newGames = MutableStateFlow<List<GameModel>>(emptyList())
    val newGames: StateFlow<List<GameModel>> = _newGames.asStateFlow()

    private val _classicGames = MutableStateFlow<List<GameModel>>(emptyList())
    val classicGames: StateFlow<List<GameModel>> = _classicGames.asStateFlow()

    private val _selectedVibe = MutableStateFlow(GameVibe.ALL)
    val selectedVibe: StateFlow<GameVibe> = _selectedVibe.asStateFlow()

    private val _vibeGames = MutableStateFlow<List<GameModel>>(emptyList())
    val vibeGames: StateFlow<List<GameModel>> = _vibeGames.asStateFlow()

    private val _showDisclaimer = MutableStateFlow(false)
    val showDisclaimer: StateFlow<Boolean> = _showDisclaimer.asStateFlow()

    // Sorteo aleatorio (Dado del Destino)
    private val _showDiceModal = MutableStateFlow(false)
    val showDiceModal: StateFlow<Boolean> = _showDiceModal.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    private val _rolledGame = MutableStateFlow<GameModel?>(null)
    val rolledGame: StateFlow<GameModel?> = _rolledGame.asStateFlow()

    init {
        checkDisclaimerStatus()
        loadGames()
    }

    private fun checkDisclaimerStatus() {
        val prefs = context.getSharedPreferences("kampai_prefs", Context.MODE_PRIVATE)
        val isAccepted = prefs.getBoolean("disclaimer_accepted", false)
        _showDisclaimer.value = !isAccepted
    }

    fun acceptDisclaimer() {
        val prefs = context.getSharedPreferences("kampai_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("disclaimer_accepted", true).apply()
        _showDisclaimer.value = false
    }

    private fun loadGames() {
        viewModelScope.launch {
            _mostPlayedGames.value = repository.getMostPlayedGames()
            _newGames.value = repository.getNewGames()
            _classicGames.value = repository.getClassicGames()
            _vibeGames.value = repository.getGamesByVibe(GameVibe.ALL)
        }
    }

    fun selectVibe(vibe: GameVibe) {
        _selectedVibe.value = vibe
        _vibeGames.value = repository.getGamesByVibe(vibe)
    }

    fun openDiceModal() {
        _showDiceModal.value = true
        rollDice()
    }

    fun closeDiceModal() {
        _showDiceModal.value = false
        _isRolling.value = false
    }

    fun rollDice() {
        viewModelScope.launch {
            _isRolling.value = true
            // Efecto de ruleta durante 800ms
            val all = repository.getAllGames()
            repeat(6) {
                _rolledGame.value = all.random()
                delay(120)
            }
            _rolledGame.value = repository.getRandomGame()
            _isRolling.value = false
        }
    }
}