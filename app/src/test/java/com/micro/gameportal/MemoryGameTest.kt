package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryGameTest {

    /** 6-card deck: a a b b c c (shuffled by seed). */
    private fun deckGame(): MemoryGame = MemoryGame(listOf("a", "b", "c"), seed = 5)

    @Test
    fun `deck has pairs`() {
        val g = deckGame()
        assertEquals(6, g.faces.size)
        assertEquals(2, g.faces.count { it == "a" })
        assertEquals(2, g.faces.count { it == "b" })
        assertEquals(2, g.faces.count { it == "c" })
    }

    @Test
    fun `first flip opens a card`() {
        assertTrue(deckGame().flip(0) is MemoryGame.FlipResult.First)
    }

    @Test
    fun `matching pair stays open and counts`() {
        val g = deckGame()
        val partner = g.faces.indices.first { it != 0 && g.faces[it] == g.faces[0] }
        g.flip(0)
        val result = g.flip(partner)
        assertTrue(result is MemoryGame.FlipResult.Matched)
        assertEquals(1, g.matchedPairs)
        assertEquals(MemoryGame.CardState.MATCHED, g.states[0])
        assertEquals(MemoryGame.CardState.MATCHED, g.states[partner])
    }

    @Test
    fun `mismatch locks the game until hideMismatch`() {
        val g = deckGame()
        val bIndex = g.faces.indexOf("b")
        val cIndex = g.faces.indexOf("c")
        g.flip(bIndex)
        val result = g.flip(cIndex)
        assertTrue(result is MemoryGame.FlipResult.Mismatched)
        assertTrue(g.flip(g.faces.indexOf("a")) is MemoryGame.FlipResult.Ignored)
        g.hideMismatch(bIndex, cIndex)
        assertTrue(g.flip(g.faces.indexOf("a")) is MemoryGame.FlipResult.First)
    }

    @Test
    fun `flipping a matched card is ignored`() {
        val g = deckGame()
        val partner = g.faces.indices.first { it != 0 && g.faces[it] == g.faces[0] }
        g.flip(0); g.flip(partner)
        assertTrue(g.flip(0) is MemoryGame.FlipResult.Ignored)
    }

    @Test
    fun `clearing all pairs sets isCleared`() {
        val g = deckGame()
        while (!g.isCleared) {
            val hidden = g.states.indices.filter { g.states[it] == MemoryGame.CardState.HIDDEN }
            if (hidden.isEmpty()) break
            val first = hidden[0]
            val partner = hidden.first { g.faces[it] == g.faces[first] && it != first }
            g.flip(first)
            when (val result = g.flip(partner)) {
                is MemoryGame.FlipResult.Mismatched -> g.hideMismatch(first, partner)
                MemoryGame.FlipResult.Ignored -> throw AssertionError(
                    "ignored: first=$first partner=$partner faces=${g.faces} states=${g.states.toList()} locked? firstOpen=${g.firstOpen}",
                )
                else -> {}
            }
        }
        assertTrue(g.isCleared)
        assertEquals(3, g.matchedPairs)
    }

    @Test
    fun `restoreDeck rebuilds deck order and matched state`() {
        val g = deckGame()
        val partner = g.faces.indices.first { it != 0 && g.faces[it] == g.faces[0] }
        g.flip(0); g.flip(partner)
        val deck = g.faces.toList()
        val mask = g.matchedMask()

        val restored = deckGame()
        restored.restoreDeck(deck, mask)
        assertEquals(deck, restored.faces)
        assertEquals(1, restored.matchedPairs)
        assertEquals(MemoryGame.CardState.MATCHED, restored.states[0])
    }

    @Test
    fun `out of range flip is ignored`() {
        val g = deckGame()
        assertTrue(g.flip(-1) is MemoryGame.FlipResult.Ignored)
        assertTrue(g.flip(99) is MemoryGame.FlipResult.Ignored)
    }

    @Test
    fun `same card twice is ignored`() {
        val g = deckGame()
        g.flip(2)
        assertTrue(g.flip(2) is MemoryGame.FlipResult.Ignored)
    }
}
