package com.example.kampai.ui.theme.roulette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.R // Importar recursos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouletteViewModel @Inject constructor() : ViewModel() {

    private val _chambers = MutableStateFlow<List<Boolean?>>(List(6) { null })
    val chambers: StateFlow<List<Boolean?>> = _chambers.asStateFlow()

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver.asStateFlow()

    private val _messageRes = MutableStateFlow(R.string.roulette_msg_start)
    val messageRes: StateFlow<Int> = _messageRes.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _currentRotation = MutableStateFlow(0f)
    val currentRotation: StateFlow<Float> = _currentRotation.asStateFlow()

    private val _tensionLevel = MutableStateFlow(0f)
    val tensionLevel: StateFlow<Float> = _tensionLevel.asStateFlow()

    private var bulletIndex = -1
    private var clickedCount = 0

    init {
        resetGame()
    }

    fun triggerChamber(index: Int) {
        if (_gameOver.value || _chambers.value[index] != null || _isSpinning.value) return

        viewModelScope.launch {
            spinCylinder()
            delay(800)

            val currentList = _chambers.value.toMutableList()

            if (index == bulletIndex) {
                currentList[index] = true
                _chambers.value = currentList
                delay(200)

                _messageRes.value = R.string.roulette_msg_bang
                _gameOver.value = true
                _tensionLevel.value = 1f
                explosionEffect()
            } else {
                // Click (Seguro)
                currentList[index] = false
                _chambers.value = currentList
                clickedCount++
                delay(100)

                updateTensionLevel()

                // Mensajes progresivos usando IDs
                _messageRes.value = when {
                    clickedCount >= 5 -> R.string.roulette_msg_safe_5
                    clickedCount >= 4 -> R.string.roulette_msg_safe_4
                    clickedCount >= 3 -> R.string.roulette_msg_safe_3
                    clickedCount >= 2 -> R.string.roulette_msg_safe_2
                    else -> R.string.roulette_msg_safe_1
                }
            }
        }
    }

    private suspend fun spinCylinder() {
        _isSpinning.value = true
        val targetRotation = _currentRotation.value + 1080f + (Math.random() * 360f).toFloat()
        val duration = 800L
        val steps = 40
        val stepDelay = duration / steps
        val rotationPerStep = (targetRotation - _currentRotation.value) / steps

        repeat(steps) {
            _currentRotation.value += rotationPerStep
            delay(stepDelay)
        }
        _isSpinning.value = false
    }

    private suspend fun explosionEffect() {
        repeat(3) {
            _currentRotation.value += 30f
            delay(50)
            _currentRotation.value -= 30f
            delay(50)
        }
    }

    private fun updateTensionLevel() {
        val chambersLeft = 6 - clickedCount
        _tensionLevel.value = when (chambersLeft) {
            6 -> 0f
            5 -> 0.2f
            4 -> 0.35f
            3 -> 0.5f
            2 -> 0.7f
            1 -> 0.95f
            else -> 1f
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            _isSpinning.value = true
            val initialRotation = (Math.random() * 360f).toFloat()
            val duration = 1200L
            val steps = 50
            val stepDelay = duration / steps
            val rotationPerStep = (initialRotation + 720f) / steps

            repeat(steps) {
                _currentRotation.value += rotationPerStep
                delay(stepDelay)
            }

            bulletIndex = (0..5).random()
            _chambers.value = List(6) { null }
            _gameOver.value = false
            _messageRes.value = R.string.roulette_msg_start
            _tensionLevel.value = 0f
            clickedCount = 0
            _isSpinning.value = false
        }
    }
}