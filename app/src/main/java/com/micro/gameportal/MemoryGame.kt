package com.micro.gameportal

import kotlin.random.Random

/**
 * Pure logic for memory match: a shuffled deck of face pairs, flip two at a
 * time; matching pairs stay open, mismatches flip back. No Android imports.
 */
class MemoryGame(
    baseFaces: List<String> = DEFAULT_FACES,
    seed: Long = System.nanoTime(),
) {
    companion object {
        val DEFAULT_FACES = listOf("🍀", "🚀", "🎲", "🌟", "🎵", "🍎", "⚡", "🧩")
    }

    enum class CardState { HIDDEN, MATCHED }

    /** Outcome of a tap on a card. */
    sealed class FlipResult {
        /** The first card of a turn was opened. */
        data class First(val index: Int) : FlipResult()

        /** Second card matched; both stay open. */
        data class Matched(val first: Int, val second: Int) : FlipResult()

        /** Second card mismatched; UI may show both briefly, then call [hideMismatch]. */
        data class Mismatched(val first: Int, val second: Int) : FlipResult()

        /** Tap ignored (card already open/matched, or mid-flip). */
        object Ignored : FlipResult()
    }

    var faces: List<String> = (baseFaces + baseFaces).shuffled(Random(seed))
        private set
    val states = Array(faces.size) { CardState.HIDDEN }

    var firstOpen = -1
        private set
    var matchedPairs = 0
        private set
    private var locked = false
    val isCleared: Boolean get() = states.all { it == CardState.MATCHED }

    /** Restores a saved deck order plus matched state (e.g. after rotation). */
    fun restoreDeck(savedFaces: List<String>, matched: BooleanArray) {
        require(savedFaces.size == states.size) { "Deck must have ${states.size} cards" }
        faces = savedFaces
        locked = false
        restore(matched)
    }

    fun flip(index: Int): FlipResult {
        if (locked || index !in states.indices || states[index] == CardState.MATCHED) {
            return FlipResult.Ignored
        }
        if (firstOpen == index) return FlipResult.Ignored
        if (firstOpen == -1) {
            firstOpen = index
            return FlipResult.First(index)
        }
        val first = firstOpen
        firstOpen = -1
        return if (faces[first] == faces[index]) {
            states[first] = CardState.MATCHED
            states[index] = CardState.MATCHED
            matchedPairs++
            FlipResult.Matched(first, index)
        } else {
            locked = true
            FlipResult.Mismatched(first, index)
        }
    }

    /** Hides both cards of a mismatched pair (call after the UI flip-back delay). */
    fun hideMismatch(first: Int, second: Int) {
        locked = false
        if (states[first] == CardState.HIDDEN || states[second] == CardState.HIDDEN) return
        states[first] = CardState.HIDDEN
        states[second] = CardState.HIDDEN
    }

    /** Restores progress after rotation: which faces are matched, in this deck order. */
    fun restore(matchedMask: BooleanArray) {
        require(matchedMask.size == states.size) { "Mask must cover ${states.size} cards" }
        matchedMask.forEachIndexed { i, matched ->
            states[i] = if (matched) CardState.MATCHED else CardState.HIDDEN
        }
        matchedPairs = states.count { it == CardState.MATCHED } / 2
        firstOpen = -1
    }

    fun matchedMask(): BooleanArray = BooleanArray(states.size) { states[it] == CardState.MATCHED }
}
