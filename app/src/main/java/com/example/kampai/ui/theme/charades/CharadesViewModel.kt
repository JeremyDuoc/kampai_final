package com.example.kampai.ui.theme.charades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharadesViewModel @Inject constructor() : ViewModel() {

    enum class Difficulty(val displayName: String, val timeSeconds: Int, val pointsPerCorrect: Int) {
        EASY("Fácil", 60, 1),
        MEDIUM("Normal", 45, 2),
        HARD("Difícil", 30, 3)
    }

    private val wordsEasy = listOf(
        "Dormir", "Comer Pizza", "Bailar", "Nadar", "Correr",
        "Cantar", "Llorar", "Reír", "Bostezar", "Estornudar",
        "Beber Agua", "Caminar", "Saltar", "Aplaudir", "Silbar"
    )

    private val wordsMedium = listOf(
        "Borracho", "T-Rex", "Caminar sobre fuego", "Bailar Salsa",
        "Enhebrar una aguja", "Pelea de gallos", "Surfear",
        "Cambiar un pañal", "Astronauta", "Zombie",
        "Malabarista", "Mimo", "Superhéroe volando", "Planchar ropa",
        "Pescar", "Escalador", "DJ en concierto"
    )

    private val wordsHard = listOf(
        "Canguro boxeador", "Peluquero cortando pelo invisible",
        "Robot sin batería", "Pingüino patinando", "Estatua cobrando vida",
        "Ninja en misión secreta", "Samurái meditando",
        "Vampiro con dolor de muelas", "Fantasma asustado",
        "Extraterrestre perdido", "Mago fallando truco",
        "Equilibrista en cuerda floja", "Sonámbulo", "Hipnotizador",
        "Contorsionista", "Ventrílocuo"
    )

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentWord = MutableStateFlow("")
    val currentWord: StateFlow<String> = _currentWord.asStateFlow()

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _currentDifficulty = MutableStateFlow(Difficulty.MEDIUM)
    val currentDifficulty: StateFlow<Difficulty> = _currentDifficulty.asStateFlow()

    sealed class GameState {
        object Idle : GameState()
        object Playing : GameState()
        object Finished : GameState()
    }

    private var timerJob: Job? = null
    private var usedWords = mutableSetOf<String>()

    fun setDifficulty(difficulty: Difficulty) {
        if (_gameState.value is GameState.Idle) {
            _currentDifficulty.value = difficulty
        }
    }

    fun startGame() {
        _score.value = 0
        usedWords.clear()
        _currentWord.value = getRandomWord()
        _timeLeft.value = _currentDifficulty.value.timeSeconds
        _gameState.value = GameState.Playing

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value -= 1
            }
            _gameState.value = GameState.Finished
        }
    }

    private fun getRandomWord(): String {
        val availableWords = when (_currentDifficulty.value) {
            Difficulty.EASY -> wordsEasy
            Difficulty.MEDIUM -> wordsMedium
            Difficulty.HARD -> wordsHard
        }.filter { it !in usedWords }

        // Si se acabaron las palabras, reiniciar pool
        if (availableWords.isEmpty()) {
            usedWords.clear()
            return getRandomWord()
        }

        val word = availableWords.random()
        usedWords.add(word)
        return word
    }

    fun gotIt() {
        if (_gameState.value != GameState.Playing) return

        // Sumar puntos según dificultad
        _score.value += _currentDifficulty.value.pointsPerCorrect

        // Bonus por tiempo rápido (si queda más del 70% del tiempo)
        val timeThreshold = (_currentDifficulty.value.timeSeconds * 0.7).toInt()
        if (_timeLeft.value > timeThreshold) {
            _score.value += 1 // Punto bonus
        }

        // Siguiente palabra
        _currentWord.value = getRandomWord()
    }

    fun skip() {
        if (_gameState.value != GameState.Playing) return

        // Penalización por pasar (-1 punto, mínimo 0)
        _score.value = (_score.value - 1).coerceAtLeast(0)

        // Siguiente palabra
        _currentWord.value = getRandomWord()
    }

    fun reset() {
        timerJob?.cancel()
        _gameState.value = GameState.Idle
        _timeLeft.value = _currentDifficulty.value.timeSeconds
        _score.value = 0
        usedWords.clear()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}