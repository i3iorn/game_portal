package com.micro.gameportal

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView

/**
 * Minesweeper: tap to reveal, long-press to flag. Field size and mine count
 * come from Settings; timer with a best time per preset. State survives rotation.
 */
class MinefieldActivity : Activity() {

    private companion object {
        const val TICK_MS = 500L
        const val GRID_MAX_PX = 660
        const val KEY_BEST_MS = "best_ms"
        const val STATE_PRESET = "preset"
        const val STATE_MINES = "mines"
        const val STATE_STATES = "states"
        const val STATE_PLANTED = "planted"
        const val STATE_HIT = "hit"
        const val STATE_ELAPSED = "elapsed"
    }

    private lateinit var field: Minefield
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView
    private var preset = 0
    private var elapsedBaseMs = 0L
    private var startedAtMs = 0L
    private var running = false
    private val handler = Handler(Looper.getMainLooper())
    private val scores by lazy { ScoreStore(this, "minefield") }

    private val tick = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preset = savedPreset(savedInstanceState) ?: GameSettings(this).minesweeperPreset
        val (rows, cols, mines) = presetFields(preset)
        field = Minefield(rows = rows, cols = cols, mineCount = mines)
        setContentView(buildContent(preset))
        restoreOrStart(savedInstanceState)
        render()
    }

    private fun presetFields(preset: Int): Triple<Int, Int, Int> = when (preset) {
        1 -> Triple(10, 10, 20)
        2 -> Triple(12, 12, 30)
        else -> Triple(8, 8, 10)
    }

    private fun savedPreset(saved: Bundle?): Int? = saved?.getInt(STATE_PRESET, -1)?.takeIf { it >= 0 }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_PRESET, preset)
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

    private fun buildContent(preset: Int): android.view.View {
        val (rows, cols, _) = presetFields(preset)
        val cell = (GRID_MAX_PX / cols).coerceIn(56, 90)
        val scaffold = Ui.gameScaffold(this)
        val root = scaffold.root
        status = Ui.status(this)
        scaffold.controls.addView(status)
        cells = Array(rows * cols) { Button(this) }
        val grid = SquareGridLayout(this).apply {
            columnCount = cols
            cells.forEachIndexed { index, button ->
                button.textSize = 14f
                button.setPadding(0, 0, 0, 0)
                Ui.cell(button, Ui.theme(this@MinefieldActivity).primary, radiusDp = 6f)
                button.setOnClickListener { onCellTap(index) }
                button.setOnLongClickListener {
                    Feedback.play(this@MinefieldActivity, Feedback.Sound.MOVE)
                    field.toggleFlag(index); render(); true
                }
                addView(button, cellParams(index / cols, index % cols, 2))
            }
        }
        scaffold.board.addView(grid, SquareGridLayout.fillLayoutParams())
        Ui.addCentered(scaffold.controls, Ui.button(this, "New game") {
            field = Minefield(rows = rows, cols = cols, mineCount = presetFields(preset).third)
            elapsedBaseMs = 0L
            render()
        })
        return root
    }

    private fun restoreOrStart(saved: Bundle?) {
        val mines = saved?.getBooleanArray(STATE_MINES)
        if (mines != null) {
            field.restore(mines, saved.getIntArray(STATE_STATES)!!, saved.getBoolean(STATE_PLANTED), saved.getBoolean(STATE_HIT))
            elapsedBaseMs = saved.getLong(STATE_ELAPSED)
            if (field.isOver) stopClock()
        }
    }

    private fun onCellTap(index: Int) {
        if (field.isOver) return
        val wasMine = field.tap(index)
        if (field.planted && !running && !field.isOver) startClock()
        if (field.isOver) {
            stopClock()
            if (wasMine) {
                Feedback.haptic(cells[index])
                Feedback.play(this@MinefieldActivity, Feedback.Sound.ERROR)
            } else {
                Feedback.play(this@MinefieldActivity, Feedback.Sound.SUCCESS)
                scores.submit(bestKey(), elapsedMs(), lowerIsBetter = true)
            }
        }
        render()
    }

    private fun bestKey(): String = "${KEY_BEST_MS}_$preset"

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

    private fun bestTimeText(): String {
        val ms = scores.best(bestKey()) ?: return ""
        return "  Best: ${ms / 1000}s"
    }

    private fun render() {
        cells.forEachIndexed { index, button ->
            when (val state = field.states[index]) {
                Minefield.REVEALED -> {
                    val count = field.adjacentMines(index)
                    button.text = if (count == 0) "" else count.toString()
                    button.setTextColor(countColor(count))
                    Ui.cell(button, Ui.theme(this@MinefieldActivity).surfaceDim, radiusDp = 6f)
                }
                Minefield.FLAGGED -> { button.text = "⚑"; Ui.cell(button, Ui.theme(this@MinefieldActivity).primary, radiusDp = 6f) }
                Minefield.BLAST -> { button.text = "✸"; Ui.cell(button, Color.RED, radiusDp = 6f) }
                else -> {
                    button.text = if (field.isOver && field.mines[index]) "✸" else ""
                    Ui.cell(button, Ui.theme(this@MinefieldActivity).primary, radiusDp = 6f)
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
