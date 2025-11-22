package com.example.kampai.ui.theme.culture

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CultureViewModel @Inject constructor() : ViewModel() {

    private val categories = listOf(
        "Marcas de Autos",
        "Nombres de Países",
        "Marcas de Cerveza",
        "Equipos de Fútbol",
        "Ingredientes de Pizza",
        "Películas de Pixar",
        "Nombres de Mujer que empiecen con M",
        "Partes del Cuerpo Humano"
    )

    private val _currentCategory = MutableStateFlow(categories.first())
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    fun nextCategory() {
        _currentCategory.value = categories.random()
    }
}