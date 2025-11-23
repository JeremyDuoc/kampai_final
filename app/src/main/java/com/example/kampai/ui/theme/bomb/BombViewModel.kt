package com.example.kampai.ui.theme.bomb

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.utils.SoundManager
import com.example.kampai.R
import dagger.hilt.android.lifecycle.HiltViewModel
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

    // Categorías expandidas y más interesantes
    private val categories = listOf(
        "Marcas de Coches",
        "Pokémones",
        "Capitales de Europa",
        "Marcas de Cerveza",
        "Ingredientes de Pizza",
        "Películas Disney",
        "Partes del Cuerpo",
        "Palabras que rimen con 'RON'",
        "Superhéroes Marvel",
        "Cosas en un baño",
        "Razas de Perros",
        "Equipos de Fútbol",
        "Nombres con 'A'",
        "Frutas Tropicales",
        "Países de América",
        "Instrumentos Musicales",
        "Colores en inglés",
        "Películas de Terror",
        "Marcas de Ropa",
        "Aplicaciones del móvil",
        "Emojis populares",
        "Canciones de Reggaeton",
        "Videojuegos famosos",
        "Redes Sociales",
        "Tipos de Queso",
        "Marcas de Comida Rápida",
        "Series de Netflix",
        "Artistas de Trap",
        "Cosas en una cocina",
        "Modelos de iPhone",
        "Palabras con 'Q'",
        "Cócteles famosos",
        "Países de Asia",
        "Marcas de Zapatillas",
        "Postres típicos",
        "Herramientas de trabajo",
        "Animales marinos",
        "Planetas del Sistema Solar",
        "Géneros musicales",
        "Tipos de Pasta"
    )

    fun startGame() {
        // Tiempo aleatorio más dinámico (entre 30 y 61 segundos)
        val duration = Random.nextInt(30, 61)
        _category.value = categories.random()
        _timeLeft.value = duration
        _uiState.value = GameState.Playing

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                // Velocidad del sonido aumenta con la urgencia
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

            // ¡EXPLOSIÓN!
            _uiState.value = GameState.Exploded
            try {
                Log.d("KampaiBomb", "💥 ¡EXPLOSIÓN!")
                soundManager.playSound(R.raw.explosion)
            } catch (e: Exception) {
                Log.e("KampaiBomb", "Error en explosión: ${e.message}")
            }

            // Efecto de vibración adicional (pequeño delay para impacto)
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