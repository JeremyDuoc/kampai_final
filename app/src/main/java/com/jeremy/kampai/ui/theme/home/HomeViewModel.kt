package com.jeremy.kampai.ui.theme.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeremy.kampai.data.GamePopularityRepository
import com.jeremy.kampai.data.GameRepository
import com.jeremy.kampai.domain.models.FeaturedGameSnapshot
import com.jeremy.kampai.domain.models.FeaturedGameSource
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
    private val popularityRepository: GamePopularityRepository,
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

    private val fallbackGame = repository.getGameById(GamePopularityRepository.DEFAULT_FEATURED_GAME_ID)
        ?: repository.getAllGames().firstOrNull()

    private val cachedFeatured = popularityRepository.cachedFeatured()
    private val _featuredGame = MutableStateFlow(
        repository.getGameById(cachedFeatured.gameId) ?: fallbackGame
    )
    val featuredGame: StateFlow<GameModel?> = _featuredGame.asStateFlow()

    private val _featuredSource = MutableStateFlow(cachedFeatured.source)
    val featuredSource: StateFlow<FeaturedGameSource> = _featuredSource.asStateFlow()

    private val _showDisclaimer = MutableStateFlow(false)
    val showDisclaimer: StateFlow<Boolean> = _showDisclaimer.asStateFlow()

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
        _mostPlayedGames.value = repository.getMostPlayedGames()
        _newGames.value = repository.getNewGames()
        _classicGames.value = repository.getClassicGames()
        _vibeGames.value = repository.getGamesByVibe(GameVibe.ALL)
        applyFeatured(popularityRepository.cachedFeatured())
    }

    fun refreshFeatured() {
        viewModelScope.launch {
            applyFeatured(popularityRepository.refreshFeatured())
        }
    }

    private fun applyFeatured(snapshot: FeaturedGameSnapshot) {
        val resolved = repository.getGameById(snapshot.gameId)
        if (resolved != null) {
            _featuredGame.value = resolved
            _featuredSource.value = snapshot.source
        } else {
            _featuredGame.value = fallbackGame
            _featuredSource.value = FeaturedGameSource.DEFAULT
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
