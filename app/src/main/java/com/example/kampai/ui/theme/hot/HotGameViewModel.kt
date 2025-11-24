package com.example.kampai.ui.theme.hot

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.data.PartyRepository
import com.example.kampai.data.repository.HotRepository
import com.example.kampai.domain.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HotGameViewModel @Inject constructor(
    private val hotRepository: HotRepository,
    private val partyRepository: PartyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    sealed class HotState {
        object Card : HotState()        // Texto clásico
        object Slots : HotState()       // Dados
        object Haptic : HotState()      // Sensor Táctil
        object Scratch : HotState()     // Raspe
        object Heartbeat : HotState()   // Pulso
        object LieDetector : HotState() // Detector Mentiras
        object GyroCup : HotState()     // Copa Inestable
        object Whisper : HotState()     // Susurrador
        object Blow : HotState()        // Soplido
        object RouletteWheel : HotState() // Ruleta Castigos
    }

    // ... (Variables de estado existentes: gameState, intensity, etc. SE MANTIENEN) ...
    private val _gameState = MutableStateFlow<HotState>(HotState.Card)
    val gameState: StateFlow<HotState> = _gameState.asStateFlow()

    private val _intensity = MutableStateFlow(HotIntensity.SOFT)
    val intensity: StateFlow<HotIntensity> = _intensity.asStateFlow()

    private val _thermometerProgress = MutableStateFlow(0f)
    val thermometerProgress: StateFlow<Float> = _thermometerProgress.asStateFlow()

    private val _isDiabloMode = MutableStateFlow(false)
    val isDiabloMode: StateFlow<Boolean> = _isDiabloMode.asStateFlow()

    private val _showTutorial = MutableStateFlow(true)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    private val _currentText = MutableStateFlow("")
    val currentText: StateFlow<String> = _currentText.asStateFlow()
    private val _currentCard = MutableStateFlow<HotChallenge?>(null)
    val currentCard: StateFlow<HotChallenge?> = _currentCard.asStateFlow()
    private val _isRevealed = MutableStateFlow(false)
    val isRevealed: StateFlow<Boolean> = _isRevealed.asStateFlow()

    // Jugadores
    private val _playerA = MutableStateFlow<PlayerModel?>(null)
    val playerA: StateFlow<PlayerModel?> = _playerA.asStateFlow()
    private val _playerB = MutableStateFlow<PlayerModel?>(null)
    val playerB: StateFlow<PlayerModel?> = _playerB.asStateFlow()

    // Estados específicos de minijuegos
    private val _slotAction = MutableStateFlow("GIRAR")
    val slotAction: StateFlow<String> = _slotAction.asStateFlow()
    private val _slotBodyPart = MutableStateFlow("GIRAR")
    val slotBodyPart: StateFlow<String> = _slotBodyPart.asStateFlow()
    private val _isSpinningSlots = MutableStateFlow(false)
    val isSpinningSlots: StateFlow<Boolean> = _isSpinningSlots.asStateFlow()
    private val _hapticStatus = MutableStateFlow("Pulsar...")
    val hapticStatus: StateFlow<String> = _hapticStatus.asStateFlow()

    // Detector de mentiras
    private val _lieResult = MutableStateFlow<Boolean?>(null) // null=analizando, true=verdad, false=mentira
    val lieResult: StateFlow<Boolean?> = _lieResult.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Configuración Rondas
    private val SOFT_LIMIT = 10
    private val MEDIUM_LIMIT = 25

    private var turnCount = 0
    private var allPlayers: List<PlayerModel> = emptyList()
    private var currentDeck: List<HotChallenge> = emptyList()

    // Vibrador
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        viewModelScope.launch {
            allPlayers = partyRepository.getPlayers()
            loadDeck()
        }
    }

    // ... (Funciones setIntensity, toggleDiabloMode, loadDeck, closeTutorial SE MANTIENEN IGUAL) ...
    fun closeTutorial() { _showTutorial.value = false }
    fun toggleDiabloMode() {
        _isDiabloMode.value = !_isDiabloMode.value
        if (_isDiabloMode.value) viewModelScope.launch { loadDeck(forceExtreme = true) }
    }
    private suspend fun loadDeck(forceExtreme: Boolean = false) {
        val targetIntensity = if (forceExtreme) HotIntensity.EXTREME else _intensity.value
        currentDeck = hotRepository.getChallengesFlow(targetIntensity).first()
    }
    fun setIntensity(newIntensity: HotIntensity) {
        if (_intensity.value != newIntensity) {
            _intensity.value = newIntensity
            viewModelScope.launch { loadDeck() }
        }
    }

    // --- LÓGICA DE SELECCIÓN DE JUEGO ---
    fun nextTurn() {
        turnCount++
        _isRevealed.value = false
        _hapticStatus.value = "Pulsar..."
        _lieResult.value = null // Reset detector

        updateIntensityAndProgress()

        val (p1, p2) = findCompatiblePair()
        _playerA.value = p1
        _playerB.value = p2

        // --- PROBABILIDADES SEGÚN INTENSIDAD ---
        val roll = Random.nextInt(100)
        val currentInt = _intensity.value

        // Si es MODO DIABLO, todo está permitido con alta probabilidad
        if (_isDiabloMode.value) {
            when {
                roll < 10 -> _gameState.value = HotState.RouletteWheel
                roll < 20 -> prepareSlotsTurn()
                roll < 30 -> prepareHapticTurn()
                roll < 40 -> _gameState.value = HotState.GyroCup
                roll < 50 -> _gameState.value = HotState.LieDetector
                else -> prepareCardTurn()
            }
            return
        }

        // PROGRESIÓN NORMAL
        when (currentInt) {
            HotIntensity.SOFT -> {
                // 90% Cartas (Preguntas), 10% Pulso (Inocente)
                if (roll < 10 && p2 != null) _gameState.value = HotState.Heartbeat
                else prepareCardTurn()
            }
            HotIntensity.MEDIUM -> {
                // Empiezan los juegos táctiles y visuales
                when {
                    roll < 15 -> _gameState.value = HotState.Scratch
                    roll < 30 -> _gameState.value = HotState.LieDetector
                    roll < 45 -> prepareSlotsTurn()
                    else -> prepareCardTurn()
                }
            }
            HotIntensity.HOT, HotIntensity.EXTREME -> {
                // Juegos físicos y sensores
                when {
                    roll < 15 -> _gameState.value = HotState.GyroCup
                    roll < 30 -> _gameState.value = HotState.Whisper
                    roll < 40 -> _gameState.value = HotState.Blow
                    roll < 55 -> prepareHapticTurn()
                    roll < 65 -> _gameState.value = HotState.RouletteWheel
                    else -> prepareCardTurn()
                }
            }
        }
    }

    private fun updateIntensityAndProgress() {
        val oldIntensity = _intensity.value
        val newIntensity = when {
            turnCount <= SOFT_LIMIT -> HotIntensity.SOFT
            turnCount <= MEDIUM_LIMIT -> HotIntensity.MEDIUM
            else -> HotIntensity.HOT
        }
        val progress = (turnCount.toFloat() / MEDIUM_LIMIT.toFloat()).coerceIn(0f, 1f)
        _thermometerProgress.value = progress

        if (newIntensity != oldIntensity && !_isDiabloMode.value) {
            _intensity.value = newIntensity
            viewModelScope.launch { loadDeck() }
        }
    }

    // --- PREPARADORES DE MINIJUEGOS ---
    private fun prepareCardTurn() {
        _gameState.value = HotState.Card
        viewModelScope.launch {
            if (currentDeck.isNotEmpty()) {
                val card = currentDeck.random()
                _currentCard.value = card
                var text = card.textString ?: card.textRes?.let { context.getString(it) } ?: ""
                text = text.replace("{A}", _playerA.value?.name ?: "A").replace("{B}", _playerB.value?.name ?: "B")
                _currentText.value = text
            } else { _currentText.value = "¡Beban todos!" }
        }
    }

    private fun prepareSlotsTurn() {
        _gameState.value = HotState.Slots
        _isSpinningSlots.value = false
    }

    private fun prepareHapticTurn() {
        _gameState.value = HotState.Haptic
    }

    // --- ACCIONES DE MINIJUEGOS ---

    // 1. Slots
    fun spinSlots() {
        viewModelScope.launch {
            _isSpinningSlots.value = true
            val actions = listOf("Besar", "Lamer", "Morder", "Soplar", "Acariciar", "Masajear", "Chupar")
            val parts = listOf("Cuello", "Oreja", "Mano", "Espalda", "Ombligo", "Muslo", "Labios")
            for (i in 0..20) {
                _slotAction.value = actions.random()
                _slotBodyPart.value = parts.random()
                delay(50 + (i * 10).toLong())
            }
            _isSpinningSlots.value = false
        }
    }

    // 2. Haptic
    fun triggerVibration() {
        val duration = Random.nextLong(200, 800)
        val amplitude = Random.nextInt(100, 255)
        if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        else vibrator.vibrate(duration)
        _hapticStatus.value = listOf("¡Detectado!", "Caliente...", "Más abajo...", "¡Ahí!").random()
    }

    // 3. Lie Detector (Sin truco, random 50/50 o bias)
    fun startLieAnalysis() {
        viewModelScope.launch {
            _lieResult.value = null // Analizando...
            delay(2500) // Suspenso
            _lieResult.value = Random.nextBoolean() // Resultado real aleatorio
            if (_lieResult.value == false) {
                // Si es mentira, vibra fuerte para avisar
                if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(500, 255))
            }
        }
    }

    // --- HELPERS (Compatible Pair, Safety) IGUAL QUE ANTES ---
    private fun findCompatiblePair(): Pair<PlayerModel?, PlayerModel?> {
        if (allPlayers.size < 2) return allPlayers.firstOrNull() to null
        val p1 = allPlayers.random()
        val candidates = allPlayers.filter { p2 -> p2.id != p1.id && isCompatible(p1, p2) }
        val p2 = if (candidates.isNotEmpty()) candidates.random() else allPlayers.filter { it.id != p1.id }.random()
        return p1 to p2
    }

    private fun isCompatible(p1: PlayerModel, p2: PlayerModel): Boolean {
        return when (p1.attraction) {
            Attraction.MEN -> p2.gender == Gender.MALE
            Attraction.WOMEN -> p2.gender == Gender.FEMALE
            Attraction.BOTH -> true
            Attraction.NONE -> false
        }
    }

    fun revealCard() { _isRevealed.value = true }
    fun safetyGreen() { nextTurn() }
    fun safetyYellow() { _currentText.value = "⚠️ ¡Penitencia! Beban un trago."; _gameState.value = HotState.Card; _isRevealed.value = true }
    fun safetyRed() { _currentText.value = "🛑 Salto seguro."; _gameState.value = HotState.Card; _isRevealed.value = true; viewModelScope.launch { delay(1500); nextTurn() } }
}