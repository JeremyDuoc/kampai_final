package com.example.kampai.ui.theme.roulette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouletteViewModel @Inject constructor() : ViewModel() {

    // Representa el estado de las 6 cámaras: true = disparada, false = segura, null = sin abrir
    private val _chambers = MutableStateFlow<List<Boolean?>>(List(6) { null })
    val chambers: StateFlow<List<Boolean?>> = _chambers.asStateFlow()

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver.asStateFlow()

    private val _message = MutableStateFlow("¿Te atreves a jugar?")
    val message: StateFlow<String> = _message.asStateFlow()

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
            // Animación de giro del tambor
            spinCylinder()

            delay(800) // Esperar a que termine el giro

            val currentList = _chambers.value.toMutableList()

            if (index == bulletIndex) {
                // ¡BANG! - Efecto dramático
                currentList[index] = true
                _chambers.value = currentList

                delay(200)

                _message.value = "💥 ¡BANG! ¡TE TOCÓ BEBER! 🍺"
                _gameOver.value = true
                _tensionLevel.value = 1f

                // Efecto de explosión (giro adicional)
                explosionEffect()
            } else {
                // Click (Seguro)
                currentList[index] = false
                _chambers.value = currentList
                clickedCount++

                delay(100)

                // Actualizar nivel de tensión
                updateTensionLevel()

                // Mensaje basado en la tensión
                _message.value = when {
                    clickedCount >= 5 -> "😰 ¡UNA MÁS Y ESTÁS MUERTO!"
                    clickedCount >= 4 -> "😱 ¡LA SUERTE NO DURARÁ MUCHO!"
                    clickedCount >= 3 -> "😬 Esto se pone tenso..."
                    clickedCount >= 2 -> "😅 Salvado por ahora..."
                    else -> "✓ Click... Pasaste esta vez"
                }
            }
        }
    }

    private suspend fun spinCylinder() {
        _isSpinning.value = true

        // Giro rápido del tambor (360 grados * 3 vueltas)
        val targetRotation = _currentRotation.value + 1080f + (Math.random() * 360f).toFloat()
        val duration = 800L
        val steps = 40
        val stepDelay = duration / steps
        val rotationPerStep = (targetRotation - _currentRotation.value) / steps

        repeat(steps) { step ->
            _currentRotation.value += rotationPerStep
            delay(stepDelay)
        }

        _isSpinning.value = false
    }

    private suspend fun explosionEffect() {
        // Giro dramático al explotar
        repeat(3) {
            _currentRotation.value += 30f
            delay(50)
            _currentRotation.value -= 30f
            delay(50)
        }
    }

    private fun updateTensionLevel() {
        // Calcular tensión basada en cuántas cámaras quedan
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

            // Giro inicial al resetear (más dramático)
            val initialRotation = (Math.random() * 360f).toFloat()
            val duration = 1200L
            val steps = 50
            val stepDelay = duration / steps
            val rotationPerStep = (initialRotation + 720f) / steps

            repeat(steps) {
                _currentRotation.value += rotationPerStep
                delay(stepDelay)
            }

            // Resetear estado
            bulletIndex = (0..5).random()
            _chambers.value = List(6) { null }
            _gameOver.value = false
            _message.value = "¿Te atreves a jugar?"
            _tensionLevel.value = 0f
            clickedCount = 0

            _isSpinning.value = false
        }
    }
}