package com.micro.gameportal

import kotlin.random.Random

/**
 * Pure Connect Four logic: 7 columns x 6 rows, gravity drops, win detection
 * along rows/columns/diagonals. Players RED (1) and YELLOW (2). No Android imports.
 */
class ConnectFourGame(
    val cols: Int = 7,
    val rows: Int = 6,
    seed: Long = System.nanoTime(),
) {
    companion object {
        const val EMPTY = 0
        const val RED = 1
        const val YELLOW = 2

        /** Number of in-a-row pieces needed to win. */
        const val CONNECT = 4
    }

    var cells = IntArray(cols * rows)
        private set
    var current = RED
        private set
    var winner = EMPTY
        private set
    var winningCells: List<Int> = emptyList()
        private set

    private var random = Random(seed)
    val isFull: Boolean get() = cells.all { it != EMPTY }
    val isOver: Boolean get() = winner != EMPTY || isFull

    init {
        require(cols >= CONNECT && rows >= CONNECT) { "Board must fit $CONNECT in a line" }
    }

    fun newGame(seed: Long = System.nanoTime()) {
        random = Random(seed)
        cells = IntArray(cols * rows)
        current = RED
        winner = EMPTY
        winningCells = emptyList()
    }

    fun restore(savedCells: IntArray, savedCurrent: Int) {
        require(savedCells.size == cells.size) { "Board must have ${cells.size} cells" }
        cells = savedCells.copyOf()
        current = savedCurrent
        winner = EMPTY
        winningCells = emptyList()
        reevaluate()
    }

    /** Drops a piece of the current player into [col]; false when the column is full. */
    fun drop(col: Int): Boolean {
        if (isOver || col !in 0 until cols) return false
        var target = -1
        for (row in rows - 1 downTo 0) {
            val index = row * cols + col
            if (cells[index] == EMPTY) { target = index; break }
        }
        if (target == -1) return false
        cells[target] = current
        evaluateFrom(target)
        if (winner == EMPTY) current = if (current == RED) YELLOW else RED
        return true
    }

    /** Row of the topmost empty cell in [col], or -1 when full. */
    fun landingRow(col: Int): Int {
        for (row in rows - 1 downTo 0) {
            if (cells[row * cols + col] == EMPTY) return row
        }
        return -1
    }

    private fun reevaluate() {
        cells.indices.forEach { if (cells[it] != EMPTY) evaluateFrom(it) }
    }

    /** Checks all lines through [index] for a CONNECT run of equal pieces. */
    private fun evaluateFrom(index: Int) {
        val player = cells[index]
        val row = index / cols
        val col = index % cols
        for ((dr, dc) in listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)) {
            val line = mutableListOf<Int>()
            var r = row - dr * (CONNECT - 1)
            var c = col - dc * (CONNECT - 1)
            repeat(2 * CONNECT - 1) {
                if (r in 0 until rows && c in 0 until cols) {
                    val i = r * cols + c
                    if (cells[i] == player) {
                        line.add(i)
                        if (line.size >= CONNECT) {
                            winner = player
                            winningCells = line.takeLast(CONNECT)
                            return
                        }
                    } else line.clear()
                }
                r += dr; c += dc
            }
        }
    }
}
