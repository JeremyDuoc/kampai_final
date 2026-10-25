package com.jeremy.kampai.ui.theme.never

import kotlin.random.Random

/**
 * Pure game logic for Yo Nunca. No timers or Compose references:
 * the game advances immediately; visual transitions belong to the UI.
 *
 * A shuffle-bag prevents repetition inside each cycle and avoids the same
 * statement on the boundary between consecutive cycles when possible.
 */
class NeverDeckEngine(
    questions: List<String>,
    private val random: Random = Random.Default
) {
    private val entries = questions.map(String::trim).filter(String::isNotBlank).distinct()
    private var order = shuffledIndices()
    private var position = 0
    private var number = 1

    data class Snapshot(val order: IntArray, val position: Int, val number: Int)
    data class State(val question: String?, val number: Int, val size: Int)

    fun current(): State = State(
        question = entries.getOrNull(order.getOrNull(position) ?: -1),
        number = if (entries.isEmpty()) 0 else number,
        size = entries.size
    )

    fun next(): State {
        if (entries.isEmpty()) return current()
        val previous = order[position]
        if (position < order.lastIndex) {
            position++
        } else {
            order = shuffledIndices()
            if (order.size > 1 && order[0] == previous) {
                val swap = random.nextInt(1, order.size)
                val temp = order[0]
                order[0] = order[swap]
                order[swap] = temp
            }
            position = 0
        }
        number++
        return current()
    }

    fun restart(): State {
        val previous = order.getOrNull(position)
        order = shuffledIndices()
        if (previous != null && order.size > 1 && order[0] == previous) {
            val swap = random.nextInt(1, order.size)
            val temp = order[0]
            order[0] = order[swap]
            order[swap] = temp
        }
        position = 0
        number = 1
        return current()
    }

    fun snapshot(): Snapshot = Snapshot(order.copyOf(), position, number)

    /** Invalid saved state is ignored instead of crashing on a catalog update. */
    fun restore(snapshot: Snapshot?): State {
        if (snapshot == null || entries.isEmpty()) return current()
        if (snapshot.order.size != entries.size ||
            !snapshot.order.sortedArray().contentEquals(entries.indices.toList().toIntArray()) ||
            snapshot.position !in snapshot.order.indices || snapshot.number < 1) return current()
        order = snapshot.order.copyOf()
        position = snapshot.position
        number = snapshot.number
        return current()
    }

    private fun shuffledIndices(): IntArray = entries.indices.shuffled(random).toIntArray()
}
