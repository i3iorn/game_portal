package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LightsOutTest {

    @Test
    fun `generated puzzle is solvable and not already solved`() {
        val g = LightsOut(size = 5, seed = 11)
        assertTrue(g.lights.any { it })
        assertFalse(g.isSolved)
        assertEquals(0, g.moves)
    }

    @Test
    fun `tap toggles the cross`() {
        val g = LightsOut(size = 5, seed = 11)
        // tap a corner: toggles itself and two neighbours
        val before = g.lights.copyOf()
        g.tap(0)
        assertTrue(g.lights[0] != before[0])
        assertTrue(g.lights[1] != before[1])
        assertTrue(g.lights[5] != before[5])
        assertEquals(before[2], g.lights[2])
        assertEquals(1, g.moves)
    }

    @Test
    fun `tapping every cell twice returns to the start`() {
        val g = LightsOut(size = 5, seed = 11)
        val start = g.lights.copyOf()
        g.lights.indices.forEach { g.tap(it) }
        g.lights.indices.forEach { g.tap(it) }
        assertTrue(start.contentEquals(g.lights))
    }

    @Test
    fun `solving by replaying generation toggles is detected`() {
        // A one-toggle-from-solved board must solve in exactly one tap.
        val oneToggle = BooleanArray(25).also {
            it[12] = true; it[11] = true; it[13] = true; it[7] = true; it[17] = true
        }
        val g = LightsOut(size = 5, seed = 1)
        g.restore(oneToggle, 0)
        assertFalse(g.isSolved)
        assertTrue(g.tap(12))
        assertTrue(g.lights.none { it })
        assertTrue(g.isSolved)
    }

    @Test
    fun `restore rejects wrong size`() {
        val g = LightsOut(size = 5, seed = 1)
        try {
            g.restore(BooleanArray(9), 0)
            throw AssertionError("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun `newGame reshuffles and resets moves`() {
        val g = LightsOut(size = 5, seed = 11)
        g.tap(0); g.tap(1)
        g.newGame(seed = 42)
        assertEquals(0, g.moves)
        assertTrue(g.lights.any { it })
    }
}
