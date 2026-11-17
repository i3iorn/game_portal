package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Game2048Test {

    private fun boardOf(vararg rows: IntArray): IntArray {
        val b = IntArray(16)
        rows.forEachIndexed { r, row -> row.forEachIndexed { c, v -> b[r * 4 + c] = v } }
        return b
    }

    private fun gameWith(board: IntArray): Game2048 {
        val g = Game2048(seed = 1)
        g.restore(board, 0, 0, false)
        return g
    }

    @Test
    fun `new game has exactly two tiles`() {
        val g = Game2048(seed = 3)
        assertEquals(2, g.board.count { it != 0 })
        assertTrue("board=" + g.board.contentToString(), g.board.filter { it != 0 }.all { it == 2 || it == 4 })
    }

    @Test
    fun `left slide merges a pair and gains score`() {
        val g = gameWith(boardOf(intArrayOf(2, 2, 4, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        assertTrue(g.move(Game2048.Direction.LEFT))
        assertTrue(intArrayOf(4, 4, 0, 0).contentEquals(g.board.copyOfRange(0, 4)))
        assertEquals(4, g.score)
    }

    @Test
    fun `no move returns false and changes nothing`() {
        val g = gameWith(boardOf(intArrayOf(2, 4, 2, 4), intArrayOf(4, 2, 4, 2), intArrayOf(2, 4, 2, 4), intArrayOf(4, 2, 4, 2)))
        val before = g.board.copyOf()
        assertFalse(g.move(Game2048.Direction.LEFT))
        assertTrue(before.contentEquals(g.board))
        assertEquals(0, g.score)
    }

    @Test
    fun `right slide merges toward the right edge`() {
        val g = gameWith(boardOf(intArrayOf(2, 2, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        assertTrue(g.move(Game2048.Direction.RIGHT))
        assertTrue(intArrayOf(0, 0, 0, 4).contentEquals(g.board.copyOfRange(0, 4)))
    }

    @Test
    fun `up slide merges a column`() {
        val g = gameWith(boardOf(intArrayOf(2, 0, 0, 0), intArrayOf(2, 0, 0, 0), intArrayOf(4, 0, 0, 0), intArrayOf(4, 0, 0, 0)))
        assertTrue(g.move(Game2048.Direction.UP))
        assertEquals(4, g.board[0])
        assertEquals(8, g.board[4])
    }

    @Test
    fun `only one merge per tile per move`() {
        val g = gameWith(boardOf(intArrayOf(2, 2, 2, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        g.move(Game2048.Direction.LEFT)
        assertTrue(intArrayOf(4, 2, 0, 0).contentEquals(g.board.copyOfRange(0, 4)))
        assertEquals(4, g.score)
    }

    @Test
    fun `move spawns exactly one new tile`() {
        // two 2s merge into one 4, then one tile spawns: 3 non-zero cells
        val g = gameWith(boardOf(intArrayOf(2, 2, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        g.move(Game2048.Direction.LEFT)
        assertEquals(2, g.board.count { it != 0 })
    }

    @Test
    fun `full board with no merges is over`() {
        val g = gameWith(boardOf(intArrayOf(2, 4, 2, 4), intArrayOf(4, 2, 4, 2), intArrayOf(2, 4, 2, 4), intArrayOf(4, 2, 4, 2)))
        g.move(Game2048.Direction.LEFT)
        assertTrue(g.isOver)
        assertFalse(g.move(Game2048.Direction.UP))
    }

    @Test
    fun `full board with one merge pair is not over`() {
        val g = gameWith(boardOf(intArrayOf(2, 2, 4, 2), intArrayOf(4, 2, 4, 2), intArrayOf(2, 4, 2, 4), intArrayOf(4, 2, 4, 2)))
        assertFalse(g.isOver)
    }

    @Test
    fun `reaching 2048 announces win but continues`() {
        val g = gameWith(boardOf(intArrayOf(1024, 1024, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        g.move(Game2048.Direction.LEFT)
        assertEquals(Game2048.WIN, g.status)
        assertFalse(g.isOver)
        // further moves still allowed
        g.move(Game2048.Direction.LEFT)
        assertEquals(Game2048.WIN, g.status)
    }

    @Test
    fun `undo reverts the last move and score`() {
        val g = gameWith(boardOf(intArrayOf(2, 2, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        g.move(Game2048.Direction.LEFT)
        assertTrue(g.canUndo())
        assertTrue(g.undo())
        assertFalse(g.canUndo())
        assertTrue(boardOf(intArrayOf(2, 2, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)).contentEquals(g.board))
        assertEquals(0, g.score)
    }

    @Test
    fun `undo is unavailable before any move and after newGame`() {
        val g = gameWith(boardOf(intArrayOf(2, 2, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        assertFalse(g.canUndo())
        assertFalse(g.undo())
        g.move(Game2048.Direction.LEFT)
        assertTrue(g.canUndo())
        g.newGame(seed = 5)
        assertFalse(g.canUndo())
    }

    @Test
    fun `only the last move can be undone once`() {
        val g = gameWith(boardOf(intArrayOf(2, 2, 0, 0), intArrayOf(2, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        g.move(Game2048.Direction.LEFT)
        g.move(Game2048.Direction.LEFT)
        assertTrue(g.undo())
        // after one undo, the snapshot is consumed
        assertFalse(g.canUndo())
    }

    @Test
    fun `no-change move does not create an undo point`() {
        val g = gameWith(boardOf(intArrayOf(2, 4, 2, 4), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0)))
        assertFalse(g.move(Game2048.Direction.LEFT))
        assertFalse(g.canUndo())
    }

    @Test
    fun `restore rebuilds position`() {
        val saved = boardOf(intArrayOf(2, 4, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0), intArrayOf(0, 0, 0, 0))
        val g = Game2048(seed = 1)
        g.restore(saved, 42, 0, false)
        assertTrue(saved.contentEquals(g.board))
        assertEquals(42, g.score)
    }
}
