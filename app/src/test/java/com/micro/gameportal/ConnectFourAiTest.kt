package com.micro.gameportal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ConnectFourAiTest {

    @Test
    fun `ai blocks an opponent three-in-a-column`() {
        // RED has 3 in col0, YELLOW to move -> must block at col0.
        val g = ConnectFourGame(seed = 5)
        listOf(0, 1, 0, 2, 0).forEach { check(g.drop(it)) }
        assertEquals(0, ConnectFourAi(g).bestMove())
    }

    @Test
    fun `ai takes an immediate winning drop`() {
        // YELLOW has 3 in col1, RED has no immediate win -> AI takes col1.
        val g = ConnectFourGame(seed = 5)
        listOf(0, 1, 2, 1, 3, 1, 4).forEach { check(g.drop(it)) } // Y:1,1,1 R:0,2,3,4
        assertEquals(1, ConnectFourAi(g).bestMove())
    }

    @Test
    fun `ai prefers the centre when neutral`() {
        assertEquals(3, ConnectFourAi(ConnectFourGame(seed = 5)).bestMove())
    }

    @Test
    fun `no move when the game is over`() {
        val g = ConnectFourGame(seed = 5)
        listOf(0, 1, 0, 1, 0, 1, 0).forEach { check(g.drop(it)) } // RED wins
        assertEquals(-1, ConnectFourAi(g).bestMove())
        assertFalse(g.drop(6))
    }
}
