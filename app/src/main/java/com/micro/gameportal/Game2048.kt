package com.micro.gameportal

import kotlin.random.Random

/**
 * Pure 2048 logic on a 4x4 grid. Slides and merges tiles per move,
 * spawns 2s (90%) and 4s (10%), tracks score and game-over state.
 */
class Game2048(seed: Long = System.nanoTime()) {

    enum class Direction { LEFT, RIGHT, UP, DOWN }

    companion object {
        const val SIZE = 4
        const val CELLS = SIZE * SIZE
        const val WIN_TILE = 2048
        const val WIN = -1 // sentinel: game won but continues
        const val LOST = -2
    }

    var board = IntArray(CELLS)
        private set
    var score = 0
        private set
    /** EMPTY while playing; WIN when 2048 reached (game continues); LOST when no moves remain. */
    var status = 0
        private set
    val isOver: Boolean get() = status == LOST

    private var random = Random(seed)
    private var winAnnounced = false

    init {
        spawn()
        spawn()
    }

    fun newGame(seed: Long = System.nanoTime()) {
        random = Random(seed)
        board = IntArray(CELLS)
        score = 0
        status = 0
        winAnnounced = false
        spawn()
        spawn()
    }

    fun restore(savedBoard: IntArray, savedScore: Int, savedStatus: Int, savedWinAnnounced: Boolean) {
        require(savedBoard.size == CELLS) { "Board must have $CELLS cells" }
        board = savedBoard.copyOf()
        score = savedScore
        status = savedStatus
        winAnnounced = savedWinAnnounced
    }

    /** Applies one move; returns true when the board changed (and a tile spawns). */
    fun move(direction: Direction): Boolean {
        if (isOver) return false
        var changed = false
        var gained = 0
        for (line in lines(direction)) {
            val result = collapse(line.map { board[it] })
            if (!changed && result.values != line.map { board[it] }) changed = true
            gained += result.gained
            result.values.forEachIndexed { i, v -> board[line[i]] = v }
        }
        if (changed) {
            score += gained
            spawn()
        }
        if (!winAnnounced && board.any { it >= WIN_TILE }) {
            status = WIN
            winAnnounced = true
        } else if (!hasMoves()) {
            status = LOST
        }
        return changed
    }

    /** Compacts a line toward index 0, merging equal neighbours once each. */
    private fun collapse(values: List<Int>): CollapseResult {
        val tiles = values.filter { it != 0 }.toMutableList()
        var gained = 0
        var i = 0
        while (i < tiles.size - 1) {
            if (tiles[i] == tiles[i + 1]) {
                tiles[i] *= 2
                gained += tiles[i]
                tiles.removeAt(i + 1)
            }
            i++
        }
        val merged = tiles + List(values.size - tiles.size) { 0 }
        return CollapseResult(merged, gained)
    }

    private data class CollapseResult(val values: List<Int>, val gained: Int)

    /** Cell indices of each line, ordered from the edge the tiles move toward. */
    private fun lines(direction: Direction): List<List<Int>> {
        val result = mutableListOf<List<Int>>()
        for (k in 0 until SIZE) {
            val line = mutableListOf<Int>()
            for (j in 0 until SIZE) {
                when (direction) {
                    Direction.LEFT -> line.add(k * SIZE + j)
                    Direction.RIGHT -> line.add(k * SIZE + (SIZE - 1 - j))
                    Direction.UP -> line.add(j * SIZE + k)
                    Direction.DOWN -> line.add((SIZE - 1 - j) * SIZE + k)
                }
            }
            result.add(line)
        }
        return result
    }

    private fun spawn() {
        val empty = board.indices.filter { board[it] == 0 }
        if (empty.isEmpty()) return
        board[empty.random(random)] = if (random.nextInt(10) < 9) 2 else 4
    }

    private fun hasMoves(): Boolean {
        if (board.any { it == 0 }) return true
        for (row in 0 until SIZE) {
            for (col in 0 until SIZE) {
                val v = board[row * SIZE + col]
                if (col < SIZE - 1 && board[row * SIZE + col + 1] == v) return true
                if (row < SIZE - 1 && board[(row + 1) * SIZE + col] == v) return true
            }
        }
        return false
    }
}
