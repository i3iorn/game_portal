package com.micro.gameportal

/** Pure tic-tac-toe rules: 3x3, players X (1) and O (2). No Android imports. */
class TicTacToeGame(cells: IntArray = IntArray(9), current: Int = X) {

    companion object {
        const val EMPTY = 0
        const val X = 1
        const val O = 2
        private val LINES = listOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6),
        )
    }

    var cells = cells.copyOf()
        private set
    var current = current
        private set
    var winner = EMPTY
        private set

    init {
        require(cells.size == 9) { "Board must have 9 cells" }
        winner = evaluate()
    }

    fun play(index: Int): Boolean {
        if (winner != EMPTY || index !in cells.indices || cells[index] != EMPTY) return false
        cells[index] = current
        winner = evaluate()
        if (winner == EMPTY) current = if (current == X) O else X
        return true
    }

    /** Places [player] at [index] without turn handling (used for probes). */
    fun forcePlay(index: Int, player: Int) {
        cells[index] = player
        winner = evaluate()
    }

    fun restore(savedCells: IntArray, savedCurrent: Int) {
        cells = savedCells.copyOf()
        current = savedCurrent
        winner = evaluate()
    }

    fun reset() {
        cells = IntArray(9)
        current = X
        winner = EMPTY
    }

    val isFull: Boolean get() = cells.all { it != EMPTY }

    /** The winning triple of cell indices, or null when nobody has won yet. */
    fun winningLine(): IntArray? =
        LINES.firstOrNull { l -> l.all { cells[it] == winner && winner != EMPTY } }

    private fun evaluate(): Int {
        LINES.forEach { line ->
            val (a, b, c) = line.map { cells[it] }
            if (a != EMPTY && a == b && b == c) return a
        }
        return EMPTY
    }
}
