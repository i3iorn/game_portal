package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Classic 4x4 sliding number puzzle with a move counter, timer and a
 * per-device best score (fewest moves, then fastest time).
 */
class SlidingPuzzleActivity : Activity() {

    private companion object {
        const val TICK_MS = 500L
        const val PREFS = "sliding_puzzle"
        const val KEY_BEST_MOVES = "best_moves"
        const val KEY_BEST_MS = "best_ms"
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

    private val tick = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        game = SlidingPuzzleGame()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        status = TextView(this).apply { textSize = 18f; setPadding(0, 24, 0, 24) }
        root.addView(status)
        root.addView(buildGrid())
        root.addView(newGameButton())
        setContentView(root)
        restoreOrStart(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
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

    private fun restoreOrStart(saved: Bundle?) {
        val board = saved?.getIntArray(STATE_BOARD)
        if (board != null && !saved.getBoolean(STATE_SOLVED)) {
            game.restore(board, saved.getInt(STATE_MOVES))
            elapsedBaseMs = saved.getLong(STATE_ELAPSED)
            solvedElapsedMs = -1L
        }
        render()
    }

    private fun buildGrid(): View {
        cells = Array(game.board.size) { Button(this) }
        return GridLayout(this).apply {
            columnCount = game.size
            cells.forEachIndexed { index, button ->
                button.textSize = 24f
                button.setOnClickListener { onCellTap(index) }
                addView(
                    button,
                    GridLayout.LayoutParams().apply {
                        width = 170; height = 170
                        setMargins(4, 4, 4, 4)
                    },
                )
            }
        }
    }

    private fun onCellTap(index: Int) {
        if (game.tap(index) && game.isSolved) onSolved()
        render()
    }

    private fun onSolved() {
        solvedElapsedMs = elapsedMs()
        running = false
        handler.removeCallbacks(tick)
        saveBestScoreIfBetter()
    }

    private fun saveBestScoreIfBetter() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val bestMoves = prefs.getInt(KEY_BEST_MOVES, Int.MAX_VALUE)
        val bestMs = prefs.getLong(KEY_BEST_MS, Long.MAX_VALUE)
        val better = game.moves < bestMoves || (game.moves == bestMoves && solvedElapsedMs < bestMs)
        if (better) {
            prefs.edit()
                .putInt(KEY_BEST_MOVES, game.moves)
                .putLong(KEY_BEST_MS, solvedElapsedMs)
                .apply()
        }
    }

    private fun newGameButton(): Button = Button(this).apply {
        text = "New game"
        setOnClickListener {
            game.newGame()
            elapsedBaseMs = 0L
            solvedElapsedMs = -1L
            startedAtMs = System.currentTimeMillis()
            running = true
            handler.removeCallbacks(tick)
            handler.postDelayed(tick, TICK_MS)
            render()
        }
    }

    private fun elapsedMs(): Long = when {
        solvedElapsedMs >= 0 -> solvedElapsedMs
        running -> elapsedBaseMs + (System.currentTimeMillis() - startedAtMs)
        else -> elapsedBaseMs
    }

    private fun bestScoreText(): String {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val moves = prefs.getInt(KEY_BEST_MOVES, -1)
        if (moves < 0) return ""
        return "  Best: $moves moves in ${prefs.getLong(KEY_BEST_MS, 0L) / 1000}s"
    }

    private fun render() {
        cells.forEachIndexed { index, button ->
            button.text = game.board[index].let { if (it == SlidingPuzzleGame.emptyValue(game.size)) "" else it.toString() }
        }
        status.text = when {
            game.isSolved -> "Solved in ${game.moves} moves, ${elapsedMs() / 1000}s!${bestScoreText()}"
            else -> "${game.moves} moves, ${elapsedMs() / 1000}s${bestScoreText()}"
        }
    }
}
