package com.jeremy.kampai.ui.theme.truth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeremy.kampai.R
import com.jeremy.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One source of truth. Party roster is snapshotted per session. */
@HiltViewModel
class TruthViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val engine = TruthRoundEngine()
    private val _state = MutableStateFlow(engine.state)
    val state: StateFlow<TruthRoundEngine.State> = _state.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _partyChanged = MutableStateFlow(false)
    val partyChanged: StateFlow<Boolean> = _partyChanged.asStateFlow()

    private var latestRoster: List<PlayerModel> = emptyList()
    private var rollJob: Job? = null
    private var hasCatalog = false

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        try {
            // Discover all truth_1.. and dare_1.. resources
            val resources = R.string::class.java.fields
            fun discover(prefix: String): List<Int> = resources
                .mapNotNull { field ->
                    val suffix = field.name.removePrefix("${prefix}_").toIntOrNull()
                    if (!field.name.startsWith("${prefix}_") || suffix == null) null
                    else suffix to field.getInt(null)
                }
                .sortedBy { it.first }
                .map { it.second }

            var truths = discover("truth")
            var dares = discover("dare")

            if (truths.isEmpty()) {
                val fallback = mutableListOf<Int>()
                for (i in 1..250) {
                    val resId = context.resources.getIdentifier("truth_$i", "string", context.packageName)
                    if (resId != 0) fallback.add(resId)
                }
                truths = fallback
            }

            if (dares.isEmpty()) {
                val fallback = mutableListOf<Int>()
                for (i in 1..250) {
                    val resId = context.resources.getIdentifier("dare_$i", "string", context.packageName)
                    if (resId != 0) fallback.add(resId)
                }
                dares = fallback
            }

            if (truths.isNotEmpty() && dares.isNotEmpty()) {
                engine.setCatalog(truths, dares)
                hasCatalog = true
                if (latestRoster.isNotEmpty() && engine.state.phase == TruthRoundEngine.Phase.EMPTY) {
                    startFresh()
                }
            } else {
                _error.value = "No se encontraron preguntas o retos en strings.xml."
            }
        } catch (e: Exception) {
            _error.value = "Error cargando preguntas: ${e.message}"
        }
    }

    /** Never changes an active session when PartyManager emits an update. */
    fun syncParty(roster: List<PlayerModel>) {
        latestRoster = roster.toList()
        if (!hasCatalog) return
        if (engine.state.phase == TruthRoundEngine.Phase.EMPTY && roster.isNotEmpty()) {
            startFresh()
        } else if (engine.state.phase != TruthRoundEngine.Phase.EMPTY) {
            _partyChanged.value = roster.map { it.id } != engine.state.players.map { it.id }
        }
    }

    fun startFresh() {
        rollJob?.cancel()
        if (!hasCatalog) return
        if (latestRoster.isEmpty()) {
            _state.value = engine.clear()
            _error.value = null
            _partyChanged.value = false
            return
        }
        val players = latestRoster.map {
            TruthRoundEngine.Player(it.id, it.name, it.getDisplayEmoji())
        }
        try {
            _state.value = engine.start(players)
            _partyChanged.value = false
            _error.value = null
        } catch (e: IllegalArgumentException) {
            _error.value = e.message ?: "No se pudo empezar la partida."
        }
    }

    fun choose(kind: TruthRoundEngine.Kind) {
        if (engine.state.phase != TruthRoundEngine.Phase.PICK) return
        _state.value = engine.choose(kind)
    }

    fun randomPick() {
        if (engine.state.phase != TruthRoundEngine.Phase.PICK) return
        _state.value = engine.startRandom()
        rollJob?.cancel()
        rollJob = viewModelScope.launch {
            delay(650)
            if (engine.state.phase == TruthRoundEngine.Phase.ROLLING) {
                _state.value = engine.revealRandom()
            }
        }
    }

    fun redraw() {
        if (!engine.canRedraw) return
        _state.value = engine.redraw()
    }

    fun complete() {
        if (engine.state.phase != TruthRoundEngine.Phase.CHALLENGE) return
        _state.value = engine.complete()
    }

    fun refuse() {
        if (engine.state.phase != TruthRoundEngine.Phase.CHALLENGE) return
        _state.value = engine.refuse()
    }

    fun acknowledgePenalty() {
        if (engine.state.phase != TruthRoundEngine.Phase.PENALTY) return
        _state.value = engine.acknowledgePenalty()
    }

    fun skipPlayer() {
        if (engine.state.phase != TruthRoundEngine.Phase.PICK) return
        _state.value = engine.skipPlayer()
    }

    fun debugForceTruth() {
        if (engine.state.phase != TruthRoundEngine.Phase.EMPTY) {
            _state.value = engine.choose(TruthRoundEngine.Kind.TRUTH)
        }
    }

    fun debugForceDare() {
        if (engine.state.phase != TruthRoundEngine.Phase.EMPTY) {
            _state.value = engine.choose(TruthRoundEngine.Kind.DARE)
        }
    }

    fun debugForcePenalty() {
        if (engine.state.phase != TruthRoundEngine.Phase.EMPTY) {
            _state.value = engine.choose(TruthRoundEngine.Kind.DARE)
            _state.value = engine.refuse()
        }
    }
}