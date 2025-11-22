package com.example.kampai.ui.theme.highlow

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HighLowViewModel @Inject constructor() : ViewModel() {

    // 1. Enum para el resultado de la APUESTA
    enum class Result { CORRECT, WRONG }

    // 2. Enum para la RELACIÓN entre las cartas (para la flecha)
    enum class CardRelation { HIGHER, LOWER, EQUAL }

    private val _currentCard = MutableStateFlow(generateCard())
    val currentCard: StateFlow<Int> = _currentCard.asStateFlow()

    private val _nextCard = MutableStateFlow(0)
    val nextCard: StateFlow<Int> = _nextCard.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _gameActive = MutableStateFlow(true)
    val gameActive: StateFlow<Boolean> = _gameActive.asStateFlow()

    private val _lastResult = MutableStateFlow<Result?>(null)
    val lastResult: StateFlow<Result?> = _lastResult.asStateFlow()

    // 3. Nuevo StateFlow para la relación de la carta
    private val _cardRelation = MutableStateFlow<CardRelation?>(null)
    val cardRelation: StateFlow<CardRelation?> = _cardRelation.asStateFlow()

    private fun generateCard(): Int = Random.nextInt(1, 14)

    fun guessHigher() {
        _gameActive.value = false
        val next = generateCard()
        _nextCard.value = next
        val current = _currentCard.value

        // **ACTUALIZA CardRelation (Para la flecha)**
        _cardRelation.value = when {
            next > current -> CardRelation.HIGHER
            next < current -> CardRelation.LOWER
            else -> CardRelation.EQUAL
        }

        // Lógica de la apuesta
        if (next >= current) {
            _message.value = "¡CORRECTO! Era $next 🎉"
            _lastResult.value = Result.CORRECT
            _streak.value += 1
        } else {
            _message.value = "¡INCORRECTO! Era $next 😢\n¡BEBE!"
            _lastResult.value = Result.WRONG
            _streak.value = 0
        }
    }

    fun guessLower() {
        _gameActive.value = false
        val next = generateCard()
        _nextCard.value = next
        val current = _currentCard.value

        // **ACTUALIZA CardRelation (Para la flecha)**
        _cardRelation.value = when {
            next > current -> CardRelation.HIGHER
            next < current -> CardRelation.LOWER
            else -> CardRelation.EQUAL
        }

        // Lógica de la apuesta
        if (next <= current) {
            _message.value = "¡CORRECTO! Era $next 🎉"
            _lastResult.value = Result.CORRECT
            _streak.value += 1
        } else {
            _message.value = "¡INCORRECTO! Era $next 😢\n¡BEBE!"
            _lastResult.value = Result.WRONG
            _streak.value = 0
        }
    }

    fun nextRound() {
        _currentCard.value = _nextCard.value
        _nextCard.value = 0
        _gameActive.value = true
        _message.value = ""
        _lastResult.value = null
        _cardRelation.value = null
    }
}