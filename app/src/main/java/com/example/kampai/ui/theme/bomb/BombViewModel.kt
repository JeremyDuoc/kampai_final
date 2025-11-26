package com.example.kampai.ui.theme.bomb

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.utils.SoundManager
import com.example.kampai.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BombViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val soundManager: SoundManager
) : ViewModel() {

    sealed class GameState {
        object Idle : GameState()
        object Playing : GameState()
        object Exploded : GameState()
    }

    private val _uiState = MutableStateFlow<GameState>(GameState.Idle)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private var timerJob: Job? = null

    private val categories = context.resources.getStringArray(R.array.bomb_categories_list).toList()

    fun startGame() {
        val duration = Random.nextInt(30, 61)
        _category.value = categories.random()
        _timeLeft.value = duration
        _uiState.value = GameState.Playing

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                val soundSpeed = when {
                    _timeLeft.value <= 3 -> 250L   // Muy rápido
                    _timeLeft.value <= 5 -> 450L   // Rápido
                    _timeLeft.value <= 10 -> 700L  // Medio
                    _timeLeft.value <= 15 -> 900L  // Normal
                    else -> 1100L                  // Lento
                }

                try {
                    Log.d("KampaiBomb", "Tic-tac - Tiempo: ${_timeLeft.value}s")
                    soundManager.playSound(R.raw.tic_tac)
                } catch (e: Exception) {
                    Log.e("KampaiBomb", "Error reproduciendo sonido: ${e.message}")
                }

                delay(soundSpeed)
                _timeLeft.value -= 1
            }


            _uiState.value = GameState.Exploded
            try {
                Log.d("KampaiBomb", "💥 ¡EXPLOSIÓN!")
                soundManager.playSound(R.raw.explosion)
            } catch (e: Exception) {
                Log.e("KampaiBomb", "Error en explosión: ${e.message}")
            }

            delay(100)
        }
    }

    fun resetGame() {
        timerJob?.cancel()
        soundManager.stopSound()
        _uiState.value = GameState.Idle
        _timeLeft.value = 0
        _category.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.stopSound()
    }
}