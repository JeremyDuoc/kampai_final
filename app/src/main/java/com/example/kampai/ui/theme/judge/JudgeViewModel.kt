package com.example.kampai.ui.theme.judge

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class JudgeViewModel @Inject constructor() : ViewModel() {

    private val rules = listOf(
        "Prohibido decir la palabra 'NO'.",
        "Solo se puede beber con la mano izquierda.",
        "Antes de beber, debes brindar por el Juez.",
        "Nadie puede tocar su propio teléfono.",
        "Hablar como robot hasta nuevo aviso.",
        "Prohibido usar nombres propios.",
        "Cada vez que alguien beba, debe aplaudir.",
        "El jugador más bajo reparte los tragos."
    )

    private val _currentRule = MutableStateFlow(rules.random())
    val currentRule: StateFlow<String> = _currentRule.asStateFlow()

    fun newRule() {
        _currentRule.value = rules.random()
    }
}