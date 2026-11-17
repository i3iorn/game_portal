package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MinefieldTest {

    private fun field(): Minefield = Minefield(rows = 4, cols = 4, mineCount = 4, seed = 7)

    @Test
    fun `first tap is never a mine and plants mines`() {
        val f = field()
        assertFalse(f.tap(5))
        assertTrue(f.planted)
        assertEquals(4, f.mines.count { it })
        assertFalse(f.mines[5])
    }

    @Test
    fun `flood reveal opens the zero region`() {
        val f = field()
        f.tap(5)
        // find a revealed cell with 0 neighbours; its region must be revealed
        val revealedZero = f.states.indices.firstOrNull { f.states[it] == Minefield.REVEALED && f.adjacentMines(it) == 0 }
        if (revealedZero != null) {
            f.neighbors(revealedZero).forEach { n ->
                if (!f.mines[n]) assertEquals(Minefield.REVEALED, f.states[n])
            }
        }
        assertFalse(f.isOver)
    }

    @Test
    fun `revealed cell shows correct adjacent count`() {
        val f = field()
        f.tap(0)
        f.states.indices.filter { f.states[it] == Minefield.REVEALED }.forEach {
            assertEquals(f.neighbors(it).count { n -> f.mines[n] }, f.adjacentMines(it))
        }
    }

    @Test
    fun `flag toggles hidden and back`() {
        val f = field()
        assertTrue(f.toggleFlag(3))
        assertEquals(Minefield.FLAGGED, f.states[3])
        assertTrue(f.toggleFlag(3))
        assertEquals(Minefield.HIDDEN, f.states[3])
    }

    @Test
    fun `flagged cell cannot be revealed`() {
        val f = field()
        f.toggleFlag(3)
        assertFalse(f.tap(3))
        assertEquals(Minefield.FLAGGED, f.states[3])
    }

    @Test
    fun `tapping a mine ends the game`() {
        val f = field()
        f.tap(0)
        val mineIndex = f.mines.indices.first { f.mines[it] }
        assertTrue(f.tap(mineIndex))
        assertTrue(f.hitMine)
        assertTrue(f.isOver)
        assertEquals(Minefield.BLAST, f.states[mineIndex])
    }

    @Test
    fun `revealing all safe cells wins`() {
        val f = field()
        f.tap(0)
        // reveal every safe cell directly (flood-safe: tap returns false for safe)
        f.states.indices.filter { !f.mines[it] }.forEach { if (f.states[it] != Minefield.REVEALED) assertFalse(f.tap(it)) }
        assertTrue(f.isWon)
        assertTrue(f.isOver)
    }

    @Test
    fun `restore rebuilds the position`() {
        val f = field()
        f.tap(5)
        val restored = Minefield(rows = 4, cols = 4, mineCount = 4)
        restored.restore(f.mines, f.states, f.planted, f.hitMine)
        assertTrue(f.mines.contentEquals(restored.mines))
        assertTrue(f.states.contentEquals(restored.states))
        assertEquals(f.planted, restored.planted)
    }

    @Test
    fun `neighbors exclude self and out of range`() {
        val f = Minefield(rows = 3, cols = 3, mineCount = 1)
        assertEquals(listOf(1, 3, 4), f.neighbors(0))
        assertEquals(8, f.neighbors(4).size)
    }
}
