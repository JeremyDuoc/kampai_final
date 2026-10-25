package com.jeremy.kampai.ui.theme.truth

import kotlin.random.Random

class TruthRoundEngine {

    enum class Phase {
        EMPTY,
        PICK,
        ROLLING,
        CHALLENGE,
        PENALTY
    }

    enum class Kind {
        TRUTH,
        DARE
    }

    data class Player(
        val id: String,
        val name: String,
        val emoji: String
    )

    data class Prompt(
        val resourceId: Int,
        val kind: Kind
    )

    data class DoneStats(
        val truth: Int = 0,
        val dare: Int = 0,
        val refused: Int = 0
    )

    data class State(
        val phase: Phase = Phase.EMPTY,
        val players: List<Player> = emptyList(),
        val currentPlayer: Player? = null,
        val turn: Int = 1,
        val prompt: Prompt? = null,
        val redrawsUsed: Int = 0,
        val penaltyDrinks: Int = 2,
        val stats: DoneStats = DoneStats()
    )

    private var _state = State()
    val state: State get() = _state

    private var truthCatalog: List<Int> = emptyList()
    private var dareCatalog: List<Int> = emptyList()

    private val truthBag = mutableListOf<Int>()
    private val dareBag = mutableListOf<Int>()

    private var lastTruth: Int? = null
    private var lastDare: Int? = null

    private var order = mutableListOf<Player>()
    private var orderIndex = 0

    var pendingRollKind: Kind? = null
        private set

    val canRedraw: Boolean
        get() = _state.phase == Phase.CHALLENGE && _state.redrawsUsed == 0

    fun setCatalog(truths: List<Int>, dares: List<Int>) {
        truthCatalog = truths
        dareCatalog = dares
        truthBag.clear()
        dareBag.clear()
        lastTruth = null
        lastDare = null
    }

    fun clear(): State {
        order.clear()
        orderIndex = 0
        truthBag.clear()
        dareBag.clear()
        lastTruth = null
        lastDare = null
        pendingRollKind = null
        _state = State(phase = Phase.EMPTY)
        return _state
    }

    fun start(players: List<Player>): State {
        require(players.isNotEmpty()) { "La lista de jugadores no puede estar vacía." }
        order = players.shuffled().toMutableList()
        orderIndex = 0
        truthBag.clear()
        dareBag.clear()
        lastTruth = null
        lastDare = null
        pendingRollKind = null
        _state = State(
            phase = Phase.PICK,
            players = players,
            currentPlayer = order.firstOrNull(),
            turn = 1,
            prompt = null,
            redrawsUsed = 0,
            penaltyDrinks = 2,
            stats = DoneStats()
        )
        return _state
    }

    private fun drawPrompt(kind: Kind, excluding: Int? = null): Int {
        val catalog = if (kind == Kind.TRUTH) truthCatalog else dareCatalog
        if (catalog.isEmpty()) return 0
        val bag = if (kind == Kind.TRUTH) truthBag else dareBag
        val lastUsed = if (kind == Kind.TRUTH) lastTruth else lastDare

        if (bag.isEmpty()) {
            val shuffled = catalog.shuffled().toMutableList()
            val omit = excluding ?: lastUsed
            if (catalog.size > 1 && omit != null && shuffled.firstOrNull() == omit) {
                shuffled.remove(omit)
                shuffled.add(omit)
            }
            bag.addAll(shuffled)
        }

        if (excluding != null && bag.size > 1 && bag.firstOrNull() == excluding) {
            val first = bag.removeAt(0)
            bag.add(first)
        }

        val chosen = if (bag.isNotEmpty()) bag.removeAt(0) else catalog.first()
        if (kind == Kind.TRUTH) {
            lastTruth = chosen
        } else {
            lastDare = chosen
        }
        return chosen
    }

    fun choose(kind: Kind): State {
        if (_state.phase != Phase.PICK && _state.phase != Phase.ROLLING) return _state
        val res = drawPrompt(kind)
        _state = _state.copy(
            phase = Phase.CHALLENGE,
            prompt = Prompt(resourceId = res, kind = kind),
            redrawsUsed = 0
        )
        return _state
    }

    fun startRandom(): State {
        if (_state.phase != Phase.PICK) return _state
        pendingRollKind = if (Random.nextBoolean()) Kind.TRUTH else Kind.DARE
        _state = _state.copy(phase = Phase.ROLLING)
        return _state
    }

    fun revealRandom(): State {
        if (_state.phase != Phase.ROLLING) return _state
        val kind = pendingRollKind ?: if (Random.nextBoolean()) Kind.TRUTH else Kind.DARE
        pendingRollKind = null
        return choose(kind)
    }

    fun redraw(): State {
        if (!canRedraw) return _state
        val currentPrompt = _state.prompt ?: return _state
        val newRes = drawPrompt(currentPrompt.kind, excluding = currentPrompt.resourceId)
        _state = _state.copy(
            prompt = Prompt(resourceId = newRes, kind = currentPrompt.kind),
            redrawsUsed = 1
        )
        return _state
    }

    fun complete(): State {
        if (_state.phase != Phase.CHALLENGE) return _state
        val kind = _state.prompt?.kind ?: Kind.TRUTH
        val newStats = if (kind == Kind.TRUTH) {
            _state.stats.copy(truth = _state.stats.truth + 1)
        } else {
            _state.stats.copy(dare = _state.stats.dare + 1)
        }
        _state = _state.copy(stats = newStats)
        return advance()
    }

    fun refuse(): State {
        if (_state.phase != Phase.CHALLENGE) return _state
        _state = _state.copy(
            phase = Phase.PENALTY,
            stats = _state.stats.copy(refused = _state.stats.refused + 1)
        )
        return _state
    }

    fun acknowledgePenalty(): State {
        if (_state.phase != Phase.PENALTY) return _state
        return advance()
    }

    fun skipPlayer(): State {
        if (_state.phase != Phase.PICK) return _state
        return advance()
    }

    private fun advance(): State {
        if (order.isEmpty()) return _state
        val oldId = order.getOrNull(orderIndex)?.id
        orderIndex++
        if (orderIndex >= order.size) {
            val shuffled = order.shuffled().toMutableList()
            if (shuffled.size > 1 && shuffled[0].id == oldId) {
                val j = Random.nextInt(1, shuffled.size)
                val temp = shuffled[0]
                shuffled[0] = shuffled[j]
                shuffled[j] = temp
            }
            order = shuffled
            orderIndex = 0
        }

        _state = _state.copy(
            phase = Phase.PICK,
            currentPlayer = order.getOrNull(orderIndex),
            turn = _state.turn + 1,
            prompt = null,
            redrawsUsed = 0
        )
        return _state
    }
}
