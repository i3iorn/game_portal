package com.micro.gameportal

import kotlin.random.Random

/**
 * Pure minesweeper logic. Mines are planted on the first tap (never on the
 * tapped cell); revealing a zero cell floods open its region. No Android imports.
 */
class Minefield(
    val rows: Int = 8,
    val cols: Int = 8,
    val mineCount: Int = 10,
    seed: Long = System.nanoTime(),
) {
    companion object {
        const val HIDDEN = 0
        const val REVEALED = 1
        const val FLAGGED = 2
        const val BLAST = 3 // the mine that was hit
    }

    val cells = rows * cols

    var mines = BooleanArray(cells)
        private set
    var states = IntArray(cells)
        private set
    var planted = false
        private set
    var hitMine = false
        private set
    val isOver: Boolean get() = hitMine || isWon
    val isWon: Boolean get() = planted && states.indices.all { mines[it] || states[it] == REVEALED }
    val flagsUsed: Int get() = states.count { it == FLAGGED }

    private var random = Random(seed)

    /** Taps a cell: reveals it (flooding when safe). Returns true when it was a mine. */
    fun tap(index: Int): Boolean {
        if (isOver || index !in 0 until cells || states[index] == FLAGGED || states[index] == REVEALED) {
            return false
        }
        if (!planted) plant(index)
        if (mines[index]) {
            states[index] = BLAST
            hitMine = true
            return true
        }
        revealFrom(index)
        return false
    }

    fun toggleFlag(index: Int): Boolean {
        if (isOver || index !in 0 until cells) return false
        when (states[index]) {
            HIDDEN -> { states[index] = FLAGGED; return true }
            FLAGGED -> { states[index] = HIDDEN; return true }
            else -> return false
        }
    }

    /** Restores a mid-game position (e.g. after rotation). */
    fun restore(savedMines: BooleanArray, savedStates: IntArray, savedPlanted: Boolean, savedHitMine: Boolean) {
        require(savedMines.size == cells && savedStates.size == cells) { "Size mismatch" }
        mines = savedMines.copyOf()
        states = savedStates.copyOf()
        planted = savedPlanted
        hitMine = savedHitMine
    }

    /** Number of mines adjacent to [index] (0 for mine cells; plant first). */
    fun adjacentMines(index: Int): Int {
        if (!planted) return 0
        return neighbors(index).count { mines[it] }
    }

    fun neighbors(index: Int): List<Int> {
        val row = index / cols
        val col = index % cols
        val result = mutableListOf<Int>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = row + dr
                val c = col + dc
                if (r in 0 until rows && c in 0 until cols) result.add(r * cols + c)
            }
        }
        return result
    }

    private fun plant(safeIndex: Int) {
        mines = BooleanArray(cells)
        val candidates = (0 until cells).filter { it != safeIndex }
        candidates.shuffled(random).take(mineCount).forEach { mines[it] = true }
        planted = true
    }

    private fun revealFrom(start: Int) {
        val stack = ArrayDeque(listOf(start))
        while (stack.isNotEmpty()) {
            val index = stack.removeLast()
            if (states[index] == REVEALED || mines[index]) continue
            states[index] = REVEALED
            if (adjacentMines(index) == 0) {
                neighbors(index).forEach { if (states[it] == HIDDEN) stack.addLast(it) }
            }
        }
    }
}
