package com.example.kampai.ui.theme.karaoke

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KaraokeViewModel @Inject constructor() : ViewModel() {

    /**
     * NOTA LEGAL: Todas las canciones usan descripciones genéricas
     * en lugar de títulos específicos para evitar problemas de copyright.
     * Ejemplo: "Canción de amor lenta" en lugar de "Thinking Out Loud"
     */
    data class Song(
        val title: String,           // Descripción genérica
        val artist: String,          // Género o estilo
        val parts: List<String>,     // Partes disponibles
        val emoji: String = "🎵"
    )

    sealed class GameState {
        object Idle : GameState()
        data class Singing(val song: Song, val part: String, val singer: PlayerModel) : GameState()
        data class Voting(val song: Song, val part: String, val singer: PlayerModel) : GameState()
        data class Results(
            val singer: PlayerModel,
            val approved: Boolean,
            val yesVotes: Int,
            val noVotes: Int
        ) : GameState()
    }

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _timeLeft = MutableStateFlow(30)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _players = MutableStateFlow<List<PlayerModel>>(emptyList())

    private val _votes = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val votes: StateFlow<Map<String, Boolean>> = _votes.asStateFlow()

    private var timerJob: Job? = null

    // Lista de canciones con descripciones genéricas (safe para copyright)
    private val songLibrary = listOf(
        Song("Canción de fiesta latina", "Reggaeton", listOf("Intro", "Estribillo", "Verso 1", "Puente"), "🔥"),
        Song("Balada romántica lenta", "Pop Romántico", listOf("Intro", "Estribillo", "Verso 2"), "❤️"),
        Song("Himno de rock clásico", "Rock", listOf("Intro", "Estribillo", "Solo de guitarra"), "🎸"),
        Song("Canción de verano bailable", "Pop", listOf("Estribillo", "Verso 1", "Puente"), "☀️"),
        Song("Reguetón pegajoso", "Urbano", listOf("Intro", "Estribillo", "Verso 1", "Outro"), "💃"),
        Song("Balada de desamor", "Balada", listOf("Estribillo", "Verso 2", "Puente"), "💔"),
        Song("Canción de fiesta 2000s", "Pop", listOf("Intro", "Estribillo", "Rap part"), "🎉"),
        Song("Rock español clásico", "Rock", listOf("Estribillo", "Verso 1", "Solo"), "🇪🇸"),
        Song("Reggaeton romántico", "Urbano", listOf("Intro", "Estribillo", "Verso 2"), "🌹"),
        Song("Pop en inglés pegajoso", "Pop", listOf("Estribillo", "Verso 1", "Puente"), "🎤"),
        Song("Cumbia moderna", "Cumbia", listOf("Estribillo", "Verso 1", "Instrumental"), "🪇"),
        Song("Trap latino", "Trap", listOf("Intro", "Estribillo", "Verso 1"), "🔊"),
        Song("Bachata romántica", "Bachata", listOf("Estribillo", "Verso 2", "Outro"), "💕"),
        Song("Rock alternativo", "Rock Alt", listOf("Estribillo", "Verso 1", "Puente"), "🎵"),
        Song("Salsa clásica bailable", "Salsa", listOf("Intro", "Estribillo", "Instrumental"), "💃"),
        Song("Pop latino actual", "Pop Latino", listOf("Estribillo", "Verso 1", "Rap part"), "🌟"),
        Song("Balada power 80s", "Rock Balada", listOf("Estribillo", "Verso 2", "Solo guitarra"), "⚡"),
        Song("Electrónica bailable", "EDM", listOf("Drop", "Buildup", "Outro"), "🎧"),
        Song("Rap español clásico", "Rap", listOf("Estribillo", "Verso 1", "Verso 2"), "🎤"),
        Song("Merengue fiestero", "Merengue", listOf("Estribillo", "Verso 1", "Instrumental"), "🎺"),
        Song("Indie español moderno", "Indie", listOf("Estribillo", "Verso 1", "Puente"), "🎸"),
        Song("Reggae relajado", "Reggae", listOf("Estribillo", "Verso 1", "Outro"), "🌴"),
        Song("Pop rock enérgico", "Pop Rock", listOf("Estribillo", "Verso 1", "Puente"), "⚡"),
        Song("Balada mexicana", "Regional", listOf("Estribillo", "Verso 2", "Final"), "🇲🇽"),
        Song("Dance pop 90s", "Dance", listOf("Estribillo", "Verso 1", "Breakdown"), "💿"),
        Song("Rock pesado", "Metal", listOf("Estribillo", "Verso 1", "Breakdown"), "🤘"),
        Song("Canción protesta", "Folk Rock", listOf("Estribillo", "Verso 2", "Puente"), "✊"),
        Song("Tropical house", "House", listOf("Drop", "Buildup", "Outro"), "🌺"),
        Song("Corrido tumbado", "Urbano Mexicano", listOf("Intro", "Estribillo", "Verso 1"), "🎺"),
        Song("Pop punk 2000s", "Pop Punk", listOf("Estribillo", "Verso 1", "Puente"), "🎸"),
        Song("R&B suave", "R&B", listOf("Estribillo", "Verso 2", "Adlibs"), "🎶"),
        Song("Rap romántico", "Hip Hop", listOf("Estribillo", "Verso 1", "Outro"), "💘"),
        Song("Electro latino", "Electro Latino", listOf("Drop", "Estribillo", "Buildup"), "⚡"),
        Song("Bolero clásico", "Bolero", listOf("Estribillo", "Verso 2", "Final"), "🎻"),
        Song("Ska alegre", "Ska", listOf("Estribillo", "Verso 1", "Instrumental"), "🎺"),
        Song("Alternative rock 90s", "Alt Rock", listOf("Estribillo", "Verso 1", "Solo"), "🎸"),
        Song("Dembow caribeño", "Dembow", listOf("Intro", "Estribillo", "Verso 1"), "🔥"),
        Song("Soul clásico", "Soul", listOf("Estribillo", "Verso 2", "Improvisación"), "🎤"),
        Song("Pop español moderno", "Pop ES", listOf("Estribillo", "Verso 1", "Puente"), "🇪🇸"),
        Song("Trap melódico", "Trap", listOf("Estribillo", "Verso 1", "Outro"), "🎵"),
        Song("Rock sinfónico", "Rock Prog", listOf("Estribillo", "Verso 2", "Solo épico"), "🎻"),
        Song("Vallenato colombiano", "Vallenato", listOf("Estribillo", "Verso 1", "Acordeón"), "🪗"),
        Song("Pop británico", "Brit Pop", listOf("Estribillo", "Verso 1", "Puente"), "🇬🇧"),
        Song("Techno underground", "Techno", listOf("Drop", "Buildup", "Break"), "🔊"),
        Song("Canción Disney popular", "Pop Infantil", listOf("Estribillo", "Verso 1", "Final"), "🏰"),
        Song("Punk rock rápido", "Punk", listOf("Estribillo", "Verso 1", "Breakdown"), "⚡"),
        Song("Jazz latino", "Jazz", listOf("Estribillo", "Improvisación", "Outro"), "🎷"),
        Song("Flamenco fusión", "Flamenco", listOf("Estribillo", "Falseta", "Final"), "👏"),
        Song("K-pop energético", "K-Pop", listOf("Estribillo", "Rap part", "Bridge"), "🇰🇷"),
        Song("Country en español", "Country", listOf("Estribillo", "Verso 2", "Puente"), "🤠")
    )

    fun setPlayers(players: List<PlayerModel>) {
        _players.value = players
    }

    fun startRound() {
        if (_players.value.isEmpty()) return

        val randomSong = songLibrary.random()
        val randomPart = randomSong.parts.random()
        val randomSinger = _players.value.random()

        _gameState.value = GameState.Singing(randomSong, randomPart, randomSinger)
        _timeLeft.value = 30
        _votes.value = emptyMap()

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value -= 1
            }
            // Timer acabó, pasar a votación automáticamente
            val currentState = _gameState.value
            if (currentState is GameState.Singing) {
                _gameState.value = GameState.Voting(
                    currentState.song,
                    currentState.part,
                    currentState.singer
                )
            }
        }
    }

    fun skipToVoting() {
        timerJob?.cancel()
        val currentState = _gameState.value
        if (currentState is GameState.Singing) {
            _gameState.value = GameState.Voting(
                currentState.song,
                currentState.part,
                currentState.singer
            )
        }
    }

    fun vote(playerId: String, approved: Boolean) {
        val currentVotes = _votes.value.toMutableMap()
        currentVotes[playerId] = approved
        _votes.value = currentVotes
    }

    fun showResults() {
        val currentState = _gameState.value
        if (currentState is GameState.Voting) {
            val yesVotes = _votes.value.values.count { it }
            val noVotes = _votes.value.values.count { !it }
            val approved = yesVotes > noVotes

            _gameState.value = GameState.Results(
                singer = currentState.singer,
                approved = approved,
                yesVotes = yesVotes,
                noVotes = noVotes
            )
        }
    }

    fun reset() {
        timerJob?.cancel()
        _gameState.value = GameState.Idle
        _timeLeft.value = 30
        _votes.value = emptyMap()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

/**
 * GUÍA PARA AÑADIR MÁS CANCIONES SIN VIOLAR COPYRIGHT:
 *
 * ✅ CORRECTO:
 * - "Canción de amor pop"
 * - "Reggaeton bailable 2020"
 * - "Rock clásico en español"
 * - "Balada romántica lenta"
 *
 * ❌ INCORRECTO (EVITAR):
 * - "Despacito"
 * - "Bohemian Rhapsody"
 * - "Shape of You"
 * - Cualquier título o letra específica
 *
 * FORMATO:
 * Song(
 *     title = "Descripción del género/estilo",
 *     artist = "Categoría musical",
 *     parts = listOf("Intro", "Estribillo", "Verso 1", "Puente"),
 *     emoji = "🎵"
 * )
 */