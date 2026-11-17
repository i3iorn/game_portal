package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView

/**
 * Sliding number puzzle with a move counter, timer and best scores.
 * Board size comes from Settings; state survives rotation.
 */
class SlidingPuzzleActivity : Activity() {

    private companion object {
        const val TICK_MS = 500L
        const val GRID_MAX_PX = 660
        const val KEY_BEST_MOVES = "best_moves_v2"
        const val KEY_BEST_MS = "best_ms_v2"
        const val STATE_SIZE = "size"
        const val STATE_BOARD = "board"
        const val STATE_MOVES = "moves"
        const val STATE_ELAPSED = "elapsed"
        const val STATE_SOLVED = "solved"
    }

    private lateinit var game: SlidingPuzzleGame
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView
    private var elapsedBaseMs = 0L
    private var startedAtMs = 0L
    private var solvedElapsedMs = -1L
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private val scores by lazy { ScoreStore(this, "sliding_puzzle") }

    private val tick = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val size = savedSize(savedInstanceState) ?: GameSettings(this).puzzleSize
        game = SlidingPuzzleGame(size = size)
        setContentView(buildContent(size))
        restoreOrStart(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SIZE, game.size)
        outState.putIntArray(STATE_BOARD, game.board)
        outState.putInt(STATE_MOVES, game.moves)
        outState.putLong(STATE_ELAPSED, elapsedMs())
        outState.putBoolean(STATE_SOLVED, game.isSolved)
    }

    override fun onResume() {
        super.onResume()
        if (solvedElapsedMs < 0) {
            startedAtMs = System.currentTimeMillis()
            running = true
            handler.postDelayed(tick, TICK_MS)
        }
    }

    override fun onPause() {
        super.onPause()
        elapsedBaseMs = elapsedMs()
        running = false
        handler.removeCallbacks(tick)
    }

    private fun savedSize(saved: Bundle?): Int? = saved?.getInt(STATE_SIZE, -1)?.takeIf { it > 0 }

    private fun buildContent(size: Int): android.view.View {
        val cell = (GRID_MAX_PX / size).coerceAtLeast(56)
        val root = Ui.gameRoot(this)
        status = Ui.status(this)
        Ui.addCentered(root, status)
        cells = Array(size * size) { Button(this) }
        val grid = GridLayout(this).apply {
            columnCount = size
            cells.forEachIndexed { index, button ->
                button.textSize = (cell / 5f).coerceAtMost(24f)
                button.setOnClickListener { onCellTap(index) }
                addView(
                    button,
                    GridLayout.LayoutParams().apply {
                        width = cell; height = cell
                        setMargins(4, 4, 4, 4)
                    },
                )
            }
        }
        Ui.addCentered(root, grid)
        Ui.addCentered(root, Ui.button(this, "New game") {
            game.newGame()
            elapsedBaseMs = 0L
            solvedElapsedMs = -1L
            startedAtMs = System.currentTimeMillis()
            running = true
            handler.removeCallbacks(tick)
            handler.postDelayed(tick, TICK_MS)
            render()
        })
        return root
    }

    private fun restoreOrStart(saved: Bundle?) {
        val board = saved?.getIntArray(STATE_BOARD)
        if (board != null && !saved.getBoolean(STATE_SOLVED)) {
            game.restore(board, saved.getInt(STATE_MOVES))
            elapsedBaseMs = saved.getLong(STATE_ELAPSED)
            solvedElapsedMs = -1L
        }
        render()
    }

    private fun onCellTap(index: Int) {
        if (game.tap(index) && game.isSolved) {
            solvedElapsedMs = elapsedMs()
            running = false
            handler.removeCallbacks(tick)
            scores.submit(KEY_BEST_MOVES, game.moves.toLong(), lowerIsBetter = true)
            scores.submit(KEY_BEST_MS, solvedElapsedMs, lowerIsBetter = true)
        }
        render()
    }

    private fun elapsedMs(): Long = when {
        solvedElapsedMs >= 0 -> solvedElapsedMs
        running -> elapsedBaseMs + (System.currentTimeMillis() - startedAtMs)
        else -> elapsedBaseMs
    }

    private fun bestText(): String {
        val moves = scores.best(KEY_BEST_MOVES)
        if (moves == null) return ""
        val ms = scores.best(KEY_BEST_MS) ?: 0L
        return "  Best: ${moves.toInt()} moves in ${ms / 1000}s"
    }

    private fun render() {
        cells.forEachIndexed { index, button ->
            button.text = game.board[index].let { if (it == SlidingPuzzleGame.emptyValue(game.size)) "" else it.toString() }
        }
        status.text = when {
            game.isSolved -> "Solved in ${game.moves} moves, ${elapsedMs() / 1000}s!${bestText()}"
            else -> "${game.moves} moves, ${elapsedMs() / 1000}s${bestText()}"
        }
    }
}
