package com.jeremy.kampai.ui.theme.kingscup

import com.jeremy.kampai.domain.models.PlayerModel

object KingsCupEngine {
    enum class Phase {
        SETUP,
        READY,
        REVEALED,
        FINISHED
    }

    enum class Rank(val label: String) {
        ACE("A"),
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8"),
        NINE("9"),
        TEN("10"),
        JACK("J"),
        QUEEN("Q"),
        KING("K")
    }

    enum class Suit(val symbol: String) {
        HEARTS("♥"),
        DIAMONDS("♦"),
        CLUBS("♣"),
        SPADES("♠")
    }

    data class Card(
        val rank: Rank,
        val suit: Suit,
        val id: String = "${rank.name}_${suit.name}"
    )

    data class State(
        val phase: Phase = Phase.SETUP,
        val kings: Int = 0,
        val remaining: Int = 52,
        val currentPlayer: PlayerModel? = null,
        val card: Card? = null
    )

    fun createFreshDeck(): List<Card> {
        return Rank.values().flatMap { rank ->
            Suit.values().map { suit ->
                Card(
                    rank = rank,
                    suit = suit,
                    id = "${rank.name}_${suit.name}"
                )
            }
        }.shuffled()
    }
}
