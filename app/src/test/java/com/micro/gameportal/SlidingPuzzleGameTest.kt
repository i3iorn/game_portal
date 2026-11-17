package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SlidingPuzzleGameTest {

    private val game = SlidingPuzzleGame(seed = 42)

    private fun indexOfTile(tile: Int) = game.board.indexOf(tile)

    @Test
    fun `fresh game is shuffled and solvable-shaped`() {
        val board = game.board
        assertEquals((1..15).toList() + listOf(16), board.sorted())
    }

    @Test
    fun `tap on tile adjacent to empty moves it`() {
        val before = game.board.copyOf()
        val emptyIndex = game.board.indexOf(SlidingPuzzleGame.emptyValue(4))
        // one of the empty cell's neighbours
        val neighbors = mutableListOf<Int>()
        val row = emptyIndex / 4; val col = emptyIndex % 4
        if (row > 0) neighbors.add(emptyIndex - 4)
        if (row < 3) neighbors.add(emptyIndex + 4)
        if (col > 0) neighbors.add(emptyIndex - 1)
        if (col < 3) neighbors.add(emptyIndex + 1)
        val moved = neighbors.first { game.tap(it) }
        assertEquals(before[moved], game.board[emptyIndex])
        // the empty cell moved to the tapped cell's old spot
        assertEquals(moved, game.board.indexOf(before[emptyIndex]))
        assertEquals(1, game.moves)
    }

    @Test
    fun `tap on non-adjacent tile is rejected`() {
        val emptyIndex = game.board.indexOf(SlidingPuzzleGame.emptyValue(4))
        // pick a cell at least 2 rows away from empty
        val far = (0..15).first { Math.abs(it / 4 - emptyIndex / 4) >= 2 }
        val moves = game.moves
        assertFalse(game.tap(far))
        assertEquals(moves, game.moves)
    }

    @Test
    fun `restore accepts a saved board`() {
        val saved = game.board.copyOf()
        val restored = SlidingPuzzleGame(seed = 7)
        restored.restore(saved, 12)
        assertTrue(saved.contentEquals(restored.board))
        assertEquals(12, restored.moves)
    }

    @Test
    fun `restore rejects wrong size`() {
        try {
            game.restore(IntArray(8), 0)
            throw AssertionError("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun `known position solves correctly`() {
        // Empty at index 14, tile 15 at index 15: one legal move solves it.
        val oneMoveAway = IntArray(16) { it + 1 }.also {
            it[14] = 16
            it[15] = 15
        }
        val g = SlidingPuzzleGame(seed = 1)
        g.restore(oneMoveAway, 0)
        assertFalse(g.isSolved)
        assertTrue(g.tap(15)) // tile 15 sits right below the empty cell
        assertTrue(g.isSolved)
    }

    @Test
    fun `newGame reshuffles and resets counters`() {
        game.newGame(seed = 99)
        assertEquals(0, game.moves)
        assertEquals((1..15).toList() + listOf(16), game.board.sorted())
    }
}
