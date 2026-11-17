package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TicTacToeGameTest {

    private fun game(vararg plays: Int): TicTacToeGame =
        TicTacToeGame().also { plays.forEach { c -> check(it.play(c)) } }

    @Test
    fun `alternates turns`() {
        val g = game(0)
        assertEquals(TicTacToeGame.O, g.current)
    }

    @Test
    fun `x wins on top row`() {
        val g = game(0, 3, 1, 4, 2)
        assertEquals(TicTacToeGame.X, g.winner)
        assertTrue(g.winningLine()!!.contentEquals(intArrayOf(0, 1, 2)))
    }

    @Test
    fun `o wins on diagonal`() {
        // X 1, O 0; X 2, O 4; X 3, O 8 -> O diagonal 0,4,8
        val g = game(1, 0, 2, 4, 3, 8)
        assertEquals(TicTacToeGame.O, g.winner)
        assertTrue(g.winningLine()!!.contentEquals(intArrayOf(0, 4, 8)))
    }

    @Test
    fun `draw when board fills`() {
        val g = game(0, 1, 2, 4, 3, 5, 7, 6, 8)
        assertTrue(g.isFull)
        assertEquals(TicTacToeGame.EMPTY, g.winner)
        assertNull(g.winningLine())
    }

    @Test
    fun `rejects occupied cell`() {
        val g = game(0)
        assertFalse(g.play(0))
    }

    @Test
    fun `rejects out of range`() {
        val g = TicTacToeGame()
        assertFalse(g.play(-1))
        assertFalse(g.play(9))
    }

    @Test
    fun `rejects play after win`() {
        val g = game(0, 3, 1, 4, 2)
        assertFalse(g.play(8))
    }

    @Test
    fun `reset clears state`() {
        val g = game(0, 3, 1)
        g.reset()
        assertTrue(g.cells.all { it == TicTacToeGame.EMPTY })
        assertEquals(TicTacToeGame.X, g.current)
        assertEquals(TicTacToeGame.EMPTY, g.winner)
    }

    @Test
    fun `winningLine is null before a win`() {
        assertNull(TicTacToeGame().winningLine())
    }

    @Test
    fun `restore rebuilds position and evaluates winner`() {
        val cells = IntArray(9)
        cells[0] = TicTacToeGame.X; cells[1] = TicTacToeGame.X; cells[2] = TicTacToeGame.X
        val restored = TicTacToeGame(cells, TicTacToeGame.O)
        assertEquals(TicTacToeGame.X, restored.winner)
    }

    @Test
    fun `constructor rejects wrong board size`() {
        try {
            TicTacToeGame(IntArray(3), TicTacToeGame.X)
            throw AssertionError("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }
}
