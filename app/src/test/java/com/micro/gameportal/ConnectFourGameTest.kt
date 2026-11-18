package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectFourGameTest {

    private fun game(): ConnectFourGame = ConnectFourGame(cols = 7, rows = 6, seed = 3)

    private fun dropMany(g: ConnectFourGame, vararg cols: Int) {
        cols.forEach { check(g.drop(it)) }
    }

    @Test
    fun `drops stack from the bottom row`() {
        val g = game()
        dropMany(g, 0, 0)
        assertEquals(ConnectFourGame.RED, g.cells[5 * 7])   // bottom row, col 0
        assertEquals(ConnectFourGame.YELLOW, g.cells[4 * 7]) // one above
    }

    @Test
    fun `landingRow finds the first free row`() {
        val g = game()
        assertEquals(5, g.landingRow(3))
        g.drop(3)
        assertEquals(4, g.landingRow(3))
    }

    @Test
    fun `rejects drops into full columns`() {
        val g = game()
        repeat(6) { dropMany(g, 2) }
        assertFalse(g.drop(2))
        assertEquals(-1, g.landingRow(2))
    }

    @Test
    fun `rejects drops after the game is won`() {
        val g = game()
        dropMany(g, 0, 1, 0, 1, 0, 1, 0) // RED wins column 0
        assertEquals(ConnectFourGame.RED, g.winner)
        assertFalse(g.drop(6))
    }

    @Test
    fun `horizontal win`() {
        val g = game()
        dropMany(g, 0, 0, 1, 1, 2, 2, 3) // RED 0,1,2,3 along bottom
        assertEquals(ConnectFourGame.RED, g.winner)
        assertEquals(4, g.winningCells.size)
    }

    @Test
    fun `vertical win`() {
        val g = game()
        dropMany(g, 0, 1, 0, 1, 0, 1, 0)
        assertEquals(ConnectFourGame.RED, g.winner)
        assertEquals(listOf(14, 21, 28, 35), g.winningCells)
    }

    @Test
    fun `diagonal win`() {
        val g = game()
        // Pre-place RED diagonal (row5,col0), (row4,col1), (row3,col2) plus
        // YELLOW blockers, then drop the winning piece into column 3.
        val cells = IntArray(42)
        cells[5 * 7 + 0] = ConnectFourGame.RED
        cells[4 * 7 + 1] = ConnectFourGame.RED
        cells[3 * 7 + 2] = ConnectFourGame.RED
        cells[5 * 7 + 1] = ConnectFourGame.YELLOW
        cells[4 * 7 + 2] = ConnectFourGame.YELLOW
        cells[5 * 7 + 3] = ConnectFourGame.YELLOW
        cells[4 * 7 + 3] = ConnectFourGame.YELLOW
        cells[3 * 7 + 3] = ConnectFourGame.YELLOW
        g.restore(cells, ConnectFourGame.RED)
        assertTrue(g.drop(3)) // lands at row2,col3
        assertEquals(ConnectFourGame.RED, g.winner)
        assertEquals(listOf(17, 23, 29, 35), g.winningCells)
    }

    @Test
    fun `draw when the board fills`() {
        val g = ConnectFourGame(cols = 4, rows = 4, seed = 5)
        // RRYY / YYRR / RYRY / YRYR — full board with no 4-in-a-line for either player.
        val noWin = IntArray(16)
        for (r in 0 until 4) for (c in 0 until 4) {
            noWin[r * 4 + c] = when (listOf("RRYY", "YYRR", "RYRY", "YRYR")[r][c]) {
                'R' -> ConnectFourGame.RED
                else -> ConnectFourGame.YELLOW
            }
        }
        g.restore(noWin, ConnectFourGame.RED)
        assertTrue(g.isFull)
        assertTrue(g.isOver) // a full board is a finished game (draw)
        assertEquals(ConnectFourGame.EMPTY, g.winner)
    }

    @Test
    fun `restore rebuilds position and reevaluates winner`() {
        val g = game()
        val cells = IntArray(42).also {
            it[5 * 7] = ConnectFourGame.RED
            it[4 * 7] = ConnectFourGame.RED
            it[3 * 7] = ConnectFourGame.RED
        }
        g.restore(cells, ConnectFourGame.RED)
        assertEquals(ConnectFourGame.EMPTY, g.winner) // only 3 in a column
        val cellsWin = cells.clone().also { it[2 * 7] = ConnectFourGame.RED }
        g.restore(cellsWin, ConnectFourGame.YELLOW)
        assertEquals(ConnectFourGame.RED, g.winner)
    }

    @Test
    fun `players alternate`() {
        val g = game()
        assertEquals(ConnectFourGame.RED, g.current)
        g.drop(0)
        assertEquals(ConnectFourGame.YELLOW, g.current)
    }
}
