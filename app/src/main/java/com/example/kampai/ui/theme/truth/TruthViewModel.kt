package com.example.kampai.ui.theme.truth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TruthViewModel @Inject constructor() : ViewModel() {

    sealed class GameState {
        object Selection : GameState()
        data class Result(val type: Type, val text: String, val player: PlayerModel?) : GameState()
    }

    enum class Type { TRUTH, DARE }

    private val _uiState = MutableStateFlow<GameState>(GameState.Selection)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _truthCount = MutableStateFlow(0)
    val truthCount: StateFlow<Int> = _truthCount.asStateFlow()

    private val _dareCount = MutableStateFlow(0)
    val dareCount: StateFlow<Int> = _dareCount.asStateFlow()

    private val _players = MutableStateFlow<List<PlayerModel>>(emptyList())

    private val _currentPlayer = MutableStateFlow<PlayerModel?>(null)
    val currentPlayer: StateFlow<PlayerModel?> = _currentPlayer.asStateFlow()

    private val truths = listOf(
        "¿Cuál es tu peor hábito?",
        "¿Quién te cae mal de esta habitación?",
        "¿Cuál es tu mayor miedo irracional?",
        "¿Qué es lo más vergonzoso que has buscado en Google?",
        "¿Te arrepientes de algún beso?",
        "¿Has mentido sobre tu edad para algo?",
        "¿Cuál es tu secreto más oscuro?",
        "¿A quién de aquí besarías si tuvieras que hacerlo?",
        "¿Has espiado el celular de alguien?",
        "¿Cuál es la mentira más grande que has dicho?",
        "¿Has fingido estar enfermo para no ir a algo?",
        "¿Qué es lo más embarazoso que te ha pasado en una cita?",
        "¿Has robado algo alguna vez?",
        "¿Cuál es tu crush secreto actual?",
        "¿Has hecho trampa en un examen?",
        "¿Cuál es tu mayor inseguridad física?",
        "¿Has llorado por alguien de este grupo?",
        "¿Cuál es el peor regalo que has recibido?",
        "¿Has acosado a alguien en redes sociales?",
        "¿Qué es lo peor que has hecho borracho?"
    ).shuffled().toMutableList()

    private val dares = listOf(
        "Haz 10 sentadillas mientras bebes.",
        "Deja que el grupo envíe un mensaje a quien quieran desde tu móvil.",
        "Imita a alguien del grupo hasta que adivinen quién es.",
        "Bebe un trago sin usar las manos.",
        "Habla con acento extranjero las próximas 3 rondas.",
        "Baila sin música durante 30 segundos.",
        "Permite que alguien revise tu galería de fotos por 1 minuto.",
        "Come algo mezclado que el grupo decida.",
        "Haz una llamada a un contacto random y canta una canción.",
        "Intercambia una prenda con alguien del grupo.",
        "Haz 20 flexiones o bebe 2 shots.",
        "Confiesa tu última búsqueda de Google en voz alta.",
        "Deja que alguien te maquille o te peine.",
        "Mantén un hielo en la boca hasta que se derrita.",
        "Imita tu animal favorito por 1 minuto.",
        "Permite que alguien publique una historia en tu Instagram.",
        "Haz un piropo a cada persona del grupo.",
        "Actúa como mesero y sirve a todos por 10 minutos.",
        "Haz beatbox durante 30 segundos.",
        "Lame el cuello de la persona a tu derecha.",
        "Cuenta un chiste. Si nadie se ríe, bebes doble.",
        "Habla solo en tercera persona hasta tu próximo turno.",
        "Permite que te hagan cosquillas por 30 segundos sin reírte.",
        "Haz una sesión de fotos vergonzosa que el grupo dirija.",
        "Camina en cuatro patas hasta tu próximo turno."
    ).shuffled().toMutableList()

    private var truthIndex = 0
    private var dareIndex = 0

    fun setPlayers(players: List<PlayerModel>) {
        _players.value = players
        if (players.isNotEmpty() && _currentPlayer.value == null) {
            _currentPlayer.value = players.random()
        }
    }

    fun pickTruth() {
        viewModelScope.launch {
            val truth = truths[truthIndex]
            truthIndex = (truthIndex + 1) % truths.size
            if (truthIndex == 0) truths.shuffle()

            _truthCount.value += 1
            _uiState.value = GameState.Result(Type.TRUTH, truth, _currentPlayer.value)
        }
    }

    fun pickDare() {
        viewModelScope.launch {
            val dare = dares[dareIndex]
            dareIndex = (dareIndex + 1) % dares.size
            if (dareIndex == 0) dares.shuffle()

            _dareCount.value += 1
            _uiState.value = GameState.Result(Type.DARE, dare, _currentPlayer.value)
        }
    }

    fun reset() {
        viewModelScope.launch {
            // Al reiniciar (Siguiente turno), elegimos al NUEVO jugador aquí
            if (_players.value.isNotEmpty()) {
                _currentPlayer.value = _players.value.random()
            }
            _uiState.value = GameState.Selection
        }
    }
}