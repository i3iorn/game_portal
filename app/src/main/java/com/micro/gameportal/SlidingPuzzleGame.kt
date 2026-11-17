package com.micro.gameportal

import kotlin.random.Random

/**
 * Pure logic for the classic N x N sliding number puzzle. No Android imports.
 * Shuffled via random legal moves so it is always solvable.
 */
class SlidingPuzzleGame(
    val size: Int = 4,
    seed: Long = System.nanoTime(),
) {
    companion object {
        /** The sentinel value stored in the empty slot (tiles are 1..size*size-1). */
        fun emptyValue(size: Int) = size * size
    }

    private val empty = emptyValue(size)

    var board = solvedBoard()
        private set
    var moves = 0
        private set
    var isSolved = true
        private set

    private var random = Random(seed)

    init {
        shuffle()
    }

    /** Starts a fresh, freshly shuffled game. */
    fun newGame(seed: Long = System.nanoTime()) {
        random = Random(seed)
        board = solvedBoard()
        moves = 0
        isSolved = false
        shuffle()
    }

    /** Restores a previously saved position (e.g. after rotation). */
    fun restore(savedBoard: IntArray, savedMoves: Int) {
        require(savedBoard.size == empty) { "Board must have ${empty} cells" }
        board = savedBoard.copyOf()
        moves = savedMoves
        isSolved = checkSolved()
    }

    /**
     * Taps the tile at [index]; it slides into the empty slot when adjacent.
     * Returns true when the move was legal.
     */
    fun tap(index: Int): Boolean {
        if (isSolved || index !in board.indices) return false
        val delta = neighborDelta(index)
        if (delta == 0) return false
        val target = index + delta
        board[target] = board[index]
        board[index] = empty
        moves++
        isSolved = checkSolved()
        return true
    }

    /** Offset to the neighbouring empty cell (up/down/left/right), or 0 when none. */
    private fun neighborDelta(index: Int): Int = when {
        index / size > 0 && board[index - size] == empty -> -size
        index / size < size - 1 && board[index + size] == empty -> size
        index % size > 0 && board[index - 1] == empty -> -1
        index % size < size - 1 && board[index + 1] == empty -> 1
        else -> 0
    }

    private fun checkSolved(): Boolean =
        board.withIndex().all { (i, v) -> v == empty || v == i + 1 }

    private fun solvedBoard() = IntArray(empty) { it + 1 }.also { it[empty - 1] = empty }

    private fun shuffle() {
        repeat(200) {
            val emptyIndex = board.indexOf(empty)
            val options = mutableListOf(
                emptyIndex - size, emptyIndex + size, emptyIndex - 1, emptyIndex + 1,
            )
            if (emptyIndex % size == 0) options.remove(emptyIndex - 1)
            if (emptyIndex % size == size - 1) options.remove(emptyIndex + 1)
            val pick = options.filter { it in board.indices }.random(random)
            board[emptyIndex] = board[pick]
            board[pick] = empty
        }
        isSolved = checkSolved()
    }
}
