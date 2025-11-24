package com.example.kampai.ui.theme.charades

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class CharadesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Modificado: Ahora nameRes es Int para traducción
    enum class Difficulty(val nameRes: Int, val timeSeconds: Int, val pointsPerCorrect: Int) {
        EASY(R.string.charades_diff_easy, 60, 1),
        MEDIUM(R.string.charades_diff_medium, 45, 2),
        HARD(R.string.charades_diff_hard, 30, 3)
    }

    // Cargamos las listas desde XML
    private val wordsEasy = context.resources.getStringArray(R.array.charades_easy_list).toList()
    private val wordsMedium = context.resources.getStringArray(R.array.charades_medium_list).toList()
    private val wordsHard = context.resources.getStringArray(R.array.charades_hard_list).toList()

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

        if (availableWords.isEmpty()) {
            usedWords.clear()
            return getRandomWord() // Recursión segura si se reinicia
        }

        val word = availableWords.random()
        usedWords.add(word)
        return word
    }

    fun gotIt() {
        if (_gameState.value != GameState.Playing) return

        _score.value += _currentDifficulty.value.pointsPerCorrect

        val timeThreshold = (_currentDifficulty.value.timeSeconds * 0.7).toInt()
        if (_timeLeft.value > timeThreshold) {
            _score.value += 1
        }

        _currentWord.value = getRandomWord()
    }

    fun skip() {
        if (_gameState.value != GameState.Playing) return
        _score.value = (_score.value - 1).coerceAtLeast(0)
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