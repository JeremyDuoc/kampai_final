package com.example.kampai.ui.theme.likely

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.kampai.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MostLikelyViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Cargamos las preguntas desde el XML y las mezclamos
    private val questions = context.resources.getStringArray(R.array.likely_questions_list)
        .toList()
        .shuffled()
        .toMutableList()

    // Usamos un índice para recorrerlas sin repetir hasta acabar la lista
    private var currentIndex = 0

    private val _question = MutableStateFlow(questions.firstOrNull() ?: "...")
    val question = _question.asStateFlow()

    fun nextQuestion() {
        if (questions.isEmpty()) return

        // Avanzamos el índice
        currentIndex = (currentIndex + 1) % questions.size

        // Si volvemos al principio, remezclamos para que el orden sea distinto
        if (currentIndex == 0) {
            questions.shuffle()
        }

        _question.value = questions[currentIndex]
    }
}