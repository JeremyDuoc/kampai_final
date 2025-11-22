package com.example.kampai.ui.theme.never

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.data.GameContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeverViewModel @Inject constructor() : ViewModel() {

    private val allQuestions = GameContent.neverQuestions.shuffled().toMutableList()
    private var currentIndex = 0

    private val _currentQuestion = MutableStateFlow(allQuestions.firstOrNull() ?: "")
    val currentQuestion: StateFlow<String> = _currentQuestion.asStateFlow()

    private val _questionNumber = MutableStateFlow(1)
    val questionNumber: StateFlow<Int> = _questionNumber.asStateFlow()

    private val _isChanging = MutableStateFlow(false)
    val isChanging: StateFlow<Boolean> = _isChanging.asStateFlow()

    fun nextQuestion() {
        viewModelScope.launch {
            _isChanging.value = true
            delay(300) // Tiempo para la animación de salida

            // Avanzar al siguiente índice
            currentIndex = (currentIndex + 1) % allQuestions.size

            // Si completamos la lista, barajar de nuevo
            if (currentIndex == 0) {
                allQuestions.shuffle()
            }

            _currentQuestion.value = allQuestions[currentIndex]
            _questionNumber.value = _questionNumber.value + 1

            delay(100) // Pequeña pausa antes de mostrar la nueva pregunta
            _isChanging.value = false
        }
    }

    fun reset() {
        viewModelScope.launch {
            _isChanging.value = true
            delay(300)

            allQuestions.shuffle()
            currentIndex = 0
            _currentQuestion.value = allQuestions[currentIndex]
            _questionNumber.value = 1

            delay(100)
            _isChanging.value = false
        }
    }
}