package com.micro.gameportal

import android.app.Activity
import android.graphics.Color
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
 * Minesweeper 8x8 with 10 mines. Tap to reveal, long-press to flag;
 * timer and a best time, same convention as the sliding puzzle.
 */
class MinefieldActivity : Activity() {

    private companion object {
        const val TICK_MS = 500L
        const val PREFS = "minefield"
        const val KEY_BEST_MS = "best_ms"
        const val STATE_MINES = "mines"
        const val STATE_STATES = "states"
        const val STATE_PLANTED = "planted"
        const val STATE_HIT = "hit"
        const val STATE_ELAPSED = "elapsed"
    }

    private lateinit var field: Minefield
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView
    private var elapsedBaseMs = 0L
    private var startedAtMs = 0L
    private var running = false
    private val handler = Handler(Looper.getMainLooper())

    private val tick = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        field = Minefield()
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
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray(STATE_MINES, field.mines)
        outState.putIntArray(STATE_STATES, field.states)
        outState.putBoolean(STATE_PLANTED, field.planted)
        outState.putBoolean(STATE_HIT, field.hitMine)
        outState.putLong(STATE_ELAPSED, elapsedMs())
    }

    override fun onResume() {
        super.onResume()
        if (!field.isOver) {
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
        val mines = saved?.getBooleanArray(STATE_MINES)
        if (mines != null) {
            field.restore(mines, saved.getIntArray(STATE_STATES)!!, saved.getBoolean(STATE_PLANTED), saved.getBoolean(STATE_HIT))
            elapsedBaseMs = saved.getLong(STATE_ELAPSED)
            if (field.isOver) stopClock()
        }
    }

    private fun buildGrid(): View {
        cells = Array(field.cells) { Button(this) }
        return GridLayout(this).apply {
            columnCount = field.cols
            cells.forEachIndexed { index, button ->
                button.textSize = 16f
                button.setPadding(0, 0, 0, 0)
                button.setOnClickListener { onCellTap(index) }
                button.setOnLongClickListener { toggleFlag(index); true }
                addView(
                    button,
                    GridLayout.LayoutParams().apply {
                        width = 90; height = 90
                        setMargins(2, 2, 2, 2)
                    },
                )
            }
        }
    }

    private fun onCellTap(index: Int) {
        if (field.isOver) return
        val wasMine = field.tap(index)
        if (field.planted && !running && !field.isOver) startClock()
        if (field.isOver) {
            stopClock()
            if (!wasMine) saveBestTime()
        }
        render()
    }

    private fun toggleFlag(index: Int) {
        field.toggleFlag(index)
        render()
    }

    private fun startClock() {
        startedAtMs = System.currentTimeMillis()
        running = true
        handler.postDelayed(tick, TICK_MS)
    }

    private fun stopClock() {
        elapsedBaseMs = elapsedMs()
        running = false
        handler.removeCallbacks(tick)
    }

    private fun elapsedMs(): Long = if (running) elapsedBaseMs + (System.currentTimeMillis() - startedAtMs) else elapsedBaseMs

    private fun saveBestTime() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val best = prefs.getLong(KEY_BEST_MS, Long.MAX_VALUE)
        if (elapsedMs() < best) prefs.edit().putLong(KEY_BEST_MS, elapsedMs()).apply()
    }

    private fun bestTimeText(): String {
        val ms = getSharedPreferences(PREFS, MODE_PRIVATE).getLong(KEY_BEST_MS, -1L)
        if (ms < 0) return ""
        return "  Best: ${ms / 1000}s"
    }

    private fun newGameButton(): Button = Button(this).apply {
        text = "New game"
        setOnClickListener {
            field = Minefield()
            elapsedBaseMs = 0L
            render()
        }
    }

    private fun render() {
        cells.forEachIndexed { index, button ->
            when (val state = field.states[index]) {
                Minefield.REVEALED -> {
                    val count = field.adjacentMines(index)
                    button.text = if (count == 0) "" else count.toString()
                    button.setTextColor(countColor(count))
                    button.setBackgroundColor(0xFF5A4E3A.toInt())
                }
                Minefield.FLAGGED -> { button.text = "⚑"; button.setBackgroundColor(0xFF6D5C43.toInt()) }
                Minefield.BLAST -> { button.text = "✸"; button.setBackgroundColor(Color.RED) }
                else -> {
                    button.text = if (field.isOver && field.mines[index]) "✸" else ""
                    button.setBackgroundColor(0xFF8A7A5E.toInt())
                }
            }
        }
        status.text = when {
            field.hitMine -> "Boom! ${field.flagsUsed} flags  ${elapsedMs() / 1000}s"
            field.isWon -> "Cleared in ${elapsedMs() / 1000}s!${bestTimeText()}"
            else -> "Mines: ${field.mineCount - field.flagsUsed} left  ${elapsedMs() / 1000}s${bestTimeText()}"
        }
    }

    private fun countColor(count: Int): Int = when (count) {
        1 -> Color.CYAN
        2 -> Color.GREEN
        3 -> Color.MAGENTA
        else -> Color.WHITE
    }
}
