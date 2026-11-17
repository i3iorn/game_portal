package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TicTacToeAiTest {

    private fun gameOf(vararg plays: Int): TicTacToeGame =
        TicTacToeGame().also { plays.forEach { c -> check(it.play(c)) } }

    @Test
    fun `hard takes an available win`() {
        // X 0,2,8 (threatens only 1); O 3,5 -> O wins now with 4
        val g = gameOf(0, 3, 2, 5, 8)
        val move = TicTacToeAi(g, TicTacToeAi.Difficulty.HARD).bestMove()
        assertEquals(4, move)
    }

    @Test
    fun `hard blocks an imminent loss`() {
        // X 0,1,6 threatens 2; O 3,8 has no winning move -> must block at 2
        val g = gameOf(3, 0, 8, 1, 6)
        val move = TicTacToeAi(g, TicTacToeAi.Difficulty.HARD).bestMove()
        assertEquals(2, move)
    }

    @Test
    fun `medium blocks an imminent loss`() {
        val g = gameOf(3, 0, 8, 1, 6)
        val move = TicTacToeAi(g, TicTacToeAi.Difficulty.MEDIUM).bestMove()
        assertEquals(2, move)
    }

    @Test
    fun `easy returns a legal empty cell`() {
        val g = gameOf(0, 4)
        val move = TicTacToeAi(g, TicTacToeAi.Difficulty.EASY).bestMove()
        assertTrue(move in g.cells.indices && g.cells[move] == TicTacToeGame.EMPTY)
    }

    @Test
    fun `human never moves`() {
        assertEquals(-1, TicTacToeAi(TicTacToeGame(), TicTacToeAi.Difficulty.HUMAN).bestMove())
    }

    @Test
    fun `no move when game over`() {
        val g = gameOf(0, 3, 1, 4, 2) // X won
        assertEquals(-1, TicTacToeAi(g, TicTacToeAi.Difficulty.HARD).bestMove())
    }

    @Test
    fun `hard opponent playing O never loses against naive center-then-corners X`() {
        val naive = listOf(4, 0, 2, 6, 8)
        val g = TicTacToeGame()
        val ai = TicTacToeAi(g, TicTacToeAi.Difficulty.HARD)
        for (x in naive) {
            if (g.winner != TicTacToeGame.EMPTY) break
            if (g.cells[x] != TicTacToeGame.EMPTY || !g.play(x)) continue
            if (g.winner != TicTacToeGame.EMPTY || g.isFull) break
            g.play(ai.bestMove())
        }
        assertTrue(g.winner == TicTacToeGame.O || g.winner == TicTacToeGame.EMPTY)
    }
}
